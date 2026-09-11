package harmonia.ledger.client

import cats.effect.IO
import harmonia.ledger.client.{ActiveContract, ParticipantLedger, TemplateCatalog}
import io.circe.Json
import harmonia.protocol.LedgerUpdate
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest

final case class LedgerSnapshot(contracts: Vector[ActiveContract], history: Vector[Json]):
  def contract(module: String, entity: String): Option[ActiveContract] =
    contracts.find(c => c.template.getModuleName == module && c.template.getEntityName == entity)
  val version: String = MessageDigest
    .getInstance("SHA-256")
    .digest(contracts.map(_.id).sorted.mkString("\n").getBytes(UTF_8))
    .map(b => f"${b & 0xff}%02x")
    .mkString
  def commits: Map[String, Json] = history.flatMap { update =>
    update.hcursor.downField("transaction").get[String]("commandId").toOption.map(_ -> update)
  }.toMap
  def updates: Vector[LedgerUpdate] = history.map { update =>
    val transaction = update.hcursor.downField("transaction")
    val events = transaction
      .get[Option[Vector[Json]]]("events")
      .fold(
        error => throw LedgerDecodingFailure("transaction.events", error.getMessage),
        _.getOrElse(Vector.empty)
      )
    LedgerUpdate(
      transaction.get[Option[String]]("updateId").fold(throw _, identity),
      transaction.get[Option[String]]("commandId").fold(throw _, identity),
      events.map { event =>
        val created = event.hcursor.downField("created")
        val exercised = event.hcursor.downField("exercised")
        val value = if created.succeeded then created else exercised
        val template = value.downField("templateId").get[String]("entityName").getOrElse("Contract")
        val operation =
          if created.succeeded then "created"
          else exercised.get[String]("choice").getOrElse("archived")
        s"$template · $operation"
      }
    )
  }

object LedgerSnapshot:
  def read(ledger: ParticipantLedger, catalog: TemplateCatalog): IO[LedgerSnapshot] = for
    contracts <- ledger.active()
    history <- ledger.events
  yield LedgerSnapshot(contracts.filter(catalog.accepts), history)

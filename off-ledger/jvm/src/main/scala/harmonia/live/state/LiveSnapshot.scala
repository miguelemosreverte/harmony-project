package harmonia.live.state

import cats.effect.IO
import harmonia.live.ledger.{ActiveContract, LiveLedger}
import io.circe.Json
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest

final case class LiveSnapshot(contracts: Vector[ActiveContract], history: Vector[Json]):
  def contract(module: String, entity: String): Option[ActiveContract] =
    contracts.find(c => c.template.getModuleName == module && c.template.getEntityName == entity)
  def application: Option[ActiveContract] = contract("PrivateFinancing", "Application")
  def progress: Option[ActiveContract] = contract("Harmonia.SharedProgress", "SharedProgress")
  def proof: Option[ActiveContract] = contract("Harmonia.Result", "VerifiedResult")
  def workflow: String = progress.map(_.text("status")).getOrElse("not-visible")
  val version: String = MessageDigest
    .getInstance("SHA-256")
    .digest(contracts.map(_.id).sorted.mkString("\n").getBytes(UTF_8))
    .map(b => f"${b & 0xff}%02x")
    .mkString
  def json(actor: String): Json =
    val eligible = Vector(
      Option.when(actor == "bank" && application.exists(_.text("status") == "pending"))(
        "approve-financing"
      ),
      Option.when(actor == "buyer" && workflow == "waiting" && proof.nonEmpty)("publish-approval")
    ).flatten
    Json.obj(
      "actor" -> Json.fromString(actor),
      "version" -> Json.fromString(version),
      "workflow" -> Json.fromString(workflow),
      "application" -> application.fold(Json.Null)(c => Json.fromString(c.text("status"))),
      "private_details" -> application.fold(Json.Null)(c =>
        Json.fromString(c.text("privateDetails"))
      ),
      "evidence_available" -> Json.fromBoolean(proof.nonEmpty),
      "eligible" -> Json.arr(eligible.map(Json.fromString)*),
      "current_step" -> Json.fromString(
        if workflow == "complete" then "Complete"
        else if proof.nonEmpty then "Buyer continues"
        else "Waiting for bank approval"
      ),
      "history" -> Json.arr(history.map { update =>
        val transaction = update.hcursor.downField("transaction")
        Json.obj(
          "update_id" -> transaction
            .get[String]("updateId")
            .toOption
            .fold(Json.Null)(Json.fromString),
          "command_id" -> transaction
            .get[String]("commandId")
            .toOption
            .fold(Json.Null)(Json.fromString),
          "events" -> Json.arr(transaction.get[Vector[Json]]("events").getOrElse(Vector.empty).map {
            event =>
              val created = event.hcursor.downField("created")
              val exercised = event.hcursor.downField("exercised")
              val value = if created.succeeded then created else exercised
              val template =
                value.downField("templateId").get[String]("entityName").getOrElse("Contract")
              val operation =
                if created.succeeded then "created"
                else exercised.get[String]("choice").getOrElse("archived")
              Json.fromString(s"$template · $operation")
          }*)
        )
      }*)
    )

object LiveSnapshot:
  def read(ledger: LiveLedger): IO[LiveSnapshot] = for
    contracts <- ledger.active()
    history <- ledger.events
  yield LiveSnapshot(contracts, history)

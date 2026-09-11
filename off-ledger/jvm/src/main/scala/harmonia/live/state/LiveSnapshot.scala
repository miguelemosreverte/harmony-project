package harmonia.live.state

import cats.effect.IO
import harmonia.financing.*
import io.circe.syntax.*
import harmonia.live.ledger.{ActiveContract, ParticipantLedger, TemplateCatalog}
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
    val applicationStatus = application.map(c =>
      Json.fromString(c.text("status")).as[ApplicationStatus].fold(throw _, identity)
    )
    val progressStatus = Json.fromString(workflow).as[ProgressStatus].fold(throw _, identity)
    val eligible = Vector(
      Option.when(actor == "bank" && applicationStatus.contains(ApplicationStatus.Pending))(
        FinancingAction.Approve
      ),
      Option.when(actor == "buyer" && progressStatus == ProgressStatus.Waiting && proof.nonEmpty)(
        FinancingAction.Continue
      )
    ).flatten
    val financing = FinancingState(
      actor,
      version,
      progressStatus,
      applicationStatus,
      application.map(_.text("privateDetails")),
      proof.nonEmpty,
      eligible,
      if progressStatus == ProgressStatus.Complete then "Complete"
      else if proof.nonEmpty then "Buyer continues"
      else "Waiting for bank approval"
    )
    financing.json.deepMerge(
      Json.obj(
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
            "events" -> Json.arr(
              transaction.get[Vector[Json]]("events").getOrElse(Vector.empty).map { event =>
                val created = event.hcursor.downField("created")
                val exercised = event.hcursor.downField("exercised")
                val value = if created.succeeded then created else exercised
                val template =
                  value.downField("templateId").get[String]("entityName").getOrElse("Contract")
                val operation =
                  if created.succeeded then "created"
                  else exercised.get[String]("choice").getOrElse("archived")
                Json.fromString(s"$template · $operation")
              }*
            )
          )
        }*)
      )
    )

object LiveSnapshot:
  def read(ledger: ParticipantLedger, catalog: TemplateCatalog): IO[LiveSnapshot] = for
    contracts <- ledger.active()
    history <- ledger.events
  yield LiveSnapshot(contracts.filter(catalog.accepts), history)

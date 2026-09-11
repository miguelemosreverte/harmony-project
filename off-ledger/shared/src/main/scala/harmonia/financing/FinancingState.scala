package harmonia.financing

import io.circe.{Decoder, Encoder, Json}

enum FinancingAction(val wire: String):
  case Approve extends FinancingAction("approve-financing")
  case Continue extends FinancingAction("publish-approval")

object FinancingAction:
  given Encoder[FinancingAction] = Encoder.encodeString.contramap(_.wire)
  given Decoder[FinancingAction] = Decoder.decodeString.emap(value =>
    values.find(_.wire == value).toRight(s"Unsupported financing action: $value")
  )

enum ApplicationStatus(val wire: String):
  case Pending extends ApplicationStatus("pending")
  case Approved extends ApplicationStatus("approved")

object ApplicationStatus:
  given Encoder[ApplicationStatus] = Encoder.encodeString.contramap(_.wire)
  given Decoder[ApplicationStatus] = Decoder.decodeString.emap(value =>
    values.find(_.wire == value).toRight(s"Unknown application status: $value")
  )

enum ProgressStatus(val wire: String):
  case Hidden extends ProgressStatus("not-visible")
  case Waiting extends ProgressStatus("waiting")
  case Complete extends ProgressStatus("complete")

object ProgressStatus:
  given Encoder[ProgressStatus] = Encoder.encodeString.contramap(_.wire)
  given Decoder[ProgressStatus] = Decoder.decodeString.emap(value =>
    values.find(_.wire == value).toRight(s"Unknown progress status: $value")
  )

final case class FinancingState(
    actor: String,
    version: String,
    progress: ProgressStatus,
    application: Option[ApplicationStatus],
    privateDetails: Option[String],
    evidenceAvailable: Boolean,
    eligible: Vector[FinancingAction],
    currentStep: String
):
  def json: Json =
    import io.circe.syntax.*
    Json.obj(
      "actor" -> actor.asJson,
      "version" -> version.asJson,
      "workflow" -> progress.asJson,
      "application" -> application.asJson,
      "private_details" -> privateDetails.asJson,
      "evidence_available" -> evidenceAvailable.asJson,
      "eligible" -> eligible.asJson,
      "current_step" -> currentStep.asJson
    )

object FinancingState:
  given Decoder[FinancingState] = Decoder.instance { cursor =>
    for
      actor <- cursor.get[String]("actor")
      version <- cursor.get[String]("version")
      progress <- cursor.get[ProgressStatus]("workflow")
      application <- cursor.get[Option[ApplicationStatus]]("application")
      details <- cursor.get[Option[String]]("private_details")
      evidence <- cursor.get[Boolean]("evidence_available")
      eligible <- cursor.get[Vector[FinancingAction]]("eligible")
      step <- cursor.get[String]("current_step")
    yield FinancingState(actor, version, progress, application, details, evidence, eligible, step)
  }

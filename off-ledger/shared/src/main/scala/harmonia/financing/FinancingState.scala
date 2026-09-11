package harmonia.financing

import io.circe.{Codec, Decoder, Encoder, Json}
import io.circe.syntax.*

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
  def json: Json = this.asJson

object FinancingState:
  given Codec.AsObject[FinancingState] = Codec.forProduct8(
    "actor",
    "version",
    "workflow",
    "application",
    "private_details",
    "evidence_available",
    "eligible",
    "current_step"
  )(FinancingState.apply)(v =>
    (
      v.actor,
      v.version,
      v.progress,
      v.application,
      v.privateDetails,
      v.evidenceAvailable,
      v.eligible,
      v.currentStep
    )
  )

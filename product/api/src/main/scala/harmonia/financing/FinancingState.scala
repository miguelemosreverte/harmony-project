package harmonia.financing

import harmonia.protocol.JsonCodec
import io.circe.{Codec, Decoder, Encoder}

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
    workflow: ProgressStatus,
    application: Option[ApplicationStatus],
    privateDetails: Option[String],
    evidenceAvailable: Boolean,
    eligible: Vector[FinancingAction],
    currentStep: String
)

object FinancingState:
  given Codec.AsObject[FinancingState] = JsonCodec.derived[FinancingState]

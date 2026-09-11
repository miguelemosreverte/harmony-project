package harmonia.protocol

import io.circe.{Decoder, Encoder}

enum SubmissionStatus(val wire: String):
  case Pending extends SubmissionStatus("pending")
  case Committed extends SubmissionStatus("committed")
  case Rejected extends SubmissionStatus("rejected")
  case Stale extends SubmissionStatus("stale")
  case Unavailable extends SubmissionStatus("unavailable")
  case Unconfirmed extends SubmissionStatus("disconnected")

object SubmissionStatus:
  given Encoder[SubmissionStatus] = Encoder.encodeString.contramap(_.wire)
  given Decoder[SubmissionStatus] = Decoder.decodeString.emap(value =>
    values.find(_.wire == value).toRight(s"Unknown submission status: $value")
  )

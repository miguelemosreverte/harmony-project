package harmonia.protocol

import io.circe.Codec

final case class LedgerUpdate(
    updateId: Option[String],
    commandId: Option[String],
    events: Vector[String]
)
object LedgerUpdate:
  given Codec.AsObject[LedgerUpdate] =
    Codec.forProduct3("update_id", "command_id", "events")(LedgerUpdate.apply)(v =>
      (v.updateId, v.commandId, v.events)
    )

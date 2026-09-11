package harmonia.protocol

import harmonia.protocol.JsonCodec
import io.circe.Codec

final case class LedgerUpdate(
    updateId: Option[String],
    commandId: Option[String],
    events: Vector[String]
)
object LedgerUpdate:
  given Codec.AsObject[LedgerUpdate] = JsonCodec.derived[LedgerUpdate]

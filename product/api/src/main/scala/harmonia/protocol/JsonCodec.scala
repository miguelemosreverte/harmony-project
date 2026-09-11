package harmonia.protocol

import io.circe.Codec
import io.circe.derivation.{Configuration, ConfiguredCodec}
import scala.deriving.Mirror

/** Public field names follow one convention: camelCase in Scala, snake_case in JSON. */
private[harmonia] object JsonCodec:
  inline def derived[A](using Mirror.Of[A]): Codec.AsObject[A] =
    given Configuration = Configuration.default.withSnakeCaseMemberNames
    ConfiguredCodec.derived[A]

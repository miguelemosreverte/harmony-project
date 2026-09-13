package harmonia.scene.support

import io.circe.{Codec, Decoder, Encoder}

/** Illustrations identify speakers; only the supplied dialogue describes an outcome. */
enum Portrait:
  case Bank, Alice, Ben, Sofia, Developer, Reviewer

object Portrait:
  given Encoder[Portrait] = Encoder.encodeString.contramap(_.toString.toLowerCase)
  given Decoder[Portrait] = Decoder.decodeString.emap(value =>
    Portrait.values.find(_.toString.toLowerCase == value).toRight("Unknown portrait")
  )

final case class Speech(portrait: Portrait, name: String, text: String)
object Speech:
  given Codec.AsObject[Speech] = Codec.AsObject.derived[Speech]

/** A short exchange beside an application, separate from its diagram and commands. */
final case class Conversation(first: Speech, second: Speech)
object Conversation:
  given Codec.AsObject[Conversation] = Codec.AsObject.derived[Conversation]

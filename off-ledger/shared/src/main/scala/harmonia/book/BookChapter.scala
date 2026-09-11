package harmonia.book

import io.circe.Decoder

final case class BookChapter(id: String, title: String, html: String)
object BookChapter:
  given Decoder[BookChapter] = Decoder.forProduct3("id", "title", "html")(BookChapter.apply)

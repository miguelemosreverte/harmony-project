package harmonia.book

import io.circe.Codec

final case class BookChapter(id: String, title: String, html: String)
object BookChapter:
  given Codec.AsObject[BookChapter] =
    Codec.forProduct3("id", "title", "html")(BookChapter.apply)(v => (v.id, v.title, v.html))

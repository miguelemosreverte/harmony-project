package harmonia.book

import io.circe.{Decoder, Json}
import harmonia.stories.compare.CompareResults

final case class RecordedStory(
    id: String,
    title: String,
    description: String,
    input: Json,
    expected: Json,
    actual: Json,
    provenance: Json
):
  def actions: Vector[Json] = input.hcursor.get[Vector[Json]]("actions").getOrElse(Vector.empty)
  def expectedActions: Vector[Json] =
    expected.hcursor.get[Vector[Json]]("actions").getOrElse(Vector.empty)
  def actualActions: Vector[Json] =
    actual.hcursor.get[Vector[Json]]("actions").getOrElse(Vector.empty)
  def differences = CompareResults.compare(expected, actual)

object RecordedStory:
  given Decoder[RecordedStory] = Decoder.instance { cursor =>
    for
      id <- cursor.get[String]("id")
      title <- cursor.get[String]("title")
      description <- cursor.get[String]("description")
      input <- cursor.get[Json]("input")
      expected <- cursor.get[Json]("expected")
      actual <- cursor.get[Json]("actual")
      provenance <- cursor.get[Json]("provenance")
    yield RecordedStory(id, title, description, input, expected, actual, provenance)
  }

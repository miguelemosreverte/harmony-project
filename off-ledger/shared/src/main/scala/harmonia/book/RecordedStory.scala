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
  def isBoundaryReport: Boolean =
    input.hcursor.get[String]("ledger_script").contains("Boundaries:run")
  def actions: Vector[Json] =
    if isBoundaryReport then
      Vector(
        Json.obj(
          "id" -> Json.fromString("core"),
          "actor" -> Json.fromString("Ledger script"),
          "action" -> Json.fromString("exercise supported limits")
        ),
        Json.obj(
          "id" -> Json.fromString("race"),
          "actor" -> Json.fromString("Bank"),
          "action" -> Json.fromString("compete for one step")
        )
      )
    else input.hcursor.get[Vector[Json]]("actions").getOrElse(Vector.empty)
  def expectedActions: Vector[Json] =
    phases(expected)
  def actualActions: Vector[Json] =
    phases(actual)
  private def phases(result: Json): Vector[Json] =
    if isBoundaryReport then
      Vector("core", "race").map(name =>
        result.hcursor
          .downField(name)
          .focus
          .getOrElse(Json.obj())
          .deepMerge(
            Json.obj("id" -> Json.fromString(name), "outcome" -> Json.fromString("observed"))
          )
      )
    else result.hcursor.get[Vector[Json]]("actions").getOrElse(Vector.empty)
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

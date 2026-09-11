package harmonia.book.project

import io.circe.Json

/** Limits and concurrent submissions are verification phases, not fabricated business actions. */
object BoundaryPhases:
  def apply(expected: Json, actual: Json): Vector[Json] = Vector(
    ("core", "Ledger script", "Exercise supported limits"),
    ("race", "Bank", "Compete for one step")
  ).map { (id, actor, description) =>
    val left = expected.hcursor.downField(id).focus.getOrElse(Json.Null)
    val right = actual.hcursor.downField(id).focus.getOrElse(Json.Null)
    PresentStory.unit(
      id,
      actor,
      description,
      left,
      right,
      s"${right.asObject.fold(0)(_.size)} observations",
      "Verification phase"
    )
  }

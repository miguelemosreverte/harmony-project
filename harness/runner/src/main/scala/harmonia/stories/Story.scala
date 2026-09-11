package harmonia.stories

import io.circe.Json

final case class StoryAction(
    id: String,
    actor: String,
    action: String,
    request: Option[String] = None
):
  def json: Json = Json.obj(
    "id" -> Json.fromString(id),
    "actor" -> Json.fromString(actor),
    "action" -> Json.fromString(action),
    "request" -> request.fold(Json.Null)(Json.fromString)
  )

trait Story:
  def id: String
  def actions: Vector[StoryAction]
  def workflow: Option[String]
  def scriptInput: Json

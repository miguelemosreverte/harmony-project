package harmonia.stories

import io.circe.Json

final case class StoryAction(id: String, actor: String, action: String)

final case class Story(
    id: String,
    bank: String,
    buyer: String,
    status: String,
    actions: Vector[StoryAction]
):
  def scriptInput: Json = Json.obj(
    "namespace" -> Json.fromString(id),
    "bankName" -> Json.fromString(bank),
    "buyerName" -> Json.fromString(buyer),
    "initialStatus" -> Json.fromString(status),
    "actions" -> Json.arr(actions.map { action =>
      Json.obj(
        "id" -> Json.fromString(action.id),
        "actor" -> Json.fromString(action.actor),
        "action" -> Json.fromString(action.action)
      )
    }*)
  )

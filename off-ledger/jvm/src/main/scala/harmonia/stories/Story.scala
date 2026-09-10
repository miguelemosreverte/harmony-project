package harmonia.stories

import io.circe.Json

final case class StoryAction(id: String, actor: String, action: String)

final case class Story(
    id: String,
    bank: String,
    buyer: String,
    status: String,
    actions: Vector[StoryAction],
    workflow: Option[String] = None,
    integration: Option[String] = None,
    reviewer: Option[String] = None,
    privateDetails: Option[String] = None
):
  def scriptInput: Json = Json.obj(
    "namespace" -> Json.fromString(id),
    "bankName" -> Json.fromString(bank),
    "buyerName" -> Json.fromString(buyer),
    "initialStatus" -> Json.fromString(status),
    "workflow" -> workflow.fold(Json.Null)(Json.fromString),
    "integration" -> integration.fold(Json.Null)(Json.fromString),
    "reviewerName" -> reviewer.fold(Json.Null)(Json.fromString),
    "privateDetails" -> privateDetails.fold(Json.Null)(Json.fromString),
    "actions" -> Json.arr(actions.map { action =>
      Json.obj(
        "id" -> Json.fromString(action.id),
        "actor" -> Json.fromString(action.actor),
        "action" -> Json.fromString(action.action)
      )
    }*)
  )

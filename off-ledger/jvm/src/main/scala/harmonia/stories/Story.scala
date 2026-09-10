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

final case class Story(
    id: String,
    bank: String,
    buyer: String,
    status: String,
    actions: Vector[StoryAction],
    workflow: Option[String] = None,
    integration: Option[String] = None,
    reviewer: Option[String] = None,
    privateDetails: Option[String] = None,
    purchase: Option[harmonia.stories.purchase.model.PurchaseSetup] = None
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
    "purchase" -> purchase.fold(Json.Null)(_.json),
    "actions" -> Json.fromValues(actions.map(_.json))
  )

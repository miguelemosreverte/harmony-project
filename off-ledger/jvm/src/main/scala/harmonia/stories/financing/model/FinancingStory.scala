package harmonia.stories.financing.model

import harmonia.stories.{Story, StoryAction}
import io.circe.Json

final case class FinancingStory(
    id: String,
    bank: String,
    buyer: String,
    status: String,
    actions: Vector[StoryAction],
    workflow: Option[String] = None,
    integration: Option[String] = None,
    reviewer: Option[String] = None,
    privateDetails: Option[String] = None
) extends Story:
  def scriptInput: Json = Json.obj(
    "namespace" -> Json.fromString(id),
    "bankName" -> Json.fromString(bank),
    "buyerName" -> Json.fromString(buyer),
    "initialStatus" -> Json.fromString(status),
    "workflow" -> workflow.fold(Json.Null)(Json.fromString),
    "integration" -> integration.fold(Json.Null)(Json.fromString),
    "reviewerName" -> reviewer.fold(Json.Null)(Json.fromString),
    "privateDetails" -> privateDetails.fold(Json.Null)(Json.fromString),
    "actions" -> Json.fromValues(actions.map(_.json))
  )

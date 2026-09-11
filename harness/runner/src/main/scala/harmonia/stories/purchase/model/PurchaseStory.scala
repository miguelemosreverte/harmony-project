package harmonia.stories.purchase.model

import harmonia.stories.{Story, StoryAction}
import io.circe.Json

final case class PurchaseStory(
    id: String,
    bank: String,
    buyer: String,
    actions: Vector[StoryAction],
    setup: PurchaseSetup
) extends Story:
  val workflow = Some("property-purchase")
  def scriptInput: Json = Json.obj(
    "namespace" -> Json.fromString(id),
    "bankName" -> Json.fromString(bank),
    "buyerName" -> Json.fromString(buyer),
    "setup" -> setup.json,
    "actions" -> Json.fromValues(actions.map(_.json))
  )

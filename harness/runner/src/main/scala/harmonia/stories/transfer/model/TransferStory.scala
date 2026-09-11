package harmonia.stories.transfer.model

import harmonia.stories.{Story, StoryAction}
import io.circe.Json

final case class TransferSetup(
    buyer: String,
    seller: String,
    source: String,
    destination: String,
    settler: String,
    asset: String,
    quantity: BigDecimal,
    receipts: String
):
  val parties = Vector(buyer, seller, source, destination)
  val nodes = Vector("buyer", "seller", "source", "destination").zip(parties).map(_.swap).toMap
  def json: Json = Json.obj(
    "buyerName" -> Json.fromString(buyer),
    "sellerName" -> Json.fromString(seller),
    "sourceName" -> Json.fromString(source),
    "destinationName" -> Json.fromString(destination),
    "settlerName" -> Json.fromString(settler),
    "asset" -> Json.fromString(asset),
    "quantity" -> Json.fromString(quantity.bigDecimal.toPlainString),
    "receipts" -> Json.fromString(receipts)
  )

final case class TransferStory(id: String, actions: Vector[StoryAction], setup: TransferSetup)
    extends Story:
  val workflow = Some("atomic-transfer")
  def scriptInput: Json = Json.obj(
    "namespace" -> Json.fromString(id),
    "setup" -> setup.json,
    "actions" -> Json.fromValues(actions.map(_.json))
  )

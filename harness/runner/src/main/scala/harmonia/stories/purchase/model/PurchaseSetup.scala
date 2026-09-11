package harmonia.stories.purchase.model

import io.circe.Json

final case class PurchaseSetup(
    buyerAgent: String,
    sellerAgent: String,
    property: String,
    documents: String,
    decision: String,
    evidence: String
):
  def json: Json = Json.obj(
    "buyerAgentName" -> Json.fromString(buyerAgent),
    "sellerAgentName" -> Json.fromString(sellerAgent),
    "property" -> Json.fromString(property),
    "documents" -> Json.fromString(documents),
    "decision" -> Json.fromString(decision),
    "evidence" -> Json.fromString(evidence)
  )

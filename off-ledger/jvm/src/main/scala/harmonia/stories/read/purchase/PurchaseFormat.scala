package harmonia.stories.read.purchase

import cats.syntax.all.*
import harmonia.stories.{Story, StoryAction}
import harmonia.stories.purchase.model.PurchaseSetup
import harmonia.stories.read.StoryFormat.{fields, text}
import io.circe.Json

object PurchaseFormat:
  def input(id: String, json: Json): Either[String, Story] = for
    root <- fields(json, "scenario", Set("workflow", "setup", "actions"))
    setup <- fields(root("setup").get, "setup", Set("application", "offer"))
    app <- fields(
      setup("application").get,
      "application",
      Set("bank", "buyer", "status", "documents", "bank_decision")
    )
    offer <- fields(
      setup("offer").get,
      "offer",
      Set("buyer_agent", "seller_agent", "property", "evidence")
    )
    bank <- text(app("bank").get, "bank")
    buyer <- text(app("buyer").get, "buyer")
    status <- text(app("status").get, "status")
    documents <- text(app("documents").get, "documents")
    decision <- text(app("bank_decision").get, "bank_decision")
    buyerAgent <- text(offer("buyer_agent").get, "buyer_agent")
    sellerAgent <- text(offer("seller_agent").get, "seller_agent")
    property <- text(offer("property").get, "property")
    evidence <- text(offer("evidence").get, "evidence")
    parties = Set(bank, buyer, buyerAgent, sellerAgent)
    _ <- Either.cond(parties.size == 4, (), "Purchase requires four distinct parties")
    _ <- Either.cond(
      status == "pending" && Set("approved", "rejected")(decision),
      (),
      "Purchase starts with a pending application and an explicit bank decision"
    )
    _ <- Either.cond(
      Set(
        "financing",
        "missing",
        "wrong-issuer",
        "wrong-subject",
        "wrong-buyer",
        "wrong-continuation"
      )(evidence),
      (),
      "Unsupported financing evidence fixture"
    )
    values <- root("actions").get.asArray.toRight("actions must be a list")
    _ <- Either.cond(
      values.nonEmpty && values.size <= 32,
      (),
      "A story must contain 1 to 32 actions"
    )
    actions <- values.traverse { value =>
      for
        fields <- fields(value, "action", Set("id", "actor", "action"))
        name <- text(fields("id").get, "id")
        actor <- text(fields("actor").get, "actor")
        action <- text(fields("action").get, "action")
        _ <- Either.cond(parties(actor), (), s"$name: unknown actor $actor")
        _ <- Either.cond(
          Set(
            "assess-financing",
            "open-offer",
            "make-proposal",
            "relay-proposal",
            "receive-proposal",
            "forge-proposal"
          )(action),
          (),
          s"$name: unsupported purchase action $action"
        )
      yield StoryAction(name, actor, action)
    }
    _ <- Either.cond(
      actions.map(_.id).distinct.size == actions.size,
      (),
      "Action IDs must be unique"
    )
  yield Story(
    id,
    bank,
    buyer,
    status,
    actions,
    workflow = Some("property-purchase"),
    purchase = Some(PurchaseSetup(buyerAgent, sellerAgent, property, documents, decision, evidence))
  )

package harmonia.book.narrative

import harmonia.book.RecordedStory
import harmonia.examples.ExampleKind
import harmonia.scene.support.*
import io.circe.Json

/** Dialogue follows observed results; golden expectations never narrate an outcome. */
object RecordedConversation:
  def apply(story: RecordedStory, index: Int): Conversation =
    val unit = story.units.lift(index)
    val actual = unit.map(_.actual).getOrElse(Json.Null)
    def text(key: String) = actual.hcursor.get[String](key).getOrElse("not observed")
    def flag(key: String) = actual.hcursor.get[Boolean](key).contains(true)
    def number(key: String) = actual.hcursor.get[Int](key).toOption.fold("not observed")(_.toString)
    val setup = story.input.hcursor.downField("setup")
    val bank = setup.downField("application").get[String]("bank").getOrElse("Bank")
    val buyer = setup.downField("application").get[String]("buyer").getOrElse("Buyer")
    def pair(a: Portrait, name: String, line: String, b: Portrait, other: String, reply: String) =
      Conversation(Speech(a, name, line), Speech(b, other, reply))
    def financing(line: String, reply: String) =
      pair(Portrait.Bank, bank, line, Portrait.Alice, buyer, reply)
    def review(line: String, reply: String) =
      pair(Portrait.Developer, "Developer", line, Portrait.Reviewer, "Reviewer", reply)
    def agents(line: String, reply: String) =
      pair(Portrait.Ben, "Ben", line, Portrait.Sofia, "Sofia", reply)
    val transfer = story.kind == ExampleKind.Transfer
    val trade = setup.downField("trade")
    val amount = trade.get[String]("quantity").getOrElse("?") + " " + trade
      .get[String]("asset")
      .getOrElse("assets")
    def custodians(line: String, reply: String) =
      pair(Portrait.Bank, "Source", line, Portrait.Bank, "Destination", reply)
    if unit.isEmpty then
      if transfer then
        pair(
          Portrait.Ben,
          "Seller",
          s"I want to transfer $amount.",
          Portrait.Alice,
          "Alice",
          "Both custodians must be ready."
        )
      else financing("Your financing application is pending.", "I’d like to make an offer.")
    else if actual.isNull then
      review("This attempt has no recorded result.", "We cannot tell what happened.")
    else if story.kind == ExampleKind.Boundaries then
      if unit.get.id == "race" then
        review(
          s"${number("committed")} request committed; ${number("conflicting")} conflicted.",
          s"${number("approved_sources")} approval was recorded."
        )
      else
        review(
          s"The run exercised ${number("maximum_steps")} workflow steps.",
          if flag("graph_overflow_rejected") then "The oversized graph was refused."
          else "Check the observed limit report."
        )
    else if story.kind == ExampleKind.Packages then
      val http = actual.hcursor.get[Int]("http").toOption
      val lines = unit.get.action match
        case "upload" => "The uploaded DAR was inspected." -> "Its package identity is now known."
        case "retrieve" if http.contains(403) =>
          "This session cannot retrieve packages." -> "Package access belongs to the bank."
        case "retrieve" if !flag("supported") =>
          "The DAR was inspected." -> "There is no reviewed mapping for it."
        case "retrieve" =>
          "The verified source returned a DAR." -> "A reviewed mapping is available."
        case "generate" if http.contains(200) =>
          "The adapter compiled successfully." -> (if flag("available_live") then
                                                     "Live availability is also confirmed."
                                                   else "Live availability is still separate.")
        case "generate"  => "Generation was refused." -> "A supported mapping is required."
        case "malformed" => "This input is not a valid DAR." -> "The request was refused."
        case "oversized" => "This upload exceeds the size limit." -> "The request was refused."
        case _ => "The package request was observed." -> s"Its response was HTTP ${number("http")}."
      review(lines._1, lines._2)
    else if text("outcome") == "duplicate" then
      financing("This request was already recorded.", "The retry did not execute it again.")
    else if text("outcome") == "rejected" then
      val action = unit.get.action
      if transfer then
        val reply = text("trade") match
          case "settled"          => "The asset had already arrived."
          case "proposed"         => "The seller’s agreement is still needed."
          case "agreed"           => "The source has not confirmed readiness."
          case "source-confirmed" => "The destination is not ready yet."
          case _                  => "Settlement needs the assigned authority."
        if text("reason") == "destination-rejected" then
          custodians("The final transfer rolled back.", "The earlier source lock remains.")
        else if action == "withdraw-directly" then
          custodians("Direct withdrawal was refused.", "Use the coordinated settlement.")
        else custodians("This attempt was refused.", reply)
      else if action == "receive-proposal" then
        agents("The proposal has not been relayed yet.", "I must wait before receiving it.")
      else if action == "make-proposal" then
        val reply =
          if text("application") == "rejected" then "Declined financing cannot authorize it."
          else if !flag("evidence_available") then "The required result is unavailable."
          else "The result does not match this offer."
        pair(Portrait.Alice, buyer, "My proposal was refused.", Portrait.Ben, "Ben", reply)
      else if action == "accept" then
        financing("I proposed this plan.", "Only the buyer can consent to it.")
      else if action == "advance" then
        review("This action was refused.", "Both its order and actor must be valid.")
      else if action == "forge-completion" || action == "forge-proposal" then
        financing("That shortcut was refused.", "Progress needs an authorized action.")
      else if text("reason") == "not-visible" then
        financing("The required contract is private.", "I cannot act on a contract I cannot see.")
      else if action == "complete-join" then
        financing(
          "The join attempt was refused.",
          if text("workflow") == "complete" then "This workflow is already complete."
          else "The selected path must finish first."
        )
      else if action == "publish-approval" then
        financing(
          "The continuation was refused.",
          if text("application") == "pending" then "The bank must approve first."
          else "Only the buyer may continue."
        )
      else if action == "choose-decline" || action == "choose-approve" then
        financing("The branch cannot be changed now.", "We follow the recorded decision.")
      else if action == "close-application" then
        financing("Closure belongs to the other branch.", "We follow the approval path.")
      else if text("branch") == "decline" then
        financing("Financing belongs to the other branch.", "We follow the decline path.")
      else if action == "confirm-review" then
        financing("The review cannot run yet.", "Financing must complete first.")
      else if text("reason") == "unauthorized" then
        financing(
          "Only the bank can approve financing.",
          "My attempt did not change the application."
        )
      else
        financing(
          "This approval attempt was refused.",
          "An existing approval cannot be exercised again."
        )
    else
      unit.get.action match
        case "assess-financing" if text("application") == "rejected" =>
          financing("Your financing was declined.", "I cannot use it to make a proposal.")
        case "assess-financing" =>
          financing(
            "Your financing is approved.",
            if flag("evidence_available") then "The next application must check the result."
            else "The handoff result is not available here."
          )
        case "approve-financing" =>
          financing(
            "The application is now approved.",
            if text("workflow") == "complete" then "The shared workflow is complete."
            else if story.kind == ExampleKind.Private then "I can now publish the scoped result."
            else "The approval is recorded."
          )
        case "open-offer" =>
          pair(
            Portrait.Alice,
            buyer,
            "My offer is open.",
            Portrait.Ben,
            "Ben",
            "We still need a valid financing result."
          )
        case "make-proposal" =>
          pair(
            Portrait.Alice,
            buyer,
            "My proposal is ready.",
            Portrait.Ben,
            "Ben",
            "I can relay it to Sofia."
          )
        case "relay-proposal" =>
          agents("I’ve relayed Alice’s proposal.", "It is ready for me to receive.")
        case "receive-proposal" =>
          agents("Alice’s proposal has reached you.", "Received — this handoff is complete.")
        case "agree-trade" =>
          pair(
            Portrait.Ben,
            "Seller",
            "I’ve agreed to the trade.",
            Portrait.Alice,
            "Alice",
            "The assets have not moved yet."
          )
        case "lock-position" =>
          custodians(s"$amount is reserved here.", "Nothing has arrived here yet.")
        case "confirm-source" =>
          custodians("The source is ready to withdraw.", "I still need to prepare receipt.")
        case "prepare-destination" =>
          custodians(
            "The source position remains locked.",
            "The receipt permission is now prepared."
          )
        case "confirm-destination" =>
          custodians("Both sides are ready.", "The assigned settler can now proceed.")
        case "settle" =>
          custodians(
            "The reserved position was withdrawn.",
            s"${text("destination")} ${trade.get[String]("asset").getOrElse("assets")} arrived in the same transaction."
          )
        case "choose-approve" =>
          financing("I’ve chosen the approval path.", "Financing and review can now proceed.")
        case "choose-decline" =>
          financing("I’ve chosen the decline path.", "We record closure instead of approval.")
        case "close-application" =>
          financing("The closure is recorded.", "The selected branch can now join.")
        case "confirm-review" =>
          financing(
            "Your review is confirmed.",
            if text("workflow") == "complete" then "The workflow is complete."
            else "The selected path is ready to join."
          )
        case "complete-join" =>
          financing("The selected path has joined.", "This workflow is complete.")
        case "publish-approval" =>
          financing(
            "The financing documents stay private.",
            "I’ve shared the scoped approval result."
          )
        case "wait" => financing("The approval remains recorded.", "My review is still pending.")
        case "reconnect" =>
          financing("The ledger kept our progress.", "I reconnected at the pending review.")
        case "propose" =>
          financing("I’ve proposed these exact actions.", "Nothing starts before my consent.")
        case "accept" =>
          financing("The plan now has its source contracts.", "I’ve accepted the proposed actions.")
        case "advance" =>
          review(
            s"${unit.get.id} was completed.",
            if text("workflow") == "complete" then "The composed workflow is complete."
            else "The next assigned action is enabled."
          )
        case _ =>
          review(
            "This action has a recorded observation.",
            "Inspect the evidence for its exact result."
          )

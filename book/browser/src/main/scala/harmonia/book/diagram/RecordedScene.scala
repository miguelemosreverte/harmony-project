package harmonia.book.diagram

import harmonia.book.{RecordedStory, StoryUnit}
import harmonia.scene.*
import io.circe.Json
import io.circe.syntax.*
import scala.scalajs.js.annotation.JSExportTopLevel

/** One projection serves both featured chapters and the complete recording laboratory. */
object RecordedScene:
  private def text(json: Json, field: String): String =
    json.hcursor.get[String](field).getOrElse("Not observed")
  private def field(json: Json, name: String): Json =
    json.hcursor.downField(name).focus.getOrElse(Json.Null)
  private def person(name: String, role: String, icon: String = "person") =
    ScenePerson(name, name, role, icon)
  def apply(story: RecordedStory, step: Int, actor: String = "all"): SceneFrame =
    val unit = story.units.lift(step - 1)
    val actual = unit.map(_.actual).getOrElse(Json.Null)
    val setup = field(story.input, "setup")
    val refused = actual.hcursor.get[String]("outcome").contains("rejected")
    val base =
      if setup.hcursor.downField("trade").succeeded then transfer(setup, unit, actual)
      else purchase(setup, unit, actual)
    val result =
      if unit.exists(_.actual.isNull) then
        base.copy(
          title = "No observation was recorded.",
          caption = "The input describes an attempt, but no resulting state is available.",
          phase = 0
        )
      else if refused then
        val reason = text(actual, "reason")
        val (title, caption) =
          if unit.exists(_.id == "rejected-financing") then
            "The offer cannot proceed." -> "The financing decision is rejected, so the property application creates no proposal."
          else if reason == "destination-rejected" then
            "Settlement rolls back." -> "The destination refused receipt. The final transaction rolled back; the earlier source lock remains."
          else
            "This attempt is refused." -> Map(
              "not-visible" -> "The required private contract is not visible to this actor.",
              "unauthorized" -> "This actor does not have the required authority.",
              "application-rejected" -> "The application refused this attempt under its rules."
            ).getOrElse(reason, unit.get.observedState)
        base.copy(title = title, caption = caption, refused = true)
      else base
    result.copy(focus =
      if actor == "all" then unit.map(_.actor).getOrElse(base.people.head.id) else actor
    )

  private def purchase(setup: Json, unit: Option[StoryUnit], actual: Json): SceneFrame =
    val app = field(setup, "application"); val offer = field(setup, "offer")
    val bank = text(app, "bank"); val buyer = text(app, "buyer")
    val agent = text(offer, "buyer_agent"); val seller = text(offer, "seller_agent")
    val phase = text(actual, "proposal") match
      case "received" => 4
      case "relayed"  => 3
      case "draft"    => 2
      case _ => if actual.hcursor.get[Boolean]("evidence_available").contains(true) then 1 else 0
    val (title, caption) = unit match
      case None =>
        s"$buyer wants to make an offer." -> s"Her financing application is with $bank. The property agents will need a verified result to move the offer forward."
      case Some(value) =>
        value.action match
          case "assess-financing" if text(actual, "application") == "approved" =>
            s"$bank approves. $buyer can make her proposal." -> s"A verified result is available to $buyer. Her financing documents stay within the financing application."
          case "assess-financing" =>
            s"$bank declines the financing." -> "The result records a refusal. It cannot authorize a purchase proposal."
          case "open-offer" =>
            s"$buyer prepares the offer." -> "The property application is ready to check her financing result."
          case "make-proposal" =>
            s"$buyer makes her proposal." -> "The property application accepts the approved financing result and creates one proposal."
          case "relay-proposal" =>
            s"$agent relays the proposal." -> "The buyer’s agent passes the proposal to the seller’s agent."
          case "receive-proposal" =>
            s"$seller receives the proposal." -> "The financing-to-offer workflow is complete. The property agents receive the proposal, without the private financing documents."
          case _ => "An additional attempt." -> value.observedState
    SceneFrame(
      SceneKind.Purchase,
      title,
      caption,
      Vector(
        person(bank, "Financing", "bank"),
        person(buyer, "Buyer"),
        person(agent, "Buyer’s agent"),
        person(seller, "Seller’s agent")
      ),
      phase,
      "",
      if phase >= 2 then "Purchase proposal"
      else if phase == 1 then "Financing · " + text(actual, "application")
      else "",
      false,
      Vector.empty
    )

  private def transfer(setup: Json, unit: Option[StoryUnit], actual: Json): SceneFrame =
    val trade = field(setup, "trade"); val source = field(actual, "source")
    val quantity = text(trade, "quantity"); val asset = text(trade, "asset")
    def observed(json: Json, key: String, initial: String) =
      if unit.isEmpty then initial else text(json, key)
    val available = observed(source, "available", quantity)
    val locked = observed(source, "locked", "0")
    val received = observed(actual, "destination", "0")
    val phase =
      if text(actual, "trade") == "settled" then 4
      else if text(actual, "trade") == "ready" then 3
      else if locked.toDoubleOption.exists(_ > 0) then 2
      else if unit.nonEmpty then 1
      else 0
    val (title, caption) = unit match
      case None =>
        "One trade. Four responsibilities." -> s"${text(trade, "seller")} is transferring $quantity $asset to ${text(trade, "buyer")}. The two custodians must prepare before settlement."
      case Some(value) =>
        value.action match
          case "agree-trade" =>
            "The seller agrees." -> s"${text(trade, "seller")} accepts the trade. No assets have moved."
          case "lock-position" =>
            "The position is locked." -> s"$quantity $asset is reserved at the source. It is no longer available for another transfer."
          case "confirm-source" =>
            "The source is ready." -> s"${text(trade, "source")} confirms that the locked position can be withdrawn during settlement."
          case "prepare-destination" =>
            "The receiving side prepares." -> s"${text(trade, "destination")} creates the permission needed to receive the asset."
          case "confirm-destination" =>
            "Both sides are ready." -> s"The destination confirms readiness. ${text(trade, "settler")} can now request settlement."
          case "settle" =>
            "The asset arrives." -> s"One final transaction withdraws the locked position and records receipt of $received $asset."
          case _ => "An additional attempt." -> value.observedState
    val artifact =
      if phase == 4 then "Received · " + received
      else if locked.toDoubleOption.exists(_ > 0) then "Locked · " + locked
      else "Available · " + available
    val amounts = Vector(
      "Available at source" -> available,
      "Locked at source" -> locked,
      "Received at destination" -> received
    ).map { (label, value) =>
      SceneAmount(
        label,
        s"$value $asset",
        (for q <- quantity.toDoubleOption.filter(_ > 0); n <- value.toDoubleOption
        yield n / q).getOrElse(0.0)
      )
    }
    SceneFrame(
      SceneKind.Transfer,
      title,
      caption,
      Vector(
        person(text(trade, "seller"), "Owns the position"),
        person(text(trade, "source"), "Source custodian", "bank"),
        person(text(trade, "buyer"), "Receives the asset"),
        person(text(trade, "destination"), "Destination custodian", "bank")
      ),
      phase,
      "",
      s"$artifact $asset",
      false,
      amounts
    )

  @JSExportTopLevel("projectHarmoniaRecording")
  def project(json: String, step: Int, actor: String): String =
    val story = io.circe.parser.decode[RecordedStory](json).fold(throw _, identity)
    apply(story, step, actor).asJson.noSpaces

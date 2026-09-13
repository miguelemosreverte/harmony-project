package harmonia.financing

import harmonia.scene.support.*

/** Inspecting a future handoff never advances its observed state. */
object FinancingConversation:
  def apply(state: FinancingState, handoff: String): Conversation =
    val complete = state.workflow == ProgressStatus.Complete
    val approved = state.application.contains(ApplicationStatus.Approved) || state.evidenceAvailable
    val (first, second) = handoff match
      case "application" if complete || approved =>
        "The financing approval is recorded." -> "The application documents stay private."
      case "application" if state.actor == "bank" =>
        "The financing case awaits my decision." -> "I’m waiting for the signed result."
      case "application" =>
        "The private decision is not visible here." -> "We can follow the shared progress."
      case "approval" if complete =>
        "The signed result was used." -> "My continuation is complete."
      case "approval" if approved =>
        "The signed result is ready." -> "I can continue with this approval."
      case "approval" =>
        "No usable result is visible here yet." -> "The buyer’s continuation must wait."
      case _ if complete =>
        "The private documents stayed with the bank." -> "The shared workflow is complete."
      case _ =>
        "The shared workflow is still waiting." -> "It completes after the buyer continues."
    Conversation(Speech(Portrait.Bank, "Bank", first), Speech(Portrait.Alice, "Buyer", second))

package harmonia.financing

import harmonia.scene.{SceneFrame, SceneKind, ScenePerson}

/** Only facts visible to this participant determine the scene. */
object FinancingScene:
  def apply(state: FinancingState): SceneFrame =
    val complete = state.workflow == ProgressStatus.Complete
    val approved = state.application.contains(ApplicationStatus.Approved) || state.evidenceAvailable
    val (title, caption) =
      if complete then
        "The handoff is complete." -> "The shared workflow records the buyer's continuation."
      else if state.actor == "bank" && approved then
        "Your approval is issued." -> "The buyer can now use the signed result. The private application stays with the bank."
      else if state.actor == "buyer" && approved then
        "Your next step is ready." -> "Use the bank's signed result to continue the shared workflow."
      else if state.actor == "bank" then
        "Review the financing case." -> "Approve the private application to give the buyer a signed result."
      else if state.actor == "buyer" then
        "The bank is reviewing your case." -> "This view will update when your signed result arrives."
      else
        "Follow the shared progress." -> "This observer session sees the shared workflow. Private financing details stay with the bank."
    SceneFrame(
      SceneKind.Financing,
      title,
      caption,
      Vector(
        ScenePerson("bank", "Bank", "Private decision", "bank"),
        ScenePerson("buyer", "Buyer", "Uses the result", "person"),
        ScenePerson("reviewer", "Observer", "Shared progress", "person")
      ),
      if complete then 4 else if approved then 2 else 0,
      if complete then "reviewer" else if approved then "buyer" else "bank",
      if approved then "Signed approval" else if complete then "Completed handoff" else "",
      false,
      Vector.empty
    )

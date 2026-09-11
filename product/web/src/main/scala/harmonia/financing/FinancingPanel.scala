package harmonia.financing

import harmonia.ui.Elements.*
import harmonia.scene.SceneView
import org.scalajs.dom

/** The scene stays mounted while polling updates its observed state. */
final class FinancingPanel:
  val root = element("section", "financing-scene")
  private val scene = new SceneView(root)
  private val actionArea = element("div", "financing-action")
  private val details = element("details", "private-case")
  private val detailBody = element("div")
  private val status = element("p", "visually-hidden"); status.id = "live-workflow"
  append(details, element("summary", text = "Your private application"), detailBody)
  append(root, actionArea, details, status)

  def render(state: FinancingState, blocked: Boolean, submit: FinancingAction => Unit): Unit =
    scene.render(FinancingScene(state))
    status.textContent = s"Workflow: ${state.workflow.wire}"
    actionArea.textContent = ""
    state.eligible.foreach { action =>
      val label = action match
        case FinancingAction.Approve  => "Approve financing →"
        case FinancingAction.Continue => "Continue with this approval →"
      val control = button(
        if blocked then "Waiting for confirmation…" else label,
        "primary",
        "live-" + action.wire
      )(submit(action))
      control.disabled = blocked
      append(actionArea, control)
    }
    hide(details, state.application.isEmpty)
    detailBody.textContent = ""
    state.application.foreach(value =>
      append(detailBody, element("p", text = "Status: " + value.wire))
    )
    state.privateDetails.foreach(value => append(detailBody, element("p", text = value)))

package harmonia.financing

import harmonia.ui.Elements.*
import org.scalajs.dom

object FinancingPanel:
  def render(
      state: FinancingState,
      blocked: Boolean,
      submit: FinancingAction => Unit
  ): dom.HTMLElement =
    val root = element("section")
    val flow = element("ol", "live-flow")
    Vector("Bank approves privately", "Bank issues a signed result", "Buyer continues").zipWithIndex
      .foreach { (label, index) =>
        val done =
          if index == 2 then state.workflow == ProgressStatus.Complete
          else state.evidenceAvailable || state.workflow == ProgressStatus.Complete
        append(
          flow,
          element("li", if done then "done" else "waiting", (if done then "✓ " else "○ ") + label)
        )
      }
    val panel = element("section", "live-panel")
    append(panel, element("h2", text = "Current state"), element("p", text = state.currentStep))
    val status = element("p", "live-workflow", s"Workflow: ${state.workflow.wire}");
    status.id = "live-workflow"
    append(panel, status)
    state.application.foreach { application =>
      append(
        panel,
        element("h3", text = "Your private application"),
        element("p", text = s"Status: ${application.wire}")
      )
      state.privateDetails.foreach(details => append(panel, element("p", text = details)))
    }
    state.eligible.foreach { action =>
      val label = action match
        case FinancingAction.Approve  => "Approve financing"
        case FinancingAction.Continue => "Continue shared workflow"
      val control = button(label, "primary", "live-" + action.wire)(submit(action))
      control.disabled = blocked; append(panel, control)
    }
    if state.eligible.isEmpty then
      append(
        panel,
        element(
          "p",
          text =
            if state.workflow == ProgressStatus.Complete then "This handoff is complete."
            else "No action is currently available for your session."
        )
      )
    append(root, flow, panel); root

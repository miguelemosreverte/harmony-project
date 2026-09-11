package harmonia.composition

import harmonia.ui.Elements.*
import harmonia.workspace.WorkspaceCommand
import org.scalajs.dom

object ComposerView:
  def render(
      state: CompositionState,
      blocked: Boolean,
      submit: WorkspaceCommand => Unit
  ): dom.HTMLElement =
    val root = element("div"); root.id = "composition-observations"
    if !state.available then
      append(root, element("p", text = "Your session has no access to the composition workspace."))
    else if !state.canPropose then
      append(
        root,
        element(
          "p",
          text = "The bank proposes; the buyer consents. Each action belongs to its assigned party."
        )
      )
    state.drafts.zipWithIndex.foreach { (draft, index) =>
      val card = element("article", "composition-record")
      append(
        card,
        element("h3", text = draft.name),
        element(
          "p",
          text = s"Reference: ${draft.reference} · Awaiting buyer consent · Sources not yet created"
        )
      )
      val list = element("ol")
      draft.steps.foreach(step =>
        append(
          list,
          element(
            "li",
            text = s"${step.id} · ${step.role} · ${step.actor.wire} · ${step.action.label}"
          )
        )
      )
      append(card, list)
      def action(label: String, command: WorkspaceCommand): Unit =
        val control = button(label, "primary", s"${command.wire}-$index")(submit(command))
        control.disabled = blocked; append(card, control)
      if draft.canAccept then
        action(
          "Accept this plan",
          WorkspaceCommand.Composition(CompositionCommand.Accept(draft.reference))
        )
      if draft.canCancel then
        action(
          "Cancel proposal",
          WorkspaceCommand.Composition(CompositionCommand.Cancel(draft.reference))
        )
      append(root, card)
    }
    state.processes.zipWithIndex.foreach { (process, index) =>
      val card = element("article", "composition-record")
      append(
        card,
        element("h3", text = process.name),
        element(
          "p",
          "composition-status",
          s"Reference: ${process.reference} · ${
              if process.complete then "Complete" else "In progress"
            }"
        )
      )
      val flow = element("ol", "live-flow")
      process.steps.zipWithIndex.foreach { (step, position) =>
        val row = element("li", if step.completed then "done" else "waiting")
        append(
          row,
          element(
            "strong",
            text = s"${step.id} · ${
                if step.completed then "Complete" else if step.enabled then "Ready" else "Waiting"
              }"
          ),
          element("p", text = s"${step.role} · ${step.actor}"),
          element(
            "p",
            text =
              s"${step.source.getOrElse("Source unavailable")} · ${step.status.getOrElse("unknown")}"
          ),
          element("p", text = s"Integration: ${step.integration.wire}")
        )
        if step.canExecute then
          val control =
            button(s"Execute ${step.id}", "primary", s"compose-execute-$index-$position") {
              submit(
                WorkspaceCommand.Composition(CompositionCommand.Advance(process.reference, step.id))
              )
            }
          control.disabled = blocked; append(row, control)
        append(flow, row)
      }
      append(card, flow); append(root, card)
    }
    if state.drafts.isEmpty && state.processes.isEmpty then
      append(root, element("p", text = "No workflow has been proposed yet."))
    root

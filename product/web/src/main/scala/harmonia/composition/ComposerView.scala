package harmonia.composition

/** @book.slice
  *   composition
  * @book.role
  *   Follow observed execution
  * @book.summary
  *   Observed completed, enabled, and executable steps determine the diagram and available actions.
  */

import harmonia.ui.Elements.*
import harmonia.workspace.WorkspaceCommand
import org.scalajs.dom
import harmonia.scene.{WorkflowDiagram, WorkflowDiagramView, DiagramNode, DiagramEdge, DiagramState}

object ComposerView:
  private var diagrams = Vector.empty[WorkflowDiagramView]
  private def diagram(
      root: dom.HTMLElement,
      title: String,
      caption: String,
      nodes: Vector[DiagramNode]
  ): Unit =
    val canvas = element("div")
    append(root, canvas)
    val renderer = new WorkflowDiagramView(canvas)
    diagrams :+= renderer
    renderer.render(
      WorkflowDiagram(
        title,
        caption,
        nodes,
        nodes
          .zip(nodes.drop(1))
          .map((a, b) =>
            DiagramEdge(
              a.id,
              b.id,
              if b.state == DiagramState.Complete then DiagramState.Complete
              else DiagramState.Pending
            )
          )
      )
    )
  def render(
      state: CompositionState,
      blocked: Boolean,
      submit: WorkspaceCommand => Unit
  ): dom.HTMLElement =
    diagrams.foreach(_.dispose()); diagrams = Vector.empty
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
      diagram(
        card,
        "The buyer reviews this exact plan.",
        "Draft proposal · no source contracts have been created.",
        draft.steps.map(step =>
          DiagramNode(step.id, step.action.label, step.role, step.actor.wire, DiagramState.Pending)
        )
      )
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
      diagram(
        card,
        if process.complete then "This plan is complete." else "Follow the next authorized action.",
        "Observed ledger state · a diagram selection never submits a command.",
        process.steps.map(step =>
          DiagramNode(
            step.id,
            step.id,
            step.source.getOrElse("Source unavailable"),
            step.actor,
            if step.completed then DiagramState.Complete
            else if step.enabled then DiagramState.Current
            else DiagramState.Pending
          )
        )
      )
      val flow = element("div", "composition-step-details")
      process.steps.zipWithIndex.foreach { (step, position) =>
        val row = element("details", "composition-step-detail")
        row.id = s"compose-detail-$index-$position"
        append(row, element("summary", text = step.id + " · inspect this action"))
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
          control.disabled = blocked; append(card, control)
        append(flow, row)
      }
      append(card, flow)
      card.addEventListener(
        "harmonia-select",
        (event: dom.Event) =>
          val selected = event.asInstanceOf[dom.CustomEvent].detail.toString
          process.steps.indexWhere(_.id == selected) match
            case -1       => ()
            case position => rowOpen(s"compose-detail-$index-$position")
      )
      append(root, card)
    }
    if state.drafts.isEmpty && state.processes.isEmpty then
      append(root, element("p", text = "No workflow has been proposed yet."))
    root

  private def rowOpen(id: String): Unit =
    Option(dom.document.getElementById(id)).foreach { node =>
      node.setAttribute("open", ""); node.scrollIntoView()
    }

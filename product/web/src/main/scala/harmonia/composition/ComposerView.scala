package harmonia.composition

/** @module.slice
  *   composition
  * @module.role
  *   Follow observed execution
  * @module.summary
  *   Observed completed, enabled, and executable steps determine the diagram and available actions.
  */

import harmonia.ui.Elements.*
import harmonia.workspace.WorkspaceCommand
import org.scalajs.dom
import harmonia.scene.*
import harmonia.scene.support.*

object ComposerView:
  private var diagrams = Vector.empty[WorkflowDiagramView]
  private var carousels = Vector.empty[StepCarousel]
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
          ),
        conversation = nodes.headOption.flatMap(_.conversation)
      )
    )
    val dock = element("nav", "workflow-dock")
    val rail = element("div")
    append(dock, rail); append(root, dock)
    val carousel = new StepCarousel(rail)
    carousels :+= carousel
    def select(id: String): Unit =
      renderer.select(id)
      carousel.render(
        CarouselFrame(
          Vector(
            CarouselPath(
              "execution",
              "Inspect",
              nodes.map(n => CarouselStep(n.id, n.label, n.state))
            )
          ),
          id
        ),
        next =>
          val url = new dom.URL(dom.window.location.href)
          url.searchParams.set("inspect", next)
          dom.window.history.pushState(null, "", url.toString)
          select(next)
      )
    val requested = Option(new dom.URLSearchParams(dom.window.location.search).get("inspect"))
    select(
      nodes
        .find(n => requested.contains(n.id))
        .orElse(nodes.find(_.state == DiagramState.Current))
        .orElse(nodes.reverse.find(_.state == DiagramState.Complete))
        .getOrElse(nodes.head)
        .id
    )
  def render(
      state: CompositionState,
      blocked: Boolean,
      submit: WorkspaceCommand => Unit
  ): dom.HTMLElement =
    diagrams.foreach(_.dispose()); diagrams = Vector.empty
    carousels.foreach(_.dispose()); carousels = Vector.empty
    val root = element("div"); root.id = "composition-observations"
    if !state.available then
      append(root, element("p", text = "Your session has no access to the composition workspace."))
    val query = new dom.URLSearchParams(dom.window.location.search)
    val requested = Option(query.get("workflow"))
    val reference = requested
      .orElse(state.drafts.lastOption.map(_.reference))
      .orElse(state.processes.lastOption.map(_.reference))
    state.drafts.filter(d => reference.contains(d.reference)).zipWithIndex.foreach {
      (draft, index) =>
        val card = element("article", "composition-record")
        append(
          card,
          element("h3", text = draft.name),
          element(
            "p",
            text =
              s"Reference: ${draft.reference} · Awaiting buyer consent · Sources not yet created"
          )
        )
        diagram(
          card,
          "The buyer reviews this exact plan.",
          "Draft proposal · no source contracts have been created.",
          draft.steps.map(step =>
            DiagramNode(
              step.id,
              step.action.label,
              step.role,
              step.actor.wire,
              DiagramState.Pending,
              Some(
                Conversation(
                  Speech(Portrait.Bank, "Bank", s"I’m proposing the “${step.id}” action."),
                  Speech(Portrait.Alice, "Buyer", "My consent is still required before it runs."),
                  Some(StoryIllustration.AgreeThePlan)
                )
              )
            )
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
    state.processes.filter(p => reference.contains(p.reference)).zipWithIndex.foreach {
      (process, index) =>
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
          if process.complete then "This plan is complete."
          else "Follow the next authorized action.",
          "Progress follows the observed ledger result.",
          process.steps.map(step =>
            DiagramNode(
              step.id,
              step.id,
              step.source.getOrElse("Source unavailable"),
              step.actor,
              if step.completed then DiagramState.Complete
              else if step.enabled then DiagramState.Current
              else DiagramState.Pending,
              Some(
                Conversation(
                  Speech(Portrait.Bank, "Bank", s"The ${step.actor} owns “${step.id}”."),
                  Speech(
                    Portrait.Alice,
                    "Buyer",
                    if step.completed then "Its completion is recorded."
                    else if step.enabled then "It is ready for its authorized participant."
                    else "It must wait for the earlier actions."
                  ),
                  Some(
                    if step.completed then
                      if step.actor == "bank" then StoryIllustration.ApprovalSigned
                      else StoryIllustration.ReviewComplete
                    else if step.enabled then StoryIllustration.PlanAccepted
                    else StoryIllustration.JoinWaiting
                  )
                )
              )
            )
          )
        )
        process.steps.zipWithIndex.foreach { (step, position) =>
          if step.canExecute then
            val control =
              button(s"Execute ${step.id}", "primary", s"compose-execute-$index-$position") {
                submit(
                  WorkspaceCommand
                    .Composition(CompositionCommand.Advance(process.reference, step.id))
                )
              }
            control.disabled = blocked; append(card, control)
        }
        append(root, card)
    }
    if state.drafts.isEmpty && state.processes.isEmpty then
      append(root, element("p", text = "No workflow has been proposed yet."))
    if reference.nonEmpty then
      val remaining = (state.drafts.map(_.reference) ++ state.processes.map(_.reference)).distinct
        .dropWhile(r => !reference.contains(r))
        .drop(1)
      val next = remaining.headOption
        .map(r => "?view=composer&workflow=" + scala.scalajs.js.URIUtils.encodeURIComponent(r))
        .getOrElse(
          if state.canPropose && state.drafts.isEmpty && state.processes.forall(_.complete) then
            "?view=composer&new=1"
          else "?view=workspace"
        )
      append(
        root,
        link(
          if remaining.nonEmpty then "Next workflow →"
          else if next.contains("new=1") then "Create another plan →"
          else "Choose the next task →",
          next
        )
      )
    else append(root, link("Choose the next task →", "?view=workspace"))
    root

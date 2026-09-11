package harmonia.composition

import harmonia.ui.Elements.*
import harmonia.composition.model.{Composition, CompositionAction, CompositionActor, PlannedStep}
import org.scalajs.dom
import harmonia.scene.{WorkflowDiagram, WorkflowDiagramView, DiagramNode, DiagramEdge, DiagramState}

/** This editor owns its DOM so participant polling preserves unfinished input. */
final class CompositionEditor(propose: Either[String, Composition] => Unit):
  private var name = "Offer checks"
  private var reference = "home-17"
  private var steps = Vector(
    PlannedStep("approval", "lender", CompositionActor.Bank, CompositionAction.Approve),
    PlannedStep("review", "reviewer", CompositionActor.Buyer, CompositionAction.Review)
  )
  private var blocked = false
  private var remaining = 8
  private val root = element("form", "composition-editor").asInstanceOf[dom.html.Form]
  root.id = "composition-editor"
  root.onsubmit = event =>
    event.preventDefault()
    if !blocked && remaining > 0 then
      propose(Composition.validate(Composition(name, reference, steps)))
  private val preview = element("div", "composition-preview")
  private val diagram = new WorkflowDiagramView(preview)
  preview.addEventListener(
    "harmonia-select",
    (event: dom.Event) =>
      val selected = event.asInstanceOf[dom.CustomEvent].detail.toString
      val index = selected.stripPrefix("draft-").toIntOption.getOrElse(-1)
      if index >= 0 then
        Option(dom.document.getElementById(s"composition-step-$index"))
          .foreach(_.asInstanceOf[dom.HTMLElement].focus())
  )
  rebuild()

  def render(disabled: Boolean, proposalsLeft: Int): dom.HTMLElement =
    blocked = disabled
    remaining = proposalsLeft
    val nodes = root.querySelectorAll("input, select, button")
    (0 until nodes.length).foreach { i =>
      val node = nodes(i).asInstanceOf[dom.html.Element]
      if blocked || remaining == 0 || node.getAttribute("data-unavailable") == "true" then
        node.setAttribute("disabled", "")
      else node.removeAttribute("disabled")
    }
    root

  private def rebuild(): Unit =
    val focus = Option(dom.document.activeElement).map(_.id)
    root.textContent = ""
    append(
      root,
      element("h3", text = "Create a proposal"),
      element(
        "p",
        text =
          "Choose up to four ordered actions. The buyer reviews and accepts this exact plan before any source contracts are created."
      )
    )
    val identity = element("div", "composition-fields")
    append(
      identity,
      textInput("Workflow name", "composition-name", name, 80)(name = _),
      textInput("Unique reference", "composition-reference", reference, 80)(reference = _)
    )
    append(root, preview, identity)
    drawPlan()
    steps.zipWithIndex.foreach { (step, index) =>
      val row = element("fieldset", "composition-step")
      append(row, element("legend", text = s"Action ${index + 1}"))
      val fields = element("div", "composition-fields")
      def update(f: PlannedStep => PlannedStep): Unit =
        steps = steps.updated(index, f(steps(index)))
        drawPlan()
      append(
        fields,
        textInput("Step name", s"composition-step-$index", step.id, 40)(v =>
          update(_.copy(id = v))
        ),
        textInput("Role", s"composition-role-$index", step.role, 40)(v => update(_.copy(role = v))),
        select(
          "Who acts?",
          s"composition-actor-$index",
          step.actor.wire,
          Vector("bank" -> "Bank", "buyer" -> "Buyer")
        )(v =>
          CompositionActor.values.find(_.wire == v).foreach(actor => update(_.copy(actor = actor)))
        ),
        select(
          "Action",
          s"composition-action-$index",
          step.action.wire,
          CompositionAction.values.toVector.map(action => action.wire -> action.label)
        )(v =>
          CompositionAction.values
            .find(_.wire == v)
            .foreach(action => update(_.copy(action = action)))
        )
      )
      append(row, fields)
      def move(offset: Int): Unit =
        val other = index + offset
        steps = steps.updated(index, steps(other)).updated(other, steps(index))
        rebuild()
      val up = control("Move earlier", s"composition-up-$index", index == 0)(move(-1))
      val down = control("Move later", s"composition-down-$index", index == steps.size - 1)(move(1))
      val remove = control("Remove action", s"composition-remove-$index", steps.size == 1) {
        steps = steps.patch(index, Nil, 1); rebuild()
      }
      append(row, up, down, remove); append(root, row)
    }
    val add = control("Add action", "composition-add", steps.size == 4) {
      val suffix =
        Iterator.from(1).map(_.toString).find(n => !steps.exists(_.id == "action-" + n)).get
      steps = steps :+ PlannedStep(
        "action-" + suffix,
        "reviewer",
        CompositionActor.Buyer,
        CompositionAction.Review
      )
      rebuild()
    }
    val submit = element("button", "primary", "Propose workflow").asInstanceOf[dom.html.Button]
    submit.id = "composition-propose"; submit.`type` = "submit"
    append(root, add, submit)
    render(blocked, remaining)
    focus
      .flatMap(id => Option(dom.document.getElementById(id)))
      .foreach(_.asInstanceOf[dom.HTMLElement].focus())

  private def drawPlan(): Unit =
    val nodes = steps.zipWithIndex.map((step, index) =>
      DiagramNode(
        "draft-" + index,
        step.action.label,
        step.id + " · " + step.role,
        step.actor.wire,
        DiagramState.Pending
      )
    )
    diagram.render(
      WorkflowDiagram(
        "Review the proposed handoffs.",
        "Editable plan · buyer consent is required before execution.",
        nodes,
        nodes.zip(nodes.drop(1)).map((a, b) => DiagramEdge(a.id, b.id, DiagramState.Pending))
      )
    )

  private def control(label: String, id: String, unavailable: Boolean)(
      action: => Unit
  ): dom.html.Button =
    val node = button(label, "secondary", id)(action)
    node.setAttribute("data-unavailable", unavailable.toString)
    node

  private def textInput(label: String, id: String, value: String, limit: Int)(
      changed: String => Unit
  ): dom.HTMLElement =
    val group = element("div")
    val caption = element("label", text = label); caption.setAttribute("for", id)
    val input = element("input").asInstanceOf[dom.html.Input]
    input.id = id; input.value = value; input.maxLength = limit; input.required = true
    input.oninput = _ => changed(input.value)
    append(group, caption, input); group

  private def select(label: String, id: String, value: String, options: Vector[(String, String)])(
      changed: String => Unit
  ): dom.HTMLElement =
    val group = element("div")
    val caption = element("label", text = label); caption.setAttribute("for", id)
    val input = element("select").asInstanceOf[dom.html.Select]
    input.id = id
    options.foreach { (key, title) =>
      val option = element("option", text = title).asInstanceOf[dom.html.Option]
      option.value = key; append(input, option)
    }
    input.value = value; input.onchange = _ => changed(input.value)
    append(group, caption, input); group

package harmonia.composition

import harmonia.ui.Elements.*
import harmonia.composition.model.{Composition, CompositionAction, CompositionActor, PlannedStep}
import harmonia.scene.{WorkflowDiagram, WorkflowDiagramView, DiagramNode, DiagramEdge, DiagramState}
import org.scalajs.dom

/** One question at a time. The address preserves the typed draft and current question. */
final class CompositionEditor(propose: Either[String, Composition] => Unit):
  private enum Field:
    case Name, Reference, Count, Review
    case Id(index: Int)
    case Role(index: Int)
    case Actor(index: Int)
    case Action(index: Int)
    case Integration(index: Int)
  import Field.*
  private val initial = Composition(
    "Offer checks",
    "home-17",
    Vector(
      PlannedStep("approval", "lender", CompositionActor.Bank, CompositionAction.Approve),
      PlannedStep("review", "reviewer", CompositionActor.Buyer, CompositionAction.Review)
    )
  )
  private var plan = initial
  private var field: Field = Name
  private var blocked = false
  private var remaining = 8
  private val root = element("form", "composition-editor").asInstanceOf[dom.html.Form]
  root.id = "composition-editor"
  private var diagram: Option[WorkflowDiagramView] = None
  private def fields: Vector[Field] =
    Vector(Name, Reference, Count) ++ plan.steps.indices.toVector.flatMap { i =>
      Vector(Id(i), Role(i), Actor(i), Action(i)) ++
        (if plan.steps(i).action == CompositionAction.Review then Vector.empty
         else Vector(Integration(i)))
    } :+ Review
  private def read(): Unit =
    val query = new dom.URLSearchParams(dom.window.location.search)
    plan = Option(query.get("draft"))
      .filter(_.length < 4000)
      .flatMap(io.circe.parser.decode[Composition](_).toOption)
      .filter(p => p.steps.nonEmpty && p.steps.size <= 4)
      .getOrElse(initial)
    field = Option(query.get("question"))
      .flatMap(value => fields.find(_.toString == value))
      .getOrElse(Name)
    draw()
  private def save(next: Field, replace: Boolean = false): Unit =
    field = next
    val url = new dom.URL(dom.window.location.href)
    url.searchParams.set("draft", plan.json.noSpaces);
    url.searchParams.set("question", field.toString)
    if replace then dom.window.history.replaceState(null, "", url.toString)
    else dom.window.history.pushState(null, "", url.toString)
    draw()
  private def advance(): Unit = save(fields.lift(fields.indexOf(field) + 1).getOrElse(Review))
  private def step(index: Int)(change: PlannedStep => PlannedStep): Unit =
    plan = plan.copy(steps = plan.steps.updated(index, change(plan.steps(index))))
  dom.window.addEventListener("popstate", (_: dom.Event) => read())
  read()

  def render(disabled: Boolean, proposalsLeft: Int): dom.HTMLElement =
    blocked = disabled; remaining = proposalsLeft
    val nodes = root.querySelectorAll("input,button")
    (0 until nodes.length).foreach { i =>
      if blocked || remaining == 0 then
        nodes(i).asInstanceOf[dom.Element].setAttribute("disabled", "")
      else nodes(i).asInstanceOf[dom.Element].removeAttribute("disabled")
    }
    root

  private def draw(): Unit =
    diagram.foreach(_.dispose()); diagram = None; root.textContent = ""
    append(
      root,
      element(
        "p",
        "eyebrow",
        s"Propose a workflow · question ${fields.indexOf(field) + 1} of ${fields.size}"
      )
    )
    root.onsubmit = event => { event.preventDefault(); if !blocked then advance() }
    def text(title: String, id: String, value: String, limit: Int)(update: String => Unit): Unit =
      val label = element("h1", text = title); label.id = id + "-label"
      val input = element("input").asInstanceOf[dom.html.Input]
      input.id = id; input.value = value; input.required = true; input.maxLength = limit
      input.setAttribute("aria-labelledby", label.id)
      input.oninput = _ =>
        update(input.value)
        val url = new dom.URL(dom.window.location.href);
        url.searchParams.set("draft", plan.json.noSpaces)
        dom.window.history.replaceState(null, "", url.toString)
      val next = element("button", "primary", "Continue →").asInstanceOf[dom.html.Button]
      next.id = "composition-next"; next.`type` = "submit"
      append(root, label, input, next)
    def choose(title: String, first: String, second: String)(answer: Boolean => Unit): Unit =
      append(
        root,
        element("h1", text = title),
        button(first, "primary", "composition-first") { answer(true); advance() },
        button(second, "secondary", "composition-second") { answer(false); advance() }
      )
    field match
      case Name =>
        text("What is this workflow called?", "composition-name", plan.name, 80)(v =>
          plan = plan.copy(name = v)
        )
      case Reference =>
        text("Give this plan a unique reference.", "composition-reference", plan.reference, 80)(v =>
          plan = plan.copy(reference = v)
        )
      case Count =>
        text(
          "How many actions, from one to four?",
          "composition-count",
          plan.steps.size.toString,
          1
        ) { value =>
          value.toIntOption.filter(n => n >= 1 && n <= 4).foreach { n =>
            plan = plan.copy(steps =
              Vector.tabulate(n)(i =>
                plan.steps
                  .lift(i)
                  .getOrElse(
                    PlannedStep(
                      "action-" + (i + 1),
                      "reviewer",
                      CompositionActor.Buyer,
                      CompositionAction.Review
                    )
                  )
              )
            )
          }
        }
        val input = root.querySelector("input").asInstanceOf[dom.html.Input]
        input.pattern = "[1-4]"; input.setAttribute("inputmode", "numeric")
      case Id(i) =>
        text(s"Name action ${i + 1}.", s"composition-step-$i", plan.steps(i).id, 40)(v =>
          step(i)(_.copy(id = v))
        )
      case Role(i) =>
        text(
          s"Which role owns ${plan.steps(i).id}?",
          s"composition-role-$i",
          plan.steps(i).role,
          40
        )(v => step(i)(_.copy(role = v)))
      case Actor(i) =>
        choose(s"Who acts as ${plan.steps(i).role}?", "Bank", "Buyer")(bank =>
          step(i)(_.copy(actor = if bank then CompositionActor.Bank else CompositionActor.Buyer))
        )
      case Action(i) =>
        choose(s"What does ${plan.steps(i).id} do?", "Approve financing", "Confirm a review")(
          approval =>
            step(i)(
              _.copy(action =
                if approval then CompositionAction.Approve else CompositionAction.Review
              )
            )
        )
      case Integration(i) =>
        choose(
          "How does the financing application participate?",
          "Direct interface",
          "Generated adapter"
        )(direct =>
          step(i)(
            _.copy(action =
              if direct then CompositionAction.Approve else CompositionAction.GeneratedApproval
            )
          )
        )
      case Review =>
        append(
          root,
          element("h1", text = plan.name),
          element(
            "p",
            text = "Review the exact handoffs. The buyer must consent before these actions can run."
          )
        )
        val canvas = element("div"); append(root, canvas)
        val renderer = new WorkflowDiagramView(canvas); diagram = Some(renderer)
        val nodes = plan.steps.map(s =>
          DiagramNode(s.id, s.action.label, s.role, s.actor.wire, DiagramState.Pending)
        )
        renderer.render(
          WorkflowDiagram(
            plan.name,
            "Proposed plan · no source contracts created",
            nodes,
            nodes.zip(nodes.drop(1)).map((a, b) => DiagramEdge(a.id, b.id, DiagramState.Pending))
          )
        )
        append(
          root,
          element("p", text = "Reference: " + plan.reference),
          button("Review my answers", "secondary", "composition-edit")(save(Name)),
          button("Propose this workflow →", "primary", "composition-propose") {
            if !blocked && remaining > 0 then propose(Composition.validate(plan))
          }
        )
    render(blocked, remaining)

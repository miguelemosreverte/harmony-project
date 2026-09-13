package harmonia.financing

import harmonia.ui.Elements.*
import harmonia.scene.*
import org.scalajs.dom

/** The scene stays mounted while polling updates its observed state. */
final class FinancingPanel:
  val root = element("section", "financing-scene")
  private val scene = new SceneView(root)
  private val dock = element("nav", "workflow-dock")
  private val rail = element("div")
  private val carousel = new StepCarousel(rail)
  private var observed = Option.empty[FinancingState]
  append(dock, rail)
  append(root, dock)
  dom.window.addEventListener("popstate", (_: dom.Event) => drawScene())
  private val actionArea = element("div", "financing-action")
  private var previousAction = Vector.empty[FinancingAction]
  private var control = Vector.empty[dom.html.Button]
  private val details = element("section", "private-case")
  private val detailBody = element("div")
  private val status = element("p", "visually-hidden"); status.id = "live-workflow"
  append(details, element("strong", text = "Your private application"), detailBody)
  append(root, actionArea, details, status)

  def render(state: FinancingState, blocked: Boolean, submit: FinancingAction => Unit): Unit =
    observed = Some(state)
    drawScene()
    status.textContent = s"Workflow: ${state.workflow.wire}"
    if state.eligible != previousAction then
      previousAction = state.eligible
      actionArea.textContent = ""
      control = state.eligible.map { action =>
        val label = action match
          case FinancingAction.Approve  => "Approve financing →"
          case FinancingAction.Continue => "Continue with this approval →"
        val next = button(label, "primary", "live-" + action.wire)(submit(action))
        append(actionArea, next)
        next
      }
    control.foreach { button =>
      button.disabled = blocked
      button.setAttribute("aria-busy", blocked.toString)
    }
    hide(details, state.application.isEmpty)
    detailBody.textContent = ""
    state.application.foreach(value =>
      append(detailBody, element("p", text = "Status: " + value.wire))
    )
    state.privateDetails.foreach(value => append(detailBody, element("p", text = value)))

  private def drawScene(): Unit = observed.foreach { state =>
    val frame = FinancingScene(state)
    val steps = Vector(
      (
        "application",
        "Bank decision",
        "bank",
        if frame.phase > 0 then DiagramState.Complete else DiagramState.Current
      ),
      (
        "approval",
        "Buyer continuation",
        "buyer",
        if frame.phase == 4 then DiagramState.Complete
        else if frame.phase > 0 then DiagramState.Current
        else DiagramState.Pending
      ),
      (
        "workflow",
        "Shared result",
        "reviewer",
        if frame.phase == 4 then DiagramState.Complete else DiagramState.Pending
      )
    )
    val requested = Option(new dom.URLSearchParams(dom.window.location.search).get("inspect"))
    val selected = steps
      .find(s => requested.contains(s._1))
      .getOrElse(steps.find(_._4 == DiagramState.Current).getOrElse(steps.last))
    scene.render(
      frame.copy(
        focus = selected._3,
        conversation = Some(FinancingConversation(state, selected._1)),
        observation = Some(SceneObservation("Inspecting", selected._2, selected._4.toString))
      )
    )
    carousel.render(
      CarouselFrame(
        Vector(CarouselPath("live", "Live state", steps.map(s => CarouselStep(s._1, s._2, s._4)))),
        selected._1
      ),
      id =>
        val url = new dom.URL(dom.window.location.href)
        url.searchParams.set("inspect", id)
        dom.window.history.pushState(null, "", url.toString)
        drawScene()
    )
  }

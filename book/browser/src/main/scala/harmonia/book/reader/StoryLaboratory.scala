package harmonia.book.reader

import harmonia.book.*
import harmonia.book.ui.Elements.*
import harmonia.book.diagram.{StoryDiagram, RecordedScene}
import harmonia.examples.ExampleKind
import harmonia.scene.*
import org.scalajs.dom
import scala.scalajs.js

/** The standalone export retains the same stage while selecting recorded observations. */
final class StoryLaboratory(
    stories: Vector[RecordedStory],
    chapters: Vector[BookChapter],
    navigate: ViewState => Unit
):
  private val position = element("p", "eyebrow")
  private val title = element("h1"); title.id = "laboratory-title"
  private val graph = element("section"); graph.id = "laboratory-stage"
  private val detail = element("section")
  private val paging = element("nav", "quiet-paging has-carousel")
  private val rail = element("div")
  private val carousel = new StepCarousel(rail)
  private var scene = Option.empty[SceneView]
  private var diagram = Option.empty[WorkflowDiagramView]
  private var current = ViewState(0, 0)
  private def finish(): Unit = dom.window.location.href = "source/design/0.2/book-overview.html"
  private val previous = button("‹", "", "previous") {
    if current.step > 0 then navigate(current.copy(step = current.step - 1))
    else if current.story > 0 then
      navigate(
        current.copy(story = current.story - 1, step = stories(current.story - 1).units.size - 1)
      )
    else finish()
  }
  private val next = button("›", "", "next") {
    if current.step + 1 < stories(current.story).units.size then
      navigate(current.copy(step = current.step + 1))
    else if current.story + 1 < stories.size then
      navigate(current.copy(story = current.story + 1, step = 0))
    else finish()
  }
  append(paging, previous, rail, next)

  def dispose(): Unit =
    scene.foreach(_.dispose()); diagram.foreach(_.dispose()); carousel.dispose()

  def render(main: dom.HTMLElement, state: ViewState): Unit =
    current = state
    if state.evidence then
      graph.setAttribute("hidden", ""); detail.removeAttribute("hidden")
    else
      graph.removeAttribute("hidden"); detail.setAttribute("hidden", "")
    val story = stories(state.story)
    if graph.parentNode != main then append(main, position, title, graph, paging, detail)
    position.textContent =
      s"Recording ${state.story + 1} of ${stories.size} · observation ${state.step + 1} of ${story.units.size}"
    title.textContent = story.title
    if Set(ExampleKind.Purchase, ExampleKind.Transfer)(story.kind) then
      if scene.isEmpty then
        diagram.foreach(_.dispose()); diagram = None; graph.textContent = ""
        scene = Some(new SceneView(graph))
      scene.foreach(_.render(RecordedScene(story, state.step + 1)))
    else
      if diagram.isEmpty then
        scene.foreach(_.dispose()); scene = None; graph.textContent = ""
        diagram = Some(new WorkflowDiagramView(graph))
      diagram.foreach(_.render(StoryDiagram(story, state.step)))
    previous.setAttribute(
      "aria-label",
      if state.step > 0 then "Previous observation" else "Previous story"
    )
    next.setAttribute(
      "aria-label",
      if state.step + 1 < story.units.size then "Next observation" else "Next story"
    )
    carousel.render(
      CarouselFrame(
        Vector(
          CarouselPath(
            story.id,
            "Observations",
            story.units.zipWithIndex.map { (unit, i) =>
              CarouselStep(
                i.toString,
                unit.id.replace('-', ' '),
                if unit.outcomeLabel == "rejected" then DiagramState.Refused
                else if i < state.step then DiagramState.Complete
                else if i == state.step then DiagramState.Current
                else DiagramState.Pending
              )
            }
          )
        ),
        state.step.toString
      ),
      id => navigate(state.copy(step = id.toInt))
    )
    detail.textContent = ""
    val unit = story.units(state.step)
    append(
      detail,
      element(
        "p",
        "citation",
        if story.differences.isEmpty then "Recorded result matches the expectation."
        else "The recording differs from the expectation."
      )
    )
    val columns = element("div", "observed-comparison")
    Vector("Committed expectation" -> unit.expected, "Recorded observation" -> unit.actual)
      .foreach { (label, value) =>
        val column = element("section")
        append(
          column,
          element("h3", text = label),
          element("pre", text = io.circe.Printer.spaces2.copy(sortKeys = true).print(value))
        )
        append(columns, column)
      }
    append(
      detail,
      columns,
      element("p", "citation", "Historical recording; playback submits no ledger commands.")
    )
    state.artifact.foreach { artifact =>
      val bundled = js.Dynamic.global.selectDynamic("HarmoniaEvidenceFiles")
      if !js.isUndefined(bundled) then
        val text = bundled.selectDynamic(s"evidence/${story.id}/${artifact.filename}")
        if !js.isUndefined(text) then
          append(detail, element("h2", text = artifact.label), element("pre", text = text.toString))
    }

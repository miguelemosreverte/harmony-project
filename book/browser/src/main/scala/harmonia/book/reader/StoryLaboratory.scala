package harmonia.book.reader

import harmonia.book.*
import harmonia.book.ui.Elements.*
import harmonia.book.diagram.{StoryDiagram, RecordedScene}
import harmonia.examples.ExampleKind
import harmonia.scene.{WorkflowDiagramView, SceneView}
import org.scalajs.dom
import scala.scalajs.js

/** A finite recording route: previous observation, next observation, then the next story. */
final class StoryLaboratory(
    stories: Vector[RecordedStory],
    chapters: Vector[BookChapter],
    navigate: ViewState => Unit
):
  private var cleanup: () => Unit = () => ()
  def dispose(): Unit = cleanup()
  def render(main: dom.HTMLElement, state: ViewState): Unit =
    val story = stories(state.story)
    append(
      main,
      element(
        "p",
        "eyebrow",
        s"Recording ${state.story + 1} of ${stories.size} · observation ${state.step + 1} of ${story.units.size}"
      ),
      element("h1", text = story.title),
      element("p", text = story.description)
    )
    val graph = element("section"); graph.id = "laboratory-stage"; append(main, graph)
    if Set(ExampleKind.Purchase, ExampleKind.Transfer)(story.kind) then
      val view = new SceneView(graph); view.render(RecordedScene(story, state.step + 1));
      cleanup = () => view.dispose()
    else
      val view = new WorkflowDiagramView(graph); view.render(StoryDiagram(story, state.step));
      cleanup = () => view.dispose()
    val paging = element("nav", "quiet-paging")
    def finish(): Unit = dom.window.location.href = "source/design/0.2/book-overview.html"
    append(
      paging,
      button(
        if state.step > 0 then "← Previous observation" else "← Previous story",
        "",
        "previous"
      ) {
        if state.step > 0 then navigate(state.copy(step = state.step - 1))
        else if state.story > 0 then
          navigate(
            state.copy(story = state.story - 1, step = stories(state.story - 1).units.size - 1)
          )
        else finish()
      },
      button(
        if state.step + 1 < story.units.size then "Next observation →" else "Next story →",
        "",
        "next"
      ) {
        if state.step + 1 < story.units.size then navigate(state.copy(step = state.step + 1))
        else if state.story + 1 < stories.size then
          navigate(state.copy(story = state.story + 1, step = 0))
        else finish()
      }
    )
    append(
      main,
      paging,
      element(
        "h2",
        text =
          if story.differences.isEmpty then "Recorded result matches the expectation."
          else "The recording differs from the expectation."
      )
    )
    main.insertBefore(paging, graph)
    val unit = story.units(state.step)
    append(
      main,
      element("p", text = unit.actor + ": " + unit.action),
      element("h3", text = "Committed expectation"),
      element("pre", text = unit.expected.spaces2),
      element("h3", text = "Recorded observation"),
      element("pre", text = unit.actual.spaces2),
      element("p", "citation", "Historical recording; playback submits no ledger commands.")
    )
    state.artifact.foreach { artifact =>
      val bundled = js.Dynamic.global.selectDynamic("HarmoniaEvidenceFiles")
      if !js.isUndefined(bundled) then
        val text = bundled.selectDynamic(s"evidence/${story.id}/${artifact.filename}")
        if !js.isUndefined(text) then
          append(main, element("h2", text = artifact.label), element("pre", text = text.toString))
    }

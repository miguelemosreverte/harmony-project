package harmonia.book.reader

import harmonia.book.*
import harmonia.book.ui.Elements.*
import io.circe.Json
import org.scalajs.dom
import harmonia.examples.ExampleKind
import harmonia.scene.{WorkflowDiagramView, SceneView}
import harmonia.book.diagram.{StoryDiagram, RecordedScene}
import harmonia.book.evidence.*
import harmonia.book.evidence.EvidenceValues.*

final class StoryLaboratory(
    stories: Vector[RecordedStory],
    chapters: Vector[BookChapter],
    navigate: ViewState => Unit
):
  private var cleanup: () => Unit = () => ()
  def dispose(): Unit = cleanup()
  def render(main: dom.HTMLElement, state: ViewState): Unit =
    dispose()
    val story = stories(state.story)
    state.originChapter.foreach { index =>
      append(
        main,
        button(s"← Return to ${chapters(index).title}", "button", "return-to-chapter")(
          navigate(state.copy(chapter = Some(index)))
        )
      )
    }
    val composition = story.kind == ExampleKind.Composition
    val builder = story.kind == ExampleKind.Packages
    val transfer = story.kind == ExampleKind.Transfer
    val hero = element("header", "hero")
    append(
      hero,
      element("div", "eyebrow", "The story laboratory"),
      element("h1", text = story.title),
      element(
        "p",
        text = story.description
      )
    )
    val toolbar = element("div", "toolbar")
    val label = element("label", "select-label", "Story")
    label.setAttribute("for", "story-select")
    val select = element("select").asInstanceOf[dom.html.Select]
    select.id = "story-select"
    stories.zipWithIndex.foreach { (item, index) =>
      val option = element(
        "option",
        text = item.title
      ).asInstanceOf[dom.html.Option]
      option.value = index.toString
      option.selected = state.story == index
      append(select, option)
    }
    select.onchange = _ =>
      val index = select.value.toInt
      val firstDifference = stories(index).differences.headOption
        .flatMap(_.path.stripPrefix("$.actions[").takeWhile(_ != ']').toIntOption)
        .getOrElse(0)
      navigate(
        state.copy(
          story = index,
          step = math.max(0, math.min(firstDifference, stories(index).units.size - 1)),
          node = None,
          evidence = false,
          artifact = None,
          perspective = None,
          originChapter = state.originChapter
        )
      )
    append(label, select)
    val differences = story.differences.size
    val badge = element(
      "span",
      "badge" + (if differences > 0 then " fail" else ""),
      if differences == 0 then "✓ All observations match"
      else s"≠ $differences ${if differences == 1 then "difference" else "differences"} found"
    )
    badge.id = "comparison-status"
    badge.setAttribute("role", "status")
    append(toolbar, label, badge)
    val panel = element("section", "panel")
    val graph = element("div", "laboratory-stage")
    graph.id = "laboratory-stage"; graph.tabIndex = 0
    if story.kind == ExampleKind.Purchase || story.kind == ExampleKind.Transfer then
      val view = new SceneView(graph)
      view.render(RecordedScene(story, state.step + 1, state.perspective.getOrElse("all")))
      cleanup = () => view.dispose()
    else
      val view = new WorkflowDiagramView(graph)
      view.render(StoryDiagram(story, state.step))
      state.node.foreach(view.select)
      graph.addEventListener(
        "harmonia-select",
        (event: dom.Event) =>
          navigate(state.copy(node = Some(event.asInstanceOf[dom.CustomEvent].detail.toString)))
      )
      cleanup = () => view.dispose()
    def move(delta: Int): Unit = navigate(
      state.copy(step = (state.step + delta + story.units.size) % story.units.size, node = None)
    )
    graph.onkeydown = event =>
      if event.key == "ArrowLeft" || event.key == "ArrowRight" then
        event.preventDefault(); move(if event.key == "ArrowLeft" then -1 else 1)
    var touch = Option.empty[(Double, Double)]
    graph.addEventListener(
      "touchstart",
      (event: dom.Event) =>
        val e = event.asInstanceOf[dom.TouchEvent]
        if e.touches.length == 1 then touch = Some(e.touches(0).clientX -> e.touches(0).clientY)
    )
    graph.addEventListener(
      "touchend",
      (event: dom.Event) =>
        val e = event.asInstanceOf[dom.TouchEvent]
        if e.changedTouches.length > 0 then
          touch.foreach { (x, y) =>
            val dx = e.changedTouches(0).clientX - x
            if math.abs(dx) > 55 && math.abs(e.changedTouches(0).clientY - y) < 70 then
              move(if dx < 0 then 1 else -1)
          }
        touch = None
    )
    val controls = element("div", "controls")
    val buttons = element("div", "buttons")
    val previous = button("← Previous", "button", "previous") {
      move(-1)
    }
    val next = button(
      if story.isBoundaryReport then "Next phase →" else "Next attempt →",
      "button primary",
      "next"
    ) {
      move(1)
    }
    val attempts = element("select").asInstanceOf[dom.html.Select]
    attempts.id = "attempt-select"; attempts.setAttribute("aria-label", "Recorded attempt")
    story.units.zipWithIndex.foreach { (unit, i) =>
      val option =
        element("option", text = s"${i + 1}. ${unit.actor}: ${unit.action} · ${unit.outcomeLabel}")
          .asInstanceOf[dom.html.Option]
      option.value = i.toString; option.selected = i == state.step; append(attempts, option)
    }
    attempts.onchange = _ => navigate(state.copy(step = attempts.value.toInt, node = None))
    append(buttons, previous, attempts, next)
    val progress = element(
      "span",
      "small",
      s"${if story.isBoundaryReport then "Phase" else "Attempt"} ${state.step + 1} of ${story.units.size}"
    )
    progress.setAttribute("aria-live", "polite")
    append(controls, progress, buttons)
    append(panel, graph, controls)
    val details = element("div", "details-grid")
    append(details, ComparisonView.render(story, state.step), EvidencePanel.render(story))
    val evidence = element("details", "laboratory-evidence")
    evidence.id = "laboratory-evidence"
    if state.evidence then evidence.setAttribute("open", "")
    append(evidence, element("summary", text = "Evidence and other observations"))
    evidence.addEventListener(
      "toggle",
      (_: dom.Event) =>
        if evidence.hasAttribute("open") != state.evidence then
          navigate(state.copy(evidence = evidence.hasAttribute("open")))
    )
    append(main, hero, toolbar, panel, evidence)
    if composition then
      append(evidence, harmonia.book.composer.CompositionEvidence.render(story, state.step))
    if builder then
      append(evidence, harmonia.book.builder.BuilderEvidence.render(story, state.step))
    if story.isBoundaryReport then
      append(evidence, harmonia.book.chapters.BoundaryEvidence.render(story, state.step))
    if transfer then append(evidence, harmonia.book.transfer.TransferView.render(story, state.step))
    append(evidence, details)
    if story.kind == ExampleKind.Generated then
      append(evidence, harmonia.book.bindings.BindingView.render(story))
    ParticipantEvidence.render(story, state, navigate).foreach(view => append(evidence, view))
    if story.differences.nonEmpty then
      val differences = element("section", "panel all-differences")
      val heading = element("div", "panel-head")
      append(heading, element("h2", text = "Every difference in this story"))
      val table = element("table")
      val header = element("tr")
      Vector("Field", "Expected", "Observed").foreach(value =>
        append(header, element("th", text = value))
      )
      append(table, header)
      story.differences.foreach { difference =>
        val row = element("tr", "different")
        append(
          row,
          element("td", text = difference.path),
          element("td", text = display(difference.expected)),
          element("td", text = display(difference.actual))
        )
        append(table, row)
      }
      append(differences, heading, table)
      append(evidence, differences)

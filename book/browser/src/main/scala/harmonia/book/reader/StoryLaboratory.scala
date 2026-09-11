package harmonia.book.reader

import harmonia.book.*
import harmonia.book.ui.Elements.*
import io.circe.Json
import org.scalajs.dom
import harmonia.examples.ExampleKind
import harmonia.book.evidence.*
import harmonia.book.evidence.EvidenceValues.*

final class StoryLaboratory(
    stories: Vector[RecordedStory],
    chapters: Vector[BookChapter],
    navigate: ViewState => Unit
):
  def render(main: dom.HTMLElement, state: ViewState): Unit =
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
      element("h1", text = "Follow the action.\nSee the proof."),
      element(
        "p",
        text =
          "Walk through a real execution, one attempt at a time. Compare what we committed to expect with the observed result."
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
        ViewState(
          index,
          math.max(0, math.min(firstDifference, stories(index).units.size - 1)),
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
    val head = element("div", "panel-head")
    append(
      head,
      element("h2", text = story.title),
      element(
        "span",
        "small",
        story.presentation.subtitle
      )
    )
    val graph = element("div", "graph")
    graph.setAttribute("aria-label", "Observed progression; select an attempt to inspect it")
    val setup = story.input.hcursor.downField("setup").downField("application")
    val start = element("div", "node")
    append(
      start,
      element("span", "eyebrow", "Starting point"),
      element(
        "strong",
        text = story.presentation.start
      ),
      element("span", text = story.presentation.startDetail)
    )
    append(graph, start)
    story.units.zipWithIndex.foreach { (action, index) =>
      val actual = story.actualActions.lift(index).getOrElse(Json.Null)
      val node =
        button("", "node" + (if state.step == index then " selected" else ""), s"step-$index") {
          navigate(state.copy(step = index))
        }
      node.setAttribute("aria-pressed", (state.step == index).toString)
      append(
        node,
        element("span", text = s"${index + 1}. ${action.actor}"),
        element("span", "small", action.action.replace('-', ' ')),
        element("strong", text = action.observedState),
        element(
          "span",
          if text(actual, "outcome") == "rejected" then "rejected" else "",
          action.outcomeLabel
        )
      )
      append(graph, node)
    }
    val controls = element("div", "controls")
    val buttons = element("div", "buttons")
    val previous = button("← Previous", "button", "previous") {
      navigate(state.copy(step = state.step - 1))
    }
    previous.disabled = state.step <= 0
    val next = button(
      if story.isBoundaryReport then "Next phase →" else "Next attempt →",
      "button primary",
      "next"
    ) {
      navigate(state.copy(step = state.step + 1))
    }
    next.disabled = state.step >= story.units.size - 1
    append(buttons, previous, next)
    val progress = element(
      "span",
      "small",
      s"${if story.isBoundaryReport then "Phase" else "Attempt"} ${state.step + 1} of ${story.units.size}"
    )
    progress.setAttribute("aria-live", "polite")
    append(controls, progress, buttons)
    append(panel, head, graph, controls)
    val details = element("div", "details-grid")
    append(details, ComparisonView.render(story, state.step), EvidencePanel.render(story))
    append(main, hero, toolbar, panel)
    if composition then
      append(main, harmonia.book.composer.CompositionEvidence.render(story, state.step))
    if builder then append(main, harmonia.book.builder.BuilderEvidence.render(story, state.step))
    if story.isBoundaryReport then
      append(main, harmonia.book.chapters.BoundaryEvidence.render(story, state.step))
    if transfer then append(main, harmonia.book.transfer.TransferView.render(story, state.step))
    append(main, details)
    if story.kind == ExampleKind.Generated then
      append(main, harmonia.book.bindings.BindingView.render(story))
    ParticipantEvidence.render(story, state, navigate).foreach(view => append(main, view))
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
      append(main, differences)

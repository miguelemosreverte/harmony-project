package harmonia.book

import cats.effect.IO
import io.circe.Json
import org.scalajs.dom
import Elements.*

final class BookView(
    stories: Vector[RecordedStory],
    chapters: Vector[Json],
    navigate: ViewState => Unit
):
  def render(state: ViewState): IO[Unit] = IO {
    val focused = Option(dom.document.activeElement).map(_.id).filter(_.nonEmpty)
    val root = dom.document.getElementById("app")
    root.textContent = ""
    val layout = element("div", "layout")
    val sidebar = element("aside", "sidebar")
    val brand = element("p", "brand")
    append(brand, element("span", "brand-mark"), dom.document.createTextNode("Harmonia"))
    val nav = element("nav")
    nav.setAttribute("aria-label", "Book chapters")
    append(
      nav,
      button(
        "The story laboratory",
        "nav-button" + (if state.chapter.isEmpty then " active" else ""),
        "nav-lab"
      ) { navigate(state.copy(chapter = None)) }
    )
    chapters.zipWithIndex.foreach { (chapter, index) =>
      append(
        nav,
        button(
          s"0${index + 1}  ${text(chapter, "title")}",
          "nav-button" + (if state.chapter.contains(index) then " active" else ""),
          s"nav-$index"
        ) { navigate(state.copy(chapter = Some(index))) }
      )
    }
    append(
      sidebar,
      brand,
      element("p", "strap", "An executable book"),
      element("p", "nav-label", "Read · run · understand"),
      nav,
      element(
        "p",
        "sidebar-note",
        "Every story has an input, a committed expectation, and an observation from the ledger. Explore the evidence behind each result."
      )
    )
    val main = element("main", "main")
    main.id = "main"
    append(main, topLine())
    state.chapter match
      case Some(index) =>
        val chapter = element("article", "chapter")
        // The exporter escapes raw HTML and sanitizes URLs before this content reaches the browser.
        chapter.innerHTML = text(chapters(index), "html")
        harmonia.book.diagram.ChapterDiagram.render(chapter)
        append(main, chapter)
      case None => laboratory(main, state)
    append(
      main,
      element(
        "p",
        "footer",
        "Harmonia is an evolving reference implementation. These demonstrations use synthetic applications and recorded local Canton executions."
      )
    )
    append(layout, sidebar, main)
    append(root, layout)
    Option(root.querySelector(".graph")).foreach { graphNode =>
      val graph = graphNode.asInstanceOf[dom.HTMLElement]
      Option(graph.querySelector(".selected")).foreach { selected =>
        graph.scrollLeft += selected.getBoundingClientRect().left - graph
          .getBoundingClientRect()
          .left -
          (graph.clientWidth - selected.getBoundingClientRect().width) / 2
      }
    }
    focused
      .flatMap(id => Option(dom.document.getElementById(id)))
      .foreach(_.asInstanceOf[dom.HTMLElement].focus())
  }

  private def topLine(): dom.HTMLElement =
    val row = element("div", "topline")
    append(
      row,
      element("span", text = "APPLICATIONS, WORKING TOGETHER"),
      element("span", "badge", "●  Recorded ledger evidence")
    )
    row

  private def laboratory(main: dom.HTMLElement, state: ViewState): Unit =
    val story = stories(state.story)
    val hero = element("header", "hero")
    append(
      hero,
      element("div", "eyebrow", "The story laboratory"),
      element("h1", text = "Follow the action.\nSee the proof."),
      element(
        "p",
        text =
          "Walk through a real execution, one attempt at a time. Compare what we committed to expect with what the ledger actually observed."
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
        text =
          if item.id.startsWith("wrong-") then "Regression experiment · wrong expectation"
          else item.title
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
      navigate(ViewState(index, math.min(firstDifference, stories(index).actions.size - 1)))
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
        if story.input.hcursor.get[String]("integration").toOption.contains("adapter") then
          "Typed adapter"
        else if story.input.hcursor.get[String]("workflow").toOption.contains("private-approval")
        then "Signed result handoff"
        else if story.input.hcursor.get[String]("workflow").toOption.contains("sequential-approval")
        then
          s"${story.actual.hcursor.downField("definition").get[String]("name").getOrElse("Workflow")} · version ${story.actual.hcursor.downField("definition").get[Int]("version").getOrElse(0)}"
        else if story.input.hcursor.get[String]("workflow").isRight then "Direct interface"
        else "Application action"
      )
    )
    val graph = element("div", "graph")
    graph.setAttribute("aria-label", "Observed progression; select an attempt to inspect it")
    val setup = story.input.hcursor.downField("setup").downField("application")
    val start = element("div", "node")
    append(
      start,
      element("span", "eyebrow", "Starting point"),
      element("strong", text = setup.get[String]("status").getOrElse("Unknown")),
      element("span", text = "Application created")
    )
    append(graph, start)
    story.actions.zipWithIndex.foreach { (action, index) =>
      val actual = story.actualActions.lift(index).getOrElse(Json.Null)
      val node =
        button("", "node" + (if state.step == index then " selected" else ""), s"step-$index") {
          navigate(state.copy(step = index))
        }
      node.setAttribute("aria-pressed", (state.step == index).toString)
      append(
        node,
        element("span", text = s"${index + 1}. ${text(action, "actor")}"),
        element("strong", text = text(actual, "application")),
        element(
          "span",
          if text(actual, "outcome") == "rejected" then "rejected" else "",
          text(actual, "outcome") + actual.hcursor
            .get[String]("workflow")
            .toOption
            .fold("")(value => s" · workflow $value")
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
    val next = button("Next attempt →", "button primary", "next") {
      navigate(state.copy(step = state.step + 1))
    }
    next.disabled = state.step >= story.actions.size - 1
    append(buttons, previous, next)
    val progress = element("span", "small", s"Attempt ${state.step + 1} of ${story.actions.size}")
    progress.setAttribute("aria-live", "polite")
    append(controls, progress, buttons)
    append(panel, head, graph, controls)
    val details = element("div", "details-grid")
    append(details, comparison(story, state.step), evidence(story))
    append(main, hero, toolbar, panel, details)
    participantEvidence(story, state).foreach(view => append(main, view))
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

  private def participantEvidence(story: RecordedStory, state: ViewState): Option[dom.HTMLElement] =
    story.actual.hcursor.downField("visibility").focus.flatMap(_.asObject).map { visibility =>
      val names = visibility.keys.toVector.sorted
      val selected = state.perspective.filter(names.contains).getOrElse(names.head)
      val panel = element("section", "panel participant-evidence")
      val heading = element("div", "panel-head")
      val select = element("select").asInstanceOf[dom.html.Select]
      select.id = "participant-select"
      select.setAttribute("aria-label", "Recorded participant evidence")
      names.foreach { name =>
        val option = element("option", text = name).asInstanceOf[dom.html.Option]
        option.value = name
        option.selected = name == selected
        append(select, option)
      }
      select.onchange = _ => navigate(state.copy(perspective = Some(select.value)))
      append(heading, element("h2", text = "Participant evidence"), select)
      val description = element(
        "p",
        "evidence",
        s"Recorded queries and event history from $selected's participant. Shared progress is the positive control for this observation channel."
      )
      val table = element("table")
      table.id = "participant-table"
      val header = element("tr")
      Vector("Observation", "Expected", "Observed")
        .foreach(value => append(header, element("th", text = value)))
      append(table, header)
      val expected = story.expected.hcursor.downField("visibility").downField(selected)
      val actual = visibility(selected).get.hcursor
      Vector(
        "application" -> "Private application visible",
        "progress" -> "Shared progress visible",
        "private_events" -> "Private application create events",
        "progress_events" -> "Shared progress create events",
        "private_payload_observed" -> "Private payload in event history"
      ).foreach { (field, label) =>
        val left = expected.downField(field).focus
        val right = actual.downField(field).focus
        val row = element("tr", if left != right then "different" else "")
        row.setAttribute("data-visibility", field)
        append(
          row,
          element("td", text = label),
          element("td", text = display(left)),
          element("td", text = display(right))
        )
        append(table, row)
      }
      append(panel, heading, description, table)
      panel
    }

  private def comparison(story: RecordedStory, step: Int): dom.HTMLElement =
    val panel = element("section", "panel")
    val head = element("div", "panel-head")
    append(
      head,
      element("h2", text = "Expected & observed"),
      element("span", "small", text(story.actions(step), "id"))
    )
    val table = element("table")
    table.id = "comparison-table"
    val header = element("thead")
    val row = element("tr")
    Vector("Contract detail", "Expected", "Observed").foreach(value =>
      append(row, element("th", text = value))
    )
    append(header, row)
    val body = element("tbody")
    val expected = story.expectedActions.lift(step).getOrElse(Json.Null)
    val actual = story.actualActions.lift(step).getOrElse(Json.Null)
    val fields = (expected.asObject.toVector.flatMap(_.keys) ++ actual.asObject.toVector.flatMap(
      _.keys
    )).distinct.filter(_ != "id")
    fields.foreach { field =>
      val left = expected.hcursor.downField(field).focus
      val right = actual.hcursor.downField(field).focus
      val tr = element("tr", if left != right then "different" else "")
      tr.setAttribute("data-field", field)
      append(
        tr,
        element("td", text = label(field)),
        element("td", text = display(left)),
        element("td", text = display(right))
      )
      append(body, tr)
    }
    append(table, header, body)
    append(panel, head, table)
    panel

  private def evidence(story: RecordedStory): dom.HTMLElement =
    val panel = element("section", "panel")
    val head = element("div", "panel-head")
    append(head, element("h2", text = "The evidence behind it"))
    val content = element("div", "evidence")
    append(content, element("p", text = story.description))
    val links = element("div", "link-list")
    Vector(
      "Input" -> "input.md",
      "Expected" -> "expected.md",
      "Actual" -> "actual.md",
      "Diff" -> "diff.md",
      "Raw observations" -> "observation.json"
    ).foreach { (name, file) => append(links, link(name, s"evidence/${story.id}/$file")) }
    val provenance = element("div", "provenance")
    val revision = text(story.provenance, "revision").take(8)
    val dirty = story.provenance.hcursor.get[Boolean]("worktree_dirty").getOrElse(true)
    append(
      provenance,
      element(
        "p",
        text = s"Recorded ${text(story.provenance, "recorded_at")}\nSource $revision${
            if dirty then " with uncommitted changes" else ""
          }."
      ),
      link("Execution provenance →", s"evidence/${story.id}/run.json"),
      element("p", text = text(story.provenance, "topology")),
      link("Application & core source →", "source/docs/architecture/001-application-integration.md")
    )
    append(content, links, provenance)
    append(panel, head, content)
    panel

  private def text(json: Json, field: String): String =
    json.hcursor.get[String](field).getOrElse("Unknown")
  private def display(value: Option[Json]): String = value match
    case None => "—"
    case Some(json) =>
      json.asString
        .orElse(json.asArray.map(_.map(v => v.asString.getOrElse(v.noSpaces)).mkString(", ")))
        .getOrElse(json.noSpaces)
  private def label(field: String): String = Map(
    "application" -> "Application",
    "review" -> "Review",
    "completed" -> "Completed steps",
    "enabled" -> "Enabled steps",
    "workflow" -> "Workflow",
    "outcome" -> "Outcome",
    "reason" -> "Rejection reason",
    "consumed" -> "Previous contract consumed",
    "active_contracts" -> "Active applications",
    "visible_to" -> "Visible to"
  ).getOrElse(field, field)

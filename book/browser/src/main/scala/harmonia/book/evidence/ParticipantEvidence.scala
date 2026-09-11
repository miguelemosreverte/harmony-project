package harmonia.book.evidence

import harmonia.book.*
import harmonia.book.ui.Elements.*
import io.circe.Json
import org.scalajs.dom
import harmonia.examples.ExampleKind
import EvidenceValues.*

object ParticipantEvidence:
  def render(
      story: RecordedStory,
      state: ViewState,
      navigate: ViewState => Unit
  ): Option[dom.HTMLElement] =
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
      val purchase =
        story.kind == ExampleKind.Purchase
      val description = element(
        "p",
        "evidence",
        s"Recorded queries and event history from $selected's participant. " + (if purchase then
                                                                                  "Visible financing or offer events provide a positive control for this party."
                                                                                else
                                                                                  "Shared progress is the positive control for this observation channel.")
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
        "application" -> (if purchase then "Financing application visible"
                          else "Private application visible"),
        "progress" -> (if purchase then "Offer visible" else "Shared progress visible"),
        "private_events" -> (if purchase then "Private financing create events"
                             else "Private application create events"),
        "progress_events" -> (if purchase then "Offer create events"
                              else "Shared progress create events"),
        "private_payload_observed" -> (if purchase then "Documents in event history"
                                       else "Private payload in event history")
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

package harmonia.book.evidence

import harmonia.book.*
import harmonia.ui.Elements.*
import io.circe.Json
import org.scalajs.dom
import EvidenceValues.*

object ComparisonView:
  def render(story: RecordedStory, step: Int): dom.HTMLElement =
    val panel = element("section", "panel")
    val head = element("div", "panel-head")
    append(
      head,
      element("h2", text = "Expected & observed"),
      element("span", "small", story.units(step).id)
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

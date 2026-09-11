package harmonia.book.composer

import harmonia.book.ui.Elements
import harmonia.book.{RecordedStory}
import Elements.*
import io.circe.Json
import org.scalajs.dom

object CompositionEvidence:
  def render(story: RecordedStory, index: Int): dom.HTMLElement =
    val panel = element("section", "panel")
    val head = element("div", "panel-head")
    append(head, element("h2", text = "The chosen actions"))
    val body = element("div", "composition-evidence")
    val actual = story.actualActions.lift(index).getOrElse(Json.Null).hcursor
    val expected = story.expectedActions.lift(index).getOrElse(Json.Null).hcursor
    val completed = actual.get[Vector[String]]("completed").getOrElse(Vector.empty)
    val enabled = actual.get[Vector[String]]("enabled").getOrElse(Vector.empty)
    append(
      body,
      element(
        "p",
        text =
          "Follow the attempts above to see consent, ordering, and each independent source state. The ledger creates sources only when the partner accepts."
      )
    )
    val flow = element("ol", "recorded-flow")
    val table = element("table")
    val header = element("tr")
    Vector("Action / role", "Actor", "Expected source", "Observed source").foreach(label =>
      append(header, element("th", text = label))
    )
    append(table, header)
    story.input.hcursor
      .downField("plan")
      .get[Vector[Json]]("steps")
      .getOrElse(Vector.empty)
      .foreach { step =>
        val id = step.hcursor.get[String]("id").getOrElse("")
        val stage =
          if completed.contains(id) then "complete"
          else if enabled.contains(id) then "enabled"
          else "waiting"
        append(
          flow,
          element("li", if stage == "complete" then "done" else "waiting", s"$id · $stage")
        )
        val baseline = expected.downField("sources").get[String](id).getOrElse("Not created")
        val observed = actual.downField("sources").get[String](id).getOrElse("Not created")
        val row = element("tr", if baseline == observed then "" else "different")
        Vector(
          s"$id / ${step.hcursor.get[String]("role").getOrElse("")}",
          step.hcursor.get[String]("actor").getOrElse(""),
          baseline,
          observed
        ).foreach(value => append(row, element("td", text = value)))
        append(table, row)
      }
    append(body, flow, table)
    append(panel, head, body)
    panel

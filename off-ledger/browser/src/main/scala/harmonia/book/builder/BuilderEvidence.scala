package harmonia.book.builder

import harmonia.book.{RecordedStory, Elements}
import Elements.*
import io.circe.Json
import org.scalajs.dom

object BuilderEvidence:
  def render(story: RecordedStory, index: Int): dom.HTMLElement =
    val root = element("section", "panel")
    val heading = element("div", "panel-head")
    append(heading, element("h2", text = "Accepted package inputs")); append(root, heading)
    val body = element("div", "composition-evidence")
    Vector("Expected" -> story.expectedActions, "Observed" -> story.actualActions).foreach {
      (label, actions) =>
        val action = actions.lift(index).getOrElse(Json.Null).hcursor
        val count = action.get[Int]("inputs").getOrElse(0)
        append(body, element("h3", text = s"$label: $count of 8 input slots"))
        val slots = element("ol", "builder-slots")
        (1 to 8).foreach(i =>
          append(
            slots,
            element(
              "li",
              if i <= count then "occupied" else "",
              if i <= count then s"$i · Used" else s"$i · Empty"
            )
          )
        )
        append(body, slots, element("p", text = s"HTTP ${action.get[Int]("http").getOrElse(0)}"))
    }
    append(
      body,
      element(
        "p",
        text =
          "Failed operations preserve the accepted input count. Inspection, supported mapping, and availability in the live composer are separate facts; compare them below."
      )
    )
    append(root, body); root

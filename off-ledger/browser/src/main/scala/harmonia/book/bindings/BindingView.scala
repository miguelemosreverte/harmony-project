package harmonia.book.bindings

import harmonia.book.{RecordedStory, Elements}
import Elements.*
import io.circe.Json
import org.scalajs.dom

object BindingView:
  def render(story: RecordedStory): dom.HTMLElement =
    val panel = element("section", "panel binding-evidence")
    val head = element("div", "panel-head")
    append(head, element("h2", text = "The generated adapter"))
    val table = element("table")
    val header = element("tr")
    Vector("Across the complete recording", "Expected", "Observed").foreach(value =>
      append(header, element("th", text = value))
    )
    append(table, header)
    Vector(
      "active_bindings" -> "Active companion contracts",
      "binding_creations" -> "Companion creation events"
    ).foreach { (field, label) =>
      def count(result: Json): String =
        result.hcursor.downField("integration").get[Int](field).toOption.fold("—")(_.toString)
      val left = count(story.expected)
      val right = count(story.actual)
      val row = element("tr", if left == right then "" else "different")
      append(
        row,
        element("td", text = label),
        element("td", text = left),
        element("td", text = right)
      )
      append(table, row)
    }
    val note = element(
      "p",
      "atomic-proof",
      "The source application is unchanged. The companion contract implements Harmonia's interface and exercises its typed source choice. These extra contracts are recorded separately from the business outcome shared with direct participation."
    )
    append(panel, head, table, note)
    panel

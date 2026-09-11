package harmonia.book.chapters

import harmonia.ui.Elements
import harmonia.book.{RecordedStory}
import Elements.*
import io.circe.Json
import org.scalajs.dom

object BoundaryEvidence:
  def render(story: RecordedStory, index: Int): dom.HTMLElement =
    val root = element("section", "panel")
    val heading = element("div", "panel-head")
    append(
      heading,
      element("h2", text = if index == 0 then "Measured ledger limits" else "One winning advance")
    )
    append(root, heading)
    val body = element("div", "balance-grid")
    val actual = story.actualActions.lift(index).getOrElse(Json.obj())
    val expected = story.expectedActions.lift(index).getOrElse(Json.obj())
    actual.asObject.toVector.flatMap(_.toVector).filter(_._2.asNumber.nonEmpty).foreach {
      (name, value) =>
        val observed = value.asNumber.map(_.toDouble).getOrElse(0.0)
        val baseline = expected.hcursor.get[Double](name).getOrElse(0.0)
        val card = element("article", "balance-card")
        append(
          card,
          element("span", text = name.replace('_', ' ')),
          element("strong", text = value.noSpaces),
          element(
            "span",
            text =
              s"Expected: ${expected.hcursor.downField(name).focus.getOrElse(Json.Null).noSpaces}"
          )
        )
        val progress = element("progress").asInstanceOf[dom.html.Progress]
        progress.max = math.max(1, math.max(observed, baseline)); progress.value = observed
        progress.setAttribute(
          "aria-label",
          s"${name.replace('_', ' ')}: observed $observed; expected $baseline"
        )
        append(card, progress); append(body, card)
    }
    append(root, body); root

package harmonia.book.transfer

import harmonia.book.ui.Elements
import harmonia.book.{RecordedStory}
import Elements.*
import io.circe.Json
import org.scalajs.dom

object TransferView:
  def render(story: RecordedStory, step: Int): dom.HTMLElement =
    val actual = story.actualActions(step).hcursor
    val trade = story.input.hcursor.downField("setup").downField("trade")
    val total = trade.get[String]("quantity").getOrElse("0")
    val asset = trade.get[String]("asset").getOrElse("units")
    val panel = element("section", "panel transfer-balances")
    val head = element("div", "panel-head")
    append(
      head,
      element("h2", text = "Where the position is"),
      element("span", "small", s"$total $asset")
    )
    val balances = element("div", "balance-grid")
    Vector(
      ("Source · available", actual.downField("source").get[String]("available").getOrElse("0")),
      ("Source · locked", actual.downField("source").get[String]("locked").getOrElse("0")),
      ("Destination · received", actual.get[String]("destination").getOrElse("0"))
    ).foreach { (label, amount) =>
      val card = element("div", "balance-card")
      val meter = element("progress").asInstanceOf[dom.html.Progress]
      meter.max = total.toDouble
      meter.value = amount.toDouble
      meter.setAttribute("aria-label", s"$label: $amount of $total $asset")
      append(
        card,
        element("span", "small", label),
        element("strong", text = s"$amount $asset"),
        meter
      )
      append(balances, card)
    }
    val count = story.actual.hcursor.get[Int]("settlement_transactions").getOrElse(0)
    val expected = story.expected.hcursor.get[Int]("settlement_transactions").getOrElse(0)
    val proof = element(
      "p",
      "atomic-proof",
      s"Complete recording: $count settlement transactions observed; $expected expected. A counted transaction contains source retirement, destination receipt, the settled trade, and workflow completion together."
    )
    append(panel, head, balances, proof)
    panel

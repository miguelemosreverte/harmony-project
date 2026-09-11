package harmonia.scene

/** @book.slice
  *   book
  * @book.role
  *   Render the shared diagrams
  * @book.summary
  *   The book and live application render the same typed diagram structure and measured connectors.
  */

import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

/** The same selectable diagram is used by the book, live plans, and package stages. */
final class WorkflowDiagramView(root: dom.HTMLElement):
  private val figure = node("figure", "workflow-diagram")
  private val map = node("div", "workflow-map")
  private val title = node("strong", "workflow-title")
  private val caption = node("p", "workflow-caption")
  caption.setAttribute("aria-live", "polite")
  figure.appendChild(map); figure.appendChild(title); figure.appendChild(caption)
  root.appendChild(figure)
  private val connections = new ConnectionLayer(map)
  private var cards = Vector.empty[dom.HTMLElement]

  def dispose(): Unit = connections.dispose()

  def render(value: WorkflowDiagram): Unit = value.layers match
    case Left(error) => title.textContent = error
    case Right(layers) =>
      cards.foreach(_.remove())
      val rows = scala.collection.mutable.Map.empty[Int, Int]
      cards = value.nodes.map { item =>
        val card = node("button", "workflow-node").asInstanceOf[dom.html.Button]
        card.`type` = "button"
        card.title = item.detail
        card.setAttribute("aria-label", item.label + ". " + item.actor + ". " + item.detail)
        card.setAttribute("data-node", item.id)
        card.setAttribute("data-state", item.state.toString.toLowerCase)
        card.setAttribute("aria-pressed", (item.state == DiagramState.Current).toString)
        val layer = layers(item.id)
        val row = rows.getOrElse(layer, 0) + 1; rows.update(layer, row)
        card.style.setProperty("--column", (layer + 1).toString)
        card.style.setProperty("--row", row.toString)
        val badge = node("span", "workflow-symbol")
        badge.textContent =
          if item.state == DiagramState.Complete then "✓" else (layer + 1).toString
        val heading = node("strong", ""); heading.textContent = item.label
        val actor = node("span", "workflow-actor"); actor.textContent = item.actor
        Vector(badge, heading, actor).foreach(card.appendChild)
        card.onclick = _ =>
          title.textContent = item.label
          caption.textContent = item.detail
          cards.foreach(other => other.setAttribute("aria-pressed", (other == card).toString))
          root.dispatchEvent(
            new dom.CustomEvent(
              "harmonia-select",
              js.Dynamic.literal(detail = item.id, bubbles = true).asInstanceOf[dom.CustomEventInit]
            )
          )
        map.appendChild(card)
        card
      }
      map.style.setProperty("--columns", (layers.values.max + 1).toString)
      map.classList.toggle("workflow-wide", layers.values.max > 3)
      val indexed = value.nodes.map(_.id).zip(cards).toMap
      connections.render(
        value.edges.map(edge =>
          ConnectionLayer.Arrow(
            edge.from + "--" + edge.to,
            indexed(edge.from),
            indexed(edge.to),
            edge.state
          )
        )
      )
      title.textContent = value.title
      caption.textContent = value.caption

  private def node(tag: String, css: String): dom.HTMLElement =
    val result = dom.document.createElement(tag).asInstanceOf[dom.HTMLElement]
    result.className = css; result

object WorkflowDiagramView:
  private val views = scala.collection.mutable.Map.empty[dom.HTMLElement, WorkflowDiagramView]
  @JSExportTopLevel("renderHarmoniaDiagram")
  def renderJson(root: dom.HTMLElement, json: String): Unit =
    io.circe.parser.decode[WorkflowDiagram](json) match
      case Right(value) => views.getOrElseUpdate(root, new WorkflowDiagramView(root)).render(value)
      case Left(_) =>
        root.textContent = "The diagram could not be read. Its source evidence remains available."

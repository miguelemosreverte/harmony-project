package harmonia.scene

/** @module.slice
  *   presentation
  * @module.role
  *   Render the shared diagrams
  * @module.summary
  *   Renders typed diagrams with measured connectors and observed node states.
  */

import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

/** A diagram presents observed state without introducing navigation controls. */
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
  private var documentation = Map.empty[String, DiagramNode]

  def dispose(): Unit = connections.dispose()

  def render(value: WorkflowDiagram): Unit = value.layers match
    case Left(error) => title.textContent = error
    case Right(layers) =>
      documentation = value.nodes.map(n => n.id -> n).toMap
      val existing = cards.map(card => card.getAttribute("data-node") -> card).toMap
      cards
        .filterNot(card => documentation.contains(card.getAttribute("data-node")))
        .foreach(_.remove())
      val rows = scala.collection.mutable.Map.empty[Int, Int]
      cards = value.nodes.sortBy(item => layers(item.id)).map { item =>
        val card = existing.getOrElse(
          item.id, {
            val fresh = node("article", "workflow-node")
            Vector(
              node("span", "workflow-symbol"),
              node("strong", "workflow-label"),
              node("span", "workflow-actor")
            ).foreach(fresh.appendChild)
            map.appendChild(fresh)
            fresh
          }
        )
        card.title = item.detail
        card.setAttribute("aria-label", item.label + ". " + item.actor + ". " + item.detail)
        card.setAttribute("data-node", item.id)
        card.setAttribute("data-state", item.state.toString.toLowerCase)
        val layer = layers(item.id)
        val row = rows.getOrElse(layer, 0) + 1; rows.update(layer, row)
        card.style.setProperty("--column", (layer + 1).toString)
        card.style.setProperty("--row", row.toString)
        val badge = card.querySelector(".workflow-symbol")
        badge.textContent =
          if item.state == DiagramState.Complete then "✓"
          else if item.state == DiagramState.Skipped then "–"
          else (layer + 1).toString
        card.querySelector(".workflow-label").textContent = item.label
        card.querySelector(".workflow-actor").textContent = item.actor
        card
      }
      cards.foreach(map.appendChild)
      map.style.setProperty("--columns", (layers.values.max + 1).toString)
      map.classList.toggle("workflow-wide", layers.values.max > 3)
      val indexed = cards.map(card => card.getAttribute("data-node") -> card).toMap
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

  def select(id: String): Unit = documentation.get(id).foreach { item =>
    title.textContent = item.label
    caption.textContent = item.detail
    cards
      .foreach(card => card.classList.toggle("selected-node", card.getAttribute("data-node") == id))
  }

  private def node(tag: String, css: String): dom.HTMLElement =
    val result = dom.document.createElement(tag).asInstanceOf[dom.HTMLElement]
    result.className = css; result

object WorkflowDiagramView:
  private val views = scala.collection.mutable.Map.empty[dom.HTMLElement, WorkflowDiagramView]
  @JSExportTopLevel("pruneHarmoniaDiagrams")
  def prune(): Unit = views.keys.filterNot(dom.document.contains).toVector.foreach { root =>
    views.remove(root).foreach(_.dispose())
  }

  @JSExportTopLevel("renderHarmoniaDiagram")
  def renderJson(root: dom.HTMLElement, json: String): Unit =
    io.circe.parser.decode[WorkflowDiagram](json) match
      case Right(value) => views.getOrElseUpdate(root, new WorkflowDiagramView(root)).render(value)
      case Left(_) =>
        root.textContent = "The diagram could not be read. Its source evidence remains available."

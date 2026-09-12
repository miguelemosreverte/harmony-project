package harmonia.scene

import org.scalajs.dom
import scala.scalajs.js

/** Connections end outside rendered nodes, including their labels on vertical layouts. */
final class ConnectionLayer(root: dom.HTMLElement):
  import ConnectionLayer.*
  private val svg = dom.document.createElementNS(namespace, "svg")
  svg.setAttribute("class", "measured-connections")
  svg.setAttribute("aria-hidden", "true")
  root.appendChild(svg)
  private var arrows = Vector.empty[Arrow]
  private val paths = scala.collection.mutable.Map.empty[String, dom.Element]
  private def pathFor(id: String): dom.Element = paths.getOrElseUpdate(
    id, {
      val path = dom.document.createElementNS(namespace, "path")
      svg.appendChild(path)
      path
    }
  )
  private val observer = new dom.ResizeObserver((_, _) => draw())
  observer.observe(root)

  private val printLayout: js.Function1[dom.Event, Unit] = _ => draw()
  dom.window.addEventListener("beforeprint", printLayout)
  dom.window.addEventListener("afterprint", printLayout)

  def dispose(): Unit =
    observer.disconnect()
    dom.window.removeEventListener("beforeprint", printLayout)
    dom.window.removeEventListener("afterprint", printLayout)

  def render(value: Vector[Arrow]): Unit =
    arrows = value
    draw()

  private def draw(): Unit =
    val visible = scala.collection.mutable.Set.empty[String]
    val bounds = root.getBoundingClientRect()
    svg.setAttribute("viewBox", s"0 0 ${bounds.width} ${bounds.height}")
    arrows.foreach { arrow =>
      val first = arrow.from.getBoundingClientRect()
      val last = arrow.to.getBoundingClientRect()
      val isDiagram = root.classList.contains("workflow-map")
      val vertical =
        if isDiagram then math.abs(last.left - first.left) < 40
        else math.abs(last.top - first.top) > math.abs(last.left - first.left)
      def rectangle(element: dom.Element): dom.DOMRect =
        val portrait = element.querySelector(".person-icon")
        (if !vertical && portrait != null then portrait else element).getBoundingClientRect()
      val a = rectangle(arrow.from)
      val b = rectangle(arrow.to)
      if a.width > 0 && b.width > 0 then
        val gap = 10.0
        val x1 = (if vertical then a.left + a.width / 2 else a.right + gap) - bounds.left
        val y1 = (if vertical then a.bottom + gap else a.top + a.height / 2) - bounds.top
        val x2 = (if vertical then b.left + b.width / 2 else b.left - gap) - bounds.left
        val y2 = (if vertical then b.top - gap else b.top + b.height / 2) - bounds.top
        val obstacles = root.querySelectorAll(".workflow-node")
        val detour = vertical && (0 until obstacles.length).exists { i =>
          val other = obstacles(i).asInstanceOf[dom.Element]
          val box = other.getBoundingClientRect()
          other != arrow.from && other != arrow.to && box.top > a.bottom && box.bottom < b.top
        }
        val skipColumn = !vertical && (0 until obstacles.length).exists { i =>
          val box = obstacles(i).asInstanceOf[dom.Element].getBoundingClientRect()
          box.left > a.right && box.right < b.left
        }
        val topLane = (0 until obstacles.length)
          .map(i => obstacles(i).asInstanceOf[dom.Element].getBoundingClientRect().top - bounds.top)
          .minOption
          .map(top => math.max(2.0, top - 10))
          .getOrElse(2.0)
        val lane = math.min(bounds.width - 4, math.max(a.right, b.right) - bounds.left + 18)
        val endX = if detour then b.right - bounds.left + 8 else x2
        val endY = if detour then b.top - bounds.top + b.height / 2 else y2
        val path = pathFor(arrow.id)
        visible += arrow.id
        val shape =
          if skipColumn then
            s"M$x1 $y1 H${a.right - bounds.left + 18} V$topLane H${b.left - bounds.left - 18} V$y2 H$x2"
          else if detour then
            s"M${a.right - bounds.left + 8} ${a.top - bounds.top + a.height / 2} H$lane V$endY H$endX"
          else if vertical then s"M$x1 $y1 C$x1 ${(y1 + y2) / 2} $x2 ${(y1 + y2) / 2} $x2 $y2"
          else s"M$x1 $y1 C${(x1 + x2) / 2} $y1 ${(x1 + x2) / 2} $y2 $x2 $y2"
        path.setAttribute("d", shape)
        path.setAttribute("data-edge", arrow.id)
        path.setAttribute("data-status", arrow.status.toString.toLowerCase)
        if arrow.head then
          val head = pathFor(arrow.id + ":head")
          visible += arrow.id + ":head"
          head.setAttribute(
            "d",
            if detour then s"M${endX + 6} ${endY - 5} L$endX $endY L${endX + 6} ${endY + 5}"
            else if vertical then s"M${x2 - 5} ${y2 - 6} L$x2 $y2 L${x2 + 5} ${y2 - 6}"
            else s"M${x2 - 6} ${y2 - 5} L$x2 $y2 L${x2 - 6} ${y2 + 5}"
          )
          head.setAttribute("class", "connector-head")
          head.setAttribute("data-edge", arrow.id)
          head.setAttribute("data-status", arrow.status.toString.toLowerCase)
    }

    paths.keys.filterNot(visible).toVector.foreach { id =>
      paths.remove(id).foreach(path => path.parentNode.removeChild(path))
    }

object ConnectionLayer:
  private val namespace = "http://www.w3.org/2000/svg"
  final case class Arrow(
      id: String,
      from: dom.Element,
      to: dom.Element,
      status: DiagramState,
      head: Boolean = true
  )

package harmonia.scene

import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

/** Connected stops show position; the scene owns the explanation. */
final class StepCarousel(root: dom.HTMLElement):
  private val track = element("div", "carousel-track")
  private val graph = element("div", "carousel-graph")
  private val hint = element("div", "carousel-hint")
  private val answers = element("div", "carousel-answers")
  private val svg = dom.document.createElementNS("http://www.w3.org/2000/svg", "svg")
  svg.setAttribute("class", "carousel-lines"); svg.setAttribute("aria-hidden", "true")
  svg.setAttribute("preserveAspectRatio", "none")
  root.classList.add("step-carousel")
  graph.appendChild(svg); track.appendChild(graph)
  root.appendChild(track); root.appendChild(answers); root.appendChild(hint)
  private var buttons = Map.empty[String, dom.html.Button]
  private var edges = Map.empty[(String, String), dom.Element]
  private var selected = ""
  private val observer = new dom.ResizeObserver((_, _) => reveal())
  observer.observe(root)
  def dispose(): Unit = observer.disconnect()

  private def reveal(): Unit = buttons.get(selected).foreach { button =>
    if track.clientWidth > 0 then
      val left = button.offsetLeft
      if left < track.scrollLeft then track.scrollLeft = left
      else if left + button.offsetWidth > track.scrollLeft + track.clientWidth then
        track.scrollLeft = left + button.offsetWidth - track.clientWidth
  }

  def render(frame: CarouselFrame, select: String => Unit): Unit =
    val layout = CarouselLayout(frame)
    val ids = layout.points.map(_.step.id).toSet
    buttons.filterNot((id, _) => ids(id)).values.foreach(_.remove())
    buttons = buttons.filter((id, _) => ids(id))
    val columns = layout.points.map(_.column).max + 1
    val rows = layout.points.map(_.row).max + 1
    // Long linear reading trails keep distinct targets in a sliding rail.
    // Workflow branches remain visible together in the available width.
    graph.style.width =
      if columns > 10 && rows == 1 then s"${columns * 32}px"
      else s"min(100%, ${columns * 48}px)"
    graph.style.height = s"${rows * 44}px"
    svg.setAttribute("viewBox", s"0 0 ${columns * 48} ${rows * 44}")
    val indexed = layout.points.map(p => p.step.id -> p).toMap
    val edgeIds = layout.links.map(e => e.from -> e.to).toSet
    edges.filterNot((key, _) => edgeIds(key)).values.foreach(_.remove())
    edges = edges.filter((key, _) => edgeIds(key))
    layout.links.foreach { link =>
      val key = link.from -> link.to
      val line = edges.getOrElse(
        key, {
          val fresh = dom.document.createElementNS("http://www.w3.org/2000/svg", "path")
          svg.appendChild(fresh); edges += key -> fresh; fresh
        }
      )
      val a = indexed(link.from); val b = indexed(link.to)
      val x1 = a.column * 48 + 24; val y1 = a.row * 44 + 22
      val x2 = b.column * 48 + 24; val y2 = b.row * 44 + 22
      line.setAttribute("d", s"M $x1 $y1 C ${x1 + 24} $y1 ${x2 - 24} $y2 $x2 $y2")
      line.setAttribute("data-state", link.state.toString.toLowerCase)
    }
    layout.points.foreach { point =>
      val step = point.step
      val button = buttons.getOrElse(
        step.id, {
          val fresh = element("button", "carousel-step").asInstanceOf[dom.html.Button]
          fresh.`type` = "button"; fresh.setAttribute("data-stop", step.id)
          fresh.appendChild(element("span", "carousel-marker"))
          graph.appendChild(fresh); buttons += step.id -> fresh; fresh
        }
      )
      button.style.left = s"${(point.column + .5) * 100 / columns}%"
      button.style.width = s"min(44px, ${100.0 / columns}%)"
      button.style.transform = "translateX(-50%)"
      button.style.top = s"${point.row * 44}px"
      button.setAttribute("data-state", step.state.toString.toLowerCase)
      button.setAttribute("aria-current", if step.id == layout.selected then "step" else "false")
      button.setAttribute("aria-label", step.label)
      button.querySelector("span").textContent = point.terminal match
        case Some(DiagramState.Complete) => "✓"
        case Some(DiagramState.Refused)  => "×"
        case _                           => if step.state == DiagramState.Refused then "×" else ""
      button.onclick = _ => select(layout.addresses(step.id))
      button.onmouseenter = _ => hint.textContent = step.label
      button.onmouseleave = _ => hint.textContent = ""
      button.onfocus = _ => hint.textContent = step.label
      button.onblur = _ => hint.textContent = ""
      button.onkeydown = event =>
        if Set("ArrowLeft", "ArrowRight", "ArrowUp", "ArrowDown", "Home", "End")(event.key) then
          val path = frame.paths
            .filterNot(_.answers.contains(true))
            .find(_.steps.exists(_.id == layout.addresses(step.id)))
            .getOrElse(frame.paths.head)
          val sequence = path.steps.zipWithIndex.map { (stop, i) =>
            if i < path.sharedPrefix.getOrElse(0) then frame.paths.head.steps(i).id else stop.id
          }
          val index = sequence.indexOf(step.id).max(0)
          val target = event.key match
            case "Home" => sequence.head
            case "End"  => sequence.last
            case "ArrowUp" | "ArrowDown" =>
              val row = point.row + (if event.key == "ArrowUp" then -1 else 1)
              layout.points
                .filter(_.row == row)
                .sortBy(p => math.abs(p.column - point.column))
                .headOption
                .map(_.step.id)
                .getOrElse(step.id)
            case "ArrowLeft" => sequence((index - 1).max(0))
            case _           => sequence((index + 1).min(sequence.size - 1))
          event.preventDefault(); buttons(target).click(); buttons.get(target).foreach(_.focus())
    }
    // Answer names remain visible because they are input, not playback positions.
    answers.textContent = ""
    frame.paths.filter(_.answers.contains(true)).flatMap(_.steps).foreach { step =>
      val button = element("button", "carousel-answer").asInstanceOf[dom.html.Button]
      button.`type` = "button"; button.textContent = step.label
      button.setAttribute("data-stop", step.id); button.onclick = _ => select(step.id)
      answers.appendChild(button)
    }
    val changed = selected != layout.selected
    selected = layout.selected
    if changed then { hint.textContent = ""; reveal() }

  private def element(tag: String, css: String): dom.HTMLElement =
    val result = dom.document.createElement(tag).asInstanceOf[dom.HTMLElement]
    result.className = css; result

object StepCarousel:
  private val views = scala.collection.mutable.Map.empty[dom.HTMLElement, StepCarousel]
  @JSExportTopLevel("pruneHarmoniaCarousels")
  def prune(): Unit = views.keys
    .filterNot(dom.document.contains)
    .toVector
    .foreach(root => views.remove(root).foreach(_.dispose()))
  @JSExportTopLevel("renderHarmoniaCarousel")
  def renderJson(root: dom.HTMLElement, json: String, select: js.Function1[String, Unit]): Unit =
    prune()
    val frame = io.circe.parser.decode[CarouselFrame](json).fold(throw _, identity)
    views.getOrElseUpdate(root, new StepCarousel(root)).render(frame, id => select(id))

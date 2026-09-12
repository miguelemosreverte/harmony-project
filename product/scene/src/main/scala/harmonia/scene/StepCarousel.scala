package harmonia.scene

import io.circe.Codec
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

/** Inspection addresses, not commands. A path can represent an alternative outcome. */
final case class CarouselStep(id: String, label: String, state: DiagramState)
object CarouselStep:
  given Codec.AsObject[CarouselStep] = Codec.AsObject.derived[CarouselStep]

final case class CarouselPath(id: String, label: String, steps: Vector[CarouselStep])
object CarouselPath:
  given Codec.AsObject[CarouselPath] = Codec.AsObject.derived[CarouselPath]

final case class CarouselFrame(paths: Vector[CarouselPath], selected: String)
object CarouselFrame:
  given Codec.AsObject[CarouselFrame] = Codec.AsObject.derived[CarouselFrame]

/** Retained controls preserve focus, scroll position, and target geometry during inspection. */
final class StepCarousel(root: dom.HTMLElement):
  private var structure = Vector.empty[(String, Vector[String])]
  private var selected = ""
  root.classList.add("step-carousel")
  private val observer = new dom.ResizeObserver((_, _) => reveal())
  observer.observe(root)
  root.addEventListener(
    "keydown",
    (event: dom.KeyboardEvent) =>
      val button = event.target.asInstanceOf[dom.Element].closest("button")
      if button != null && Set("ArrowLeft", "ArrowRight", "Home", "End")(event.key) then
        val row = button.closest(".carousel-path")
        val buttons = row.querySelectorAll("button")
        val index = (0 until buttons.length).find(i => buttons(i) == button).getOrElse(0)
        val target = event.key match
          case "Home"      => 0
          case "End"       => buttons.length - 1
          case "ArrowLeft" => (index - 1).max(0)
          case _           => (index + 1).min(buttons.length - 1)
        event.preventDefault()
        val next = buttons(target).asInstanceOf[dom.html.Button]
        next.click()
        if dom.document.contains(next) then next.focus()
  )
  def dispose(): Unit = observer.disconnect()

  private def reveal(): Unit =
    Option(root.querySelector("[aria-current=step]")).foreach { node =>
      val button = node.asInstanceOf[dom.HTMLElement]
      val track = button.closest(".carousel-track").asInstanceOf[dom.HTMLElement]
      if track.clientWidth > 0 then
        val left = button.offsetLeft
        if left < track.scrollLeft then track.scrollLeft = left
        else if left + button.offsetWidth > track.scrollLeft + track.clientWidth then
          track.scrollLeft = left + button.offsetWidth - track.clientWidth
    }

  def render(frame: CarouselFrame, select: String => Unit): Unit =
    val next = frame.paths.map(p => p.id -> p.steps.map(_.id))
    if next != structure then
      structure = next
      root.textContent = ""
      frame.paths.foreach { path =>
        val row = element("div", "carousel-path")
        row.setAttribute("data-path", path.id)
        val label = element("span", "carousel-path-label")
        val track = element("div", "carousel-track")
        val progress = element("progress", "carousel-progress")
        progress.setAttribute("max", path.steps.size.max(1).toString)
        val stops = element("div", "carousel-stops")
        path.steps.foreach { step =>
          val button = element("button", "carousel-step").asInstanceOf[dom.html.Button]
          button.`type` = "button"
          button.setAttribute("data-stop", step.id)
          button.appendChild(element("span", "carousel-marker"))
          button.appendChild(element("span", "carousel-label"))
          stops.appendChild(button)
        }
        track.appendChild(stops); track.appendChild(progress)
        row.appendChild(label); row.appendChild(track); root.appendChild(row)
      }
    frame.paths.zipWithIndex.foreach { (path, rowIndex) =>
      val row = root.children(rowIndex)
      row.querySelector(".carousel-path-label").textContent = path.label
      val index = path.steps.indexWhere(_.id == frame.selected)
      row.setAttribute("data-selected", (index >= 0).toString)
      val progress = row.querySelector("progress")
      progress.setAttribute("value", (index + 1).max(0).toString)
      progress.setAttribute("aria-label", path.label + " progress")
      val buttons = row.querySelectorAll("button")
      path.steps.zipWithIndex.foreach { (step, i) =>
        val button = buttons(i).asInstanceOf[dom.html.Button]
        button.setAttribute("data-state", step.state.toString.toLowerCase)
        button.setAttribute("aria-current", if step.id == frame.selected then "step" else "false")
        button.setAttribute("aria-label", path.label + ": " + step.label)
        button.title = step.label
        button.querySelector(".carousel-label").textContent = step.label
        button.querySelector(".carousel-marker").textContent = step.state match
          case DiagramState.Refused  => "!"
          case DiagramState.Complete => "✓"
          case DiagramState.Skipped  => "–"
          case _                     => (i + 1).toString
        button.onclick = _ => select(step.id)
        if step.id == frame.selected && selected != frame.selected then
          val track = row.querySelector(".carousel-track").asInstanceOf[dom.HTMLElement]
          val left = button.offsetLeft
          if left < track.scrollLeft then track.scrollLeft = left
          else if left + button.offsetWidth > track.scrollLeft + track.clientWidth then
            track.scrollLeft = left + button.offsetWidth - track.clientWidth
      }
    }
    val changed = selected != frame.selected
    selected = frame.selected
    if changed then reveal()

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
    views.keys
      .filterNot(dom.document.contains)
      .toVector
      .foreach(root => views.remove(root).foreach(_.dispose()))
    val frame = io.circe.parser.decode[CarouselFrame](json).fold(throw _, identity)
    views.getOrElseUpdate(root, new StepCarousel(root)).render(frame, id => select(id))

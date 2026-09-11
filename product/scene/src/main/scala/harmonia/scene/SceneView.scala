package harmonia.scene

import org.scalajs.dom
import scala.scalajs.js.annotation.JSExportTopLevel

/** Stable HTML objects make changes of state visible without replacing the whole scene. */
final class SceneView(root: dom.HTMLElement):
  private val figure = element("figure", "harmonia-scene")
  private val map = element("div", "scene-map")
  private val privateArea = element("div", "scene-zone scene-private")
  private val sharedArea = element("div", "scene-zone scene-shared")
  private val trail = element("div", "scene-trail")
  private val people = element("div", "scene-people")
  private val documents = element("div", "scene-documents")
  private val artifact = element("div", "scene-artifact")
  private val amounts = element("div", "scene-amounts")
  private val caption = element("figcaption", "scene-caption")
  private val title = element("strong", "scene-title")
  private val description = element("p", "scene-description")
  private var previousPeople = Vector.empty[ScenePerson]

  append(privateArea, element("span", "scene-zone-label"))
  append(sharedArea, element("span", "scene-zone-label"))
  append(
    documents,
    element("span", "document-sheet", "▤"),
    element("span", text = "Private documents")
  )
  append(artifact, element("span", "document-sheet", "▤"), element("span", "artifact-label"))
  append(map, privateArea, sharedArea, trail, people, documents, artifact)
  append(caption, title, description)
  append(figure, map, amounts, caption)
  append(root, figure)

  def render(frame: SceneFrame): Unit =
    val kind = frame.kind.toString.toLowerCase
    figure.setAttribute("data-kind", kind)
    figure.setAttribute("data-phase", frame.phase.max(0).min(4).toString)
    figure.setAttribute("data-refused", frame.refused.toString)
    figure.setAttribute("aria-label", frame.title)
    privateArea.firstChild.textContent =
      if frame.kind == SceneKind.Transfer then "Source custody" else "Financing"
    sharedArea.firstChild.textContent =
      if frame.kind == SceneKind.Transfer then "Destination custody"
      else if frame.kind == SceneKind.Financing then "Shared workflow"
      else "Property offer"
    hide(documents, frame.kind == SceneKind.Transfer)
    artifact.querySelector(".artifact-label").textContent = frame.artifact
    hide(artifact, frame.artifact.isEmpty)
    title.textContent = frame.title
    description.textContent = frame.caption
    if frame.people != previousPeople then
      people.textContent = ""
      frame.people.zipWithIndex.foreach { (person, index) =>
        val place = element("div", "scene-person")
        place.setAttribute("data-person", person.id)
        place.setAttribute("data-position", index.toString)
        place.setAttribute("data-icon", person.icon)
        val icon = element("span", "person-icon", person.name.take(1))
        icon.setAttribute("aria-hidden", "true")
        append(
          place,
          icon,
          element("strong", text = person.name),
          element("small", text = person.role)
        )
        append(people, place)
      }
      previousPeople = frame.people
    val nodes = people.children
    (0 until nodes.length).foreach { index =>
      val person = nodes(index).asInstanceOf[dom.HTMLElement]
      person.classList.toggle("scene-focus", person.getAttribute("data-person") == frame.focus)
    }
    amounts.textContent = ""
    hide(amounts, frame.amounts.isEmpty)
    frame.amounts.foreach { amount =>
      val item = element("div", "scene-amount")
      val meter = element("meter")
      meter.setAttribute("min", "0"); meter.setAttribute("max", "1")
      meter.setAttribute("value", amount.fraction.max(0).min(1).toString)
      meter.setAttribute("aria-label", amount.label + ": " + amount.value)
      append(
        item,
        element("small", text = amount.label),
        element("strong", text = amount.value),
        meter
      )
      append(amounts, item)
    }

  private def hide(node: dom.HTMLElement, hidden: Boolean): Unit =
    if hidden then node.setAttribute("hidden", "") else node.removeAttribute("hidden")

  private def element(tag: String, css: String = "", text: String = ""): dom.HTMLElement =
    val node = dom.document.createElement(tag).asInstanceOf[dom.HTMLElement]
    node.className = css; node.textContent = text; node

  private def append(parent: dom.Node, children: dom.Node*): Unit =
    children.foreach(parent.appendChild)

object SceneView:
  private val views = scala.collection.mutable.Map.empty[dom.HTMLElement, SceneView]

  /** The static book uses the same renderer; JSON ends at this typed presentation boundary. */
  @JSExportTopLevel("renderHarmoniaScene")
  def renderJson(root: dom.HTMLElement, json: String): Unit =
    io.circe.parser.decode[SceneFrame](json) match
      case Right(frame) =>
        views.getOrElseUpdate(root, new SceneView(root)).render(frame)
      case Left(_) =>
        root.textContent = "This scene could not be read. The recorded evidence remains available."
        root.setAttribute("role", "alert")

package harmonia.scene

import org.scalajs.dom
import scala.scalajs.js.annotation.JSExportTopLevel

/** Stable HTML objects make changes of state visible without replacing the whole scene. */
final class SceneView(root: dom.HTMLElement):
  private val figure = element("figure", "harmonia-scene")
  private val observation = new ObservationView(figure)
  private val map = element("div", "scene-map")
  private val privateArea = element("div", "scene-zone scene-private")
  private val sharedArea = element("div", "scene-zone scene-shared")
  private val people = element("div", "scene-people")
  private val documents = element("div", "scene-documents")
  private val approval = document("scene-approval", "Approval", verified = true)
  private val artifact = document("scene-artifact", "Proposal", verified = false)
  private val house = SceneArtwork.illustration(SceneArtwork.Illustration.House)
  private val amounts = element("div", "scene-amounts")
  private val caption = element("figcaption", "scene-caption")
  private val title = element("strong", "scene-title")
  private val description = element("p", "scene-description")
  private var previousPeople = Vector.empty[ScenePerson]

  append(
    privateArea,
    SceneArtwork.mark(SceneArtwork.Mark.Bank),
    element("span", "scene-zone-label")
  )
  append(
    sharedArea,
    SceneArtwork.mark(SceneArtwork.Mark.House),
    element("span", "scene-zone-label")
  )
  append(
    documents,
    SceneArtwork.mark(SceneArtwork.Mark.Lock),
    element("span", text = "Private documents")
  )
  append(
    map,
    privateArea,
    sharedArea,
    people,
    documents,
    approval,
    artifact,
    house
  )
  append(caption, title, description)
  append(figure, map, amounts, caption)
  append(root, figure)
  private val connections = new ConnectionLayer(map)

  def dispose(): Unit = connections.dispose()

  private def document(css: String, label: String, verified: Boolean): dom.HTMLElement =
    val node = element("div", css + " scene-document")
    val paper = element("span", "document-sheet")
    (0 until 4).foreach(_ => append(paper, element("i")))
    if verified then append(paper, element("span", "document-check", "✓"))
    append(node, paper, element("span", "artifact-label", label))
    node

  def render(frame: SceneFrame): Unit =
    observation.render(frame.observation)
    val kind = frame.kind.toString.toLowerCase
    figure.setAttribute("data-kind", kind)
    figure.setAttribute("data-phase", frame.phase.max(0).min(4).toString)
    figure.setAttribute("data-refused", frame.refused.toString)
    val approvalState = frame.approval.getOrElse(
      if frame.phase > 0 then DiagramState.Complete else DiagramState.Pending
    )
    approval.setAttribute("data-status", approvalState.toString.toLowerCase)
    approval.querySelector(".document-check").textContent =
      if approvalState == DiagramState.Refused then "!" else "✓"
    figure.setAttribute("aria-label", frame.title)
    privateArea.querySelector(".scene-zone-label").textContent =
      if frame.kind == SceneKind.Transfer then "Source custody" else "Financing"
    sharedArea.querySelector(".scene-zone-label").textContent =
      if frame.kind == SceneKind.Transfer then "Destination custody"
      else if frame.kind == SceneKind.Financing then "Shared workflow"
      else "Property offer"
    hide(documents, frame.kind == SceneKind.Transfer)
    hide(approval, frame.kind == SceneKind.Transfer)
    hide(house, frame.kind != SceneKind.Purchase)
    artifact.querySelector(".artifact-label").textContent =
      if frame.kind == SceneKind.Transfer then frame.artifact
      else if frame.kind == SceneKind.Financing then "Continuation"
      else "Proposal"
    artifact.setAttribute(
      "aria-label",
      if frame.artifact.nonEmpty then frame.artifact else "Proposal: not yet created"
    )
    hide(artifact, frame.kind == SceneKind.Transfer && frame.artifact.isEmpty)
    title.textContent = ""
    val sentence = frame.title.indexOf(". ")
    if sentence < 0 then title.textContent = frame.title
    else
      append(
        title,
        element("span", text = frame.title.take(sentence + 1)),
        element("span", text = frame.title.drop(sentence + 1))
      )
    description.textContent = frame.caption
    if frame.people != previousPeople then
      people.textContent = ""
      frame.people.zipWithIndex.foreach { (person, index) =>
        val place = element("div", "scene-person")
        place.setAttribute("data-person", person.id)
        place.setAttribute("data-position", index.toString)
        place.setAttribute("data-icon", person.icon)
        val illustration =
          if person.icon == "bank" then SceneArtwork.Illustration.Bank
          else if index == 1 then SceneArtwork.Illustration.Alice
          else if index == 2 && frame.kind == SceneKind.Purchase then SceneArtwork.Illustration.Ben
          else SceneArtwork.Illustration.Sofia
        val icon = element("span", "person-icon")
        icon.setAttribute("aria-hidden", "true")
        append(
          icon,
          SceneArtwork.illustration(illustration),
          element("span", "person-initial", person.name.take(1))
        )
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
    def person(index: Int): dom.Element =
      people.children(index)
    def status(phase: Int): DiagramState =
      if frame.phase >= phase then DiagramState.Complete
      else if frame.refused then DiagramState.Refused
      else DiagramState.Pending
    val approvalPaper = approval.querySelector(".document-sheet")
    val proposalPaper = artifact.querySelector(".document-sheet")
    val links =
      if frame.kind == SceneKind.Transfer then Vector.empty
      else
        val common = Vector(
          ConnectionLayer.Arrow(
            "bank",
            people.children(0).querySelector("strong"),
            approvalPaper,
            DiagramState.Pending,
            false
          ),
          ConnectionLayer.Arrow("approval", approvalPaper, person(1), approvalState),
          ConnectionLayer.Arrow("proposal", person(1), proposalPaper, status(2))
        )
        if frame.kind == SceneKind.Purchase then
          common ++ Vector(
            ConnectionLayer.Arrow("relay", proposalPaper, person(2), status(3)),
            ConnectionLayer.Arrow("receipt", person(2), person(3), status(4))
          )
        else common :+ ConnectionLayer.Arrow("receipt", proposalPaper, person(2), status(4))
    connections.render(links)
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

  @JSExportTopLevel("pruneHarmoniaScenes")
  def prune(): Unit = views.keys.filterNot(dom.document.contains).toVector.foreach { root =>
    views.remove(root).foreach(_.dispose())
  }

  /** JSON ends at this typed presentation boundary. */
  @JSExportTopLevel("renderHarmoniaScene")
  def renderJson(root: dom.HTMLElement, json: String): Unit =
    io.circe.parser.decode[SceneFrame](json) match
      case Right(frame) =>
        views.getOrElseUpdate(root, new SceneView(root)).render(frame)
      case Left(_) =>
        root.textContent = "This scene could not be read. The recorded evidence remains available."
        root.setAttribute("role", "alert")

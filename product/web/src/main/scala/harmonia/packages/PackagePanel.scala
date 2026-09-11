package harmonia.packages

/** @module.slice
  *   packages
  * @module.role
  *   Inspect, then compile
  * @module.summary
  *   One package journey separates its origin, reviewed mapping, compilation and live availability.
  */

import cats.effect.{IO, Resource}
import cats.effect.std.Dispatcher
import harmonia.ui.Elements.*
import harmonia.live.LiveApi
import harmonia.scene.{WorkflowDiagram, WorkflowDiagramView, DiagramNode, DiagramEdge, DiagramState}
import io.circe.Json
import org.scalajs.dom
import scala.concurrent.duration.*

/** Each page presents one package and at most two possible actions. */
final class PackagePanel(capability: String, dispatcher: Dispatcher[IO]):
  private enum Page:
    case Start, Input, Upload, Source
    case Inspect(id: String)
  private val root = element("section", "live-panel"); root.id = "package-builder"
  private var initialized = false
  private var busy = false
  private var message = ""
  private var snapshot = PackageState.empty
  private var page: Page = Page.Start
  private var sourceIndex = 0
  private var diagram: Option[WorkflowDiagramView] = None
  private val file = element("input").asInstanceOf[dom.html.Input]
  file.id = "builder-file"; file.`type` = "file"; file.accept = ".dar"
  private def readPage(): Unit =
    val query = new dom.URLSearchParams(dom.window.location.search)
    page = Option(query.get("package"))
      .map(Page.Inspect(_))
      .getOrElse(
        Option(query.get("package-step"))
          .flatMap(v =>
            Vector(Page.Start, Page.Input, Page.Upload, Page.Source).find(_.toString == v)
          )
          .getOrElse(Page.Start)
      )
    sourceIndex =
      Option(query.get("source-index")).flatMap(_.toIntOption).filter(_ >= 0).getOrElse(0)
  readPage()
  dom.window.addEventListener("popstate", (_: dom.Event) => { readPage(); draw() })
  private def go(next: Page, replace: Boolean = false): Unit =
    page = next
    val url = new dom.URL(dom.window.location.href)
    url.searchParams.delete("package"); url.searchParams.set("package-step", page.toString)
    url.searchParams.set("source-index", sourceIndex.toString)
    page match
      case Page.Inspect(id) =>
        url.searchParams.set("package", id); url.searchParams.delete("package-step")
      case _ => ()
    if replace then dom.window.history.replaceState(null, "", url.toString)
    else if url.toString != dom.window.location.href then
      dom.window.history.pushState(null, "", url.toString)
    draw()
  def render(): dom.HTMLElement =
    if !initialized then
      initialized = true
      run("Reading package inputs", api("GET", "/api/builder"), _ => page, replace = true)
    root
  private def api(method: String, path: String, body: Option[Json] = None): IO[Json] =
    LiveApi.request(capability, method, path, body, deadline = 100.seconds)
  private def run(
      label: String,
      operation: IO[Json],
      next: PackageState => Page,
      replace: Boolean = false
  ): Unit =
    if !busy then
      busy = true; message = label; draw()
      dispatcher.unsafeRunAndForget(
        operation.flatMap(j => IO.fromEither(j.as[PackageState])).attempt.flatMap { result =>
          IO {
            busy = false
            result match
              case Right(value) => snapshot = value; message = ""; go(next(value), replace)
              case Left(error)  => message = error.getMessage; draw()
          }
        }
      )
  private def latest(value: PackageState): Page =
    value.inputs.lastOption.map(p => Page.Inspect(p.id)).getOrElse(Page.Start)
  private def draw(): Unit =
    diagram.foreach(_.dispose()); diagram = None; root.textContent = ""
    append(root, element("p", "eyebrow", "Application integration"))
    val status = element("p", "live-connection", message); status.id = "builder-status";
    status.setAttribute("role", "status")
    if message.nonEmpty then append(root, status)
    if busy then append(root, element("h1", text = "Working on the package…"))
    else
      page match
        case Page.Start if snapshot.inputs.nonEmpty =>
          append(
            root,
            element("h1", text = "Continue an integration."),
            button("Review the latest package →", "primary", "builder-latest")(
              go(latest(snapshot))
            ),
            button("Inspect another package →", "secondary", "builder-another")(go(Page.Input))
          )
        case Page.Start | Page.Input =>
          append(
            root,
            element("h1", text = "Where is the application?"),
            element(
              "p",
              text = "Inspect the compiled DAR before deciding how an action can participate."
            ),
            button("Upload a local DAR", "primary", "builder-local")(go(Page.Upload)),
            button("Use a verified source", "secondary", "builder-remote")(go(Page.Source))
          )
        case Page.Upload =>
          val label = element("label", text = "Choose a DAR, up to 8 MiB.");
          label.setAttribute("for", "builder-file")
          append(
            root,
            element("h1", text = "Inspect your application."),
            label,
            file,
            button("Inspect this DAR →", "primary", "builder-upload") {
              Option(file.files).filter(_.length > 0).map(_(0)) match
                case Some(selected) if selected.size <= 8 * 1024 * 1024 =>
                  run(
                    "Inspecting the uploaded package",
                    LiveApi.request(
                      capability,
                      "POST",
                      "/api/builder/upload",
                      None,
                      Some(selected),
                      100.seconds
                    ),
                    latest
                  )
                case _ => message = "Choose a DAR no larger than 8 MiB."; draw()
            }
          )
        case Page.Source =>
          val sources = Vector(
            "participant" -> "The local participant’s legacy application"
          ) ++ snapshot.sources.map(s => s.id -> s.source)
          sourceIndex = math.min(sourceIndex, sources.size - 1)
          val (id, label) = sources(sourceIndex)
          append(
            root,
            element("h1", text = label),
            element(
              "p",
              text =
                s"Verified source ${sourceIndex + 1} of ${sources.size}. The operator supplies this source; a business session receives no administrator access."
            ),
            button("Retrieve and inspect →", "primary", "builder-retrieve") {
              run(
                "Retrieving the verified package",
                api(
                  "POST",
                  "/api/builder/retrieve",
                  Some(Json.obj("source" -> Json.fromString(id)))
                ),
                latest
              )
            },
            button(
              if sourceIndex + 1 < sources.size then "Next source →" else "Upload a DAR instead →",
              "secondary",
              "builder-next-source"
            ) {
              if sourceIndex + 1 < sources.size then { sourceIndex += 1; go(Page.Source) }
              else go(Page.Upload)
            }
          )
        case Page.Inspect(id) =>
          snapshot.inputs.find(_.id == id) match
            case None =>
              append(
                root,
                element("h1", text = "This package is not in the current workspace."),
                button("Refresh the workspace", "primary", "builder-refresh")(
                  run("Reading package inputs", api("GET", "/api/builder"), _ => Page.Inspect(id))
                ),
                button("Choose an input →", "secondary", "builder-another")(go(Page.Input))
              )
            case Some(value) =>
              append(
                root,
                element("h1", text = value.matchedSource.getOrElse("Inspected application")),
                element("p", text = value.diagnostic)
              )
              val actions = element("nav", "package-actions"); append(root, actions)
              val canvas = element("div"); append(root, canvas)
              val renderer = new WorkflowDiagramView(canvas); diagram = Some(renderer)
              val nodes = Vector(
                DiagramNode(
                  "input",
                  "Inspect the DAR",
                  "Compiled package identity",
                  "Application",
                  DiagramState.Complete
                ),
                DiagramNode(
                  "mapping",
                  "Review the mapping",
                  value.diagnostic,
                  "Binding",
                  if value.matchedSource.isDefined then DiagramState.Complete
                  else DiagramState.Refused
                ),
                DiagramNode(
                  "compile",
                  "Compile the adapter",
                  "Portable project",
                  "Daml compiler",
                  if value.compiled then DiagramState.Complete
                  else if value.canGenerate then DiagramState.Current
                  else DiagramState.Pending
                ),
                DiagramNode(
                  "register",
                  "Check live availability",
                  "This sandbox already registers supported actions independently of this download",
                  "Live workspace",
                  if value.availableLive then DiagramState.Complete else DiagramState.Pending
                )
              )
              renderer.render(
                WorkflowDiagram(
                  "From application identity to usable integration.",
                  "Compilation and live availability are separate observed facts.",
                  nodes,
                  Vector(("input", "mapping"), ("mapping", "compile"), ("mapping", "register"))
                    .map((from, to) =>
                      DiagramEdge(
                        from,
                        to,
                        if nodes.find(_.id == to).exists(_.state == DiagramState.Complete) then
                          DiagramState.Complete
                        else DiagramState.Pending
                      )
                    )
                )
              )
              if value.compiled then
                append(
                  actions,
                  button("Download the compiled project →", "primary", "builder-download-" + id)(
                    download(id)
                  )
                )
              else if value.canGenerate then
                append(
                  actions,
                  button("Generate and compile →", "primary", "builder-generate-" + id) {
                    run(
                      "Generating and compiling the reviewed mapping",
                      api(
                        "POST",
                        "/api/builder/generate",
                        Some(Json.obj("id" -> Json.fromString(id)))
                      ),
                      _ => Page.Inspect(id)
                    )
                  }
                )
              append(
                actions,
                button("Choose another input →", "secondary", "builder-another")(go(Page.Input))
              )
              append(
                root,
                element(
                  "p",
                  "live-footnote",
                  s"Daml-LF ${value.lf} · Origin: ${value.origin}\nSHA-256: ${value.sha256}\nPackage: ${value.packageId}"
                )
              )

  private def download(id: String): Unit = dispatcher.unsafeRunAndForget(
    LiveApi
      .file(capability, "/api/builder/project/" + id)
      .flatMap { blob =>
        Resource
          .make(IO(dom.URL.createObjectURL(blob)))(url => IO(dom.URL.revokeObjectURL(url)))
          .use { url =>
            IO {
              val a = element("a").asInstanceOf[dom.html.Anchor]; a.href = url;
              a.download = "harmonia-generated-project.zip"; a.click()
            } *> IO.sleep(1.second)
          }
      }
      .handleErrorWith(error => IO { message = error.getMessage; draw() })
  )

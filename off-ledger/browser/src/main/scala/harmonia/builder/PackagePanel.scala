package harmonia.builder

import cats.effect.{IO, Resource}
import cats.effect.std.Dispatcher
import harmonia.book.Elements.*
import harmonia.live.LiveApi
import io.circe.Json
import org.scalajs.dom
import scala.concurrent.duration.*

/** Package operations have their own bounded lifetime and no ledger command authority. */
final class PackagePanel(capability: String, dispatcher: Dispatcher[IO]):
  private val root = element("section", "live-panel")
  root.id = "package-builder"
  private var initialized = false
  private var busy = false
  private var message = ""
  private var snapshot = Json.obj()

  def render(): dom.HTMLElement =
    if !initialized then
      initialized = true
      run("Reading package sources", api("GET", "/api/builder"))
    root

  private def api(method: String, path: String, input: Option[Json] = None): IO[Json] =
    LiveApi.request(capability, method, path, input, deadline = 100.seconds)

  private def run(label: String, operation: IO[Json]): Unit =
    if !busy then
      busy = true; message = label; draw()
      dispatcher.unsafeRunAndForget(
        operation.attempt.flatMap(result =>
          IO {
            busy = false
            result match
              case Right(value) => snapshot = value; message = "Package information refreshed"
              case Left(error)  => message = error.getMessage
            draw()
          }
        )
      )

  private def draw(): Unit =
    root.textContent = ""
    append(
      root,
      element("h2", text = "Bring an application package"),
      element(
        "p",
        text =
          "Inspect a DAR, check its identity, and generate a portable adapter project for a reviewed mapping. Newly generated actions need typed registration and a fresh evaluation before they can run here."
      )
    )
    val status = element("p", "live-connection", (if busy then "Working · " else "") + message)
    status.id = "builder-status"; status.setAttribute("role", "status"); append(root, status)
    val refresh = button("Refresh package inputs", "secondary", "builder-refresh") {
      run("Reading current package inputs", api("GET", "/api/builder"))
    }
    refresh.disabled = busy
    append(root, refresh)
    val controls = element("div", "composition-fields")
    val local = element("div")
    val label = element("label", text = "Local DAR · at most 8 MiB");
    label.setAttribute("for", "builder-file")
    val file = element("input").asInstanceOf[dom.html.Input]
    file.id = "builder-file"; file.`type` = "file"; file.accept = ".dar"; file.disabled = busy
    val upload = button("Inspect selected DAR", "primary", "builder-upload") {
      Option(file.files).filter(_.length > 0) match
        case None => message = "Choose a local DAR file first"; draw()
        case Some(files) =>
          val selected = files(0)
          if selected.size > 8 * 1024 * 1024 then
            message = "Upload a DAR no larger than 8 MiB"; draw()
          else
            run(
              "Inspecting uploaded package",
              LiveApi.request(
                capability,
                "POST",
                "/api/builder/upload",
                None,
                Some(selected),
                100.seconds
              )
            )
    }
    upload.disabled = busy || snapshot.hcursor.get[Int]("remaining").contains(0)
    append(local, label, file, upload)
    val remote = element("div")
    val caption = element("label", text = "Verified source");
    caption.setAttribute("for", "builder-source")
    val select = element("select").asInstanceOf[dom.html.Select]; select.id = "builder-source";
    select.disabled = busy
    val options =
      Vector("participant" -> "Local participant · legacy DAR export") ++ snapshot.hcursor
        .get[Vector[Json]]("sources")
        .getOrElse(Vector.empty)
        .map(value =>
          value.hcursor.get[String]("id").getOrElse("") -> value.hcursor
            .get[String]("id")
            .getOrElse("")
        )
    options.foreach { (id, title) =>
      val option = element("option", text = title).asInstanceOf[dom.html.Option]; option.value = id;
      append(select, option)
    }
    val retrieve = button("Retrieve and inspect", "primary", "builder-retrieve") {
      val source = select.value
      run(
        "Retrieving verified package",
        api("POST", "/api/builder/retrieve", Some(Json.obj("source" -> Json.fromString(source))))
      )
    }
    retrieve.disabled = busy || snapshot.hcursor.get[Int]("remaining").contains(0)
    append(remote, caption, select, retrieve); append(controls, local, remote);
    append(root, controls)
    append(
      root,
      element(
        "p",
        text =
          s"${snapshot.hcursor.get[Int]("remaining").getOrElse(8)} input slots remain. Participant exports are prepared by the local operator; business sessions do not gain administrator access."
      )
    )
    snapshot.hcursor.get[Vector[Json]]("inputs").getOrElse(Vector.empty).reverse.foreach { input =>
      val value = input.hcursor
      val id = value.get[String]("id").getOrElse("")
      val card = element("article", "composition-record")
      append(
        card,
        element("h3", text = value.get[String]("matched_source").getOrElse("Inspected package")),
        element("p", text = value.get[String]("diagnostic").getOrElse("")),
        element("p", text = "Daml-LF " + value.get[String]("lf").getOrElse("unknown"))
      )
      val details = element("details")
      append(details, element("summary", text = "Package identity and origin"))
      Vector("sha256", "package_id", "origin").foreach { key =>
        append(details, element("p", text = s"$key: ${value.get[String](key).getOrElse("")}"))
      }
      append(card, details)
      if value.get[Boolean]("compiled").contains(true) then
        append(
          card,
          element("p", text = "Adapter library and example compiled successfully."),
          button("Download compiled project", "primary", "builder-download-" + id)(download(id))
        )
      else if value.get[Boolean]("can_generate").contains(true) then
        val generate = button("Generate and compile project", "primary", "builder-generate-" + id) {
          run(
            "Generating and compiling the reviewed mapping",
            api("POST", "/api/builder/generate", Some(Json.obj("id" -> Json.fromString(id))))
          )
        }
        generate.disabled = busy; append(card, generate)
      append(root, card)
    }

  private def download(id: String): Unit = dispatcher.unsafeRunAndForget(
    LiveApi
      .file(capability, "/api/builder/project/" + id)
      .flatMap { blob =>
        Resource
          .make(IO(dom.URL.createObjectURL(blob)))(url => IO(dom.URL.revokeObjectURL(url)))
          .use { url =>
            IO {
              val anchor = element("a").asInstanceOf[dom.html.Anchor]
              anchor.href = url; anchor.download = "harmonia-generated-project.zip"; anchor.click()
            } *> IO.sleep(1.second)
          }
      }
      .handleErrorWith(error => IO { message = error.getMessage; draw() })
  )

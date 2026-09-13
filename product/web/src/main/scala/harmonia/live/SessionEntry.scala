package harmonia.live

import cats.effect.IO
import harmonia.ui.Elements.*
import org.scalajs.dom

/** Local demonstration roles are supplied by the sandbox, never pasted by a reader. */
private[live] object SessionEntry:
  private val roles = Set("bank", "buyer", "reviewer")
  private def actor =
    Option(new dom.URLSearchParams(dom.window.location.search).get("actor")).filter(roles)

  def home: Boolean =
    val query = new dom.URLSearchParams(dom.window.location.search)
    !query.has("view") && !query.has("actor") && !dom.window.location.hash.startsWith("#session=")

  def access: IO[String] =
    LiveApi.request("", "GET", "/api/entry", None).attempt.flatMap {
      case Right(mode) if mode.hcursor.get[Boolean]("sandbox").contains(true) =>
        actor match
          case Some(name) =>
            LiveApi
              .request("", "POST", "/api/sandbox/entry/" + name, None)
              .flatMap(json => IO.fromEither(json.hcursor.get[String]("capability")))
              .flatTap(key =>
                IO {
                  dom.window.sessionStorage.setItem("harmonia-live", key)
                  dom.window.sessionStorage.setItem("harmonia-actor", name)
                }
              )
              .handleErrorWith(_ => IO(render(sandbox = false)) *> IO.never)
          case None => IO(render(sandbox = true)) *> IO.never
      case _ => IO(render(sandbox = false)) *> IO.never
    }

  def changedRole: Boolean =
    actor.exists(name => Option(dom.window.sessionStorage.getItem("harmonia-actor")) != Some(name))

  def expired: IO[Unit] =
    IO(dom.window.sessionStorage.removeItem("harmonia-live")) *>
      LiveApi
        .request("", "GET", "/api/entry", None)
        .attempt
        .flatMap(result =>
          IO(render(result.toOption.exists(_.hcursor.get[Boolean]("sandbox").contains(true))))
        )

  private def render(sandbox: Boolean): Unit =
    val main = element("main", "live-main session-entry"); main.id = "main"
    val header = element("header", "reference-header")
    append(header, element("span", "reference-brand", "Harmonia"))
    val content = element("section", "session-entry-content")
    append(
      content,
      element("p", "eyebrow", if sandbox then "Try the local sandbox" else "Workspace"),
      element(
        "h1",
        text =
          if sandbox then "One decision. A shared next step." else "The workspace is unavailable."
      ),
      element(
        "p",
        text =
          if sandbox then "Northbank reviews the financing. Alice carries the result forward."
          else "Reconnect when the workspace is running."
      )
    )
    if sandbox then
      val illustration = element("div", "entry-illustration")
      illustration.setAttribute("role", "img")
      illustration.setAttribute("aria-label", "Alice and a banker at their separate workspaces")
      val choices = element("nav", "entry-choices")
      choices.setAttribute("aria-label", "Choose your workspace")
      Vector("bank" -> "Open Northbank’s workspace", "buyer" -> "Open Alice’s workspace").foreach {
        (name, label) =>
          val query = new dom.URLSearchParams(dom.window.location.search)
          query.set("actor", name)
          append(choices, link(label + " →", "?" + query.toString))
      }
      append(content, illustration, choices)
    else
      append(content, button("Reconnect", "primary", "entry-retry")(dom.window.location.reload()))
    append(main, header, content)
    val root = dom.document.getElementById("app"); root.textContent = ""; append(root, main)

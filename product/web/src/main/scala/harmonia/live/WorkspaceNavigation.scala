package harmonia.live

import org.scalajs.dom

/** Each task has its own address. Appearance remains reproducible without a control panel. */
final class WorkspaceNavigation(changed: () => Unit):
  private def render(): Unit =
    val query = new dom.URLSearchParams(dom.window.location.search)
    Vector(
      "theme" -> (Set("light", "dark", "paper"), "light"),
      "text" -> (Set("compact", "standard", "large"), "standard")
    ).foreach { (key, options) =>
      dom.document.documentElement.setAttribute(
        "data-" + key,
        Option(query.get(key)).filter(options._1).getOrElse(options._2)
      )
    }
    changed()
  dom.window.addEventListener("popstate", (_: dom.Event) => render())
  dom.document.addEventListener(
    "click",
    (event: dom.MouseEvent) =>
      val target = event.target.asInstanceOf[dom.Element].closest("a[href]")
      if target != null && !event.defaultPrevented && event.button == 0 &&
        !event.metaKey && !event.ctrlKey && !event.shiftKey && !event.altKey &&
        !target.hasAttribute("download") && !target.hasAttribute("target")
      then
        val destination = new dom.URL(target.asInstanceOf[dom.html.Anchor].href)
        if destination.origin == dom.window.location.origin &&
          destination.pathname == dom.window.location.pathname &&
          destination.searchParams.has("view")
        then
          event.preventDefault()
          dom.window.history.pushState(null, "", destination.toString)
          dom.window.dispatchEvent(new dom.Event("popstate"))
  )
  render()

object WorkspaceNavigation:
  def page: String =
    Option(new dom.URLSearchParams(dom.window.location.search).get("view"))
      .filter(Set("financing", "composer", "packages", "evidence", "workspace"))
      .getOrElse("financing")

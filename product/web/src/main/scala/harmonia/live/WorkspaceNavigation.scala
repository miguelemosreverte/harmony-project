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
  render()

object WorkspaceNavigation:
  def page: String =
    Option(new dom.URLSearchParams(dom.window.location.search).get("view"))
      .filter(Set("financing", "composer", "packages", "evidence", "workspace"))
      .getOrElse("financing")

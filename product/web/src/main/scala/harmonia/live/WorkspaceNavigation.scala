package harmonia.live

import harmonia.ui.Elements.*
import org.scalajs.dom

/** Navigation is URL state. Credentials remain confined to the participant session. */
final class WorkspaceNavigation(nav: dom.HTMLElement, changed: () => Unit):
  private val themes = Vector("light", "dark", "paper")
  private val sizes = Vector("compact", "standard", "large")
  private val appearance = element("details", "workspace-appearance")
  private val appearanceToggle = element("summary", "reference-appearance")
  appearanceToggle.setAttribute("aria-label", "Appearance")
  appearanceToggle.innerHTML =
    """<svg viewBox="0 0 32 32" aria-hidden="true"><circle cx="16" cy="16" r="6.5"/><path d="M16 1v5m0 20v5M1 16h5m20 0h5M5.4 5.4l3.5 3.5m14.2 14.2 3.5 3.5M5.4 26.6l3.5-3.5M23.1 8.9l3.5-3.5"/></svg>"""
  append(appearance, appearanceToggle)
  Vector("theme" -> themes, "text" -> sizes).foreach { (key, values) =>
    val label = element("label", text = if key == "theme" then "Color " else "Text size ")
    val select = element("select").asInstanceOf[dom.html.Select]
    select.id = "workspace-" + key
    values.foreach { value =>
      val option = element("option", text = value.capitalize).asInstanceOf[dom.html.Option]
      option.value = value; append(select, option)
    }
    select.onchange = _ =>
      val url = new dom.URL(dom.window.location.href)
      url.searchParams.set(key, select.value)
      dom.window.history.pushState(null, "", url.toString)
      render()
    append(label, select); append(appearance, label)
  }
  append(nav, appearance)
  nav.addEventListener(
    "click",
    (event: dom.MouseEvent) =>
      Option(event.target)
        .collect { case node: dom.Element => node.closest("a") }
        .filter(_ != null)
        .foreach { node =>
          val anchor = node.asInstanceOf[dom.html.Anchor]
          if anchor
              .getAttribute("href")
              .startsWith("?view=") && !event.metaKey && !event.ctrlKey && !event.shiftKey
          then
            event.preventDefault()
            val url = new dom.URL(dom.window.location.href)
            url.searchParams.set("view", new dom.URL(anchor.href).searchParams.get("view"))
            dom.window.history.pushState(null, "", url.toString)
            render()
        }
  )
  dom.window.addEventListener("popstate", (_: dom.Event) => render())
  render()

  private def render(): Unit =
    val query = new dom.URLSearchParams(dom.window.location.search)
    Vector("theme" -> (themes, "light"), "text" -> (sizes, "standard")).foreach { (key, options) =>
      val value = Option(query.get(key)).filter(options._1.contains).getOrElse(options._2)
      dom.document.documentElement.setAttribute("data-" + key, value)
      dom.document.getElementById("workspace-" + key) match
        case select: dom.html.Select => select.value = value
        case _                       => ()
    }
    val anchors = nav.querySelectorAll("a[href^='?view=']")
    (0 until anchors.length).foreach { i =>
      val anchor = anchors(i).asInstanceOf[dom.html.Anchor]
      anchor.setAttribute(
        "aria-current",
        if new dom.URL(anchor.href).searchParams.get("view") == WorkspaceNavigation.page then "page"
        else "false"
      )
    }
    changed()

object WorkspaceNavigation:
  def navigate(page: String): Unit =
    val url = new dom.URL(dom.window.location.href)
    url.searchParams.set("view", page)
    dom.window.history.pushState(null, "", url.toString)
    dom.window.dispatchEvent(new dom.Event("popstate"))

  def page: String =
    Option(new dom.URLSearchParams(dom.window.location.search).get("view"))
      .filter(Set("financing", "composer", "packages", "evidence"))
      .getOrElse("financing")

package harmonia.ui

import org.scalajs.dom

private[harmonia] object Elements:
  def element(tag: String, css: String = "", text: String = ""): dom.HTMLElement =
    val node = dom.document.createElement(tag).asInstanceOf[dom.HTMLElement]
    node.className = css
    if text.nonEmpty then node.textContent = text
    node

  def append(parent: dom.Element, children: dom.Node*): Unit = children.foreach(parent.appendChild)

  def link(label: String, href: String): dom.HTMLElement =
    val node = element("a", text = label)
    // Task links retain the sandbox character when copied or opened in a new tab.
    val destination = new dom.URL(href, dom.window.location.href)
    if href.startsWith("?") && destination.searchParams.has("view") then
      Option(new dom.URLSearchParams(dom.window.location.search).get("actor"))
        .foreach(actor =>
          if !destination.searchParams.has("actor") then
            destination.searchParams.set("actor", actor)
        )
    node.setAttribute("href", if href.startsWith("?") then destination.search else href)
    node

  def button(label: String, css: String, id: String)(action: => Unit): dom.html.Button =
    val node = element("button", css, label).asInstanceOf[dom.html.Button]
    node.id = id
    node.`type` = "button"
    node.onclick = _ => action
    node

  def hide(node: dom.HTMLElement, hidden: Boolean): Unit =
    if hidden then node.setAttribute("hidden", "") else node.removeAttribute("hidden")

package harmonia.book.ui

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
    node.setAttribute("href", href)
    node

  def button(label: String, css: String, id: String)(action: => Unit): dom.html.Button =
    val node = element("button", css, label).asInstanceOf[dom.html.Button]
    node.id = id
    node.`type` = "button"
    node.onclick = _ => action
    node

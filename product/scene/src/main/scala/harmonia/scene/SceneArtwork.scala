package harmonia.scene

import org.scalajs.dom

/** Reference illustrations are sprites; small interface marks remain crisp native SVG. */
object SceneArtwork:
  enum Illustration:
    case Bank, Alice, Ben, Sofia, House

  def illustration(value: Illustration): dom.HTMLElement =
    val node = dom.document.createElement("span").asInstanceOf[dom.HTMLElement]
    node.className = "scene-illustration illustration-" + value.toString.toLowerCase
    node.setAttribute("aria-hidden", "true")
    node

  enum Mark:
    case Bank, House, Lock

  def mark(name: Mark): dom.Element =
    val svg = dom.document.createElementNS("http://www.w3.org/2000/svg", "svg")
    svg.setAttribute("viewBox", "0 0 32 32")
    svg.setAttribute("aria-hidden", "true")
    svg.setAttribute("class", "scene-mark")
    val paths = name match
      case Mark.Bank  => "M2 11 16 1 30 11v3H2zm3 5h5v11H5zm9 0h4v11h-4zm8 0h5v11h-5zM2 29h28v3H2z"
      case Mark.House => "M1 14 16 1l15 13-3 4-2-2v15H6V16l-2 2zm12 4v9h7v-9z"
      case Mark.Lock =>
        "M10 13V9a6 6 0 0 1 12 0v4h2a2 2 0 0 1 2 2v13H6V15a2 2 0 0 1 2-2zm3 0h6V9a3 3 0 0 0-6 0zm2 7v5h2v-5z"
    val path = dom.document.createElementNS("http://www.w3.org/2000/svg", "path")
    path.setAttribute("d", paths)
    path.setAttribute("fill-rule", "evenodd")
    svg.appendChild(path)
    svg

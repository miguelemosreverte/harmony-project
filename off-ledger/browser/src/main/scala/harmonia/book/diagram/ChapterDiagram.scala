package harmonia.book.diagram

import org.scalajs.dom
import scala.collection.mutable

/** Renders the simple, authored left-to-right flowcharts used in the book chapters. */
object ChapterDiagram:
  private val edge = raw"\s*(\w+)(?:\[([^\]]+)\])?\s*-->\s*(\w+)(?:\[([^\]]+)\])?\s*".r

  def render(chapter: dom.Element): Unit =
    val blocks = chapter.querySelectorAll("code.language-mermaid")
    (0 until blocks.length).foreach { index =>
      val code = blocks(index).asInstanceOf[dom.Element]
      diagram(code.textContent).foreach(svg =>
        code.parentNode.parentNode.replaceChild(svg, code.parentNode)
      )
    }

  private def diagram(source: String): Option[dom.Element] =
    val labels = mutable.LinkedHashMap.empty[String, String]
    val edges = source.linesIterator.drop(1).filter(_.trim.nonEmpty).toVector.map {
      case edge(from, fromLabel, to, toLabel) =>
        labels.update(from, Option(fromLabel).getOrElse(labels.getOrElse(from, from)))
        labels.update(to, Option(toLabel).getOrElse(labels.getOrElse(to, to)))
        Some(from -> to)
      case _ => None
    }
    if edges.isEmpty || edges.exists(_.isEmpty) then None
    else
      val links = edges.flatten
      val depths = mutable.Map.from(labels.keys.map(_ -> 0))
      (0 until labels.size).foreach { _ =>
        links.foreach { (from, to) => depths.update(to, math.max(depths(to), depths(from) + 1)) }
      }
      if depths.values.exists(_ >= labels.size) then None
      else
        val levels = labels.keys.toVector.groupBy(depths)
        val width = (depths.values.max + 1) * 220 + 20
        val height = levels.values.map(_.size).max * 100 + 20
        val positions = levels.toVector.flatMap { (level, nodes) =>
          nodes.zipWithIndex.map((id, row) => id -> (level * 220 + 20, row * 100 + 20))
        }.toMap
        val wrapper = dom.document.createElement("div")
        wrapper.setAttribute("class", "chapter-diagram")
        val svg = node(
          "svg",
          "width" -> width.toString,
          "height" -> height.toString,
          "viewBox" -> s"0 0 $width $height",
          "role" -> "img",
          "aria-label" -> labels.values.mkString("Workflow: ", "; ", "")
        )
        links.foreach { (from, to) =>
          val (x1, y1) = positions(from)
          val (x2, y2) = positions(to)
          svg.appendChild(
            node(
              "path",
              "d" -> s"M ${x1 + 170} ${y1 + 35} L ${x2 - 7} ${y2 + 35} m -7 -5 l 7 5 l -7 5",
              "fill" -> "none",
              "stroke" -> "#236b55",
              "stroke-width" -> "1.5"
            )
          )
        }
        labels.foreach { (id, label) =>
          val (x, y) = positions(id)
          svg.appendChild(
            node(
              "rect",
              "x" -> x.toString,
              "y" -> y.toString,
              "width" -> "170",
              "height" -> "70",
              "rx" -> "6",
              "fill" -> "#e8f1e8",
              "stroke" -> "#b4cabc"
            )
          )
          val lines = label.split(" ").foldLeft(Vector("")) { (lines, word) =>
            if (lines.last + " " + word).trim.length <= 22 then
              lines.init :+ (lines.last + " " + word).trim
            else lines :+ word
          }
          lines.zipWithIndex.foreach { (line, index) =>
            val text = node(
              "text",
              "x" -> (x + 85).toString,
              "y" -> (y + 39 - (lines.size - 1) * 8 + index * 16).toString,
              "text-anchor" -> "middle",
              "font-family" -> "system-ui, sans-serif",
              "font-size" -> "12",
              "fill" -> "#253934"
            )
            text.textContent = line
            svg.appendChild(text)
          }
        }
        wrapper.appendChild(svg)
        Some(wrapper)

  private def node(name: String, attributes: (String, String)*): dom.Element =
    val value = dom.document.createElementNS("http://www.w3.org/2000/svg", name)
    attributes.foreach((key, content) => value.setAttribute(key, content))
    value

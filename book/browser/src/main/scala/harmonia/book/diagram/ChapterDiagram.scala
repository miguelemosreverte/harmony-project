package harmonia.book.diagram

import harmonia.scene.*
import org.scalajs.dom
import scala.collection.mutable

/** Converts the book's authored flowchart subset into the shared typed diagram. Unsupported syntax
  * stays visible as source; it never becomes an invented graph.
  */
object ChapterDiagram:
  private val edge = raw"\s*(\w+)(?:\[([^\]]+)\])?\s*-->\s*(\w+)(?:\[([^\]]+)\])?\s*".r

  def render(chapter: dom.Element): Vector[WorkflowDiagramView] =
    val blocks = chapter.querySelectorAll("code.language-mermaid")
    (0 until blocks.length).toVector.flatMap { index =>
      val code = blocks(index).asInstanceOf[dom.Element]
      parse(code.textContent).map { value =>
        val root = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
        root.className = "chapter-diagram"
        code.parentNode.parentNode.replaceChild(root, code.parentNode)
        val view = new WorkflowDiagramView(root)
        view.render(value)
        view
      }
    }

  def parse(source: String): Option[WorkflowDiagram] =
    val labels = mutable.LinkedHashMap.empty[String, String]
    val edges = source.linesIterator.drop(1).filter(_.trim.nonEmpty).toVector.map {
      case edge(from, fromLabel, to, toLabel) =>
        labels.update(from, Option(fromLabel).getOrElse(labels.getOrElse(from, from)))
        labels.update(to, Option(toLabel).getOrElse(labels.getOrElse(to, to)))
        Some(DiagramEdge(from, to, DiagramState.Pending))
      case _ => None
    }
    val graph = WorkflowDiagram(
      "Follow the handoff.",
      "Authored explanation. The recorded observations and golden comparison are separate evidence.",
      labels.toVector.map((id, label) =>
        DiagramNode(id, label, label, "Chapter explanation", DiagramState.Pending)
      ),
      edges.flatten
    )
    Option.when(
      source.linesIterator.nextOption().exists(_.trim == "flowchart LR") && edges.nonEmpty && edges
        .forall(_.nonEmpty) && graph.layers.isRight
    )(graph)

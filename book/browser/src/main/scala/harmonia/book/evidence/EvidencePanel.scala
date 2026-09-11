package harmonia.book.evidence

import harmonia.book.*
import harmonia.book.ui.Elements.*
import io.circe.Json
import org.scalajs.dom
import EvidenceValues.*

object EvidencePanel:
  def render(story: RecordedStory): dom.HTMLElement =
    val panel = element("section", "panel")
    val head = element("div", "panel-head")
    append(head, element("h2", text = "The evidence behind it"))
    val content = element("div", "evidence")
    append(content, element("p", text = story.description))
    val links = element("div", "link-list")
    RecordedArtifact.values.filterNot(_ == RecordedArtifact.Provenance).foreach { artifact =>
      append(links, link(artifact.label, s"evidence/${story.id}/${artifact.filename}"))
    }
    val provenance = element("div", "provenance")
    val revision = text(story.provenance, "revision").take(8)
    val dirty = story.provenance.hcursor.get[Boolean]("worktree_dirty").getOrElse(true)
    append(
      provenance,
      element(
        "p",
        text = s"Recorded ${text(story.provenance, "recorded_at")}\nSource $revision${
            if dirty then " with uncommitted changes" else ""
          }."
      ),
      link("Execution provenance →", s"evidence/${story.id}/run.json"),
      element("p", text = text(story.provenance, "topology")),
      link("Read the operation →", "source/" + story.presentation.operation)
    )
    append(content, links, provenance)
    append(panel, head, content)
    panel

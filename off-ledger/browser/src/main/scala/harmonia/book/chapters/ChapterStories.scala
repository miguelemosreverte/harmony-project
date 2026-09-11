package harmonia.book.chapters

import harmonia.book.{RecordedStory, ViewState, Elements}
import Elements.*
import org.scalajs.dom

object ChapterStories:
  def render(
      index: Int,
      stories: Vector[RecordedStory],
      navigate: ViewState => Unit
  ): dom.HTMLElement =
    val prefixes = Vector(
      Vector("financing-approved", "already-approved"),
      Vector("workflow-", "adapter-"),
      Vector("private-approval", "live-handoff"),
      Vector("sequence-", "branch-"),
      Vector("purchase-"),
      Vector("transfer-"),
      Vector("generated-"),
      Vector("composer-", "package-builder"),
      Vector("execution-boundaries", "generated-approved", "transfer-final-leg-rejected")
    )
    val selected = stories.zipWithIndex.filter((story, _) =>
      prefixes.lift(index).getOrElse(Vector.empty).exists(story.id.startsWith)
    )
    val root = element("section", "chapter-experiments")
    append(root, element("h2", text = "Try the recorded examples"))
    if selected.isEmpty then
      append(
        root,
        element(
          "p",
          text =
            "This export does not include this chapter's recordings. Build the complete evaluation bundle to include them."
        )
      )
    else
      append(
        root,
        element(
          "p",
          text =
            "Open an actual recording, select its attempts, and compare the committed expectation with the observed result."
        )
      )
      selected.foreach { (story, position) =>
        append(
          root,
          button(story.title, "button chapter-demo", "chapter-demo-" + story.id)(
            navigate(ViewState(position, 0))
          )
        )
      }
    root

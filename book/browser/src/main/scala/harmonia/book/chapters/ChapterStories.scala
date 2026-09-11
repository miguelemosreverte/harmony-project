package harmonia.book.chapters

import harmonia.book.ui.Elements
import harmonia.book.{RecordedStory, ViewState}
import Elements.*
import org.scalajs.dom
import harmonia.examples.Examples

object ChapterStories:
  def render(
      index: Int,
      stories: Vector[RecordedStory],
      navigate: ViewState => Unit
  ): dom.HTMLElement =
    val selected =
      stories.zipWithIndex.filter((story, _) => Examples.forChapter(index).contains(story.id))
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
            navigate(ViewState(position, 0, originChapter = Some(index)))
          )
        )
      }
    root

package harmonia.book

import cats.effect.IO
import harmonia.book.ui.Elements.*
import harmonia.book.reader.StoryLaboratory
import org.scalajs.dom
import scala.scalajs.js

/** Compatibility entry for exported evidence: one chapter or one observation at a time. */
final class BookView(
    stories: Vector[RecordedStory],
    chapters: Vector[BookChapter],
    navigate: ViewState => Unit
):
  private val laboratory = new StoryLaboratory(stories, chapters, navigate)
  private val root = dom.document.getElementById("app")
  private val main = element("main", "quiet-content"); main.id = "main"
  private val header = element("header", "reference-header")
  private var wasChapter = false
  root.textContent = ""; root.setAttribute("class", "reference-shell")
  append(header, element("span", "reference-brand", "Harmonia"))
  append(root, header, main)
  def render(state: ViewState): IO[Unit] = IO {
    if state.chapter.nonEmpty || wasChapter then main.textContent = ""
    wasChapter = state.chapter.nonEmpty
    state.chapter match
      case None => laboratory.render(main, state)
      case Some(index) =>
        val article = element("article", "prose"); article.innerHTML = chapters(index).html
        val links = article.querySelectorAll("a")
        (0 until links.length).foreach(i =>
          links(i).asInstanceOf[dom.Element].removeAttribute("href")
        )
        append(main, article)
        val paging = element("nav", "quiet-paging")
        append(
          paging,
          button("← Previous chapter", "", "chapter-previous") {
            if index > 0 then navigate(state.copy(chapter = Some(index - 1)))
            else dom.window.location.href = guide + "book-overview.html"
          },
          button(
            if index + 1 < chapters.size then "Next chapter →" else "Finish reading →",
            "",
            "chapter-next"
          ) {
            if index + 1 < chapters.size then navigate(state.copy(chapter = Some(index + 1)))
            else dom.window.location.href = guide + "book-overview.html"
          }
        )
        append(main, paging)
    val paging = root.querySelectorAll(".quiet-paging > button")
    (0 until paging.length).foreach { i =>
      val control = paging(i).asInstanceOf[dom.HTMLElement]
      if control.textContent != "‹" && control.textContent != "›" then
        control.setAttribute("aria-label", control.textContent)
        control.textContent = if i % 2 == 0 then "‹" else "›"
    }
    val controls = root.querySelectorAll(".quiet-paging")
    (0 until controls.length).foreach { i =>
      val control = controls(i).asInstanceOf[dom.Element]
      if state.embed then control.setAttribute("hidden", "") else control.removeAttribute("hidden")
    }
    dom.document.documentElement.setAttribute("data-theme", state.theme)
    dom.document.documentElement.setAttribute("data-text", state.text)
  }
  private def guide: String =
    val value = js.Dynamic.global.selectDynamic("HarmoniaGuide")
    if js.isUndefined(value) then "source/design/0.2/" else value.toString

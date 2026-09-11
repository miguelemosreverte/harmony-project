package harmonia.book

import harmonia.ui.Elements
import cats.effect.IO
import org.scalajs.dom
import Elements.*

final class BookView(
    stories: Vector[RecordedStory],
    chapters: Vector[BookChapter],
    navigate: ViewState => Unit,
    inspector: Inspector
):
  private val laboratory = new harmonia.book.reader.StoryLaboratory(stories, chapters, navigate)
  private var previousView: Option[ViewState] = None
  private var currentChapter: Option[Int] = None
  private var chapterScroll = Map.empty[Int, Double]
  def render(state: ViewState): IO[Unit] = IO {
    val pageChanged = previousView.exists(p => p.chapter != state.chapter || p.story != state.story)
    previousView = Some(state)
    currentChapter.foreach(i => chapterScroll = chapterScroll.updated(i, dom.window.scrollY))
    currentChapter = state.chapter
    val focused = Option(dom.document.activeElement).map(_.id).filter(_.nonEmpty)
    val root = dom.document.getElementById("app")
    root.textContent = ""
    val layout = element("div", "layout")
    val sidebar = element("aside", "sidebar")
    val brand = element("p", "brand")
    append(brand, element("span", "brand-mark"), dom.document.createTextNode("Harmonia"))
    val nav = element("nav")
    nav.setAttribute("aria-label", "Book chapters")
    append(
      nav,
      button(
        "The story laboratory",
        "nav-button" + (if state.chapter.isEmpty then " active" else ""),
        "nav-lab"
      ) { navigate(state.copy(chapter = None)) }
    )
    chapters.zipWithIndex.foreach { (chapter, index) =>
      append(
        nav,
        button(
          s"0${index + 1}  ${chapter.title}",
          "nav-button" + (if state.chapter.contains(index) then " active" else ""),
          s"nav-$index"
        ) { navigate(state.copy(chapter = Some(index))) }
      )
    }
    append(
      sidebar,
      brand,
      element("p", "strap", "An executable book"),
      element("p", "nav-label", "Read · run · understand"),
      nav,
      element(
        "p",
        "sidebar-note",
        "Every story has an input, a committed expectation, and an observed execution. Explore the evidence behind each result."
      )
    )
    val main = element("main", "main")
    main.id = "main"
    main.tabIndex = -1
    append(main, topLine())
    state.chapter match
      case Some(index) =>
        val chapter = element("article", "chapter")
        // The exporter escapes raw HTML and sanitizes URLs before this content reaches the browser.
        chapter.innerHTML = chapters(index).html
        harmonia.book.diagram.ChapterDiagram.render(chapter)
        val scrollRegions = chapter.querySelectorAll("pre, table")
        (0 until scrollRegions.length).foreach { i =>
          val region = scrollRegions(i).asInstanceOf[dom.HTMLElement]
          region.tabIndex = 0;
          region.setAttribute("aria-label", "Code or table; scroll horizontally if needed")
        }
        append(main, chapter)
        val experiments = harmonia.book.chapters.ChapterStories.render(index, stories, navigate)
        Option(chapter.querySelector("h2")) match
          case Some(firstSection) => chapter.insertBefore(experiments, firstSection)
          case None               => append(chapter, experiments)
        val paging = element("nav", "chapter-paging");
        paging.setAttribute("aria-label", "Continue reading")
        if index > 0 then
          append(
            paging,
            button("← Previous chapter", "button", "chapter-previous")(
              navigate(state.copy(chapter = Some(index - 1)))
            )
          )
        if index + 1 < chapters.size then
          append(
            paging,
            button("Next chapter →", "button primary", "chapter-next")(
              navigate(state.copy(chapter = Some(index + 1)))
            )
          )
        append(main, paging)
      case None => laboratory.render(main, state)
    append(
      main,
      element(
        "p",
        "footer",
        "Harmonia is a local reference implementation. These demonstrations use synthetic applications and recorded executions; each recording carries its own source provenance."
      )
    )
    append(layout, sidebar, main)
    append(root, layout)
    val sourceLinks = root.querySelectorAll("a[href]")
    (0 until sourceLinks.length).foreach { index =>
      val anchor = sourceLinks(index).asInstanceOf[dom.html.Anchor]
      val path = anchor.getAttribute("href")
      if path.startsWith("source/") || path.startsWith("evidence/") then
        anchor.onclick = event =>
          if !event.ctrlKey && !event.metaKey then
            event.preventDefault(); inspector.open(path, anchor.textContent)
    }
    Option(nav.querySelector(".active")).foreach(_.setAttribute("aria-current", "page"))
    Option(root.querySelector(".graph")).foreach { graphNode =>
      val graph = graphNode.asInstanceOf[dom.HTMLElement]
      Option(graph.querySelector(".selected")).foreach { selected =>
        graph.scrollLeft += selected.getBoundingClientRect().left - graph
          .getBoundingClientRect()
          .left -
          (graph.clientWidth - selected.getBoundingClientRect().width) / 2
      }
    }
    if pageChanged && state.chapter.nonEmpty then
      main.focus();
      dom.window.scrollTo(0, state.chapter.flatMap(chapterScroll.get).getOrElse(0.0).toInt)
    else if pageChanged || focused.exists(id =>
        id.startsWith("nav-") || id.startsWith("chapter-demo-")
      )
    then
      main.focus()
      main.scrollIntoView()
    else if focused.contains("return-to-chapter") then
      main.focus();
      dom.window.scrollTo(0, state.chapter.flatMap(chapterScroll.get).getOrElse(0.0).toInt)
    else
      focused
        .flatMap(id => Option(dom.document.getElementById(id)))
        .filterNot(_.hasAttribute("disabled"))
        .orElse(
          Option
            .when(focused.nonEmpty)(root.querySelector(s"#step-${state.step}"))
            .flatMap(Option(_))
        )
        .foreach(_.asInstanceOf[dom.HTMLElement].focus())
  }

  private def topLine(): dom.HTMLElement =
    val row = element("div", "topline")
    append(
      row,
      element("span", text = "APPLICATIONS, WORKING TOGETHER"),
      element("span", "badge", "●  Recorded execution evidence")
    )
    row

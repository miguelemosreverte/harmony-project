package harmonia.book

import harmonia.book.ui.Elements
import cats.effect.IO
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.URIUtils.encodeURIComponent
import Elements.*

final class BookView(
    stories: Vector[RecordedStory],
    chapters: Vector[BookChapter],
    navigate: ViewState => Unit,
    inspector: Inspector
):
  private val laboratory = new harmonia.book.reader.StoryLaboratory(stories, chapters, navigate)
  private var diagrams = Vector.empty[harmonia.scene.WorkflowDiagramView]
  private var previousView: Option[ViewState] = None
  private var currentChapter: Option[Int] = None
  private var chapterScroll = Map.empty[Int, Double]
  def render(state: ViewState): IO[Unit] = IO {
    Vector(
      "theme" -> state.theme,
      "text" -> state.text,
      "embed" -> (if state.embed then "1" else "0"),
      "presentation" -> (if state.present then "1" else "0")
    ).foreach((key, value) => dom.document.documentElement.setAttribute("data-" + key, value))
    laboratory.dispose()
    diagrams.foreach(_.dispose()); diagrams = Vector.empty
    val pageChanged = previousView.exists(p => p.chapter != state.chapter || p.story != state.story)
    val closedArtifact = previousView.flatMap(_.artifact).filter(_ => state.artifact.isEmpty)
    previousView = Some(state)
    currentChapter.foreach(i => chapterScroll = chapterScroll.updated(i, dom.window.scrollY))
    currentChapter = state.chapter
    val focused = Option(dom.document.activeElement).map(_.id).filter(_.nonEmpty)
    val root = dom.document.getElementById("app")
    root.textContent = ""
    val layout = element("div", "layout")
    val sidebar = element("details", "sidebar")
    if state.navigation then sidebar.setAttribute("open", "")
    sidebar.addEventListener(
      "toggle",
      (_: dom.Event) =>
        if sidebar.hasAttribute("open") != state.navigation then
          navigate(state.copy(navigation = sidebar.hasAttribute("open")))
    )
    append(sidebar, element("summary", text = "Book chapters and recordings"))
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
      ) { navigate(state.copy(chapter = None, navigation = false, artifact = None)) }
    )
    chapters.zipWithIndex.foreach { (chapter, index) =>
      append(
        nav,
        button(
          s"0${index + 1}  ${chapter.title}",
          "nav-button" + (if state.chapter.contains(index) then " active" else ""),
          s"nav-$index"
        ) { navigate(state.copy(chapter = Some(index), navigation = false, artifact = None)) }
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
    append(main, topLine(state))
    state.chapter match
      case Some(index) =>
        val chapter = element("article", "chapter")
        // The exporter escapes raw HTML and sanitizes URLs before this content reaches the browser.
        chapter.innerHTML = chapters(index).html
        diagrams = harmonia.book.diagram.ChapterDiagram.render(chapter)
        val scrollRegions = chapter.querySelectorAll("pre, table")
        (0 until scrollRegions.length).foreach { i =>
          val region = scrollRegions(i).asInstanceOf[dom.HTMLElement]
          region.tabIndex = 0;
          region.setAttribute("aria-label", "Code or table; scroll horizontally if needed")
        }
        append(main, chapter)
        val experiments = harmonia.book.chapters.ChapterStories.render(
          index,
          stories,
          next => navigate(next.copy(theme = state.theme, text = state.text, embed = state.embed))
        )
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
      if path.startsWith("source/") then
        val file = new dom.URL(path, "https://source.invalid/").pathname.stripPrefix("/source/")
        val atlas = js.Dynamic.global.selectDynamic("HarmoniaAtlas")
        val known = !js.isUndefined(atlas) && !js.isUndefined(atlas.files.selectDynamic(file))
        anchor.href =
          if known then
            guide + "code.html?file=" + encodeURIComponent(
              file
            ) + "&theme=" + state.theme + "&text=" + state.text
          else guide + "../../" + file
      else if path.startsWith("evidence/") then
        anchor.onclick = event =>
          if !event.ctrlKey && !event.metaKey then
            event.preventDefault();
            navigate(
              state.copy(
                artifact = RecordedArtifact.values.find(_.filename == path.split('/').last),
                evidence = true
              )
            )
    }
    Option(nav.querySelector(".active")).foreach(_.setAttribute("aria-current", "page"))
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
    state.artifact match
      case Some(artifact) =>
        inspector.show(
          s"evidence/${stories(state.story).id}/${artifact.filename}",
          artifact.label,
          () => navigate(state.copy(artifact = None))
        )
      case None =>
        inspector.hide()
        closedArtifact.foreach { artifact =>
          Option(
            root.querySelector(
              s"a[href='evidence/${stories(state.story).id}/${artifact.filename}']"
            )
          ).foreach(_.asInstanceOf[dom.HTMLElement].focus())
        }
  }

  private def guide: String =
    val value = js.Dynamic.global.selectDynamic("HarmoniaGuide")
    if js.isUndefined(value) then "source/design/0.2/" else value.toString

  private def topLine(state: ViewState): dom.HTMLElement =
    val row = element("div", "topline")
    append(
      row,
      link("Harmonia · The field guide", guide + "workflows.html"),
      element("span", "badge", "●  Recorded execution evidence")
    )
    val appearance = element("details", "laboratory-appearance")
    if state.appearance then appearance.setAttribute("open", "")
    appearance.addEventListener(
      "toggle",
      (_: dom.Event) =>
        if appearance.hasAttribute("open") != state.appearance then
          navigate(state.copy(appearance = appearance.hasAttribute("open")))
    )
    append(appearance, element("summary", text = "Appearance"))
    def choose(label: String, values: Vector[String], selected: String)(
        change: String => Unit
    ): Unit =
      val select = element("select").asInstanceOf[dom.html.Select]
      select.setAttribute("aria-label", label)
      values.foreach { value =>
        val option = element("option", text = value.capitalize).asInstanceOf[dom.html.Option]
        option.value = value; option.selected = value == selected; append(select, option)
      }
      select.onchange = _ => change(select.value)
      append(appearance, select)
    choose("Color", Vector("light", "dark", "paper"), state.theme)(value =>
      navigate(state.copy(theme = value))
    )
    choose("Text size", Vector("compact", "standard", "large"), state.text)(value =>
      navigate(state.copy(text = value))
    )
    append(
      appearance,
      button("Print / save PDF", "button", "print-reader")(dom.window.print()),
      button(
        if state.present then "Leave presentation" else "Present this story",
        "button",
        "present-reader"
      )(navigate(state.copy(present = !state.present)))
    )
    append(row, appearance)
    row

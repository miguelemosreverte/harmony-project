package harmonia.book

import cats.effect.{IO, IOApp, Resource}
import cats.effect.std.Dispatcher
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.Thenable.Implicits.*
import scala.concurrent.ExecutionContext.Implicits.global

final case class ViewState(
    story: Int,
    step: Int,
    chapter: Option[Int] = None,
    perspective: Option[String] = None,
    originChapter: Option[Int] = None,
    evidence: Boolean = false,
    node: Option[String] = None,
    theme: String = "light",
    text: String = "standard",
    embed: Boolean = false,
    present: Boolean = false,
    artifact: Option[RecordedArtifact] = None,
    navigation: Boolean = false,
    appearance: Boolean = false
)

object BookApp extends IOApp.Simple:
  def run: IO[Unit] =
    if dom.document.getElementById("app") == null then IO.unit else reader

  private def reader: IO[Unit] = Dispatcher
    .sequential[IO]
    .use { dispatcher =>
      for
        text <- evidence
        json <- IO.fromEither(io.circe.parser.parse(text))
        stories <- IO.fromEither(json.hcursor.get[Vector[RecordedStory]]("stories"))
        chapters <- IO.fromEither(json.hcursor.get[Vector[BookChapter]]("chapters"))
        _ <- IO.raiseWhen(stories.isEmpty || chapters.isEmpty)(
          RuntimeException("This book has no stories or chapters")
        )
        _ <- {
          lazy val view: BookView = new BookView(
            stories,
            chapters,
            next =>
              dispatcher.unsafeRunAndForget(
                IO(
                  dom.window.history
                    .pushState(null, "", BookNavigation.address(next, stories, chapters))
                ) *> view.render(next) *> IO(
                  dom.window.dispatchEvent(new dom.Event("harmonia-view"))
                )
              )
          )
          def current = BookNavigation.read(
            if dom.window.location.search.nonEmpty then dom.window.location.search
            else dom.window.location.hash,
            stories,
            chapters
          )
          val onHistory: dom.Event => Unit =
            _ =>
              dispatcher.unsafeRunAndForget(
                view.render(current) *> IO(dom.window.dispatchEvent(new dom.Event("harmonia-view")))
              )
          Resource
            .make(IO(dom.window.addEventListener("popstate", onHistory)))(_ =>
              IO(dom.window.removeEventListener("popstate", onHistory))
            )
            .use(_ => view.render(current) *> IO.never)
        }
      yield ()
    }
    .handleErrorWith(error =>
      IO {
        dom.document.getElementById("app").textContent =
          s"Could not open the book: ${error.getMessage}"
      }
    )

  private def evidence: IO[String] =
    val bundled = js.Dynamic.global.selectDynamic("HarmoniaLaboratoryEvidence")
    if !js.isUndefined(bundled) then IO.pure(js.JSON.stringify(bundled))
    else
      for
        response <- IO.fromFuture(IO(dom.fetch("evidence.json").toFuture))
        _ <- IO.raiseUnless(response.ok)(
          RuntimeException(s"Evidence request failed: ${response.status}")
        )
        text <- IO.fromFuture(IO(response.text().toFuture))
      yield text

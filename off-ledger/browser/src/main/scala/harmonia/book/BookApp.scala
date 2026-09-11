package harmonia.book

import cats.effect.{IO, IOApp, Resource}
import cats.effect.std.Dispatcher
import org.scalajs.dom
import scala.scalajs.js.Thenable.Implicits.*
import scala.concurrent.ExecutionContext.Implicits.global

final case class ViewState(
    story: Int,
    step: Int,
    chapter: Option[Int] = None,
    perspective: Option[String] = None,
    originChapter: Option[Int] = None
)

object BookApp extends IOApp.Simple:
  def run: IO[Unit] =
    if dom.document.body.getAttribute("data-mode") == "live" then harmonia.live.LiveApp.run
    else recorded

  private def recorded: IO[Unit] = Dispatcher
    .sequential[IO]
    .use { dispatcher =>
      for
        response <- IO.fromFuture(IO(dom.fetch("evidence.json").toFuture))
        _ <- IO.raiseUnless(response.ok)(
          RuntimeException(s"Evidence request failed: ${response.status}")
        )
        text <- IO.fromFuture(IO(response.text().toFuture))
        json <- IO.fromEither(io.circe.parser.parse(text))
        stories <- IO.fromEither(json.hcursor.get[Vector[RecordedStory]]("stories"))
        chapters <- IO.fromEither(json.hcursor.get[Vector[BookChapter]]("chapters"))
        _ <- IO.raiseWhen(stories.isEmpty || chapters.isEmpty)(
          RuntimeException("This book has no stories or chapters")
        )
        _ <- Resource.make(IO(new Inspector(dispatcher)))(i => IO(i.dispose())).use { inspector =>
          lazy val view: BookView = new BookView(
            stories,
            chapters,
            next =>
              dispatcher.unsafeRunAndForget(
                IO(
                  dom.window.history
                    .pushState(null, "", BookNavigation.address(next, stories, chapters))
                ) *> view.render(next)
              ),
            inspector
          )
          def current = BookNavigation.read(dom.window.location.hash, stories, chapters)
          val onHistory: dom.Event => Unit =
            _ => dispatcher.unsafeRunAndForget(view.render(current))
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

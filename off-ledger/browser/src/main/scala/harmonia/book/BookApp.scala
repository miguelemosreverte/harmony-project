package harmonia.book

import cats.effect.{IO, IOApp, Ref}
import cats.effect.std.Dispatcher
import io.circe.Json
import org.scalajs.dom
import scala.scalajs.js.Thenable.Implicits.*
import scala.concurrent.ExecutionContext.Implicits.global

final case class ViewState(
    story: Int,
    step: Int,
    chapter: Option[Int] = None,
    perspective: Option[String] = None
)

object BookApp extends IOApp.Simple:
  def run: IO[Unit] = Dispatcher
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
        chapters <- IO.fromEither(json.hcursor.get[Vector[Json]]("chapters"))
        _ <- IO.raiseWhen(stories.isEmpty)(RuntimeException("This book has no recorded stories"))
        initial = ViewState(math.max(0, stories.indexWhere(_.id == "workflow-approved")), 0)
        state <- Ref.of[IO, ViewState](initial)
        renderer = new BookView(
          stories,
          chapters,
          next =>
            dispatcher.unsafeRunAndForget(
              state.set(next) *> BookApp.draw(state, stories, chapters, dispatcher)
            )
        )
        _ <- renderer.render(initial)
        _ <- IO.never
      yield ()
    }
    .handleErrorWith(error =>
      IO {
        dom.document.getElementById("app").textContent =
          s"Could not open the book: ${error.getMessage}"
      }
    )

  private def draw(
      state: Ref[IO, ViewState],
      stories: Vector[RecordedStory],
      chapters: Vector[Json],
      dispatcher: Dispatcher[IO]
  ): IO[Unit] =
    state.get.flatMap { current =>
      new BookView(
        stories,
        chapters,
        next =>
          dispatcher.unsafeRunAndForget(
            state.set(next) *> draw(state, stories, chapters, dispatcher)
          )
      ).render(current)
    }

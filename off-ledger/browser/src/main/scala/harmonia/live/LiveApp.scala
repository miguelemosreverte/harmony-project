package harmonia.live

import cats.effect.{IO, Ref}
import cats.effect.std.Dispatcher
import cats.syntax.all.*
import io.circe.Json
import org.scalajs.dom
import scala.scalajs.js
import scala.concurrent.duration.*

private final case class ClientState(
    snapshot: Option[Json],
    connection: String,
    unconfirmed: Option[Json],
    submitting: Boolean = false
)

object LiveApp:
  def run: IO[Unit] = Dispatcher.parallel[IO].use { dispatcher =>
    for
      capability <- IO {
        val fragment = dom.window.location.hash.stripPrefix("#session=")
        if fragment.matches("[A-Za-z0-9_-]{43}") then
          dom.window.sessionStorage.setItem("harmonia-live", fragment)
          dom.window.history.replaceState(null, "", "/")
        Option(dom.window.sessionStorage.getItem("harmonia-live")).getOrElse("")
      }
      remembered <- IO(
        Option(dom.window.sessionStorage.getItem("harmonia-request-" + capability))
          .flatMap(io.circe.parser.parse(_).toOption)
      )
      state <- Ref.of[IO, ClientState](ClientState(None, "Connecting", remembered))
      session = new BrowserSession(capability, state, dispatcher)
      _ <- (session.refresh *> IO.sleep(1.second)).foreverM
    yield ()
  }

private final class BrowserSession(
    capability: String,
    state: Ref[IO, ClientState],
    dispatcher: Dispatcher[IO]
):
  private val storageKey = "harmonia-request-" + capability
  private def remember(input: Option[Json]): IO[Unit] = IO {
    input match
      case Some(value) => dom.window.sessionStorage.setItem(storageKey, value.noSpaces)
      case None        => dom.window.sessionStorage.removeItem(storageKey)
  }
  def refresh: IO[Unit] = LiveApi
    .request(capability, "GET", "/api/state", None)
    .flatMap { snapshot =>
      state.update { current =>
        val found = current.unconfirmed.exists { request =>
          snapshot.hcursor
            .get[Vector[Json]]("jobs")
            .getOrElse(Vector.empty)
            .exists(job =>
              job.hcursor.get[String]("id").toOption == request.hcursor.get[String]("id").toOption
            )
        }
        current.copy(
          snapshot = Some(snapshot),
          connection = "Connected",
          unconfirmed = if found then None else current.unconfirmed
        )
      } *> state.get.flatMap(s => remember(s.unconfirmed))
    }
    .handleErrorWith { error =>
      val message = error match
        case failure: LiveHttpFailure if failure.code == 401 => failure.getMessage
        case _ => "Disconnected — showing the last observed state"
      state.update(_.copy(connection = message))
    } *> draw

  private def send(input: Json): IO[Unit] =
    state
      .modify { current =>
        if current.submitting then current -> false
        else current.copy(unconfirmed = Some(input), submitting = true) -> true
      }
      .flatMap {
        case false => IO.unit
        case true =>
          remember(Some(input)) *> draw *> LiveApi
            .request(capability, "POST", "/api/actions", Some(input))
            .attempt
            .flatMap {
              case Right(_) =>
                state.update(_.copy(unconfirmed = None, submitting = false)) *> remember(
                  None
                ) *> refresh
              case Left(error: LiveHttpFailure) if Set(400, 401, 403).contains(error.code) =>
                state.update(
                  _.copy(unconfirmed = None, submitting = false, connection = error.getMessage)
                ) *> remember(None) *> draw
              case Left(_) =>
                state.update(
                  _.copy(
                    submitting = false,
                    connection = "Disconnected — submission result unknown"
                  )
                ) *> draw
            }
      }

  private def draw: IO[Unit] = state.get.flatMap { current =>
    IO {
      LiveView.render(
        current.snapshot,
        current.connection,
        current.unconfirmed.nonEmpty,
        current.submitting,
        () => dispatcher.unsafeRunAndForget(refresh),
        () => current.unconfirmed.foreach(input => dispatcher.unsafeRunAndForget(send(input))),
        action =>
          current.snapshot.foreach { snapshot =>
            val input = Json.obj(
              "id" -> Json.fromString(js.Dynamic.global.crypto.randomUUID().asInstanceOf[String]),
              "action" -> Json.fromString(action),
              "version" -> snapshot.hcursor
                .get[String]("version")
                .toOption
                .fold(Json.Null)(Json.fromString)
            )
            dispatcher.unsafeRunAndForget(send(input))
          }
      )
    }
  }

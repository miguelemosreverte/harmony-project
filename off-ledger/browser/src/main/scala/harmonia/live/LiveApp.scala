package harmonia.live

import cats.effect.{IO, Ref}
import cats.effect.std.Dispatcher
import cats.syntax.all.*
import io.circe.Json
import harmonia.workspace.WorkspaceSnapshot
import org.scalajs.dom
import scala.scalajs.js
import scala.concurrent.duration.*

private final case class ClientState(
    snapshot: Option[WorkspaceSnapshot],
    connection: String,
    unconfirmed: Option[Json],
    submitting: Boolean = false,
    notice: Option[String] = None
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
      _ <- IO {
        dom.window.addEventListener(
          "hashchange",
          (_: dom.Event) =>
            if dom.window.location.hash.matches("#session=[A-Za-z0-9_-]{43}") then
              dom.window.location.reload()
        )
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
  private val packages = new harmonia.builder.PackagePanel(capability, dispatcher)
  private val editor =
    new harmonia.composition.CompositionEditor(input => submit("compose-propose", Some(input)))

  private def submit(action: String, parameters: Option[Json]): Unit =
    dispatcher.unsafeRunAndForget(state.get.flatMap { current =>
      if current.submitting || current.unconfirmed.nonEmpty || current.connection != "Connected"
      then IO.unit
      else
        current.snapshot.fold(IO.unit) { snapshot =>
          val input = Json
            .obj(
              "id" -> Json.fromString(js.Dynamic.global.crypto.randomUUID().asInstanceOf[String]),
              "action" -> Json.fromString(action),
              "version" -> Json.fromString(snapshot.financing.version)
            )
            .deepMerge(parameters.fold(Json.obj())(value => Json.obj("input" -> value)))
          send(input)
        }
    })
  private def remember(input: Option[Json]): IO[Unit] = IO {
    input match
      case Some(value) => dom.window.sessionStorage.setItem(storageKey, value.noSpaces)
      case None        => dom.window.sessionStorage.removeItem(storageKey)
  }
  def refresh: IO[Unit] = LiveApi
    .request(capability, "GET", "/api/state", None)
    .flatMap(json => IO.fromEither(json.as[WorkspaceSnapshot]))
    .flatMap { snapshot =>
      state.update { current =>
        val found = current.unconfirmed.exists { request =>
          snapshot.submissions.exists(job => request.hcursor.get[String]("id").contains(job.id))
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
        else current.copy(unconfirmed = Some(input), submitting = true, notice = None) -> true
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
                  _.copy(unconfirmed = None, submitting = false, notice = Some(error.getMessage))
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
        current.notice,
        editor,
        packages,
        () => dispatcher.unsafeRunAndForget(refresh),
        () => current.unconfirmed.foreach(input => dispatcher.unsafeRunAndForget(send(input))),
        () => dispatcher.unsafeRunAndForget(state.update(_.copy(notice = None)) *> draw),
        action => submit(action, None),
        (action, input) => submit(action, Some(input))
      )
    }
  }

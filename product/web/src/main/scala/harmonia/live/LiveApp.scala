package harmonia.live

import cats.effect.{IO, Ref, Resource}
import cats.effect.std.Dispatcher
import cats.syntax.all.*
import io.circe.Json
import harmonia.workspace.{WorkspaceSnapshot, WorkspaceCommand}
import harmonia.composition.CompositionCommand
import org.scalajs.dom
import scala.scalajs.js
import scala.concurrent.duration.*

private final case class ClientState(
    snapshot: Option[WorkspaceSnapshot],
    connection: ConnectionState,
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
          dom.window.history.replaceState(
            null,
            "",
            dom.window.location.pathname + dom.window.location.search
          )
        Option(dom.window.sessionStorage.getItem("harmonia-live")).getOrElse("")
      }
      remembered <- IO(
        Option(dom.window.sessionStorage.getItem("harmonia-request-" + capability))
          .flatMap(io.circe.parser.parse(_).toOption)
      )
      state <- Ref.of[IO, ClientState](ClientState(None, ConnectionState.Connecting, remembered))
      listener: (dom.Event => Unit) = _ =>
        if dom.window.location.hash.matches("#session=[A-Za-z0-9_-]{43}") then
          dom.window.location.reload()
      _ <- Resource
        .make(IO(dom.window.addEventListener("hashchange", listener)))(_ =>
          IO(dom.window.removeEventListener("hashchange", listener))
        )
        .use { _ =>
          if capability.isEmpty then IO(SessionEntry.render(expired = false)) *> IO.never
          else
            val session = new BrowserSession(capability, state, dispatcher)
            (session.refresh *> IO.sleep(1.second)).foreverM
        }
    yield ()
  }

private final class BrowserSession(
    capability: String,
    state: Ref[IO, ClientState],
    dispatcher: Dispatcher[IO]
):
  private val view = new LiveView
  private val storageKey = "harmonia-request-" + capability
  private val packages = new harmonia.packages.PackagePanel(capability, dispatcher)
  private val editor =
    new harmonia.composition.CompositionEditor({
      case Right(plan) => submit(WorkspaceCommand.Composition(CompositionCommand.Propose(plan)))
      case Left(message) =>
        dispatcher.unsafeRunAndForget(state.update(_.copy(notice = Some(message))) *> draw)
    })

  private def submit(command: WorkspaceCommand): Unit =
    dispatcher.unsafeRunAndForget(state.get.flatMap { current =>
      if current.submitting || current.unconfirmed.nonEmpty || current.connection != ConnectionState.Connected
      then IO.unit
      else
        current.snapshot.fold(IO.unit) { snapshot =>
          val input = Json
            .obj(
              "id" -> Json.fromString(js.Dynamic.global.crypto.randomUUID().asInstanceOf[String]),
              "action" -> Json.fromString(command.wire),
              "version" -> Json.fromString(snapshot.financing.version)
            )
            .deepMerge(command.parameters.fold(Json.obj())(value => Json.obj("input" -> value)))
          send(input)
        }
    })
  private def remember(input: Option[Json]): IO[Unit] = IO {
    input match
      case Some(value) => dom.window.sessionStorage.setItem(storageKey, value.noSpaces)
      case None        => dom.window.sessionStorage.removeItem(storageKey)
  }
  def refresh: IO[Unit] = state.get.flatMap { current =>
    if current.connection == ConnectionState.SessionRequired then IO.unit else observe
  }

  private def observe: IO[Unit] = LiveApi
    .request(capability, "GET", "/api/state", None)
    .flatMap(json => IO.fromEither(json.as[WorkspaceSnapshot]))
    .flatMap { snapshot =>
      state.update { current =>
        val found = current.unconfirmed.exists { request =>
          snapshot.submissions.exists(job => request.hcursor.get[String]("id").contains(job.id))
        }
        current.copy(
          snapshot = Some(snapshot),
          connection = ConnectionState.Connected,
          unconfirmed = if found then None else current.unconfirmed
        )
      } *> state.get.flatMap(s => remember(s.unconfirmed))
    }
    .handleErrorWith { error =>
      val connection = error match
        case failure: LiveHttpFailure if failure.code == 401 => ConnectionState.SessionRequired
        case _ => ConnectionState.Disconnected("Disconnected — showing the last observed state")
      state.update(_.copy(connection = connection))
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
                    connection =
                      ConnectionState.Disconnected("Disconnected — submission result unknown")
                  )
                ) *> draw
            }
      }

  private def draw: IO[Unit] = state.get.flatMap { current =>
    if current.connection == ConnectionState.SessionRequired then
      IO(SessionEntry.render(expired = true))
    else
      IO {
        view.render(
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
          submit
        )
      }
  }

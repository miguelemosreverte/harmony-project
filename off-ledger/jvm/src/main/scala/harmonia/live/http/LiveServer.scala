package harmonia.live.http

import cats.effect.{IO, Resource}
import cats.effect.std.Dispatcher
import cats.syntax.all.*
import com.sun.net.httpserver.{HttpExchange, HttpServer}
import harmonia.live.actions.{ActionRequest, LiveActions}
import harmonia.ledger.auth.LocalCredentials
import harmonia.live.run.LiveRuntime
import io.circe.Json
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Path}
import java.util.concurrent.Executors

final case class LiveServer(port: Int, capabilities: Map[String, String]):
  override def toString: String = s"LiveServer($port, <redacted>)"

object LiveServer:
  def resource(root: Path, artifacts: Path, runtime: LiveRuntime): Resource[IO, LiveServer] = for
    actions <- LiveActions.resource(runtime)
    sessions <- Resource.eval(
      runtime.participants.keys.toVector
        .traverse(name => LocalCredentials.random.map(name -> _))
        .map(_.toMap)
    )
    dispatcher <- Dispatcher.parallel[IO]
    server <- Resource.make(IO.blocking {
      val executor = Executors.newFixedThreadPool(4)
      val http = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 16)
      http.setExecutor(executor)
      http.createContext(
        "/",
        (exchange: HttpExchange) =>
          dispatcher.unsafeRunAndForget(
            respond(root, http.getAddress.getPort, sessions, actions, exchange)
              .handleErrorWith { error =>
                val code = if error.isInstanceOf[IllegalArgumentException] then 400 else 503
                send(
                  exchange,
                  code,
                  "application/json",
                  Json
                    .obj(
                      "error" -> Json.fromString(
                        if code == 400 then error.getMessage
                        else "Participant disconnected; refresh to recover current state"
                      )
                    )
                    .noSpaces
                    .getBytes(UTF_8)
                )
              }
              .guarantee(IO.blocking(exchange.close()))
          )
      )
      http.start()
      (http, executor)
    }) { (http, executor) => IO.blocking { http.stop(0); executor.shutdownNow(); () } }
    port = server._1.getAddress.getPort
    _ <- Resource.eval(
      LocalCredentials.privateWrite(
        artifacts.resolve("sessions.json"),
        Json
          .fromFields(
            sessions
              .map((name, cap) => name -> Json.fromString(s"http://127.0.0.1:$port/#session=$cap"))
          )
          .spaces2
      )
    )
  yield LiveServer(port, sessions)

  private def respond(
      root: Path,
      port: Int,
      sessions: Map[String, String],
      actions: LiveActions,
      exchange: HttpExchange
  ): IO[Unit] =
    val path = exchange.getRequestURI.getPath
    val method = exchange.getRequestMethod
    val origin = s"http://127.0.0.1:$port"
    val hostValid = Option(exchange.getRequestHeaders.getFirst("Host")).contains(s"127.0.0.1:$port")
    val originValid = Option(exchange.getRequestHeaders.getFirst("Origin")).forall(_ == origin)
    if !hostValid || !originValid then
      send(exchange, 403, "text/plain", "Origin denied".getBytes(UTF_8))
    else if path.startsWith("/api/") then
      val bearer = Option(exchange.getRequestHeaders.getFirst("Authorization"))
        .getOrElse("")
        .stripPrefix("Bearer ")
      sessions.find(_._2 == bearer) match
        case None =>
          send(
            exchange,
            401,
            "text/plain",
            "Open the session provisioned by the local operator".getBytes(UTF_8)
          )
        case Some((actor, _)) =>
          val response = (method, path) match
            case ("GET", "/api/state") => actions.state(actor)
            case ("POST", "/api/actions") =>
              for
                bytes <- IO.blocking(exchange.getRequestBody.readNBytes(16385))
                _ <- IO.raiseWhen(bytes.length > 16384)(
                  IllegalArgumentException("Request exceeds 16 KiB")
                )
                json <- IO.fromEither(
                  io.circe.parser
                    .parse(new String(bytes, UTF_8))
                    .leftMap(_ => IllegalArgumentException("Invalid JSON"))
                )
                _ <- IO.raiseUnless(
                  json.asObject.exists(_.keys.toSet == Set("id", "action", "version"))
                )(IllegalArgumentException("Expected only id, action, and version"))
                request <- IO
                  .fromEither(for
                    id <- json.hcursor.get[String]("id")
                    action <- json.hcursor.get[String]("action")
                    version <- json.hcursor.get[String]("version")
                  yield ActionRequest(id, action, version))
                  .adaptError { case _: io.circe.Error =>
                    IllegalArgumentException("Action fields must be text")
                  }
                job <- actions.submit(actor, request)
              yield job.json
            case _ => IO.raiseError(IllegalArgumentException("Unsupported endpoint or method"))
          response.flatMap(json =>
            send(
              exchange,
              if method == "POST" then 202 else 200,
              "application/json",
              json.noSpaces.getBytes(UTF_8)
            )
          )
    else if method != "GET" then send(exchange, 405, "text/plain", Array.emptyByteArray)
    else
      val file = path match
        case "/"         => Some(root.resolve("book/site/live.html") -> "text/html")
        case "/book.css" => Some(root.resolve("book/site/book.css") -> "text/css")
        case "/main.js" =>
          Some(
            root.resolve(
              "off-ledger/browser/target/scala-3.3.6/harmonia-book-fastopt/main.js"
            ) -> "text/javascript"
          )
        case _ => None
      file.fold(send(exchange, 404, "text/plain", Array.emptyByteArray)) { (file, kind) =>
        IO.blocking(Files.readAllBytes(file)).flatMap(send(exchange, 200, kind, _))
      }

  private def send(exchange: HttpExchange, code: Int, kind: String, bytes: Array[Byte]): IO[Unit] =
    IO.blocking {
      val headers = exchange.getResponseHeaders
      headers.set("Content-Type", kind + "; charset=utf-8")
      headers.set("Cache-Control", "no-store")
      headers.set("X-Content-Type-Options", "nosniff")
      headers.set("Referrer-Policy", "no-referrer")
      headers.set(
        "Content-Security-Policy",
        "default-src 'self'; connect-src 'self'; script-src 'self'; style-src 'self'; frame-ancestors 'none'; base-uri 'none'"
      )
      exchange.sendResponseHeaders(code, bytes.length.toLong)
      exchange.getResponseBody.write(bytes)
    }

package harmonia.app.http

import cats.effect.{IO, Resource}
import cats.effect.std.Dispatcher
import cats.syntax.all.*
import com.sun.net.httpserver.{HttpExchange, HttpServer}
import harmonia.app.workspace.Workspace
import harmonia.ledger.auth.LocalCredentials
import harmonia.app.Connections
import io.circe.{Encoder, Json}
import io.circe.syntax.*
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Path}
import java.util.concurrent.Executors

final case class LiveServer(port: Int, capabilities: Map[String, String]):
  override def toString: String = s"LiveServer($port, <redacted>)"

object LiveServer:
  def resource(root: Path, artifacts: Path, runtime: Connections): Resource[IO, LiveServer] = for
    actions <- Workspace.resource(runtime)
    builder <- Resource.eval(
      harmonia.packages.workspace.PackageBuilder
        .create(root, artifacts.resolve("builder"), runtime.packageExports)
    )
    sessions <- Resource.eval(
      runtime.ledgers.keys.toVector
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
            respond(root, http.getAddress.getPort, sessions, actions, builder, exchange)
              .handleErrorWith { error =>
                val code = if error.isInstanceOf[IllegalArgumentException] then 400 else 503
                val message =
                  if code == 400 then error.getMessage
                  else "Participant disconnected; refresh to recover current state"
                sendJson(exchange, code, Json.obj("error" -> Json.fromString(message)))
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
      actions: Workspace,
      builder: harmonia.packages.workspace.PackageBuilder,
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
          if path.startsWith("/api/builder") && actor != "bank" then
            send(
              exchange,
              403,
              "text/plain",
              "The builder belongs to the bank operator session".getBytes(UTF_8)
            )
          else if method == "GET" && path.startsWith("/api/builder/project/") then
            builder
              .download(path.stripPrefix("/api/builder/project/"))
              .flatMap(file =>
                IO.blocking(Files.readAllBytes(file))
                  .flatMap(send(exchange, 200, "application/zip", _))
              )
          else apiResponse(actor, method, path, actions, builder, exchange)
    else if method != "GET" then send(exchange, 405, "text/plain", Array.emptyByteArray)
    else
      val file = path match
        case "/"         => Some(root.resolve("product/web/site/index.html") -> "text/html")
        case "/live.css" => Some(root.resolve("product/web/site/live.css") -> "text/css")
        case "/main.js" =>
          Some(
            root.resolve(
              "product/web/target/scala-3.3.6/harmonia-web-fastopt/main.js"
            ) -> "text/javascript"
          )
        case _ => None
      file.fold(send(exchange, 404, "text/plain", Array.emptyByteArray)) { (file, kind) =>
        IO.blocking(Files.readAllBytes(file)).flatMap(send(exchange, 200, kind, _))
      }

  private def apiResponse(
      actor: String,
      method: String,
      path: String,
      actions: Workspace,
      builder: harmonia.packages.workspace.PackageBuilder,
      exchange: HttpExchange
  ): IO[Unit] =
    def reply[A: Encoder](value: IO[A], code: Int = 200): IO[Unit] =
      value.flatMap(value => sendJson(exchange, code, value.asJson))

    val body = exchange.getRequestBody
    (method, path) match
      case ("GET", "/api/state")   => reply(actions.state(actor))
      case ("GET", "/api/builder") => reply(builder.state)
      case ("POST", "/api/builder/upload") =>
        reply(
          builder.upload(
            IO.blocking(body.readNBytes(harmonia.packages.inspect.InspectDar.maximumBytes + 1))
          )
        )
      case ("POST", "/api/builder/retrieve") =>
        reply(RequestBody.packageKey(body, "source").flatMap(builder.retrieve))
      case ("POST", "/api/builder/generate") =>
        reply(RequestBody.packageKey(body, "id").flatMap(builder.generate))
      case ("POST", "/api/actions") =>
        reply(RequestBody.action(body).flatMap(actions.submit(actor, _)).map(_.view), 202)
      case _ => IO.raiseError(IllegalArgumentException("Unsupported endpoint or method"))

  private def sendJson(exchange: HttpExchange, code: Int, value: Json): IO[Unit] =
    send(exchange, code, "application/json", value.noSpaces.getBytes(UTF_8))

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

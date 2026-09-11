package harmonia.app.http

import cats.effect.{IO, Resource}
import cats.effect.std.Dispatcher
import cats.syntax.all.*
import com.sun.net.httpserver.{HttpExchange, HttpServer}
import harmonia.app.workspace.Workspace
import harmonia.submission.ActionRequest
import harmonia.ledger.auth.LocalCredentials
import harmonia.app.Connections
import io.circe.Json
import io.circe.syntax.*
import harmonia.workspace.WorkspaceCommand
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Path}
import java.util.concurrent.Executors

final case class LiveServer(port: Int, capabilities: Map[String, String]):
  override def toString: String = s"LiveServer($port, <redacted>)"

object LiveServer:
  def resource(
      root: Path,
      artifacts: Path,
      runtime: Connections,
      book: Option[Path] = None
  ): Resource[IO, LiveServer] = for
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
            respond(root, http.getAddress.getPort, sessions, actions, builder, book, exchange)
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
      actions: Workspace,
      builder: harmonia.packages.workspace.PackageBuilder,
      book: Option[Path],
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
    else if path == "/book/source/design/0.2/context.js" && book.nonEmpty then
      send(exchange, 200, "text/javascript", "window.HarmoniaLiveRoot = '/';".getBytes(UTF_8))
    else if path == "/book" || path.startsWith("/book/") then
      IO.blocking(
        book.flatMap(directory =>
          StaticFiles.resolve(directory, path.stripPrefix("/book").stripPrefix("/"))
        )
      ).flatMap(
        _.fold(
          send(
            exchange,
            404,
            "text/plain",
            "The book is not mounted on this server.".getBytes(UTF_8)
          )
        ) { (file, kind) =>
          IO.blocking(Files.readAllBytes(file)).flatMap(send(exchange, 200, kind, _))
        }
      )
    else
      val file = path match
        case "/"          => Some(root.resolve("product/web/site/index.html") -> "text/html")
        case "/scene.css" => Some(root.resolve("product/scene/site/scene.css") -> "text/css")
        case "/live.css"  => Some(root.resolve("product/web/site/live.css") -> "text/css")
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
    val response = (method, path) match
      case ("GET", "/api/state")   => actions.state(actor).map(_.asJson)
      case ("GET", "/api/builder") => builder.state.map(_.asJson)
      case ("POST", "/api/builder/upload") =>
        builder
          .upload(
            IO.blocking(
              exchange.getRequestBody.readNBytes(
                harmonia.packages.inspect.InspectDar.maximumBytes + 1
              )
            )
          )
          .map(_.asJson)
      case ("POST", "/api/builder/retrieve") =>
        builderKey(exchange, "source").flatMap(builder.retrieve).map(_.asJson)
      case ("POST", "/api/builder/generate") =>
        builderKey(exchange, "id").flatMap(builder.generate).map(_.asJson)
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
            json.asObject.exists(obj =>
              Set("id", "action", "version").subsetOf(
                obj.keys.toSet
              ) && (obj.keys.toSet -- Set("id", "action", "version", "input")).isEmpty
            )
          )(
            IllegalArgumentException(
              "Expected id, action, version, and optional composition input"
            )
          )
          request <- IO
            .fromEither(for
              id <- json.hcursor.get[String]("id")
              action <- json.hcursor.get[String]("action")
              command <- WorkspaceCommand
                .read(action, json.hcursor.downField("input").focus)
                .leftMap(message => io.circe.DecodingFailure(message, json.hcursor.history))
              version <- json.hcursor.get[String]("version")
            yield ActionRequest(id, command, version))
            .adaptError { case error: io.circe.Error =>
              IllegalArgumentException(error.getMessage)
            }
          job <- actions.submit(actor, request)
        yield job.json
      case _ => IO.raiseError(IllegalArgumentException("Unsupported endpoint or method"))
    response.flatMap(json =>
      send(
        exchange,
        if method == "POST" && path == "/api/actions" then 202 else 200,
        "application/json",
        json.noSpaces.getBytes(UTF_8)
      )
    )

  private def builderKey(exchange: HttpExchange, field: String): IO[String] = for
    bytes <- IO.blocking(exchange.getRequestBody.readNBytes(1025))
    _ <- IO.raiseWhen(bytes.length > 1024)(
      IllegalArgumentException("Package request exceeds 1 KiB")
    )
    json <- IO.fromEither(
      io.circe.parser
        .parse(new String(bytes, UTF_8))
        .leftMap(_ => IllegalArgumentException("Invalid package request JSON"))
    )
    _ <- IO.raiseUnless(json.asObject.exists(_.keys.toSet == Set(field)))(
      IllegalArgumentException(s"Expected only $field")
    )
    key <- IO.fromEither(
      json.hcursor.get[String](field).leftMap(_ => IllegalArgumentException(s"$field must be text"))
    )
  yield key

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

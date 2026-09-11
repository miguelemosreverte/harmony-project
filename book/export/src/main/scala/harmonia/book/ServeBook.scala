package harmonia.book

import cats.effect.{IO, Resource}
import com.sun.net.httpserver.{HttpExchange, HttpServer}
import java.net.InetSocketAddress
import java.nio.file.{Files, Path}
import java.util.concurrent.Executors

object ServeBook:
  def serve(directory: Path): IO[Unit] =
    val root = directory.toAbsolutePath.normalize()
    val server = Resource.make(IO.blocking {
      val executor = Executors.newFixedThreadPool(4)
      val port = sys.env.get("HARMONIA_BOOK_PORT").fold(0)(_.toInt)
      require(port >= 0 && port <= 65535, "HARMONIA_BOOK_PORT must be between 0 and 65535")
      val http = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0)
      http.setExecutor(executor)
      http.createContext("/", (exchange: HttpExchange) => respond(root, exchange))
      http.start()
      (http, executor)
    }) { (http, executor) => IO.blocking { http.stop(0); executor.shutdownNow(); () } }
    server.use { (http, _) =>
      IO.println(
        s"Open http://127.0.0.1:${http.getAddress.getPort}/ — Ctrl-C stops the book server."
      ) *> IO.never
    }

  private def respond(root: Path, exchange: HttpExchange): Unit =
    try
      val request = exchange.getRequestURI.getPath.stripPrefix("/")
      val path = root.resolve(if request.isEmpty then "index.html" else request).normalize()
      if exchange.getRequestMethod != "GET" then exchange.sendResponseHeaders(405, -1)
      else if !path.startsWith(root) || !Files
          .isRegularFile(path) || !path.toRealPath().startsWith(root.toRealPath())
      then exchange.sendResponseHeaders(404, -1)
      else
        val contentType = harmonia.app.http.StaticFiles.contentType(path)
        exchange.getResponseHeaders.set("Content-Type", s"$contentType; charset=utf-8")
        exchange.getResponseHeaders.set("X-Content-Type-Options", "nosniff")
        exchange.getResponseHeaders.set("Cache-Control", "no-store")
        exchange.sendResponseHeaders(200, Files.size(path))
        Files.copy(path, exchange.getResponseBody)
    finally exchange.close()

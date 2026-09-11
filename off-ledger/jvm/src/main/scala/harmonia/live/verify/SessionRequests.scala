package harmonia.live.verify

import cats.effect.IO
import harmonia.app.http.LiveServer
import io.circe.Json
import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import scala.concurrent.duration.*

object SessionRequests:
  def state(server: LiveServer, actor: String): IO[Json] =
    request(server, actor, "GET", "/api/state", None)
  def post(server: LiveServer, actor: String, input: Json): IO[Json] =
    request(server, actor, "POST", "/api/actions", Some(input))
  def request(
      server: LiveServer,
      actor: String,
      method: String,
      path: String,
      input: Option[Json]
  ): IO[Json] = for
    response <- http(server, server.capabilities(actor), method, path, input)
    _ <- IO.raiseUnless(Set(200, 202).contains(response._1))(
      RuntimeException(s"HTTP ${response._1}: ${response._2}")
    )
    json <- IO.fromEither(io.circe.parser.parse(response._2))
  yield json
  def http(
      server: LiveServer,
      capability: String,
      method: String,
      path: String,
      input: Option[Json]
  ): IO[(Int, String)] = IO.interruptible {
    val request = HttpRequest
      .newBuilder(URI.create(s"http://127.0.0.1:${server.port}$path"))
      .timeout(java.time.Duration.ofSeconds(45))
      .header("Authorization", "Bearer " + capability)
      .header("Content-Type", "application/json")
      .method(
        method,
        input.fold(HttpRequest.BodyPublishers.noBody())(value =>
          HttpRequest.BodyPublishers.ofString(value.noSpaces)
        )
      )
      .build()
    val client = HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(5)).build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    response.statusCode() -> response.body()
  }
  def awaitJob(server: LiveServer, actor: String, id: String): IO[Json] =
    def poll: IO[Json] = state(server, actor).flatMap { snapshot =>
      snapshot.hcursor
        .get[Vector[Json]]("jobs")
        .getOrElse(Vector.empty)
        .find(_.hcursor.get[String]("id").contains(id)) match
        case Some(job) if !job.hcursor.get[String]("outcome").contains("pending") => IO.pure(job)
        case _ => IO.sleep(100.millis) *> IO.defer(poll)
    }
    poll.timeout(45.seconds)
  def awaitCondition(condition: IO[Boolean]): IO[Unit] =
    def poll: IO[Unit] =
      condition.flatMap(ok => if ok then IO.unit else IO.sleep(100.millis) *> IO.defer(poll))
    poll.timeout(15.seconds)

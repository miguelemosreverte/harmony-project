package harmonia.live

import cats.effect.{IO, Resource}
import io.circe.Json
import org.scalajs.dom
import scala.scalajs.js.Thenable.Implicits.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*

final case class LiveHttpFailure(code: Int)
    extends RuntimeException(
      if code == 401 then "Session unavailable — open your provisioned session link"
      else s"Request unavailable (HTTP $code) — reconnect to recover state"
    )

private object LiveApi:
  def request(capability: String, verb: String, path: String, body: Option[Json]): IO[Json] =
    Resource
      .make(IO(new dom.AbortController()))(controller => IO(controller.abort()))
      .use { controller =>
        for
          response <- IO.fromFuture(IO {
            val requestHeaders = new dom.Headers()
            requestHeaders.set("Authorization", "Bearer " + capability)
            requestHeaders.set("Content-Type", "application/json")
            val options = new dom.RequestInit {
              this.method = (if verb == "POST" then dom.HttpMethod.POST else dom.HttpMethod.GET)
              this.headers = requestHeaders
              this.signal = controller.signal
            }
            body.foreach(value => options.body = value.noSpaces)
            dom.fetch(path, options).toFuture
          })
          _ <- IO.raiseUnless(response.ok)(LiveHttpFailure(response.status.toInt))
          text <- IO.fromFuture(IO(response.text().toFuture))
          json <- IO.fromEither(io.circe.parser.parse(text))
        yield json
      }
      .timeoutTo(
        45.seconds,
        IO.raiseError(RuntimeException("Disconnected — participant request timed out"))
      )

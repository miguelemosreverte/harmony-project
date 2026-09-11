package harmonia.live

import cats.effect.{IO, Resource}
import io.circe.Json
import org.scalajs.dom
import scala.scalajs.js.Thenable.Implicits.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*

final case class LiveHttpFailure(code: Int, detail: Option[String] = None)
    extends RuntimeException(
      if code == 401 then "Session unavailable — open your provisioned session link"
      else detail.getOrElse(s"Request unavailable (HTTP $code) — reconnect to recover state")
    )

private[harmonia] object LiveApi:
  def request(
      capability: String,
      verb: String,
      path: String,
      body: Option[Json],
      upload: Option[dom.Blob] = None,
      deadline: FiniteDuration = 45.seconds
  ): IO[Json] =
    Resource
      .make(IO(new dom.AbortController()))(controller => IO(controller.abort()))
      .use { controller =>
        for
          response <- IO.fromFuture(IO {
            val requestHeaders = new dom.Headers()
            requestHeaders.set("Authorization", "Bearer " + capability)
            requestHeaders.set(
              "Content-Type",
              if upload.nonEmpty then "application/octet-stream" else "application/json"
            )
            val options = new dom.RequestInit {
              this.method = (if verb == "POST" then dom.HttpMethod.POST else dom.HttpMethod.GET)
              this.headers = requestHeaders
              this.signal = controller.signal
            }
            body.foreach(value => options.body = value.noSpaces)
            upload.foreach(value => options.body = value)
            dom.fetch(path, options).toFuture
          })
          text <- IO.fromFuture(IO(response.text().toFuture))
          _ <- IO.raiseUnless(response.ok)(
            LiveHttpFailure(
              response.status.toInt,
              io.circe.parser.parse(text).toOption.flatMap(_.hcursor.get[String]("error").toOption)
            )
          )
          json <- IO.fromEither(io.circe.parser.parse(text))
        yield json
      }
      .timeoutTo(
        deadline,
        IO.raiseError(RuntimeException("Disconnected — participant request timed out"))
      )

  def file(capability: String, path: String): IO[dom.Blob] = Resource
    .make(IO(new dom.AbortController()))(controller => IO(controller.abort()))
    .use { controller =>
      for
        response <- IO.fromFuture(IO {
          val options = new dom.RequestInit {
            method = dom.HttpMethod.GET
            headers = new dom.Headers()
            signal = controller.signal
          }
          options.headers.asInstanceOf[dom.Headers].set("Authorization", "Bearer " + capability)
          dom.fetch(path, options).toFuture
        })
        _ <- IO.raiseUnless(response.ok)(LiveHttpFailure(response.status.toInt))
        blob <- IO.fromFuture(IO(response.blob().toFuture))
      yield blob
    }
    .timeout(45.seconds)

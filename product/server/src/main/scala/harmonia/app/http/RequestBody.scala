package harmonia.app.http

import cats.effect.IO
import cats.syntax.all.*
import harmonia.workspace.ActionRequest
import io.circe.Json
import java.io.InputStream
import java.nio.charset.StandardCharsets.UTF_8

/** External byte bounds and decoding live together; routes receive validated values. */
private[http] object RequestBody:
  def action(body: InputStream): IO[ActionRequest] =
    json(body, 16384, "Request exceeds 16 KiB", "Invalid JSON")
      .flatMap(value =>
        IO.fromEither(ActionRequest.read(value).leftMap(IllegalArgumentException(_)))
      )

  def packageKey(body: InputStream, field: String): IO[String] = for
    value <- json(body, 1024, "Package request exceeds 1 KiB", "Invalid package request JSON")
    _ <- IO.raiseUnless(value.asObject.exists(_.keys.toSet == Set(field)))(
      IllegalArgumentException(s"Expected only $field")
    )
    key <- IO.fromEither(
      value.hcursor
        .get[String](field)
        .leftMap(_ => IllegalArgumentException(s"$field must be text"))
    )
  yield key

  private def json(body: InputStream, limit: Int, tooLarge: String, malformed: String): IO[Json] =
    for
      bytes <- IO.blocking(body.readNBytes(limit + 1))
      _ <- IO.raiseWhen(bytes.length > limit)(IllegalArgumentException(tooLarge))
      value <- IO.fromEither(
        io.circe.parser
          .parse(new String(bytes, UTF_8))
          .leftMap(_ => IllegalArgumentException(malformed))
      )
    yield value

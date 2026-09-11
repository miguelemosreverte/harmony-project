package harmonia.ledger.client

import com.daml.ledger.api.v2.ValueOuterClass.Identifier
import io.circe.{Decoder, Json}
import scala.util.Try

final case class LedgerDecodingFailure(location: String, problem: String)
    extends RuntimeException(s"Invalid ledger observation at $location: $problem")

/** Raw participant evidence. Features decode their payload before interpreting it. */
final case class ActiveContract(id: String, template: Identifier, fields: Map[String, Json]):
  private def location = s"${template.getModuleName}.${template.getEntityName} ($id)"

  def text(name: String): String =
    fields.get(name).flatMap(_.hcursor.get[String]("text").toOption).getOrElse {
      throw LedgerDecodingFailure(s"$location.$name", "expected a Text field")
    }

  def decode[A: Decoder]: Either[LedgerDecodingFailure, A] =
    Try(LedgerValue.fields(this)).toEither.left
      .map(error => LedgerDecodingFailure(location, error.getMessage))
      .flatMap(_.as[A].left.map(error => LedgerDecodingFailure(location, error.getMessage)))

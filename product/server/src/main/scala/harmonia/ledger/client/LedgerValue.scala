package harmonia.ledger.client

import com.daml.ledger.api.v2.ValueOuterClass as V
import io.circe.{Decoder, Json}

/** Typed protobuf construction and verbose Ledger API value decoding. */
object LedgerValue:
  def text(value: String): V.Value = V.Value.newBuilder().setText(value).build()
  def party(value: String): V.Value = V.Value.newBuilder().setParty(value).build()
  def record(fields: (String, V.Value)*): V.Value =
    val value = V.Record.newBuilder()
    fields.foreach((name, field) =>
      value.addFields(V.RecordField.newBuilder().setLabel(name).setValue(field))
    )
    V.Value.newBuilder().setRecord(value).build()
  def list(values: Vector[V.Value]): V.Value =
    val value = V.List.newBuilder()
    values.foreach(value.addElements)
    V.Value.newBuilder().setList(value).build()
  def plain(json: Json): Json =
    val entries = json.asObject.toVector.flatMap(_.toVector)
    if entries.size != 1 then invalid("value", "expected one Ledger API value variant")
    entries.head match
      case ("record", record) =>
        val fields = repeated(record, "fields").map { field =>
          required[String](field, "label") -> plain(required[Json](field, "value"))
        }
        if fields.map(_._1).distinct.size != fields.size then
          invalid("record", "duplicate field labels")
        Json.fromFields(fields)
      case ("list", list) => Json.arr(repeated(list, "elements").map(plain)*)
      case ("optional", value) =>
        objectValue(value)
        value.hcursor.downField("value").focus.fold(Json.Null)(plain)
      case ("variant", value) =>
        Json.obj(
          "constructor" -> Json.fromString(required[String](value, "constructor")),
          "value" -> plain(required[Json](value, "value"))
        )
      case ("text" | "party" | "contractId" | "int64" | "numeric", value) =>
        Json.fromString(value.as[String].fold(e => invalid("value", e.getMessage), identity))
      case ("bool", value) =>
        Json.fromBoolean(value.as[Boolean].fold(e => invalid("value", e.getMessage), identity))
      case ("unit", value) => objectValue(value); Json.Null
      case _               => invalid("value", "unsupported Ledger API value variant")

  private def required[A: Decoder](json: Json, name: String): A =
    json.hcursor.get[A](name).fold(error => invalid(name, error.getMessage), identity)

  private def repeated(json: Json, name: String): Vector[Json] =
    objectValue(json)
    // Protobuf JSON omits empty repeated fields. A malformed present field still fails.
    if json.hcursor.downField(name).succeeded then required[Vector[Json]](json, name)
    else Vector.empty

  private def objectValue(json: Json): Unit =
    if !json.isObject then invalid("value", "expected a Ledger API record object")

  private def invalid(location: String, message: String): Nothing =
    throw LedgerDecodingFailure(location, message)

  def fields(contract: ActiveContract): Json =
    Json.fromFields(contract.fields.map((name, value) => name -> plain(value)))

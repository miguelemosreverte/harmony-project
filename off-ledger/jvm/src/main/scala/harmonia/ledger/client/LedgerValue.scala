package harmonia.ledger.client

import com.daml.ledger.api.v2.ValueOuterClass as V
import io.circe.Json

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
    val cursor = json.hcursor
    json.asObject.flatMap(_.toVector.headOption) match
      case Some(("record", record)) =>
        Json.fromFields(
          record.hcursor.get[Vector[Json]]("fields").getOrElse(Vector.empty).map { field =>
            field.hcursor.get[String]("label").fold(throw _, identity) -> plain(
              field.hcursor.downField("value").focus.get
            )
          }
        )
      case Some(("list", list)) =>
        Json.arr(list.hcursor.get[Vector[Json]]("elements").getOrElse(Vector.empty).map(plain)*)
      case Some(("optional", value)) =>
        value.hcursor.downField("value").focus.fold(Json.Null)(plain)
      case Some(("variant", value)) =>
        Json.obj(
          "constructor" -> value.hcursor.downField("constructor").focus.get,
          "value" -> plain(value.hcursor.downField("value").focus.get)
        )
      case Some(("text" | "party" | "contractId" | "int64" | "numeric" | "bool", value)) => value
      case Some(("unit", _)) => Json.Null
      case _ => throw IllegalArgumentException("Unsupported or missing Ledger API value")
  def fields(contract: ActiveContract): Json =
    Json.fromFields(contract.fields.map((name, value) => name -> plain(value)))

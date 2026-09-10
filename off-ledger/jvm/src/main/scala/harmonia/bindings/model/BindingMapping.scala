package harmonia.bindings.model

import io.circe.{Json, JsonObject}

final case class BindingMapping(
    module: String,
    source: String,
    sourceModule: String,
    template: String,
    choice: String,
    actor: String,
    readers: Vector[String],
    subject: String,
    arguments: JsonObject,
    observation: String,
    example: JsonObject
)

enum FieldType(val daml: String):
  case Party extends FieldType("Party")
  case Text extends FieldType("Text")
  case Integer extends FieldType("Int")
  case Boolean extends FieldType("Bool")
  case Decimal extends FieldType("Decimal")

final case class Field(name: String, kind: FieldType)
final case class TemplateShape(fields: Vector[Field], arguments: Vector[Field])
final case class GeneratedFile(path: String, content: String)

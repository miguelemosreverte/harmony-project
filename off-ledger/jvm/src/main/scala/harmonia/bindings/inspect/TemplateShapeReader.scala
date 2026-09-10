package harmonia.bindings.inspect

import cats.syntax.all.*
import harmonia.bindings.model.*
import java.util.regex.Pattern

/** Reads the pinned compiler's public LF description; unsupported shapes fail closed. */
object TemplateShapeReader:
  def read(pretty: String, mapping: BindingMapping): Either[String, TemplateShape] = for
    module <- section(pretty, s"module ${mapping.sourceModule} where", "(?m)^module ")
    fields <- record(module, mapping.template)
    arguments <- record(module, mapping.choice)
    template <- section(module, s"template ${mapping.template} this where", "(?m)^template ")
    signature <- ("(?m)^  consuming choice " + Pattern.quote(
      mapping.choice
    ) + " ([^\\n]+)\\n    controller").r
      .findFirstMatchIn(template)
      .map(_.group(1).replaceAll("\\s+", " "))
      .toRight(s"${mapping.choice}: expected a consuming action choice in ${mapping.template}")
    _ <- Either.cond(
      signature.endsWith(s": ContractId ${mapping.sourceModule}:${mapping.template}") && signature
        .contains(s"(arg : ${mapping.sourceModule}:${mapping.choice})"),
      (),
      "The choice must return a replacement contract of its own source template"
    )
    _ <- (Vector(mapping.actor) ++ mapping.readers).traverse(name =>
      requireType(fields, name, FieldType.Party)
    )
    _ <- Vector(mapping.subject, mapping.observation).traverse(name =>
      requireType(fields, name, FieldType.Text)
    )
    _ <- Either.cond(
      fields.size <= 16 && arguments.size <= 8,
      (),
      "At most sixteen source fields and eight choice arguments are supported"
    )
    _ <- Either.cond(
      mapping.example.keys.toSet == fields.map(_.name).toSet,
      (),
      "example must supply exactly the inspected source fields"
    )
    _ <- Either.cond(
      mapping.arguments.keys.toSet == arguments.map(_.name).toSet,
      (),
      "arguments must supply exactly the inspected choice fields"
    )
  yield TemplateShape(fields, arguments)

  private def section(text: String, start: String, next: String): Either[String, String] =
    text.linesIterator.find(_ == start).toRight(s"DAR does not contain $start").map { _ =>
      val beginning = text.indexOf(start)
      val tail = text.substring(beginning + start.length)
      next.r.findFirstMatchIn(tail).fold(tail)(found => tail.substring(0, found.start))
    }

  private def record(module: String, name: String): Either[String, Vector[Field]] =
    ("(?s)record @serializable " + Pattern.quote(name) + " =\\s*\\{([^}]*)\\}").r
      .findFirstMatchIn(module)
      .map(_.group(1).trim)
      .toRight(s"Cannot inspect record $name; only primitive records are supported")
      .flatMap { body =>
        if body.isEmpty then Right(Vector.empty)
        else
          body.split(';').toVector.traverse { item =>
            item.trim.split("\\s*:\\s*", 2).toVector match
              case Vector(field, kind) if field.matches("[a-z][A-Za-z0-9_]*") =>
                val normalized = kind.replaceAll("\\s+", " ").trim
                val supported = Map(
                  "Party" -> FieldType.Party,
                  "Text" -> FieldType.Text,
                  "Int64" -> FieldType.Integer,
                  "Bool" -> FieldType.Boolean,
                  "Numeric 10" -> FieldType.Decimal
                )
                supported
                  .get(normalized)
                  .toRight(
                    s"$name.$field has unsupported type $normalized; use primitive Party, Text, Int, Bool, or Decimal fields"
                  )
                  .map(Field(field, _))
              case _ => Left(s"Cannot inspect field declaration in $name: $item")
          }
      }

  private def requireType(
      fields: Vector[Field],
      name: String,
      kind: FieldType
  ): Either[String, Unit] =
    Either.cond(
      fields.exists(field => field.name == name && field.kind == kind),
      (),
      s"$name must name a source ${kind.daml} field"
    )

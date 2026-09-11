package harmonia.bindings.inspect

import cats.syntax.all.*
import harmonia.bindings.model.*
import harmonia.packages.inspect.LfPackage
import com.digitalasset.daml.lf.archive.{DamlLf2 as LF}
import scala.jdk.CollectionConverters.*
import scala.util.Try

/** Validates supported source records and consuming replacement choices in the LF syntax tree. */
object TemplateShapeReader:
  def read(source: LfPackage, mapping: BindingMapping): Either[String, TemplateShape] =
    Try(inspect(source, mapping)).toEither.left
      .map(e => s"Invalid LF package: ${e.getMessage}")
      .flatten

  private def inspect(source: LfPackage, mapping: BindingMapping): Either[String, TemplateShape] =
    for
      module <- source.proto.getModulesList.asScala
        .find(m => source.name(m.getNameInternedDname) == mapping.sourceModule)
        .toRight(s"DAR does not contain module ${mapping.sourceModule}")
      fields <- record(source, module, mapping.template)
      arguments <- record(source, module, mapping.choice)
      template <- module.getTemplatesList.asScala
        .find(t => source.name(t.getTyconInternedDname) == mapping.template)
        .toRight(s"DAR does not contain template ${mapping.template}")
      choice <- template.getChoicesList.asScala
        .find(c => source.string(c.getNameInternedStr) == mapping.choice)
        .toRight(s"DAR does not contain choice ${mapping.choice}")
      _ <- Either.cond(
        choice.getConsuming,
        (),
        s"${mapping.choice}: expected a consuming action choice"
      )
      _ <- Either.cond(
        choice.hasArgBinder && sameRecord(
          source,
          choice.getArgBinder.getType,
          mapping.sourceModule,
          mapping.choice
        ),
        (),
        "The choice argument must be its own declared record"
      )
      result = source.resolve(choice.getRetType)
      _ <- Either.cond(
        result.hasBuiltin && result.getBuiltin.getBuiltin == LF.BuiltinType.CONTRACT_ID &&
          result.getBuiltin.getArgsCount == 1 && sameRecord(
            source,
            result.getBuiltin.getArgs(0),
            mapping.sourceModule,
            mapping.template
          ),
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

  private def sameRecord(
      source: LfPackage,
      original: LF.Type,
      module: String,
      name: String
  ): Boolean =
    val tpe = source.resolve(original)
    if !tpe.hasCon || tpe.getCon.getArgsCount != 0 then false
    else
      val id = tpe.getCon.getTycon
      id.hasModule && id.getModule.getPackageId.hasSelfPackageId &&
      source.name(id.getModule.getModuleNameInternedDname) == module && source.name(
        id.getNameInternedDname
      ) == name

  private def record(
      source: LfPackage,
      module: LF.Module,
      name: String
  ): Either[String, Vector[Field]] = for
    record <- module.getDataTypesList.asScala
      .find(d => source.name(d.getNameInternedDname) == name)
      .toRight(s"Cannot inspect record $name")
    _ <- Either.cond(
      record.hasRecord && record.getSerializable && record.getParamsCount == 0,
      (),
      s"$name must be a serializable primitive record"
    )
    fields <- record.getRecord.getFieldsList.asScala.toVector.traverse { field =>
      val label = source.string(field.getFieldInternedStr)
      val tpe = source.resolve(field.getType)
      val kind =
        if !tpe.hasBuiltin then None
        else
          val builtin = tpe.getBuiltin
          builtin.getBuiltin match
            case LF.BuiltinType.PARTY if builtin.getArgsCount == 0 => Some(FieldType.Party)
            case LF.BuiltinType.TEXT if builtin.getArgsCount == 0  => Some(FieldType.Text)
            case LF.BuiltinType.INT64 if builtin.getArgsCount == 0 => Some(FieldType.Integer)
            case LF.BuiltinType.BOOL if builtin.getArgsCount == 0  => Some(FieldType.Boolean)
            case LF.BuiltinType.NUMERIC if builtin.getArgsCount == 1 =>
              val scale = source.resolve(builtin.getArgs(0))
              Option.when(scale.hasNat && scale.getNat == 10)(FieldType.Decimal)
            case _ => None
      for
        _ <- Either.cond(
          label.matches("[a-z][A-Za-z0-9_]*"),
          (),
          s"Unsupported source field name: $label"
        )
        supported <- kind.toRight(
          s"$name.$label has unsupported type; use primitive Party, Text, Int, Bool, or Decimal fields"
        )
      yield Field(label, supported)
    }
    _ <- Either.cond(
      fields.map(_.name).distinct.size == fields.size,
      (),
      s"Duplicate field in $name"
    )
  yield fields

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

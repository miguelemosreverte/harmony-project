package harmonia.bindings

import munit.FunSuite
import cats.effect.unsafe.implicits.global
import harmonia.packages.inspect.{LfArchive, LfPackage}
import com.digitalasset.daml.lf.archive.{DamlLf2 as LF}
import scala.jdk.CollectionConverters.*
import harmonia.bindings.read.BindingFormat
import harmonia.bindings.inspect.TemplateShapeReader
import harmonia.bindings.generate.GenerateSources
import io.circe.Json
import java.nio.file.{Files, Path}

class BindingGenerationSuite extends FunSuite:
  private val root = Path.of(sys.env.getOrElse("HARMONIA_ROOT", ".."))
  private def read(path: String): String = Files.readString(root.resolve(path))
  private val mapping = BindingFormat.read(read("packages/mappings/financing.md")).toOption.get
  private val resolved =
    io.circe.parser.parse(read(".artifacts/packages/resolved.json")).toOption.get
  private def inspected(alias: String): LfPackage =
    val file =
      resolved.hcursor.downField("inputs").downField(alias).get[String]("file").toOption.get
    LfArchive.read(root.resolve(file)).unsafeRunSync()

  private def changeModule(source: LfPackage)(f: LF.Module => LF.Module): LfPackage =
    val result = source.proto.toBuilder.clearModules()
    source.proto.getModulesList.asScala.foreach(m =>
      result.addModules(
        if source.name(m.getNameInternedDname) == mapping.sourceModule then f(m) else m
      )
    )
    source.copy(proto = result.build())

  test("a source text field cannot become a signing party in a mapping") {
    val rejected =
      TemplateShapeReader.read(inspected("legacy-financing"), mapping.copy(actor = "status"))
    assert(
      rejected.left.toOption.exists(_.contains("status must name a source Party field")),
      clue(rejected)
    )
  }

  test("reject unsupported source types and nonconsuming replacement choices") {
    val source = inspected("legacy-financing")
    val unsupported = changeModule(source) { module =>
      val result = module.toBuilder.clearDataTypes()
      module.getDataTypesList.asScala.foreach { record =>
        if source.name(record.getNameInternedDname) == mapping.template then
          val fields = record.getRecord.toBuilder
          val first = fields
            .getFields(0)
            .toBuilder
            .setType(
              LF.Type
                .newBuilder()
                .setBuiltin(LF.Type.Builtin.newBuilder().setBuiltin(LF.BuiltinType.OPTIONAL))
            )
          result.addDataTypes(record.toBuilder.setRecord(fields.setFields(0, first)))
        else result.addDataTypes(record)
      }
      result.build()
    }
    assert(
      TemplateShapeReader
        .read(unsupported, mapping)
        .left
        .toOption
        .exists(_.contains("unsupported type"))
    )
    val nonconsuming = changeModule(source) { module =>
      val result = module.toBuilder.clearTemplates()
      module.getTemplatesList.asScala.foreach { template =>
        val changed = template.toBuilder.clearChoices()
        template.getChoicesList.asScala.foreach(c =>
          changed.addChoices(c.toBuilder.setConsuming(false))
        )
        result.addTemplates(changed)
      }
      result.build()
    }
    assert(TemplateShapeReader.read(nonconsuming, mapping).isLeft)
  }

  test("invalid and cyclic interned type references fail explicitly") {
    val source = inspected("legacy-financing")
    intercept[IndexOutOfBoundsException](
      source.resolve(LF.Type.newBuilder().setInternedType(Int.MaxValue).build())
    )
    val loop = LF.Type.newBuilder().setInternedType(0).build()
    val cyclic = source.copy(proto =
      source.proto.toBuilder.clearInternedTypes().addInternedTypes(loop).build()
    )
    intercept[IllegalArgumentException](cyclic.resolve(loop))
  }

  test("primitive argument values are checked against the inspected choice, not coerced") {
    val primitive = BindingFormat.read(read("packages/mappings/primitive-approval.md")).toOption.get
    val shape =
      TemplateShapeReader.read(inspected("primitive-approval"), primitive).fold(fail(_), identity)
    assert(
      GenerateSources(
        primitive.copy(arguments = primitive.arguments.add("days", Json.fromString("two"))),
        shape
      ).left.toOption.exists(_.contains("days must be an Int64"))
    )
    assert(
      GenerateSources(
        primitive.copy(arguments =
          primitive.arguments.add("approver", Json.fromString("Stranger"))
        ),
        shape
      ).isLeft
    )
    assert(
      GenerateSources(
        primitive.copy(arguments =
          primitive.arguments.add("cost", Json.fromString("0.00000000001"))
        ),
        shape
      ).isLeft
    )
  }

  test("mapping identifiers cannot supply paths or generated code") {
    val text = read("packages/mappings/financing.md")
    assert(
      BindingFormat.read(text.replace("binding: GeneratedFinancing", "binding: ../Escape")).isLeft
    )
    assert(
      BindingFormat
        .read(text.replace("result: replacement", "result: arbitrary-continuation"))
        .isLeft
    )
  }

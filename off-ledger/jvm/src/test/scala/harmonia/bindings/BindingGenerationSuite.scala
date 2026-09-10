package harmonia.bindings

import munit.FunSuite
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
  private def inspected(alias: String): String =
    val digest =
      resolved.hcursor.downField("inputs").downField(alias).get[String]("sha256").toOption.get
    read(s".artifacts/packages/cache/$digest/main.daml-lf")

  test("a source text field cannot become a signing party in a mapping") {
    val rejected =
      TemplateShapeReader.read(inspected("legacy-financing"), mapping.copy(actor = "status"))
    assert(rejected.left.toOption.exists(_.contains("status must name a source Party field")))
  }

  test("reject unsupported source types and nonconsuming replacement choices") {
    val pretty = inspected("legacy-financing")
    assert(
      TemplateShapeReader
        .read(pretty.replace("bank : Party", "bank : Optional Party"), mapping)
        .left
        .toOption
        .exists(_.contains("unsupported type"))
    )
    assert(
      TemplateShapeReader
        .read(pretty.replace("consuming choice Approve", "nonconsuming choice Approve"), mapping)
        .isLeft
    )
  }

  test("primitive argument values are checked against the inspected choice, not coerced") {
    val primitive = BindingFormat.read(read("packages/mappings/primitive-approval.md")).toOption.get
    val shape = TemplateShapeReader.read(inspected("primitive-approval"), primitive).toOption.get
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

package harmonia.bindings.generate

import cats.syntax.all.*
import harmonia.bindings.model.*
import io.circe.Json

object GenerateSources:
  def packageName(mapping: BindingMapping): String =
    "harmonia-binding-" + mapping.module.toLowerCase(java.util.Locale.ROOT)

  def apply(mapping: BindingMapping, shape: TemplateShape): Either[String, Vector[GeneratedFile]] =
    for
      sourceValues <- shape.fields.traverse(field =>
        literal(field, mapping.example(field.name).get, shape, mapping)
      )
      argumentValues <- shape.arguments.traverse(field =>
        literal(field, mapping.arguments(field.name).get, shape, mapping)
      )
      parties = shape.fields.filter(_.kind == FieldType.Party)
      names <- parties.traverse(field =>
        mapping.example(field.name).get.asString.toRight(s"${field.name} must have a party name")
      )
      _ <- Either.cond(
        names.distinct.size == names.size,
        (),
        "Example party names must be distinct so the authority test has an independent reader"
      )
    yield Vector(
      GeneratedFile(s"library/daml/${mapping.module}.daml", binding(mapping, shape)),
      GeneratedFile(
        "library/daml.yaml",
        configuration(
          packageName(mapping),
          Vector("../vendor/source.dar", "../vendor/interfaces.dar"),
          false
        )
      ),
      GeneratedFile(
        "example/daml/Example.daml",
        example(mapping, shape, sourceValues, argumentValues)
      ),
      GeneratedFile(
        "example/daml.yaml",
        configuration(
          packageName(mapping) + "-example",
          Vector(
            s"../library/.daml/dist/${packageName(mapping)}-0.1.0.dar",
            "../vendor/source.dar",
            "../vendor/interfaces.dar",
            "../vendor/core.dar"
          ),
          true
        )
      ),
      GeneratedFile(
        "README.md",
        s"""# Generated ${mapping.module}
        |
        |This project was generated from an authored mapping and an inspected, pinned source DAR. `generation.json` records the input identities. The source application is copied unchanged into `vendor/source.dar`.
        |
        |Build with Daml SDK 3.4.11:
        |
        |```sh
        |daml build --package-root library
        |daml build --package-root example
        |daml test --package-root example
        |```
        |
        |`Example:run` is also a runnable Daml Script on a configured Canton participant. It tries the reader's unauthorized action, executes the actor's authorized action, queries the source replacement and core, and tries the completed action again. Use Harmonia's `bindings-check` command for a fresh local Canton execution and comparison with the committed expectation.
        |
        |The library contains no Daml Script dependency. Its binding checks actor, readers, and subject against the actual source contract before exercising the typed choice. Compilation establishes type compatibility; source authority, disclosure, and business predicates still require runtime validation. This adapter supports a consuming action choice that returns its own source template, with primitive fields and arguments. It does not infer a workflow or grant authority.
        |""".stripMargin
      )
    )

  private def literal(
      field: Field,
      value: Json,
      shape: TemplateShape,
      mapping: BindingMapping
  ): Either[String, String] = field.kind match
    case FieldType.Party =>
      value.asString.toRight(s"${field.name} must name an example party").flatMap { name =>
        shape.fields
          .find(other =>
            other.kind == FieldType.Party && mapping
              .example(other.name)
              .flatMap(_.asString)
              .contains(name)
          )
          .map(other => s"party_${other.name}")
          .toRight(s"${field.name}: party $name is not an example source party")
      }
    case FieldType.Text =>
      value.asString
        .filter(_.length <= 1000)
        .map(value => Json.fromString(value).noSpaces)
        .toRight(s"${field.name} must be text of at most 1000 characters")
    case FieldType.Integer =>
      value.asNumber.flatMap(_.toLong).map(_.toString).toRight(s"${field.name} must be an Int64")
    case FieldType.Boolean =>
      value.asBoolean.map(if _ then "True" else "False").toRight(s"${field.name} must be boolean")
    case FieldType.Decimal =>
      value.asString
        .flatMap(value => scala.util.Try(BigDecimal(value)).toOption)
        .filter(value => value.scale <= 10 && value.abs <= BigDecimal("1000000000000"))
        .map { value =>
          val number = value.bigDecimal.toPlainString
          s"(${if number.contains(".") then number else number + ".0"} : Decimal)"
        }
        .toRight(
          s"${field.name} must be a quoted Decimal with at most ten places and magnitude at most one trillion"
        )

  private def configuration(name: String, dependencies: Vector[String], script: Boolean): String =
    s"""sdk-version: 3.4.11
       |name: $name
       |version: 0.1.0
       |source: daml
       |dependencies:
       |  - daml-prim
       |  - daml-stdlib
       |${if script then "  - daml-script\n" else ""}data-dependencies:
       |${dependencies.map(value => s"  - $value").mkString("\n")}
       |""".stripMargin

  private def binding(m: BindingMapping, shape: TemplateShape): String =
    val arguments = if shape.arguments.nonEmpty then s"\n    arguments : Source.${m.choice}" else ""
    val choice = if shape.arguments.nonEmpty then "arguments" else s"Source.${m.choice}"
    val readers = m.readers.map(name => s"value.$name").mkString(", ")
    s"""module ${m.module} where
       |
       |import DA.List (sort)
       |import Harmonia.Action
       |import qualified ${m.sourceModule} as Source
       |
       |-- Generated from the authored mapping. Source authority remains enforced.
       |template Adapter with
       |    actor : Party
       |    readers : [Party]
       |    subject : Text
       |    source : ContractId Source.${m.template}$arguments
       |  where
       |    signatory actor
       |    observer readers
       |
       |    choice Apply : ContractId Adapter
       |      controller actor
       |      do
       |        value <- fetch source
       |        assertMsg "Binding must match source actor, readers, and subject"
       |          (value.${m.actor} == actor && sort [$readers] == sort readers && value.${m.subject} == subject)
       |        replacement <- exercise source $choice
       |        create this with source = replacement
       |
       |    interface instance StepAction for Adapter where
       |      view = StepView with actor; subject
       |      performAction self = do
       |        next <- exercise (fromInterfaceContractId @Adapter self) Apply
       |        pure (toInterfaceContractId @StepAction next)
       |""".stripMargin

  private def example(
      m: BindingMapping,
      shape: TemplateShape,
      sourceValues: Vector[String],
      argumentValues: Vector[String]
  ): String =
    val parties = shape.fields
      .filter(_.kind == FieldType.Party)
      .map { field =>
        s"  party_${field.name} <- allocateParty ${m.example(field.name).get.noSpaces}"
      }
      .mkString("\n")
    val fields =
      shape.fields
        .zip(sourceValues)
        .map((field, value) => s"      ${field.name} = $value")
        .mkString("\n")
    val arguments =
      if shape.arguments.isEmpty then ""
      else
        "\n      arguments = Source." + m.choice + " with\n" + shape.arguments
          .zip(argumentValues)
          .map((field, value) => s"        ${field.name} = $value")
          .mkString("\n")
    val readers = m.readers.map(name => s"party_$name").mkString(", ")
    val subject = m.example(m.subject).get.noSpaces
    s"""module Example where
       |
       |import Daml.Script
       |import Harmonia.Action
       |import Harmonia.Workflow
       |import qualified ${m.sourceModule} as Source
       |import qualified ${m.module} as Binding
       |
       |data Evidence = Evidence with
       |    unauthorizedReader : Bool
       |    sourceStatus : Text
       |    originalActive : Bool
       |    activeSources : Int
       |    workflowStatus : Text
       |    repeatedRejected : Bool
       |    readerVisible : Bool
       |    readerError : Optional Text
       |    repeatedError : Optional Text
       |  deriving (Eq, Show)
       |
       |run : Script Evidence
       |run = script do
       |$parties
       |  original <- submit party_${m.actor} do
       |    createCmd Source.${m.template} with
       |$fields
       |  adapter <- submit party_${m.actor} do
       |    createCmd Binding.Adapter with
       |      actor = party_${m.actor}
       |      readers = [$readers]
       |      subject = $subject
       |      source = original$arguments
       |  process <- submit party_${m.actor} do
       |    createCmd WorkflowInstance with
       |      owner = party_${m.actor}
       |      readers = [$readers]
       |      assignee = party_${m.actor}
       |      subject = $subject
       |      action = toInterfaceContractId @StepAction adapter
       |      status = "waiting"
       |  wrong <- trySubmit party_${m.readers.head} do exerciseCmd process Advance
       |  unauthorizedReader <- case wrong of
       |    Left AuthorizationError{} -> pure True
       |    Left (FailureStatusError failure) | failure.errorId == "DAML_AUTHORIZATION_ERROR" -> pure True
       |    Left error -> abort ("Unexpected reader failure: " <> show error)
       |    Right _ -> pure False
       |  (completed, action) <- submit party_${m.actor} do exerciseCmd process Advance
       |  replacement <- queryContractId party_${m.actor} (fromInterfaceContractId @Binding.Adapter action)
       |  next <- optional (abort "Missing generated binding") pure replacement
       |  application <- queryContractId party_${m.actor} next.source
       |  value <- optional (abort "Missing source replacement") pure application
       |  readerApplication <- queryContractId party_${m.readers.head} next.source
       |  previous <- queryContractId party_${m.actor} original
       |  active <- query @Source.${m.template} party_${m.actor}
       |  workflow <- queryContractId party_${m.actor} completed
       |  current <- optional (abort "Missing completed workflow") pure workflow
       |  repeated <- trySubmit party_${m.actor} do exerciseCmd completed Advance
       |  repeatedRejected <- case repeated of
       |    Left UnhandledException{} -> pure True
       |    Left UserError{} -> pure True
       |    Left (FailureStatusError failure) | failure.errorId == "UNHANDLED_EXCEPTION/DA.Exception.AssertionFailed:AssertionFailed" -> pure True
       |    Left error -> abort ("Unexpected repeated-action failure: " <> show error)
       |    Right _ -> pure False
       |  let readerError = case wrong of Left error -> Some (show error); Right _ -> None
       |  let repeatedError = case repeated of Left error -> Some (show error); Right _ -> None
       |  pure Evidence with
       |    readerVisible = readerApplication /= None
       |    readerError; repeatedError; unauthorizedReader; repeatedRejected
       |    sourceStatus = value.${m.observation}
       |    originalActive = previous /= None
       |    activeSources = length active
       |    workflowStatus = current.status
       |""".stripMargin

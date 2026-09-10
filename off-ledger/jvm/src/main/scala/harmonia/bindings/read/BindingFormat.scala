package harmonia.bindings.read

import cats.syntax.all.*
import harmonia.bindings.model.BindingMapping
import harmonia.stories.read.MarkdownYaml
import io.circe.{Json, JsonObject}

object BindingFormat:
  def read(markdown: String): Either[String, BindingMapping] = for
    json <- MarkdownYaml.read(markdown, "Mapping")
    root <- fields(
      json,
      "mapping",
      Set("binding", "source", "roles", "subject", "arguments", "result", "observe", "example")
    )
    module <- identifier(root("binding").get, "binding", "[A-Z][A-Za-z0-9]*")
    _ <- Either.cond(module != "Example", (), "The generated module cannot be named Example")
    source <- fields(root("source").get, "source", Set("package", "module", "template", "choice"))
    alias <- identifier(source("package").get, "package", "[a-z][a-z0-9-]*")
    sourceModule <- identifier(
      source("module").get,
      "source.module",
      "[A-Z][A-Za-z0-9]*(\\.[A-Z][A-Za-z0-9]*)*"
    )
    template <- identifier(source("template").get, "source.template", "[A-Z][A-Za-z0-9]*")
    choice <- identifier(source("choice").get, "source.choice", "[A-Z][A-Za-z0-9]*")
    roles <- fields(root("roles").get, "roles", Set("actor", "readers"))
    actor <- fieldName(roles("actor").get, "actor")
    readerValues <- roles("readers").get.asArray
      .toRight("readers must be a list of source Party fields")
    readers <- readerValues.traverse(value => fieldName(value, "reader"))
    _ <- Either.cond(
      readers.nonEmpty && readers.size <= 4 && readers.distinct == readers && !readers.contains(
        actor
      ),
      (),
      "Use one to four distinct reader fields, separate from the actor field"
    )
    subject <- fieldName(root("subject").get, "subject")
    observation <- fieldName(root("observe").get, "observe")
    arguments <- root("arguments").get.asObject
      .toRight("arguments must map choice fields to typed values")
    example <- root("example").get.asObject.toRight("example must supply each source field")
    _ <- Either.cond(
      root("result").get.asString.contains("replacement"),
      (),
      "Only action choices returning a replacement contract are supported"
    )
  yield BindingMapping(
    module,
    alias,
    sourceModule,
    template,
    choice,
    actor,
    readers,
    subject,
    arguments,
    observation,
    example
  )

  private def fields(json: Json, name: String, required: Set[String]): Either[String, JsonObject] =
    json.asObject.toRight(s"$name must be a mapping").flatMap { obj =>
      val missing = required -- obj.keys.toSet
      val unknown = obj.keys.toSet -- required
      Either.cond(
        missing.isEmpty && unknown.isEmpty,
        obj,
        s"$name: missing fields ${missing.toVector.sorted.mkString(", ")}; unknown fields ${unknown.toVector.sorted.mkString(", ")}"
      )
    }
  private def identifier(json: Json, name: String, pattern: String): Either[String, String] =
    json.asString
      .filter(value => value.length <= 120 && value.matches(pattern))
      .toRight(s"$name is not a supported identifier")
  private def fieldName(json: Json, name: String): Either[String, String] =
    identifier(json, name, "[a-z][A-Za-z0-9_]*")

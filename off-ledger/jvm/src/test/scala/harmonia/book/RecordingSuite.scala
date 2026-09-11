package harmonia.book

import munit.FunSuite
import harmonia.book.project.PresentStory
import harmonia.examples.{Examples, ExampleKind}
import harmonia.stories.read.{MarkdownYaml, StoryFormat}
import io.circe.Json
import java.nio.file.{Files, Path}
import java.security.MessageDigest

class RecordingSuite extends FunSuite:
  private val root = Path.of(sys.env.getOrElse("HARMONIA_ROOT", ".."))
  test("every registered example has a valid result schema and a nonempty reader presentation") {
    Examples.all.foreach { example =>
      val input = MarkdownYaml
        .read(Files.readString(root.resolve(example.path + "/input.md")), "Scenario")
        .fold(e => fail(e.toString), identity)
      val result = StoryFormat
        .result(
          Files.readString(root.resolve(example.path + "/expected.md")),
          example.kind.resultKind
        )
        .fold(e => fail(e.toString), identity)
      // This checks rendering schema only. Ledger checks obtain actual results independently.
      assert(
        PresentStory(example.kind, input, result, result).as[StoryPresentation].isRight,
        example.id
      )
    }
  }
  test("phase projection preserves raw observations without inventing an action outcome") {
    val expected = Json.obj(
      "core" -> Json.obj("limit" -> Json.fromInt(4)),
      "race" -> Json.obj("winners" -> Json.fromInt(1))
    )
    val actual = expected.mapObject(_.add("race", Json.obj("winners" -> Json.fromInt(2))))
    val presentation = PresentStory(ExampleKind.Boundaries, Json.obj(), expected, actual)
      .as[StoryPresentation]
      .fold(e => fail(e.toString), identity)
    assertEquals(presentation.units(1).actual, actual.hcursor.downField("race").focus.get)
    assert(!presentation.units(1).actual.hcursor.downField("outcome").succeeded)
    assertNotEquals(presentation.units(1).expected, presentation.units(1).actual)
  }
  test("first-draft example inputs and expectations remain byte-identical after relocation") {
    val records = io.circe.parser
      .parse(Files.readString(root.resolve("docs/second-draft/example-moves.json")))
      .toOption
      .get
      .asArray
      .get
    records.foreach { record =>
      val path = record.hcursor.get[String]("after").toOption.get
      val expected = record.hcursor.get[String]("sha256").toOption.get
      val actual = MessageDigest
        .getInstance("SHA-256")
        .digest(Files.readAllBytes(root.resolve(path)))
        .map(b => f"${b & 0xff}%02x")
        .mkString
      assertEquals(actual, expected, path)
    }
  }

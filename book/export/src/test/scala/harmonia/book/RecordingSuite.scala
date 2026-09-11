package harmonia.book

import munit.FunSuite
import harmonia.book.project.{PresentStory, RecordingKind}
import harmonia.examples.{Examples, ExampleKind}
import harmonia.files.MarkdownYaml
import harmonia.stories.read.StoryFormat
import io.circe.Json
import io.circe.syntax.*
import java.nio.file.{Files, Path}
import java.security.MessageDigest

class RecordingSuite extends FunSuite:
  private val root = Path.of(sys.env.getOrElse("HARMONIA_ROOT", "."))
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
        PresentStory(example.kind, input, result, result).units.nonEmpty,
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
    assertEquals(presentation.units(1).actual, actual.hcursor.downField("race").focus.get)
    assert(!presentation.units(1).actual.hcursor.downField("outcome").succeeded)
    assertNotEquals(presentation.units(1).expected, presentation.units(1).actual)
  }
  test("first-draft example inputs and expectations remain byte-identical after relocation") {
    val records = io.circe.parser
      .parse(Files.readString(root.resolve("docs/history/second-draft/example-moves.json")))
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

  test("an ad-hoc regression experiment uses its validated scenario declaration") {
    val input = Files.readString(root.resolve("examples/stories/workflow-approved/input.md"))
    assertEquals(RecordingKind.read("wrong-workflow", input), Right(ExampleKind.Workflow))
    assert(
      RecordingKind
        .read("unknown", input.replace("workflow: approval", "workflow: invented"))
        .isLeft
    )
  }

  test("typed projection preserves a missing observation and the complete independent mismatch") {
    val input = Json.obj(
      "setup" -> Json.obj("application" -> Json.obj("status" -> Json.fromString("pending"))),
      "actions" -> Json.arr(
        Json.obj(
          "id" -> Json.fromString("approve"),
          "actor" -> Json.fromString("Bank"),
          "action" -> Json.fromString("approve-financing")
        )
      )
    )
    val expected = Json.obj(
      "actions" -> Json.arr(
        Json.obj(
          "id" -> Json.fromString("approve"),
          "outcome" -> Json.fromString("committed"),
          "application" -> Json.fromString("approved")
        )
      )
    )
    val actual = Json.obj("actions" -> Json.arr())
    val presentation = PresentStory(ExampleKind.Financing, input, expected, actual)
    assert(presentation.units.head.actual.isNull)
    assertEquals(presentation.units.head.outcomeLabel, "Missing outcome")
    val recording =
      RecordedStory("missing", "Missing", "", input, expected, actual, Json.obj(), presentation)
    assert(recording.differences.nonEmpty)
    assertEquals(recording.asJson.as[RecordedStory], Right(recording))
  }

  test("pinned recordings and source chapters cover the compiler-owned example inventory exactly") {
    val directory = root.resolve("book/edition-0.2/recordings")
    val manifest = io.circe.parser
      .parse(Files.readString(directory.resolve("manifest.json")))
      .toOption
      .get
      .hcursor
      .downField("stories")
      .focus
      .get
      .asObject
      .get
    assertEquals(manifest.keys.toSet, Examples.all.map(_.id).toSet)
    Examples.all.foreach { example =>
      val story = io.circe.parser
        .decode[RecordedStory](Files.readString(directory.resolve(example.id + ".json")))
        .toOption
        .get
      assertEquals(story.kind, example.kind, example.id)
    }
  }

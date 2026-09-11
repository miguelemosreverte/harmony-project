package harmonia.stories.read

import harmonia.files.MarkdownYaml

import munit.FunSuite
import io.circe.Json

class StoryFormatSuite extends FunSuite:
  private val input = """# A story
    |
    |## Scenario
    |
    |```yaml
    |setup:
    |  application:
    |    bank: Northbank
    |    buyer: Alice
    |    status: pending
    |actions:
    |  - id: approval
    |    actor: Northbank
    |    action: approve-financing
    |```
    |""".stripMargin

  test("prose and unrelated illustrative blocks cannot change execution") {
    val prose = "# Edited explanation\n\n```yaml\nthis: is only illustrative\n```\n\n"
    assertEquals(StoryFormat.input("example", input), StoryFormat.input("example", prose + input))
  }
  test("reject ambiguous sections and duplicate YAML fields") {
    assert(StoryFormat.input("example", input + input).isLeft)
    assert(
      StoryFormat
        .input(
          "example",
          input.replace("bank: Northbank", "bank: Northbank\n    bank: SomeoneElse")
        )
        .isLeft
    )
  }
  test("reject unknown fields, actors, and actions") {
    assert(
      StoryFormat
        .input(
          "example",
          input.replace("status: pending", "status: pending\n    secret_default: true")
        )
        .isLeft
    )
    assert(
      StoryFormat.input("example", input.replace("actor: Northbank", "actor: Stranger")).isLeft
    )
    assert(StoryFormat.input("example", input.replace("approve-financing", "send-money")).isLeft)
  }
  test("reject executable YAML tags") {
    assert(
      MarkdownYaml
        .read("## Scenario\n\n```yaml\nx: !!java/object java.lang.Runtime\n```", "Scenario")
        .isLeft
    )
  }
  test("reject YAML indirection and non-text mapping keys") {
    assert(
      MarkdownYaml
        .read("## Scenario\n\n```yaml\nx: &person Alice\ny: *person\n```", "Scenario")
        .isLeft
    )
    assert(MarkdownYaml.read("## Scenario\n\n```yaml\n1: Alice\n```", "Scenario").isLeft)
  }
  test("visibility is a set while the action sequence remains ordered") {
    val result = """## Result
      |
      |```yaml
      |actions:
      |  - id: approval
      |    outcome: committed
      |    application: approved
      |    consumed: true
      |    active_contracts: 1
      |    visible_to: [Northbank, Alice]
      |```
      |""".stripMargin
    assertEquals(
      StoryFormat.result(result),
      StoryFormat.result(result.replace("[Northbank, Alice]", "[Alice, Northbank]"))
    )
  }
  test("generated YAML preserves string types, numbers, and nested values") {
    val json = Json.obj(
      "text" -> Json.fromString("true"),
      "count" -> Json.fromInt(1),
      "party" -> Json.fromString("Northbank")
    )
    assertEquals(
      MarkdownYaml.read(MarkdownYaml.render("Result", "Result", json), "Result"),
      Right(json)
    )
  }

  test("private workflows require distinct roles and explicit private input") {
    val privateInput = input
      .replace("setup:", "workflow: private-approval\nsetup:")
      .replace(
        "status: pending",
        "status: pending\n    reviewer: Olivia\n    private_details: synthetic"
      )
    assert(StoryFormat.input("private", privateInput).isRight)
    assert(
      StoryFormat
        .input("private", privateInput.replace("reviewer: Olivia", "reviewer: Northbank"))
        .isLeft
    )
    assert(
      StoryFormat
        .input("private", privateInput.replace("    private_details: synthetic\n", ""))
        .isLeft
    )
    assert(
      StoryFormat
        .input("private", privateInput.replace("workflow: private-approval", "workflow: approval"))
        .isLeft
    )
  }

  test("privacy observations survive parsing and fail comparison when disclosure changes") {
    val expected = """## Result
      |
      |```yaml
      |actions: []
      |visibility:
      |  Alice:
      |    application: false
      |    progress: true
      |    private_events: 0
      |    progress_events: 2
      |    private_payload_observed: false
      |```
      |""".stripMargin
    val actual =
      expected.replace("private_payload_observed: false", "private_payload_observed: true")
    val differences = for
      left <- StoryFormat.result(expected)
      right <- StoryFormat.result(actual)
    yield harmonia.stories.compare.CompareResults.compare(left, right)
    assertEquals(
      differences.map(_.map(_.path)),
      Right(Vector("$.visibility.Alice.private_payload_observed"))
    )
    assert(StoryFormat.result(expected.replace("private_events: 0", "private_events: -1")).isLeft)
    assert(StoryFormat.result(expected.replace("progress: true", "progress: unknown")).isLeft)
  }

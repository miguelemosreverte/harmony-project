package harmonia.stories.read

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

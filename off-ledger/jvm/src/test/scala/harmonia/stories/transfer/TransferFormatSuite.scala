package harmonia.stories.transfer

import munit.FunSuite
import harmonia.stories.read.StoryFormat

class TransferFormatSuite extends FunSuite:
  private val input = """## Scenario
    |
    |```yaml
    |workflow: atomic-transfer
    |setup:
    |  trade:
    |    buyer: Alice
    |    seller: Seller
    |    source: Source
    |    destination: Destination
    |    settler: Alice
    |    asset: TEST
    |    quantity: "7.1250000001"
    |  destination_receipts: accept
    |actions:
    |  - id: settlement
    |    actor: Alice
    |    action: settle
    |```
    |""".stripMargin

  test("preserve exact ten-place quantities through the script boundary") {
    val quantity = StoryFormat
      .input("exact", input)
      .map(_.scriptInput.hcursor.downField("setup").get[String]("quantity"))
    assertEquals(quantity, Right(Right("7.1250000001")))
    assert(StoryFormat.input("too-precise", input.replace("7.1250000001", "7.12500000001")).isLeft)
    assert(StoryFormat.input("too-large", input.replace("7.1250000001", "1000000000001")).isLeft)
    assert(StoryFormat.input("negative", input.replace("7.1250000001", "-1")).isLeft)
  }

  test("the settler is one of four distinct parties, never an added fifth identity") {
    assert(
      StoryFormat.input("outsider", input.replace("settler: Alice", "settler: Outsider")).isLeft
    )
    assert(
      StoryFormat
        .input("same-custodian", input.replace("destination: Destination", "destination: Source"))
        .isLeft
    )
    assert(
      StoryFormat
        .input("source-settler", input.replace("settler: Alice", "settler: Source"))
        .isRight
    )
  }

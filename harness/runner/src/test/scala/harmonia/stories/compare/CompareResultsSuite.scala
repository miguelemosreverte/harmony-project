package harmonia.stories.compare

import munit.FunSuite
import io.circe.parser.parse

class CompareResultsSuite extends FunSuite:
  private def json(text: String) = parse(text).toOption.get

  test("object formatting and key order do not affect comparison") {
    assertEquals(
      CompareResults.compare(json("""{"a":1,"b":2}"""), json("""{"b":2,"a":1}""")),
      Vector.empty
    )
  }
  test("unexpected effects and missing values cannot pass subset comparison") {
    val result = CompareResults.compare(
      json("""{"actions":[]}"""),
      json("""{"actions":[{"outcome":"committed"}]}""")
    )
    assertEquals(result.map(_.path), Vector("$.actions[0]"))
  }
  test("action order and changed outcome remain meaningful") {
    val result = CompareResults.compare(
      json("""{"actions":["rejected","committed"]}"""),
      json("""{"actions":["committed","rejected"]}""")
    )
    assertEquals(result.map(_.path), Vector("$.actions[0]", "$.actions[1]"))
  }

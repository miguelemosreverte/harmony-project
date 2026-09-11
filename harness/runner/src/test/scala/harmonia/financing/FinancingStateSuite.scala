package harmonia.financing

import io.circe.Json
import io.circe.syntax.*
import munit.FunSuite

class FinancingStateSuite extends FunSuite:
  private val observed = FinancingState(
    "buyer",
    "a" * 64,
    ProgressStatus.Waiting,
    None,
    None,
    true,
    Vector(FinancingAction.Continue),
    "Buyer continues"
  )

  test("hidden private state remains absent while a shared continuation is available") {
    assertEquals(observed.asJson.as[FinancingState], Right(observed))
    assertEquals(observed.application, None)
    assertEquals(observed.eligible, Vector(FinancingAction.Continue))
  }

  test("unknown statuses and actions cannot become an apparently valid view") {
    Vector(
      "workflow" -> Json.fromString("done-ish"),
      "application" -> Json.fromString("unknown"),
      "eligible" -> Json.arr(Json.fromString("grant-authority"))
    ).foreach { (field, value) =>
      assert(observed.asJson.mapObject(_.add(field, value)).as[FinancingState].isLeft)
    }
  }

  test("missing evidence and progression fail decoding rather than becoming empty success") {
    Vector("workflow", "evidence_available", "eligible").foreach { field =>
      assert(observed.asJson.mapObject(_.remove(field)).as[FinancingState].isLeft)
    }
  }

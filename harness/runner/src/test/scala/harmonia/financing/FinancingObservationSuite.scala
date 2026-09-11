package harmonia.financing

import com.daml.ledger.api.v2.ValueOuterClass.Identifier
import harmonia.ledger.client.{ActiveContract, LedgerSnapshot, LedgerDecodingFailure, LedgerValue}
import io.circe.Json
import munit.FunSuite

class FinancingObservationSuite extends FunSuite:
  private def application(fields: Map[String, Json]) = ActiveContract(
    "application-1",
    Identifier.newBuilder().setModuleName("PrivateFinancing").setEntityName("Application").build(),
    fields
  )
  private def text(value: String) = Json.obj("text" -> Json.fromString(value))
  private val valid = Map("status" -> text("pending"), "privateDetails" -> text("synthetic"))
  private def observe(contract: ActiveContract) =
    FinancingObservation.read(LedgerSnapshot(Vector(contract), Vector.empty))

  test("an undisclosed application is absent; malformed visible data is a failure") {
    assertEquals(
      FinancingObservation
        .read(LedgerSnapshot(Vector.empty, Vector.empty))
        .toOption
        .get
        .application,
      None
    )
    Vector(
      valid - "status",
      valid - "privateDetails",
      valid.updated("status", text("unknown")),
      valid.updated("privateDetails", Json.obj("bool" -> Json.True))
    ).foreach { fields =>
      val result = observe(application(fields))
      assert(result.isLeft)
      assert(result.swap.toOption.get.getMessage.contains("PrivateFinancing.Application"))
    }
    assertEquals(
      observe(application(valid)).toOption.get.application.map(_.status),
      Some(ApplicationStatus.Pending)
    )
  }

  test("a visible progress contract cannot claim it was not observed") {
    val progress = ActiveContract(
      "progress-1",
      Identifier
        .newBuilder()
        .setModuleName("Harmonia.SharedProgress")
        .setEntityName("SharedProgress")
        .build(),
      Map("status" -> text("not-visible"))
    )
    assert(observe(progress).isLeft)
  }

  test("required ledger text never silently becomes an empty string") {
    intercept[LedgerDecodingFailure](application(Map.empty).text("status"))
    intercept[LedgerDecodingFailure](
      application(Map("status" -> Json.obj("bool" -> Json.True))).text("status")
    )
    assertEquals(application(Map("status" -> text(""))).text("status"), "")
  }

  test(
    "omitted protobuf collections are empty; malformed collections and competing variants fail"
  ) {
    assertEquals(LedgerValue.plain(Json.obj("list" -> Json.obj())), Json.arr())
    Vector(
      Json.obj("list" -> Json.obj("elements" -> Json.fromString("invalid"))),
      Json.obj("record" -> Json.fromString("invalid")),
      Json.obj("text" -> Json.fromString("pending"), "bool" -> Json.True)
    ).foreach(value => intercept[LedgerDecodingFailure](LedgerValue.plain(value)))
  }

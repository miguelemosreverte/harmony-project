package harmonia.composition

import harmonia.composition.ledger.ComposerSnapshot
import harmonia.ledger.client.ActiveContract
import com.daml.ledger.api.v2.ValueOuterClass.Identifier
import io.circe.Json
import munit.FunSuite

class CompositionObservationSuite extends FunSuite:
  private val parties = Map("bank" -> "bank-party", "buyer" -> "buyer-party")
  private def text(value: String) = Json.obj("text" -> Json.fromString(value))
  private def contract(entity: String, fields: Map[String, Json]) = ActiveContract(
    entity + "-1",
    Identifier.newBuilder().setModuleName("Composer").setEntityName(entity).build(),
    fields
  )
  private def read(values: ActiveContract*) =
    ComposerSnapshot.read(values.toVector, parties, "bank")

  test("an absent workspace differs from a malformed visible workspace") {
    assertEquals(read().toOption.get.available, false)
    assert(read(contract("Workspace", Map.empty)).isLeft)
    val empty = contract("Workspace", Map("references" -> Json.obj("list" -> Json.obj())))
    assertEquals(
      read(empty).toOption.get,
      CompositionState(true, true, 8, Vector.empty, Vector.empty)
    )
  }

  test("a draft with missing or malformed steps cannot become an empty successful proposal") {
    val fields = Map("name" -> text("Plan"), "reference" -> text("one"))
    assert(read(contract("Draft", fields)).isLeft)
    assert(read(contract("Draft", fields.updated("steps", text("invalid")))).isLeft)
  }

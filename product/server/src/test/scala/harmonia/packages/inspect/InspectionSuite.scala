package harmonia.packages.inspect

import io.circe.Json
import io.circe.syntax.*
import munit.FunSuite

class InspectionSuite extends FunSuite:
  private val metadata = Json.obj(
    "main_package_id" -> Json.fromString("verified-id"),
    "packages" -> Json.obj("verified-id" -> Json.obj("name" -> Json.fromString("application")))
  )

  test("compiler metadata becomes inspected facts after the LF identity agrees") {
    val result = InspectedDar.read(metadata, "verified-id", "2.2").toOption.get
    assertEquals(result.packageId, "verified-id")
    assertEquals(result.lf, "2.2")
    assertEquals(result.packages("verified-id").hcursor.get[String]("name"), Right("application"))
  }

  test("a namesake compiler result cannot replace the verified package identity") {
    assert(InspectedDar.read(metadata, "different-id", "2.2").isLeft)
  }

  test("missing or malformed dependency metadata fails at inspection") {
    val invalid = Vector(
      Json.obj("main_package_id" -> Json.fromString("verified-id")),
      metadata.mapObject(_.add("packages", Json.arr())),
      metadata.mapObject(_.add("main_package_id", Json.Null))
    )
    invalid.foreach(value => assert(InspectedDar.read(value, "verified-id", "2.2").isLeft))
  }

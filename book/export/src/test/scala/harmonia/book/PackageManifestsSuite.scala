package harmonia.book

import munit.FunSuite
import harmonia.book.atlas.PackageManifests

class PackageManifestsSuite extends FunSuite:
  private val valid =
    "name: example\nversion: '1.0'\ndependencies: [daml-prim, daml-stdlib]\ndata-dependencies:\n  - '../interfaces/.daml/dist/interfaces-1.0.dar' # actual YAML comment\n"
  test("manifest dependencies are parsed as YAML data, including comments and flow lists") {
    val value = PackageManifests.decode("daml.yaml", valid)
    assertEquals(value.name, "example")
    assertEquals(value.version, "1.0")
    assertEquals(value.dependencies, Vector("daml-prim", "daml-stdlib"))
    assertEquals(value.imports, Vector("../interfaces/.daml/dist/interfaces-1.0.dar"))
    assertEquals(value.sha256.length, 64)
  }
  test("duplicate fields and non-text dependency entries fail instead of becoming graph edges") {
    intercept[Exception](PackageManifests.decode("daml.yaml", valid + "name: duplicate\n"))
    intercept[IllegalArgumentException](
      PackageManifests.decode("daml.yaml", valid.replace("[daml-prim, daml-stdlib]", "[37]"))
    )
  }

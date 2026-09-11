package harmonia.app

import munit.FunSuite
import scala.compiletime.testing.typeChecks

class ProductBoundarySuite extends FunSuite:
  test("the service compiles with its public API and supplied connections") {
    assert(typeChecks("Option.empty[harmonia.app.Connections]"))
    assert(typeChecks("Option.empty[harmonia.workspace.WorkspaceSnapshot]"))
  }

  test("book, demo, golden runner, and release classes are unavailable to the product") {
    assert(!typeChecks("harmonia.book.ExportBook"))
    assert(!typeChecks("harmonia.demo.Demo"))
    assert(!typeChecks("harmonia.stories.run.CheckStories"))
    assert(!typeChecks("harmonia.release.CheckRelease"))
    assert(!typeChecks("harmonia.composition.model.CompositionResult"))
    assert(!typeChecks("harmonia.packages.workspace.BuilderResult"))
    assert(!typeChecks("harmonia.ledger.auth.DemoCredentials"))
  }

  test("connection configuration requires complete participants and valid local ports") {
    val participant = """{"port":5001,"party":"party-id","user":"user-id","token_file":"token"}"""
    val configured =
      s"""{"catalog_dar":"application.dar","package_exports":"exports","participants":{"bank":$participant,"buyer":$participant,"reviewer":$participant}}"""
    assert(io.circe.parser.decode[ServerConfig](configured).isRight)
    assert(io.circe.parser.decode[ServerConfig](configured.replace("5001", "0")).isLeft)
    assert(io.circe.parser.decode[ServerConfig](configured.replace("reviewer", "unknown")).isLeft)
  }

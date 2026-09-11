package harmonia.app.http

import cats.effect.unsafe.implicits.global
import harmonia.composition.CompositionCommand
import harmonia.financing.FinancingAction
import harmonia.workspace.{ActionRequest, WorkspaceCommand}
import io.circe.parser.parse
import io.circe.syntax.*
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets.UTF_8
import munit.FunSuite

class RequestBodySuite extends FunSuite:
  private def stream(value: String) = new ByteArrayInputStream(value.getBytes(UTF_8))
  private val financing = """{"id":"request-1","action":"approve-financing","version":"abc"}"""
  private val composition =
    """{"id":"request-2","action":"compose-advance","version":"abc","input":{"reference":"plan-1","step":"approve"}}"""

  test("existing financing and composition envelopes remain the shared wire format") {
    val examples = Vector(
      financing -> ActionRequest(
        "request-1",
        WorkspaceCommand.Financing(FinancingAction.Approve),
        "abc"
      ),
      composition -> ActionRequest(
        "request-2",
        WorkspaceCommand.Composition(CompositionCommand.Advance("plan-1", "approve")),
        "abc"
      )
    )
    examples.foreach { (wire, request) =>
      assertEquals(RequestBody.action(stream(wire)).unsafeRunSync(), request)
      assertEquals(request.asJson, parse(wire).toOption.get)
    }
  }

  test("actor injection and malformed command envelopes fail before submission") {
    val invalid = Vector(
      financing.dropRight(1) + ",\"actor\":\"bank\"}",
      financing.replace(",\"version\":\"abc\"", ""),
      financing.dropRight(1) + ",\"input\":{}}",
      composition.replace("\"approve\"", "null"),
      "[]",
      "{"
    )
    invalid.foreach(body =>
      intercept[IllegalArgumentException](RequestBody.action(stream(body)).unsafeRunSync())
    )
  }

  test("action reading accepts the byte limit and rejects its first excess byte") {
    val padded = financing + " " * (16384 - financing.getBytes(UTF_8).length)
    assertEquals(RequestBody.action(stream(padded)).unsafeRunSync().id, "request-1")
    val body = stream(padded + "  ")
    val error = intercept[IllegalArgumentException](RequestBody.action(body).unsafeRunSync())
    assertEquals(error.getMessage, "Request exceeds 16 KiB")
    assertEquals(body.available(), 1)
  }

  test("package requests accept exactly one textual field and bound their read") {
    val valid = """{"source":"metadata"}"""
    val padded = valid + " " * (1024 - valid.length)
    assertEquals(RequestBody.packageKey(stream(padded), "source").unsafeRunSync(), "metadata")
    Vector(
      "{}",
      "[]",
      "{",
      """{"source":1}""",
      """{"source":"metadata","actor":"bank"}""",
      padded + " "
    )
      .foreach(body =>
        intercept[IllegalArgumentException](
          RequestBody.packageKey(stream(body), "source").unsafeRunSync()
        )
      )
  }

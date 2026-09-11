package harmonia.workspace

import harmonia.financing.*
import harmonia.composition.*
import harmonia.protocol.{LedgerUpdate, SubmissionStatus}
import io.circe.Json
import io.circe.syntax.*
import munit.FunSuite

class WorkspaceContractSuite extends FunSuite:
  test("typed workspace encoding preserves the existing HTTP fields and command identity") {
    val state = WorkspaceSnapshot(
      FinancingState(
        "bank",
        "version",
        ProgressStatus.Waiting,
        Some(ApplicationStatus.Pending),
        Some("synthetic"),
        false,
        Vector(FinancingAction.Approve),
        "Waiting for bank approval"
      ),
      CompositionState(true, true, 8, Vector.empty, Vector.empty),
      Vector(
        SubmissionView("one", "bank", "approve-financing", SubmissionStatus.Committed, "Confirmed")
      ),
      Vector(LedgerUpdate(Some("update"), Some("command"), Vector("Application · created")))
    )
    val json = state.asJson
    assertEquals(
      json.asObject.get.keys.toSet,
      Set(
        "actor",
        "version",
        "workflow",
        "application",
        "private_details",
        "evidence_available",
        "eligible",
        "current_step",
        "composer",
        "jobs",
        "history"
      )
    )
    assertEquals(json.hcursor.downField("jobs").downArray.get[String]("actor"), Right("bank"))
    assertEquals(
      json.hcursor.downField("history").downArray.get[String]("command_id"),
      Right("command")
    )
    assertEquals(json.as[WorkspaceSnapshot], Right(state))
  }

  test("composition routing retains its command family and exact external payload") {
    val command = WorkspaceCommand.Composition(CompositionCommand.Advance("flow", "review"))
    assertEquals(command.wire, "compose-advance")
    assertEquals(
      command.parameters,
      Some(Json.obj("reference" -> Json.fromString("flow"), "step" -> Json.fromString("review")))
    )
    assertEquals(WorkspaceCommand.read(command.wire, command.parameters), Right(command))
    assert(WorkspaceCommand.read("approve-financing", command.parameters).isLeft)
  }

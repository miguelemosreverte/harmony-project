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

  test("typed editor plans and HTTP input enforce the same composition constraints") {
    import harmonia.composition.model.*
    val step = PlannedStep("approval", "lender", CompositionActor.Bank, CompositionAction.Approve)
    val plan = Composition("Offer", "offer-1", Vector(step))
    assertEquals(Composition.validate(plan), Right(plan))
    assertEquals(Composition.read(plan.json), Right(plan))
    val invalid = Vector(
      plan.copy(name = ""),
      plan.copy(steps = Vector.empty),
      plan.copy(steps = Vector(step, step)),
      plan.copy(steps = Vector(step, step.copy(id = "other", actor = CompositionActor.Buyer)))
    )
    invalid.foreach { value =>
      assert(Composition.validate(value).isLeft)
      assert(Composition.read(value.json).isLeft)
    }
  }

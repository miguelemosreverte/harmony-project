package harmonia.composition.ledger

import harmonia.workspace.WorkspaceCommand
import harmonia.ledger.client.{ActiveContract, LedgerValue as V, LiveLedger}
import com.daml.ledger.api.v2.ValueOuterClass.Value

object ComposerCommands:
  def select(
      command: WorkspaceCommand,
      requestId: String,
      contracts: Vector[ActiveContract],
      party: String,
      parties: Map[String, String]
  ): Option[(ActiveContract, String, Value)] =
    def find(module: String, entity: String, reference: Option[String] = None) = contracts.find(c =>
      c.template.getModuleName == module && c.template.getEntityName == entity && reference.forall(
        _ == c.text("reference")
      )
    )
    command match
      case WorkspaceCommand.Propose(plan) =>
        val steps = plan.steps.map(step =>
          V.record(
            "id" -> V.text(step.id),
            "role" -> V.text(step.role),
            "actor" -> V.party(parties(step.actor.wire)),
            "action" -> V.text(step.action.wire)
          )
        )
        find("Composer", "Workspace").map(c =>
          (
            c,
            "Propose",
            V.record(
              "name" -> V.text(plan.name),
              "reference" -> V.text(plan.reference),
              "steps" -> V.list(steps)
            )
          )
        )
      case WorkspaceCommand.Accept(reference) =>
        find("Composer", "Draft", Some(reference)).map(c => (c, "Accept", LiveLedger.emptyArgument))
      case WorkspaceCommand.Cancel(reference) =>
        find("Composer", "Draft", Some(reference)).map(c => (c, "Cancel", LiveLedger.emptyArgument))
      case WorkspaceCommand.Advance(reference, step) =>
        find("Harmonia.Process.Engine", "ProcessInstance", Some(reference)).map(c =>
          (
            c,
            "AdvanceStep",
            V.record(
              "step" -> V.text(step),
              "actor" -> V.party(party),
              "request" -> V.text(requestId)
            )
          )
        )
      case WorkspaceCommand.Financing(_) => None

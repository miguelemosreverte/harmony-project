package harmonia.composition.ledger

/** @module.slice
  *   composition
  * @module.role
  *   Dispatch an authorized command
  * @module.summary
  *   The command interpreter maps a validated composition request to the intended Daml choice.
  */

import harmonia.composition.CompositionCommand
import harmonia.ledger.client.{ActiveContract, LedgerExercise, LedgerValue as V, LiveLedger}

object ComposerCommands:
  def select(
      command: CompositionCommand,
      requestId: String,
      contracts: Vector[ActiveContract],
      party: String,
      parties: Map[String, String]
  ): Option[LedgerExercise] =
    def find(module: String, entity: String, reference: Option[String] = None) = contracts.find(c =>
      c.template.getModuleName == module && c.template.getEntityName == entity && reference.forall(
        _ == c.text("reference")
      )
    )
    command match
      case CompositionCommand.Propose(plan) =>
        val steps = plan.steps.map(step =>
          V.record(
            "id" -> V.text(step.id),
            "role" -> V.text(step.role),
            "actor" -> V.party(parties(step.actor.wire)),
            "action" -> V.text(step.action.wire)
          )
        )
        find("Composer", "Workspace").map(c =>
          LedgerExercise(
            c,
            "Propose",
            V.record(
              "name" -> V.text(plan.name),
              "reference" -> V.text(plan.reference),
              "steps" -> V.list(steps)
            )
          )
        )
      case CompositionCommand.Accept(reference) =>
        find("Composer", "Draft", Some(reference)).map(c =>
          LedgerExercise(c, "Accept", LiveLedger.emptyArgument)
        )
      case CompositionCommand.Cancel(reference) =>
        find("Composer", "Draft", Some(reference)).map(c =>
          LedgerExercise(c, "Cancel", LiveLedger.emptyArgument)
        )
      case CompositionCommand.Advance(reference, step) =>
        find("Harmonia.Process.Engine", "ProcessInstance", Some(reference)).map(c =>
          LedgerExercise(
            c,
            "AdvanceStep",
            V.record(
              "step" -> V.text(step),
              "actor" -> V.party(party),
              "request" -> V.text(requestId)
            )
          )
        )

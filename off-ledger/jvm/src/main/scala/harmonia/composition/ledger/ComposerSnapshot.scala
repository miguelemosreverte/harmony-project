package harmonia.composition.ledger

import cats.syntax.all.*
import harmonia.composition.*
import harmonia.composition.model.{CompositionActor, PlannedStep}
import harmonia.ledger.client.{ActiveContract, LedgerDecodingFailure}
import CompositionContracts.*

/** Decodes visible ledger records and projects the same model the browser receives. */
object ComposerSnapshot:
  def read(
      contracts: Vector[ActiveContract],
      parties: Map[String, String],
      actor: String
  ): Either[LedgerDecodingFailure, CompositionState] =
    def identified(c: ActiveContract, module: String, entity: String) =
      c.template.getModuleName == module && c.template.getEntityName == entity
    def missing(location: String) = LedgerDecodingFailure(location, "required binding is missing")
    def name(party: String) =
      parties.find(_._2 == party).map(_._1).toRight(missing("Composer.party"))
    def planned(step: Planned): Either[LedgerDecodingFailure, PlannedStep] = for
      owner <- name(step.actor)
      bound <- CompositionActor.values.find(_.wire == owner).toRight(missing("Composer.step.actor"))
    yield PlannedStep(step.id, step.role, bound, step.action)

    def processView(process: Process): Either[LedgerDecodingFailure, ComposedProcess] =
      val completed = process.completed.toSet
      val roles = process.roles.map(role => role.name -> role.party).toMap
      val bindings = process.bindings.map(binding => binding.step -> binding.action).toMap
      for
        _ <- Either.cond(
          process.definition.steps.nonEmpty,
          (),
          missing("ProcessInstance.definition.steps")
        )
        steps <- process.definition.steps.traverse { step =>
          for
            party <- roles.get(step.role).toRight(missing(s"ProcessInstance.roles.${step.role}"))
            owner <- name(party)
            bound <- bindings.get(step.id).toRight(missing(s"ProcessInstance.bindings.${step.id}"))
            source = contracts.find(_.id == bound)
            generated = source.exists(_.template.getModuleName == "GeneratedFinancing")
            application <- source
              .traverse { contract =>
                if generated then
                  contract.decode[Adapter].map(adapter => contracts.find(_.id == adapter.source))
                else Right(Some(contract))
              }
              .map(_.flatten)
            status <- application.traverse(_.decode[SourceStatus]).map(_.map(_.status))
          yield
            val enabled = !completed(step.id) && step.prerequisites.forall(completed)
            ExecutionStep(
              step.id,
              step.role,
              owner,
              completed(step.id),
              enabled,
              enabled && owner == actor,
              application.map(c => c.template.getModuleName + "." + c.template.getEntityName),
              status,
              if generated then Integration.Generated else Integration.Direct
            )
        }
      yield ComposedProcess(
        process.definition.name,
        process.reference,
        steps.forall(_.completed),
        steps
      )

    for
      workspace <- contracts
        .find(c => identified(c, "Composer", "Workspace"))
        .traverse(_.decode[Workspace])
      references = workspace.fold(Vector.empty[String])(_.references)
      drafts <- contracts.filter(c => identified(c, "Composer", "Draft")).traverse { contract =>
        for
          draft <- contract.decode[Draft]
          steps <- draft.steps.traverse(planned)
        yield Proposal(draft.name, draft.reference, steps, actor == "buyer", actor == "bank")
      }
      visibleProcesses <- contracts
        .filter(c => identified(c, "Harmonia.Process.Engine", "ProcessInstance"))
        .traverse(_.decode[Process])
      processes <- visibleProcesses
        .filter(p => references.contains(p.reference))
        .traverse(processView)
    yield CompositionState(
      workspace.nonEmpty,
      actor == "bank" && workspace.nonEmpty && references.size < 8,
      math.max(0, 8 - references.size),
      drafts,
      processes
    )

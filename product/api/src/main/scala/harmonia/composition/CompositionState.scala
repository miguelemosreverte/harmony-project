package harmonia.composition

import harmonia.composition.model.PlannedStep
import io.circe.{Codec, Decoder, Encoder}

final case class Proposal(
    name: String,
    reference: String,
    steps: Vector[PlannedStep],
    canAccept: Boolean,
    canCancel: Boolean
)
object Proposal:
  given Codec.AsObject[Proposal] =
    Codec.forProduct5("name", "reference", "steps", "can_accept", "can_cancel")(Proposal.apply)(v =>
      (v.name, v.reference, v.steps, v.canAccept, v.canCancel)
    )

final case class ExecutionStep(
    id: String,
    role: String,
    actor: String,
    completed: Boolean,
    enabled: Boolean,
    canExecute: Boolean,
    source: Option[String],
    status: Option[String],
    integration: Integration
)
enum Integration(val wire: String):
  case Direct extends Integration("direct")
  case Generated extends Integration("generated")
object Integration:
  given Encoder[Integration] = Encoder.encodeString.contramap(_.wire)
  given Decoder[Integration] = Decoder.decodeString.emap(s =>
    Integration.values.find(_.wire == s).toRight(s"Unknown integration: $s")
  )
object ExecutionStep:
  given Codec.AsObject[ExecutionStep] = Codec.forProduct9(
    "id",
    "role",
    "actor",
    "completed",
    "enabled",
    "can_execute",
    "source",
    "status",
    "integration"
  )(ExecutionStep.apply)(v =>
    (v.id, v.role, v.actor, v.completed, v.enabled, v.canExecute, v.source, v.status, v.integration)
  )

final case class ComposedProcess(
    name: String,
    reference: String,
    complete: Boolean,
    steps: Vector[ExecutionStep]
)
object ComposedProcess:
  given Codec.AsObject[ComposedProcess] =
    Codec.forProduct4("name", "reference", "complete", "steps")(ComposedProcess.apply)(v =>
      (v.name, v.reference, v.complete, v.steps)
    )

final case class CompositionState(
    available: Boolean,
    canPropose: Boolean,
    remainingProposals: Int,
    drafts: Vector[Proposal],
    processes: Vector[ComposedProcess]
)
object CompositionState:
  given Codec.AsObject[CompositionState] =
    Codec.forProduct5("available", "can_propose", "remaining_proposals", "drafts", "processes")(
      CompositionState.apply
    )(v => (v.available, v.canPropose, v.remainingProposals, v.drafts, v.processes))

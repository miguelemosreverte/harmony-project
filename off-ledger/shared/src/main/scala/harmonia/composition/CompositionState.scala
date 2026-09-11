package harmonia.composition

import harmonia.composition.model.PlannedStep
import io.circe.Decoder

final case class Proposal(
    name: String,
    reference: String,
    steps: Vector[PlannedStep],
    canAccept: Boolean,
    canCancel: Boolean
)
object Proposal:
  given Decoder[Proposal] =
    Decoder.forProduct5("name", "reference", "steps", "can_accept", "can_cancel")(Proposal.apply)

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
  given Decoder[Integration] = Decoder.decodeString.emap(s =>
    Integration.values.find(_.wire == s).toRight(s"Unknown integration: $s")
  )
object ExecutionStep:
  given Decoder[ExecutionStep] = Decoder.forProduct9(
    "id",
    "role",
    "actor",
    "completed",
    "enabled",
    "can_execute",
    "source",
    "status",
    "integration"
  )(ExecutionStep.apply)

final case class ComposedProcess(
    name: String,
    reference: String,
    complete: Boolean,
    steps: Vector[ExecutionStep]
)
object ComposedProcess:
  given Decoder[ComposedProcess] =
    Decoder.forProduct4("name", "reference", "complete", "steps")(ComposedProcess.apply)

final case class CompositionState(
    available: Boolean,
    canPropose: Boolean,
    remainingProposals: Int,
    drafts: Vector[Proposal],
    processes: Vector[ComposedProcess]
)
object CompositionState:
  given Decoder[CompositionState] =
    Decoder.forProduct5("available", "can_propose", "remaining_proposals", "drafts", "processes")(
      CompositionState.apply
    )

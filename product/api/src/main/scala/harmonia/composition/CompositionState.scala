package harmonia.composition

import harmonia.protocol.JsonCodec
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
  given Codec.AsObject[Proposal] = JsonCodec.derived[Proposal]

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
  given Codec.AsObject[ExecutionStep] = JsonCodec.derived[ExecutionStep]

final case class ComposedProcess(
    name: String,
    reference: String,
    complete: Boolean,
    steps: Vector[ExecutionStep]
)
object ComposedProcess:
  given Codec.AsObject[ComposedProcess] = JsonCodec.derived[ComposedProcess]

final case class CompositionState(
    available: Boolean,
    canPropose: Boolean,
    remainingProposals: Int,
    drafts: Vector[Proposal],
    processes: Vector[ComposedProcess]
)
object CompositionState:
  given Codec.AsObject[CompositionState] = JsonCodec.derived[CompositionState]

package harmonia.composition.ledger

import harmonia.composition.model.CompositionAction
import io.circe.Decoder

/** The ledger payloads this feature reads; field names stop at this boundary. */
private[ledger] object CompositionContracts:
  final case class Workspace(references: Vector[String])
  given Decoder[Workspace] = Decoder.forProduct1("references")(Workspace.apply)

  final case class Planned(id: String, role: String, actor: String, action: CompositionAction)
  given Decoder[Planned] = Decoder.forProduct4("id", "role", "actor", "action")(Planned.apply)
  final case class Draft(name: String, reference: String, steps: Vector[Planned])
  given Decoder[Draft] = Decoder.forProduct3("name", "reference", "steps")(Draft.apply)

  final case class Step(id: String, role: String, prerequisites: Vector[String])
  given Decoder[Step] = Decoder.forProduct3("id", "role", "prerequisites")(Step.apply)
  final case class Definition(name: String, steps: Vector[Step])
  given Decoder[Definition] = Decoder.forProduct2("name", "steps")(Definition.apply)
  final case class Role(name: String, party: String)
  given Decoder[Role] = Decoder.forProduct2("name", "party")(Role.apply)
  final case class Binding(step: String, action: String)
  given Decoder[Binding] = Decoder.forProduct2("step", "action")(Binding.apply)
  final case class Process(
      reference: String,
      definition: Definition,
      completed: Vector[String],
      roles: Vector[Role],
      bindings: Vector[Binding]
  )
  given Decoder[Process] =
    Decoder.forProduct5("reference", "definition", "completed", "roles", "bindings")(Process.apply)

  final case class Adapter(source: String)
  given Decoder[Adapter] = Decoder.forProduct1("source")(Adapter.apply)

  final case class SourceStatus(status: String)
  given Decoder[SourceStatus] = Decoder.forProduct1("status")(SourceStatus.apply)

package harmonia.book

import io.circe.{Codec, Decoder, Encoder, Json}
import harmonia.examples.ExampleKind
import harmonia.stories.compare.CompareResults

/** A reader unit is either a real attempted action or an explicitly labeled verification phase. */
final case class StoryUnit(
    id: String,
    actor: String,
    action: String,
    expected: Json,
    actual: Json,
    observedState: String,
    outcomeLabel: String
)
object StoryUnit:
  given Codec.AsObject[StoryUnit] = Codec.forProduct7(
    "id",
    "actor",
    "action",
    "expected",
    "actual",
    "observed_state",
    "outcome_label"
  )(StoryUnit.apply)(v =>
    (v.id, v.actor, v.action, v.expected, v.actual, v.observedState, v.outcomeLabel)
  )

final case class StoryPresentation(
    kind: ExampleKind,
    subtitle: String,
    start: String,
    startDetail: String,
    operation: String,
    units: Vector[StoryUnit]
)
object StoryPresentation:
  private val fields: Codec.AsObject[StoryPresentation] =
    Codec.forProduct6("kind", "subtitle", "start", "start_detail", "operation", "units")(
      StoryPresentation.apply
    )(v => (v.kind, v.subtitle, v.start, v.startDetail, v.operation, v.units))
  given Encoder.AsObject[StoryPresentation] = fields
  given Decoder[StoryPresentation] = fields.emap(p =>
    Either.cond(
      p.units.nonEmpty && p.units.map(_.id).distinct.size == p.units.size,
      p,
      "A recording needs distinct presentation units"
    )
  )

final case class RecordedStory(
    id: String,
    title: String,
    description: String,
    input: Json,
    expected: Json,
    actual: Json,
    provenance: Json,
    presentation: StoryPresentation
):
  def kind: ExampleKind = presentation.kind
  def isBoundaryReport: Boolean = kind == ExampleKind.Boundaries
  def units: Vector[StoryUnit] = presentation.units
  def expectedActions: Vector[Json] = units.map(_.expected)
  def actualActions: Vector[Json] = units.map(_.actual)
  def differences = CompareResults.compare(expected, actual)

object RecordedStory:
  given Codec.AsObject[RecordedStory] = Codec.forProduct8(
    "id",
    "title",
    "description",
    "input",
    "expected",
    "actual",
    "provenance",
    "presentation"
  )(RecordedStory.apply)(v =>
    (v.id, v.title, v.description, v.input, v.expected, v.actual, v.provenance, v.presentation)
  )

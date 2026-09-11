package harmonia.book

import io.circe.{Decoder, Json}
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
  given Decoder[StoryUnit] = Decoder.forProduct7(
    "id",
    "actor",
    "action",
    "expected",
    "actual",
    "observed_state",
    "outcome_label"
  )(StoryUnit.apply)

final case class StoryPresentation(
    kind: ExampleKind,
    subtitle: String,
    start: String,
    startDetail: String,
    operation: String,
    units: Vector[StoryUnit]
)
object StoryPresentation:
  given Decoder[StoryPresentation] = Decoder
    .forProduct6("kind", "subtitle", "start", "start_detail", "operation", "units")(
      StoryPresentation.apply
    )
    .emap(p =>
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
  given Decoder[RecordedStory] = Decoder.forProduct8(
    "id",
    "title",
    "description",
    "input",
    "expected",
    "actual",
    "provenance",
    "presentation"
  )(RecordedStory.apply)

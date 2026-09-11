package harmonia.examples

import harmonia.stories.model.ResultKind
import io.circe.{Decoder, Encoder}

enum ExampleKind:
  case Financing, Workflow, Adapter, Private, Progression, Purchase, Transfer, Generated,
    Composition, Packages, Boundaries
  def resultKind: ResultKind = this match
    case Transfer    => ResultKind.Transfer
    case Composition => ResultKind.Composition
    case Packages    => ResultKind.Packages
    case Boundaries  => ResultKind.Boundaries
    case _           => ResultKind.Ordinary
object ExampleKind:
  given Encoder[ExampleKind] = Encoder.encodeString.contramap(_.toString)
  given Decoder[ExampleKind] = Decoder.decodeString.emap(s =>
    ExampleKind.values.find(_.toString == s).toRight(s"Unknown example kind: $s")
  )

final case class Example(id: String, collection: String, kind: ExampleKind, chapter: Int):
  def path: String = s"examples/$collection/$id"

/** One explicit inventory for runner discovery, chapter coverage, and release membership. */
object Examples:
  val chapters: Vector[String] = Vector(
    "01-first-story.md",
    "02-two-integration-paths.md",
    "03-participant-views.md",
    "04-progression.md",
    "05-financing-and-offer.md",
    "06-atomic-transfer.md",
    "07-generated-bindings.md",
    "08-compose-a-workflow.md",
    "09-extend-with-evidence.md"
  )
  val all: Vector[Example] = Vector(
    Example("financing-approved", "stories", ExampleKind.Financing, 0),
    Example("already-approved", "stories", ExampleKind.Financing, 0),
    Example("workflow-approved", "stories", ExampleKind.Workflow, 1),
    Example("workflow-rejected", "stories", ExampleKind.Workflow, 1),
    Example("adapter-approved", "stories", ExampleKind.Adapter, 1),
    Example("adapter-rejected", "stories", ExampleKind.Adapter, 1),
    Example("private-approval", "stories", ExampleKind.Private, 2),
    Example("live-handoff", "evaluations", ExampleKind.Private, 2),
    Example("branch-approved", "stories", ExampleKind.Progression, 3),
    Example("branch-declined", "stories", ExampleKind.Progression, 3),
    Example("sequence-complete", "stories", ExampleKind.Progression, 3),
    Example("sequence-resumed", "stories", ExampleKind.Progression, 3),
    Example("purchase-approved", "stories", ExampleKind.Purchase, 4),
    Example("purchase-missing", "stories", ExampleKind.Purchase, 4),
    Example("purchase-rejected", "stories", ExampleKind.Purchase, 4),
    Example("purchase-reused-proof", "stories", ExampleKind.Purchase, 4),
    Example("purchase-wrong-buyer", "stories", ExampleKind.Purchase, 4),
    Example("purchase-wrong-continuation", "stories", ExampleKind.Purchase, 4),
    Example("purchase-wrong-issuer", "stories", ExampleKind.Purchase, 4),
    Example("purchase-wrong-subject", "stories", ExampleKind.Purchase, 4),
    Example("transfer-approved", "stories", ExampleKind.Transfer, 5),
    Example("transfer-final-leg-rejected", "stories", ExampleKind.Transfer, 5),
    Example("transfer-missing-agreement", "stories", ExampleKind.Transfer, 5),
    Example("transfer-missing-lock", "stories", ExampleKind.Transfer, 5),
    Example("transfer-missing-readiness", "stories", ExampleKind.Transfer, 5),
    Example("transfer-source-settler", "stories", ExampleKind.Transfer, 5),
    Example("generated-approved", "stories", ExampleKind.Generated, 6),
    Example("generated-rejected", "stories", ExampleKind.Generated, 6),
    Example("composer-direct", "evaluations", ExampleKind.Composition, 7),
    Example("composer-generated", "evaluations", ExampleKind.Composition, 7),
    Example("package-builder", "evaluations", ExampleKind.Packages, 7),
    Example("execution-boundaries", "evaluations", ExampleKind.Boundaries, 8)
  )
  val ids: Set[String] = all.map(_.id).toSet
  require(ids.size == all.size, "Duplicate example identifiers")
  require(all.forall(e => e.chapter >= 0 && e.chapter < chapters.size), "Invalid example chapter")
  def find(id: String): Option[Example] = all.find(_.id == id)
  def forChapter(index: Int): Set[String] = all.filter(_.chapter == index).map(_.id).toSet ++
    (if index == 8 then Set("generated-approved", "transfer-final-leg-rejected")
     else Set.empty[String])

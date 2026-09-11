package harmonia.book.project

import harmonia.examples.{Examples, ExampleKind}
import harmonia.stories.read.StoryFormat
import harmonia.stories.financing.model.FinancingStory
import harmonia.stories.purchase.model.PurchaseStory
import harmonia.stories.transfer.model.TransferStory

/** Registered evaluations have explicit kinds; ad-hoc experiments use the validated scenario
  * declaration.
  */
object RecordingKind:
  def read(id: String, markdown: String): Either[String, ExampleKind] = Examples.find(id) match
    case Some(example) => Right(example.kind)
    case None =>
      StoryFormat.input(id, markdown).flatMap {
        case _: PurchaseStory => Right(ExampleKind.Purchase)
        case _: TransferStory => Right(ExampleKind.Transfer)
        case story: FinancingStory =>
          (story.workflow, story.integration) match
            case (_, Some("generated"))        => Right(ExampleKind.Generated)
            case (_, Some("adapter"))          => Right(ExampleKind.Adapter)
            case (None, _)                     => Right(ExampleKind.Financing)
            case (Some("approval"), _)         => Right(ExampleKind.Workflow)
            case (Some("private-approval"), _) => Right(ExampleKind.Private)
            case (Some("sequential-approval" | "branching-approval"), _) =>
              Right(ExampleKind.Progression)
            case _ =>
              Left("Unsupported experiment presentation; register its validated scenario kind")
      }

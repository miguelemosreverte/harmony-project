package harmonia.book.narrative

import harmonia.scene.support.StoryIllustration
import harmonia.scene.support.StoryIllustration.*

/** Art direction belongs to the edition, independent of the product's execution model. */
object RecordedIllustrations:
  val stories: Map[String, StoryIllustration] = Map(
    "purchase-approved" -> PropertyVisit,
    "purchase-missing" -> MissingEvidence,
    "purchase-rejected" -> DeclinedFinancing,
    "purchase-reused-proof" -> ConsumedResult,
    "purchase-wrong-buyer" -> BuyerIdentity,
    "purchase-wrong-continuation" -> ContinuationPurpose,
    "purchase-wrong-issuer" -> IssuingBank,
    "purchase-wrong-subject" -> PropertyIdentity,
    "financing-approved" -> Consultation,
    "already-approved" -> ExistingRecord,
    "workflow-approved" -> SharedPlan,
    "workflow-rejected" -> ApplicationAuthority,
    "adapter-approved" -> AdapterWorkshop,
    "adapter-rejected" -> AdapterAuthority,
    "generated-approved" -> AdapterTemplate,
    "generated-rejected" -> GeneratedAuthority,
    "private-approval" -> PrivateReview,
    "live-handoff" -> SeparateWorkspaces,
    "branch-approved" -> ParallelReview,
    "branch-declined" -> RecordedClosure,
    "sequence-complete" -> BuyerReview,
    "sequence-resumed" -> ReturnToWork,
    "composer-direct" -> AgreeThePlan,
    "composer-generated" -> ReorderThePlan,
    "package-builder" -> ApplicationWorkshop,
    "execution-boundaries" -> OneAvailablePlace,
    "transfer-approved" -> CustodyHandoff,
    "transfer-final-leg-rejected" -> ClosedDestination,
    "transfer-missing-agreement" -> AwaitingAgreement,
    "transfer-missing-lock" -> UnreservedPosition,
    "transfer-missing-readiness" -> ReceivingPreparation,
    "transfer-source-settler" -> SourceSettlement
  )

package harmonia.scene.support

import io.circe.{Decoder, Encoder}

/** Context illustrations have no authority to declare a workflow outcome. */
enum StoryIllustration(val asset: String, val description: String):
  case PropertyVisit
      extends StoryIllustration(
        "purchase-approved",
        "Alice prepares an offer during a property visit."
      )
  case MissingEvidence
      extends StoryIllustration(
        "purchase-missing",
        "An empty document sleeve waits in the offer folder."
      )
  case DeclinedFinancing
      extends StoryIllustration(
        "purchase-rejected",
        "Alice pauses outside the bank with her closed laptop."
      )
  case ConsumedResult
      extends StoryIllustration(
        "purchase-reused-proof",
        "One filed result and a second offer wait on the agent’s desk."
      )
  case BuyerIdentity
      extends StoryIllustration(
        "purchase-wrong-buyer",
        "An agent compares two different buyer identities."
      )
  case ContinuationPurpose
      extends StoryIllustration(
        "purchase-wrong-continuation",
        "A clerk compares the intended next actions."
      )
  case IssuingBank
      extends StoryIllustration(
        "purchase-wrong-issuer",
        "A document seal is compared with its issuing bank."
      )
  case PropertyIdentity
      extends StoryIllustration(
        "purchase-wrong-subject",
        "Alice and Sofia compare two different property models."
      )
  case Consultation
      extends StoryIllustration(
        "financing-approved",
        "Alice and a bank adviser discuss her financing application."
      )
  case ExistingRecord
      extends StoryIllustration(
        "already-approved",
        "An archivist returns one existing financing folder to its shelf."
      )
  case SharedPlan
      extends StoryIllustration(
        "workflow-approved",
        "A banker and buyer follow the same agreed plan."
      )
  case ApplicationAuthority
      extends StoryIllustration(
        "workflow-rejected",
        "The application stays on the bank’s side of the counter."
      )
  case AdapterWorkshop
      extends StoryIllustration(
        "adapter-approved",
        "An engineer connects an existing terminal to a new interface."
      )
  case AdapterAuthority
      extends StoryIllustration(
        "adapter-rejected",
        "The original application keeps its lock beside the adapter."
      )
  case AdapterTemplate
      extends StoryIllustration(
        "generated-approved",
        "A developer fits an adapter to a reviewed template."
      )
  case GeneratedAuthority
      extends StoryIllustration(
        "generated-rejected",
        "A generated housing leaves the original application lock intact."
      )
  case PrivateReview
      extends StoryIllustration(
        "private-approval",
        "Private documents remain behind the bank office partition."
      )
  case SeparateWorkspaces
      extends StoryIllustration(
        "live-handoff",
        "Alice and the banker work on opposite sides of a privacy screen."
      )
  case ParallelReview
      extends StoryIllustration(
        "branch-approved",
        "Two workstations share a selected path through the consultation room."
      )
  case RecordedClosure
      extends StoryIllustration(
        "branch-declined",
        "A banker files the closure as the buyer prepares to leave."
      )
  case BuyerReview
      extends StoryIllustration(
        "sequence-complete",
        "Alice reviews the next document at a quiet window seat."
      )
  case ReturnToWork
      extends StoryIllustration(
        "sequence-resumed",
        "Alice returns to her laptop and a bookmarked page."
      )
  case AgreeThePlan
      extends StoryIllustration(
        "composer-direct",
        "Banker and buyer arrange the proposed actions before signing."
      )
  case ReorderThePlan
      extends StoryIllustration(
        "composer-generated",
        "The buyer reorders the plan beside an existing application."
      )
  case ApplicationWorkshop
      extends StoryIllustration(
        "package-builder",
        "A developer unpacks an application for inspection."
      )
  case OneAvailablePlace
      extends StoryIllustration(
        "execution-boundaries",
        "Two requests approach one available receiving slot."
      )
  case CustodyHandoff
      extends StoryIllustration(
        "transfer-approved",
        "Two custodians prepare a single secured position for transfer."
      )
  case ClosedDestination
      extends StoryIllustration(
        "transfer-final-leg-rejected",
        "The locked position remains at the source beside a closed destination."
      )
  case AwaitingAgreement
      extends StoryIllustration(
        "transfer-missing-agreement",
        "Seller and custodian wait over an unsigned agreement."
      )
  case UnreservedPosition
      extends StoryIllustration(
        "transfer-missing-lock",
        "A token box has not yet been placed in its reservation cradle."
      )
  case ReceivingPreparation
      extends StoryIllustration(
        "transfer-missing-readiness",
        "The destination custodian prepares the receiving space."
      )
  case SourceSettlement
      extends StoryIllustration(
        "transfer-source-settler",
        "The source custodian coordinates with the receiving desk."
      )
  case EvidenceReview
      extends StoryIllustration(
        "evidence-review",
        "A reviewer compares inputs, expectations and observations."
      )
  case ReleasePreparation
      extends StoryIllustration(
        "release-preparation",
        "An engineer prepares an application kit for another team."
      )
  case OriginalReading
      extends StoryIllustration(
        "original-reading",
        "A reader follows the commitments in two original documents."
      )
  case SourceWorkbench
      extends StoryIllustration(
        "source-workbench",
        "Developers examine a model of separate connected modules."
      )

object StoryIllustration:
  given Encoder[StoryIllustration] = Encoder.encodeString.contramap(_.asset)
  given Decoder[StoryIllustration] = Decoder.decodeString.emap(value =>
    StoryIllustration.values.find(_.asset == value).toRight("Unknown story illustration")
  )

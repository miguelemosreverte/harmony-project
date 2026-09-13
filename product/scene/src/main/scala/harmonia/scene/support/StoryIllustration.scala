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

  case ApprovalSigned
      extends StoryIllustration(
        "approval-signed",
        "An older silver-haired Northbank adviser in navy signs one approval sheet and slides it toward Alice, dark bob hair, blue sweater."
      )

  case OfferOpened
      extends StoryIllustration(
        "offer-opened",
        "Alice and Ben, a dark-haired male agent in a white shirt, open a fresh empty property offer folder on the riverside house terrace."
      )

  case ProposalPrepared
      extends StoryIllustration(
        "proposal-prepared",
        "Alice hands Ben a prepared blue property proposal folder with one signed financing sheet tucked inside."
      )

  case ProposalRelayed
      extends StoryIllustration(
        "proposal-relayed",
        "Ben, dark-haired agent in white shirt, sends a single blue proposal envelope through an elegant property-office service hatch toward Sofia, a short-haired woman in a navy blazer."
      )

  case ProposalReceived
      extends StoryIllustration(
        "proposal-received",
        "Sofia, short-haired woman in a navy blazer, has received the blue proposal folder and places it carefully in her property office receiving tray."
      )

  case ResultPublished
      extends StoryIllustration(
        "result-published",
        "Alice places a single signed approval card into a shared tray outside a frosted bank office partition."
      )

  case TradeAgreed
      extends StoryIllustration(
        "trade-agreed",
        "At a quiet custody desk the seller signs a transfer agreement while the source custodian watches."
      )

  case PositionLocked
      extends StoryIllustration(
        "position-locked",
        "Close three-quarter view of a source custodian placing a transparent case of blue asset tokens into a fitted reservation cradle and closing a small padlock."
      )

  case SourceReady
      extends StoryIllustration(
        "source-ready",
        "A male source custodian stands beside the locked blue token case and raises a small confirmation card toward a female destination custodian across the counter."
      )

  case DestinationReady
      extends StoryIllustration(
        "destination-ready",
        "A female destination custodian places a prepared receipt permit in front of her empty receiving tray and signals readiness across the custody counter."
      )

  case TransferSettled
      extends StoryIllustration(
        "transfer-settled",
        "The same two custodians after a coordinated transfer: left source cradle clearly empty and unlocked, right destination tray now contains the one transparent blue token case."
      )

  case PlanAccepted
      extends StoryIllustration(
        "plan-accepted",
        "Overhead intimate desk scene: Alice signs the exact two-action plan offered by the older bank adviser."
      )

  case ReviewComplete
      extends StoryIllustration(
        "review-complete",
        "Alice closes the reviewed blue folder with a calm satisfied expression at her window seat."
      )

  case JoinComplete
      extends StoryIllustration(
        "join-complete",
        "An overhead view of two completed work folders being gathered by two different hands into one common archive sleeve."
      )

  case JoinWaiting
      extends StoryIllustration(
        "join-waiting",
        "Two colleagues at a shared desk: one finished blue folder lies ready, the other workstation has an empty chair and an open unfinished folder."
      )

  case PathFixed
      extends StoryIllustration(
        "path-fixed",
        "Close view of a hand reaching toward a closed unused work folder while another hand gently points back to the already selected open folder."
      )

  case RetryRecord
      extends StoryIllustration(
        "retry-record",
        "An archivist compares a newly arrived request slip against the single filed original request in a drawer, then places the new slip beside it."
      )

  case ReviewWaiting
      extends StoryIllustration(
        "review-waiting",
        "An empty window seat with Alice's open laptop and blue review folder awaiting her return, afternoon light, cup of tea untouched, quiet anticipation."
      )

  case CompiledAdapter
      extends StoryIllustration(
        "compiled-adapter",
        "In a small bright workshop an engineer has assembled a finished blue connector that bridges a legacy application terminal and a new interface."
      )

  case MappingMissing
      extends StoryIllustration(
        "mapping-missing",
        "An engineer opens an unfamiliar application module on the workbench and compares its unusual socket against the empty matching space in a set of reviewed adapter templates."
      )

  case InputInvalid
      extends StoryIllustration(
        "input-invalid",
        "Overhead application-inspection desk: a developer lifts a visibly broken incomplete package module from its box, with an open inspection tray beside it."
      )

  case InputOversized
      extends StoryIllustration(
        "input-oversized",
        "A developer holds an oversized but intact application package beside a small inspection opening; the package clearly cannot fit."
      )

  case LimitsChecked
      extends StoryIllustration(
        "limits-checked",
        "An engineer measures an orderly row of small workflow modules against a physical ruler boundary."
      )

  case ContinuationWaiting
      extends StoryIllustration(
        "continuation-waiting",
        "Alice waits at a property-office counter beside an empty approval-card tray."
      )

  case SettlementWaiting
      extends StoryIllustration(
        "settlement-waiting",
        "A coordinator looks across two custody stations with an incomplete receipt-permit folder on the desk."
      )

  case AlreadySettled
      extends StoryIllustration(
        "already-settled",
        "The destination custodian points to the token case already in her receiving tray while declining a second transfer request slip."
      )

  case ResultReady
      extends StoryIllustration(
        "result-ready",
        "A signed result waits for Alice to use it; the bank retains the private application."
      )
  case UnselectedClosure
      extends StoryIllustration(
        "unselected-closure",
        "The unused archive path is closed; the work folder remains outside."
      )
  case BuyerAuthority
      extends StoryIllustration(
        "buyer-authority",
        "Alice retains the right to consent and perform her assigned review."
      )

  case CompilationRefused
      extends StoryIllustration(
        "compilation-refused",
        "Incompatible parts are set aside; no adapter has been built."
      )

  case ProposalRefused
      extends StoryIllustration(
        "proposal-refused",
        "Ben returns the closed proposal folder to Alice; the property handoff cannot proceed."
      )

object StoryIllustration:
  given Encoder[StoryIllustration] = Encoder.encodeString.contramap(_.asset)
  given Decoder[StoryIllustration] = Decoder.decodeString.emap(value =>
    StoryIllustration.values.find(_.asset == value).toRight("Unknown story illustration")
  )

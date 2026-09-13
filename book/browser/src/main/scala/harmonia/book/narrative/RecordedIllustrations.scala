package harmonia.book.narrative

import harmonia.scene.support.StoryIllustration
import harmonia.scene.support.StoryIllustration.*
import harmonia.book.RecordedStory
import harmonia.examples.ExampleKind

/** Art direction belongs to the edition, independent of the product's execution model. */
object RecordedIllustrations:
  /** Outcome-sensitive direction: a picture of success needs an observed success. */
  def apply(story: RecordedStory, index: Int): StoryIllustration =
    story.units.lift(index) match
      case None =>
        story.kind match
          case ExampleKind.Purchase   => PropertyVisit
          case ExampleKind.Transfer   => CustodyHandoff
          case ExampleKind.Packages   => ApplicationWorkshop
          case ExampleKind.Boundaries => LimitsChecked
          case _                      => Consultation
      case Some(unit) if unit.actual.isNull => EvidenceReview
      case Some(unit) =>
        val actual = unit.actual.hcursor
        def text(key: String) = actual.get[String](key).getOrElse("")
        def flag(key: String) = actual.get[Boolean](key).contains(true)
        val rejected = text("outcome") == "rejected"
        if text("outcome") == "duplicate" then RetryRecord
        else if story.kind == ExampleKind.Boundaries then
          if unit.id == "race" then OneAvailablePlace else LimitsChecked
        else if story.kind == ExampleKind.Packages then
          unit.action match
            case "malformed" => InputInvalid
            case "oversized" => InputOversized
            case "generate" =>
              if actual.get[Int]("http").contains(200) then CompiledAdapter else CompilationRefused
            case "retrieve" if actual.get[Int]("http").contains(403) => ApplicationAuthority
            case "retrieve" if !flag("supported")                    => MappingMissing
            case "retrieve"                                          => AdapterTemplate
            case _                                                   => ApplicationWorkshop
        else if rejected then
          if story.kind == ExampleKind.Transfer then
            if text("reason") == "destination-rejected" then ClosedDestination
            else if text("trade") == "settled" then AlreadySettled
            else if unit.action == "settle" then SettlementWaiting
            else if unit.action == "withdraw-directly" then CustodyHandoff
            else if unit.action == "confirm-source" && text("trade") == "proposed" then
              AwaitingAgreement
            else if unit.action == "confirm-source" then UnreservedPosition
            else ReceivingPreparation
          else
            unit.action match
              case "make-proposal" if text("application") == "rejected" => ProposalRefused
              case "make-proposal"    => stories.getOrElse(story.id, MissingEvidence)
              case "receive-proposal" => ContinuationWaiting
              case "accept"           => BuyerAuthority
              case "advance" =>
                if unit.id == "wrong-reviewer" then BuyerAuthority else JoinWaiting
              case "forge-completion" | "forge-proposal" => GeneratedAuthority
              case "complete-join" =>
                if text("workflow") == "complete" then ExistingRecord else JoinWaiting
              case "choose-approve" | "choose-decline" => PathFixed
              case "close-application"                 => UnselectedClosure
              case "confirm-review"                    => ReviewWaiting
              case "publish-approval" =>
                if text("application") == "pending" then ContinuationWaiting
                else ApplicationAuthority
              case _ =>
                if text("branch") == "decline" then PathFixed
                else if text("reason") == "not-visible" then PrivateReview
                else if text("application") == "approved" then ExistingRecord
                else ApplicationAuthority
        else
          unit.action match
            case "assess-financing" =>
              if text("application") == "rejected" then DeclinedFinancing
              else if text("application") == "approved" then ApprovalSigned
              else EvidenceReview
            case "approve-financing" =>
              if text("application") == "approved" then ApprovalSigned else EvidenceReview
            case "open-offer" =>
              if unit.id == "open-second-offer" then PropertyVisit else OfferOpened
            case "make-proposal"       => ProposalPrepared
            case "relay-proposal"      => ProposalRelayed
            case "receive-proposal"    => ProposalReceived
            case "agree-trade"         => TradeAgreed
            case "lock-position"       => PositionLocked
            case "confirm-source"      => SourceReady
            case "prepare-destination" => ReceivingPreparation
            case "confirm-destination" => DestinationReady
            case "settle" =>
              if text("trade") == "settled" then TransferSettled else SettlementWaiting
            case "choose-approve"    => ParallelReview
            case "choose-decline"    => DeclinedFinancing
            case "close-application" => RecordedClosure
            case "confirm-review"    => ReviewComplete
            case "complete-join"     => JoinComplete
            case "publish-approval"  => ResultPublished
            case "wait"              => ReviewWaiting
            case "reconnect"         => ReturnToWork
            case "propose"           => AgreeThePlan
            case "accept"            => PlanAccepted
            case "advance" =>
              if Set("approval", "financing")(unit.id) then ApprovalSigned
              else if unit.id == "inspection" then BuyerReview
              else ReviewComplete
            case _ => EvidenceReview

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

package harmonia.financing

/** @module.slice
  *   financing
  * @module.role
  *   Observe what is visible
  * @module.summary
  *   Participant-visible contracts become the public financing state. Private details are not
  *   invented for other actors.
  */

import cats.syntax.all.*
import harmonia.ledger.client.{ActiveContract, LedgerSnapshot, LedgerDecodingFailure}
import io.circe.Decoder

final case class ObservedApplication(
    contract: ActiveContract,
    status: ApplicationStatus,
    privateDetails: String
)
final case class ObservedProgress(contract: ActiveContract, status: ProgressStatus)

/** Decoded once from the current participant's visible contracts. */
final case class FinancingObservation(
    version: String,
    application: Option[ObservedApplication],
    progress: Option[ObservedProgress],
    proof: Option[ActiveContract]
):
  def workflow: ProgressStatus = progress.fold(ProgressStatus.Hidden)(_.status)

  def state(actor: String): FinancingState =
    val eligible = Vector(
      Option.when(actor == "bank" && application.exists(_.status == ApplicationStatus.Pending))(
        FinancingAction.Approve
      ),
      Option.when(actor == "buyer" && workflow == ProgressStatus.Waiting && proof.nonEmpty)(
        FinancingAction.Continue
      )
    ).flatten
    FinancingState(
      actor,
      version,
      workflow,
      application.map(_.status),
      application.map(_.privateDetails),
      proof.nonEmpty,
      eligible,
      if workflow == ProgressStatus.Complete then "Complete"
      else if proof.nonEmpty then "Buyer continues"
      else "Waiting for bank approval"
    )

object FinancingObservation:
  private final case class ApplicationData(status: ApplicationStatus, privateDetails: String)
  private given Decoder[ApplicationData] =
    Decoder.forProduct2("status", "privateDetails")(ApplicationData.apply)
  private final case class ProgressData(status: ProgressStatus)
  private given Decoder[ProgressData] = Decoder.forProduct1("status")(ProgressData.apply)

  def read(snapshot: LedgerSnapshot): Either[LedgerDecodingFailure, FinancingObservation] = for
    application <- snapshot.contract("PrivateFinancing", "Application").traverse { contract =>
      contract
        .decode[ApplicationData]
        .map(data => ObservedApplication(contract, data.status, data.privateDetails))
    }
    progress <- snapshot.contract("Harmonia.SharedProgress", "SharedProgress").traverse {
      contract =>
        contract
          .decode[ProgressData]
          .flatMap(data =>
            Either.cond(
              data.status != ProgressStatus.Hidden,
              ObservedProgress(contract, data.status),
              LedgerDecodingFailure(
                "SharedProgress.status",
                "a visible contract cannot be not-visible"
              )
            )
          )
    }
  yield FinancingObservation(
    snapshot.version,
    application,
    progress,
    snapshot.contract("Harmonia.Result", "VerifiedResult")
  )

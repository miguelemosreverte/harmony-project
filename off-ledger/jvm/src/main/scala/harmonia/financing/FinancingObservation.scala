package harmonia.financing

import harmonia.ledger.client.{ActiveContract, LedgerSnapshot}
import io.circe.Json

/** Projects only contracts visible to the authenticated participant. */
final case class FinancingObservation(snapshot: LedgerSnapshot):
  def application: Option[ActiveContract] = snapshot.contract("PrivateFinancing", "Application")
  def progress: Option[ActiveContract] =
    snapshot.contract("Harmonia.SharedProgress", "SharedProgress")
  def proof: Option[ActiveContract] = snapshot.contract("Harmonia.Result", "VerifiedResult")
  def workflow: String = progress.map(_.text("status")).getOrElse("not-visible")
  def version: String = snapshot.version
  def state(actor: String): FinancingState =
    val applicationStatus = application.map(c =>
      Json.fromString(c.text("status")).as[ApplicationStatus].fold(throw _, identity)
    )
    val progressStatus = Json.fromString(workflow).as[ProgressStatus].fold(throw _, identity)
    val eligible = Vector(
      Option.when(actor == "bank" && applicationStatus.contains(ApplicationStatus.Pending))(
        FinancingAction.Approve
      ),
      Option.when(actor == "buyer" && progressStatus == ProgressStatus.Waiting && proof.nonEmpty)(
        FinancingAction.Continue
      )
    ).flatten
    FinancingState(
      actor,
      version,
      progressStatus,
      applicationStatus,
      application.map(_.text("privateDetails")),
      proof.nonEmpty,
      eligible,
      if progressStatus == ProgressStatus.Complete then "Complete"
      else if proof.nonEmpty then "Buyer continues"
      else "Waiting for bank approval"
    )

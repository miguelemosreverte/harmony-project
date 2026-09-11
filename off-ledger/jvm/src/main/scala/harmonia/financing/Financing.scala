package harmonia.financing

import harmonia.ledger.client.{ActiveContract, LiveLedger}
import com.daml.ledger.api.v2.ValueOuterClass.Value

/** Maps a supported financing operation to its actual application choice. */
object Financing:
  def select(
      action: FinancingAction,
      snapshot: FinancingObservation
  ): Option[(ActiveContract, String, Value)] =
    action match
      case FinancingAction.Approve =>
        snapshot.application.map(contract => (contract, "Approve", LiveLedger.emptyArgument))
      case FinancingAction.Continue =>
        snapshot.progress.map(contract =>
          (contract, "Continue", LiveLedger.continuation(snapshot.proof))
        )

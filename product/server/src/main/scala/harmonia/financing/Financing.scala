package harmonia.financing

import harmonia.ledger.client.{LedgerExercise, LiveLedger}

/** Maps a supported financing operation to its actual application choice. */
object Financing:
  def select(
      action: FinancingAction,
      snapshot: FinancingObservation
  ): Option[LedgerExercise] =
    action match
      case FinancingAction.Approve =>
        snapshot.application.map(application =>
          LedgerExercise(application.contract, "Approve", LiveLedger.emptyArgument)
        )
      case FinancingAction.Continue =>
        snapshot.progress.map(progress =>
          LedgerExercise(progress.contract, "Continue", LiveLedger.continuation(snapshot.proof))
        )

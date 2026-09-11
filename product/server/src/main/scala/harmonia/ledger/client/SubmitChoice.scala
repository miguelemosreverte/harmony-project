package harmonia.ledger.client

/** @book.slice
  *   process
  * @book.role
  *   Submit through a participant
  * @book.summary
  *   Typed exercise values travel through the Ledger API. The service does not replace the Daml
  *   choice authorization.
  */

import cats.effect.IO
import harmonia.submission.SubmissionResult
import harmonia.protocol.SubmissionStatus.*
import io.grpc.Status

/** A failed read is not a rejected submission. Only this submission boundary classifies gRPC
  * responses.
  */
object SubmitChoice:
  def apply(
      ledger: ParticipantLedger,
      operation: LedgerExercise,
      commandId: String
  ): IO[SubmissionResult] =
    ledger
      .exercise(operation.contract, operation.choice, operation.argument, commandId)
      .map(tx => SubmissionResult(Committed, "Confirmed by the ledger", Some(tx)))
      .handleError { error =>
        val code = Status.fromThrowable(error).getCode
        val definite = Set(
          Status.Code.INVALID_ARGUMENT,
          Status.Code.FAILED_PRECONDITION,
          Status.Code.PERMISSION_DENIED,
          Status.Code.NOT_FOUND,
          Status.Code.UNAUTHENTICATED
        ).contains(code)
        SubmissionResult(
          if definite then Rejected else Unconfirmed,
          if definite then s"Ledger rejected the command ($code)"
          else "No definitive completion received. Reconnect to reconcile; do not assume failure."
        )
      }

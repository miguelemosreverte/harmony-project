package harmonia.ledger.client

import cats.effect.IO
import com.daml.ledger.api.v2.ValueOuterClass.Value
import harmonia.submission.SubmissionResult
import harmonia.protocol.SubmissionStatus.*
import io.grpc.Status

/** A failed read is not a rejected submission. Only this submission boundary classifies gRPC
  * responses.
  */
object SubmitChoice:
  def apply(
      ledger: ParticipantLedger,
      contract: ActiveContract,
      choice: String,
      argument: Value,
      commandId: String
  ): IO[SubmissionResult] =
    ledger
      .exercise(contract, choice, argument, commandId)
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

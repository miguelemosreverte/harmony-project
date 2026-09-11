package harmonia.workspace

import harmonia.financing.FinancingState
import harmonia.composition.CompositionState
import harmonia.protocol.{SubmissionStatus, LedgerUpdate}
import io.circe.{Codec, Decoder, Encoder}
import io.circe.syntax.*

final case class SubmissionView(
    id: String,
    actor: String,
    action: String,
    outcome: SubmissionStatus,
    detail: String
)
object SubmissionView:
  given Codec.AsObject[SubmissionView] =
    Codec.forProduct5("id", "actor", "action", "outcome", "detail")(SubmissionView.apply)(v =>
      (v.id, v.actor, v.action, v.outcome, v.detail)
    )

final case class WorkspaceSnapshot(
    financing: FinancingState,
    composition: CompositionState,
    submissions: Vector[SubmissionView],
    history: Vector[LedgerUpdate]
)
object WorkspaceSnapshot:
  given Decoder[WorkspaceSnapshot] = Decoder.instance { c =>
    for
      financing <- c.as[FinancingState]
      composition <- c.get[CompositionState]("composer")
      submissions <- c.get[Vector[SubmissionView]]("jobs")
      history <- c.get[Vector[LedgerUpdate]]("history")
    yield WorkspaceSnapshot(financing, composition, submissions, history)
  }

  /** Preserve the established HTTP shape; internal producers use the model above. */
  given Encoder.AsObject[WorkspaceSnapshot] = Encoder.AsObject.instance { state =>
    state.financing.asJsonObject
      .add("composer", state.composition.asJson)
      .add("jobs", state.submissions.asJson)
      .add("history", state.history.asJson)
  }

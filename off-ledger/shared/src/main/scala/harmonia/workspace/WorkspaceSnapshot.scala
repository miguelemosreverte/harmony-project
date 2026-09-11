package harmonia.workspace

import harmonia.financing.FinancingState
import harmonia.protocol.SubmissionStatus
import io.circe.{Decoder, Json}

final case class SubmissionView(
    id: String,
    action: String,
    outcome: SubmissionStatus,
    detail: String
)
object SubmissionView:
  given Decoder[SubmissionView] = Decoder.instance { c =>
    for
      id <- c.get[String]("id")
      action <- c.get[String]("action")
      outcome <- c.get[SubmissionStatus]("outcome")
      detail <- c.get[String]("detail")
    yield SubmissionView(id, action, outcome, detail)
  }

final case class HistoryView(updateId: Option[String], events: Vector[String])
object HistoryView:
  given Decoder[HistoryView] = Decoder.instance { c =>
    for
      id <- c.get[Option[String]]("update_id")
      events <- c.get[Vector[String]]("events")
    yield HistoryView(id, events)
  }

final case class WorkspaceSnapshot(
    financing: FinancingState,
    composition: Json,
    submissions: Vector[SubmissionView],
    history: Vector[HistoryView]
)
object WorkspaceSnapshot:
  given Decoder[WorkspaceSnapshot] = Decoder.instance { c =>
    for
      financing <- c.as[FinancingState]
      composition <- c.get[Json]("composer")
      submissions <- c.get[Vector[SubmissionView]]("jobs")
      history <- c.get[Vector[HistoryView]]("history")
    yield WorkspaceSnapshot(financing, composition, submissions, history)
  }

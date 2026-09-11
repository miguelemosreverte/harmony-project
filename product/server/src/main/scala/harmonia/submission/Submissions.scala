package harmonia.submission

import cats.effect.{IO, Ref, Resource}
import cats.effect.std.{Semaphore, Supervisor}
import cats.syntax.all.*
import harmonia.workspace.{WorkspaceCommand, SubmissionView}
import harmonia.protocol.SubmissionStatus
import harmonia.protocol.SubmissionStatus.*
import io.circe.Json
import io.circe.syntax.*

final case class ActionRequest(
    id: String,
    command: WorkspaceCommand,
    version: String
)
final case class LiveJob(
    actor: String,
    request: ActionRequest,
    outcome: SubmissionStatus,
    detail: String,
    transaction: Option[Json] = None
):
  def view: SubmissionView =
    SubmissionView(request.id, actor, request.command.wire, outcome, detail)
  def json: Json = view.asJson

/** Preparation observes state; the returned effect submits only after the version check. */
final case class PreparedSubmission(version: String, execute: Option[IO[SubmissionResult]])
final case class SubmissionResult(
    outcome: SubmissionStatus,
    detail: String,
    transaction: Option[Json] = None
)

final class Submissions private (
    prepare: (String, ActionRequest, String) => IO[PreparedSubmission],
    jobs: Ref[IO, Vector[LiveJob]],
    lock: Semaphore[IO],
    supervisor: Supervisor[IO]
):
  def current(actor: String): IO[Vector[LiveJob]] = jobs.get.map(_.filter(_.actor == actor))

  def submit(actor: String, request: ActionRequest): IO[LiveJob] =
    IO.raiseUnless(
      request.id.matches("[a-zA-Z0-9-]{1,64}") && request.version.matches("[0-9a-f]{64}")
    )(IllegalArgumentException("Invalid request identifier or snapshot version")) *> lock.permit
      .use { _ =>
        jobs.get.flatMap { current =>
          current.find(j => j.actor == actor && j.request.id == request.id) match
            case Some(existing) =>
              IO.raiseUnless(existing.request == request)(
                IllegalArgumentException(
                  "Request identifier already belongs to a different command"
                )
              ).as(existing)
            case None =>
              IO.raiseWhen(current.size >= 100)(
                IllegalArgumentException(
                  "This evaluation session has reached its 100-request limit"
                )
              ) *> {
                val job = LiveJob(actor, request, Pending, "Waiting for the ledger result")
                jobs.update(_ :+ job) *> supervisor.supervise(execute(job)).as(job)
              }
        }
      }

  private def execute(job: LiveJob): IO[Unit] = lock.permit.use { _ =>
    val result = for
      prepared <- prepare(job.actor, job.request, commandId(job))
      next <-
        if prepared.version != job.request.version then
          IO.pure(
            job.copy(
              outcome = Stale,
              detail = "The visible contracts changed. Refresh before trying again."
            )
          )
        else
          prepared.execute match
            case None =>
              IO.pure(
                job.copy(
                  outcome = Unavailable,
                  detail = "The required contract is not visible to this session"
                )
              )
            case Some(execute) =>
              execute.map(result =>
                job.copy(
                  outcome = result.outcome,
                  detail = result.detail,
                  transaction = result.transaction
                )
              )
    yield next
    result
      .handleError(_ =>
        job.copy(
          outcome = Unconfirmed,
          detail =
            "Could not observe the required ledger state. Reconnect before submitting; no business rejection was observed."
        )
      )
      .flatMap(replace)
  }
  private def commandId(job: LiveJob): String = s"live-${job.actor}-${job.request.id}"
  private def replace(next: LiveJob): IO[Unit] = jobs.update(
    _.map(j => if j.actor == next.actor && j.request.id == next.request.id then next else j)
  )
  def reconcile(actor: String, commits: Map[String, Json]): IO[Unit] =
    jobs.get.flatMap(_.filter(j => j.actor == actor && j.outcome == Unconfirmed).traverse_ { job =>
      commits
        .get(commandId(job))
        .fold(IO.unit)(tx =>
          replace(
            job.copy(
              outcome = Committed,
              detail = "Commit recovered from ledger history after reconnect",
              transaction = Some(tx)
            )
          )
        )
    })

object Submissions:
  def resource(
      prepare: (String, ActionRequest, String) => IO[PreparedSubmission]
  ): Resource[IO, Submissions] = for
    jobs <- Resource.eval(Ref.of[IO, Vector[LiveJob]](Vector.empty))
    lock <- Resource.eval(Semaphore[IO](1))
    supervisor <- Supervisor[IO]
  yield new Submissions(prepare, jobs, lock, supervisor)

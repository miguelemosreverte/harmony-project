package harmonia.live.actions

import cats.effect.{IO, Ref, Resource}
import cats.effect.std.{Semaphore, Supervisor}
import cats.syntax.all.*
import harmonia.live.run.LiveRuntime
import harmonia.live.state.LiveSnapshot
import harmonia.live.ledger.LiveLedger
import io.circe.Json
import io.grpc.Status

final case class ActionRequest(id: String, action: String, version: String)
final case class LiveJob(
    actor: String,
    request: ActionRequest,
    outcome: String,
    detail: String,
    transaction: Option[Json] = None
):
  def json: Json = Json.obj(
    "id" -> Json.fromString(request.id),
    "actor" -> Json.fromString(actor),
    "action" -> Json.fromString(request.action),
    "outcome" -> Json.fromString(outcome),
    "detail" -> Json.fromString(detail)
  )

final class LiveActions private (
    runtime: LiveRuntime,
    jobs: Ref[IO, Vector[LiveJob]],
    lock: Semaphore[IO],
    supervisor: Supervisor[IO]
):
  def state(actor: String): IO[Json] = for
    snapshot <- LiveSnapshot.read(runtime.participants(actor).ledger)
    _ <- reconcile(actor, snapshot)
    current <- jobs.get
  yield snapshot
    .json(actor)
    .deepMerge(Json.obj("jobs" -> Json.arr(current.filter(_.actor == actor).map(_.json)*)))

  def submit(actor: String, request: ActionRequest): IO[LiveJob] =
    IO.raiseUnless(
      request.id.matches("[a-zA-Z0-9-]{1,64}") && request.version.matches("[0-9a-f]{64}") && Set(
        "approve-financing",
        "publish-approval"
      ).contains(request.action)
    )(
      IllegalArgumentException("Invalid action, request identifier, or snapshot version")
    ) *> lock.permit.use { _ =>
      jobs.get.flatMap { current =>
        current.find(j => j.actor == actor && j.request.id == request.id) match
          case Some(existing) =>
            IO.raiseUnless(existing.request == request)(
              IllegalArgumentException("Request identifier already belongs to a different command")
            ).as(existing)
          case None =>
            IO.raiseWhen(current.size >= 100)(
              IllegalArgumentException("This evaluation session has reached its 100-request limit")
            ) *> {
              val job = LiveJob(actor, request, "pending", "Waiting for the ledger result")
              jobs.update(_ :+ job) *> supervisor.supervise(execute(job)).as(job)
            }
      }
    }

  private def execute(job: LiveJob): IO[Unit] = lock.permit.use { _ =>
    val ledger = runtime.participants(job.actor).ledger
    val result = for
      snapshot <- LiveSnapshot.read(ledger)
      next <-
        if snapshot.version != job.request.version then
          IO.pure(
            job.copy(
              outcome = "stale",
              detail = "The visible contracts changed. Refresh before trying again."
            )
          )
        else
          val selection = job.request.action match
            case "approve-financing" =>
              snapshot.application.map(c => (c, "Approve", LiveLedger.emptyArgument))
            case "publish-approval" =>
              snapshot.progress.map(c => (c, "Continue", LiveLedger.continuation(snapshot.proof)))
          selection match
            case None =>
              IO.pure(
                job.copy(
                  outcome = "rejected",
                  detail = "The required contract is not visible to this session"
                )
              )
            case Some((contract, choice, argument)) =>
              ledger
                .exercise(contract, choice, argument, commandId(job))
                .map(tx =>
                  job.copy(
                    outcome = "committed",
                    detail = "Confirmed by the ledger",
                    transaction = Some(tx)
                  )
                )
    yield next
    result
      .handleError(error =>
        val code = Status.fromThrowable(error).getCode
        val definite = Set(
          Status.Code.INVALID_ARGUMENT,
          Status.Code.FAILED_PRECONDITION,
          Status.Code.PERMISSION_DENIED,
          Status.Code.NOT_FOUND,
          Status.Code.UNAUTHENTICATED
        ).contains(code)
        job.copy(
          outcome = if definite then "rejected" else "disconnected",
          detail =
            if definite then s"Ledger rejected the command ($code)"
            else "No definitive completion received. Reconnect to reconcile; do not assume failure."
        )
      )
      .flatMap(replace)
  }
  private def commandId(job: LiveJob): String = s"live-${job.actor}-${job.request.id}"
  private def replace(next: LiveJob): IO[Unit] = jobs.update(
    _.map(j => if j.actor == next.actor && j.request.id == next.request.id then next else j)
  )
  private def reconcile(actor: String, snapshot: LiveSnapshot): IO[Unit] =
    jobs.get.flatMap(_.filter(j => j.actor == actor && j.outcome == "disconnected").traverse_ {
      job =>
        snapshot.history
          .find(
            _.hcursor
              .downField("transaction")
              .get[String]("commandId")
              .toOption
              .contains(commandId(job))
          )
          .fold(IO.unit)(tx =>
            replace(
              job.copy(
                outcome = "committed",
                detail = "Commit recovered from ledger history after reconnect",
                transaction = Some(tx)
              )
            )
          )
    })

object LiveActions:
  def resource(runtime: LiveRuntime): Resource[IO, LiveActions] = for
    jobs <- Resource.eval(Ref.of[IO, Vector[LiveJob]](Vector.empty))
    lock <- Resource.eval(Semaphore[IO](1))
    supervisor <- Supervisor[IO]
  yield new LiveActions(runtime, jobs, lock, supervisor)

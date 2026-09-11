package harmonia.app.workspace

import cats.effect.{IO, Resource}
import harmonia.app.live.LiveRuntime
import harmonia.ledger.client.LedgerSnapshot
import harmonia.financing.FinancingObservation
import harmonia.submission.*
import harmonia.workspace.WorkspaceCommand
import harmonia.financing.Financing
import harmonia.composition.ledger.{ComposerCommands, ComposerSnapshot}
import harmonia.ledger.client.SubmitChoice
import io.circe.Json

/** Composes participant observations and routes valid feature commands. */
final class Workspace private (runtime: LiveRuntime, submissions: Submissions):
  def state(actor: String): IO[Json] = for
    snapshot <- LedgerSnapshot.read(runtime.participants(actor).ledger, runtime.catalog)
    _ <- submissions.reconcile(actor, snapshot.commits)
    current <- submissions.current(actor)
  yield FinancingObservation(snapshot)
    .state(actor)
    .json
    .deepMerge(snapshot.historyJson)
    .deepMerge(
      Json.obj(
        "composer" -> ComposerSnapshot.json(
          snapshot.contracts,
          runtime.participants.map((name, p) => name -> p.ledger.party),
          actor
        ),
        "jobs" -> Json.arr(current.map(_.json)*)
      )
    )

  def submit(actor: String, request: ActionRequest): IO[LiveJob] =
    submissions.submit(actor, request)

object Workspace:
  def resource(runtime: LiveRuntime): Resource[IO, Workspace] =
    Submissions
      .resource { (actor, request, commandId) =>
        val ledger = runtime.participants(actor).ledger
        LedgerSnapshot.read(ledger, runtime.catalog).map { snapshot =>
          val selected = request.command match
            case WorkspaceCommand.Financing(action) =>
              Financing.select(action, FinancingObservation(snapshot))
            case command =>
              ComposerCommands.select(
                command,
                request.id,
                snapshot.contracts,
                ledger.party,
                runtime.participants.map((name, p) => name -> p.ledger.party)
              )
          PreparedSubmission(
            snapshot.version,
            selected.map { (contract, choice, argument) =>
              SubmitChoice(ledger, contract, choice, argument, commandId)
            }
          )
        }
      }
      .map(new Workspace(runtime, _))

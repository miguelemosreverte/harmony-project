package harmonia.app.workspace

import cats.effect.{IO, Resource}
import harmonia.app.Connections
import harmonia.ledger.client.LedgerSnapshot
import harmonia.financing.FinancingObservation
import harmonia.submission.*
import harmonia.workspace.{WorkspaceCommand, WorkspaceSnapshot}
import harmonia.financing.Financing
import harmonia.composition.ledger.{ComposerCommands, ComposerSnapshot}
import harmonia.ledger.client.SubmitChoice

/** Composes participant observations and routes valid feature commands. */
final class Workspace private (runtime: Connections, submissions: Submissions):
  def state(actor: String): IO[WorkspaceSnapshot] = for
    snapshot <- LedgerSnapshot.read(runtime.ledgers(actor), runtime.catalog)
    _ <- submissions.reconcile(actor, snapshot.commits)
    current <- submissions.current(actor)
    financing <- IO.fromEither(FinancingObservation.read(snapshot))
    composition <- IO.fromEither(ComposerSnapshot.read(snapshot.contracts, runtime.parties, actor))
  yield WorkspaceSnapshot(
    financing.state(actor),
    composition,
    current.map(_.view),
    snapshot.updates
  )

  def submit(actor: String, request: ActionRequest): IO[LiveJob] =
    submissions.submit(actor, request)

object Workspace:
  def resource(runtime: Connections): Resource[IO, Workspace] =
    Submissions
      .resource { (actor, request, commandId) =>
        val ledger = runtime.ledgers(actor)
        for
          snapshot <- LedgerSnapshot.read(ledger, runtime.catalog)
          selected <- request.command match
            case WorkspaceCommand.Financing(action) =>
              IO.fromEither(FinancingObservation.read(snapshot)).map(Financing.select(action, _))
            case WorkspaceCommand.Composition(command) =>
              IO.delay(
                ComposerCommands.select(
                  command,
                  request.id,
                  snapshot.contracts,
                  ledger.party,
                  runtime.parties
                )
              )
        yield PreparedSubmission(
          snapshot.version,
          selected.map(operation => SubmitChoice(ledger, operation, commandId))
        )
      }
      .map(new Workspace(runtime, _))

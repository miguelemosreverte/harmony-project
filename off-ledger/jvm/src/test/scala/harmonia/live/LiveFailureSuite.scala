package harmonia.live

import cats.effect.{IO, Ref, Deferred}
import cats.effect.unsafe.implicits.global
import cats.syntax.all.*
import harmonia.live.actions.{LiveActions, ActionRequest}
import harmonia.live.ledger.{ActiveContract, ParticipantLedger, TemplateCatalog, LiveLedger}
import harmonia.live.run.{LiveRuntime, LiveParticipant}
import harmonia.live.state.LiveSnapshot
import com.daml.ledger.api.v2.ValueOuterClass
import io.circe.Json
import harmonia.workspace.WorkspaceCommand
import harmonia.financing.FinancingAction
import io.grpc.Status
import java.nio.file.Path
import munit.FunSuite
import scala.concurrent.duration.*

class LiveFailureSuite extends FunSuite:
  private val packageId = "a" * 64
  private val catalog = TemplateCatalog(Map("private-financing" -> packageId))
  private val application = ActiveContract(
    "application-1",
    ValueOuterClass.Identifier
      .newBuilder()
      .setPackageId(packageId)
      .setModuleName("PrivateFinancing")
      .setEntityName("Application")
      .build(),
    Map("status" -> Json.obj("text" -> Json.fromString("pending")))
  )

  test(
    "permission and missing-contract failures during observation never become business rejection"
  ) {
    Vector(Status.PERMISSION_DENIED, Status.NOT_FOUND, Status.INVALID_ARGUMENT).foreach { failure =>
      val result = (for
        failRead <- Ref.of[IO, Boolean](false)
        observed <- Deferred[IO, Unit]
        calls <- Ref.of[IO, Int](0)
        ledger = new Stub(failRead, observed, calls, failure, IO.pure(Json.obj()))
        runtime = LiveRuntime(
          Map("bank" -> LiveParticipant("bank", ledger)),
          Path.of("unused"),
          catalog
        )
        result <- LiveActions.resource(runtime).use { actions =>
          for
            initial <- actions.state("bank")
            _ <- failRead.set(true)
            _ <- actions.submit(
              "bank",
              ActionRequest(
                "attempt",
                WorkspaceCommand.Financing(FinancingAction.Approve),
                initial.hcursor.get[String]("version").toOption.get
              )
            )
            _ <- observed.get
            job <- finalJob(actions)
            submissions <- calls.get
          yield job -> submissions
        }
      yield result).timeout(5.seconds).unsafeRunSync()
      assertEquals(result._1.hcursor.get[String]("outcome").toOption, Some("disconnected"))
      assertEquals(result._2, 0)
    }
  }

  test(
    "only a definite submission response is a rejection; unavailable submission remains uncertain"
  ) {
    Vector(Status.PERMISSION_DENIED -> "rejected", Status.UNAVAILABLE -> "disconnected").foreach {
      (failure, expected) =>
        val result = (for
          failRead <- Ref.of[IO, Boolean](false)
          observed <- Deferred[IO, Unit]
          calls <- Ref.of[IO, Int](0)
          ledger = new Stub(
            failRead,
            observed,
            calls,
            Status.UNKNOWN,
            IO.raiseError(failure.asRuntimeException())
          )
          runtime = LiveRuntime(
            Map("bank" -> LiveParticipant("bank", ledger)),
            Path.of("unused"),
            catalog
          )
          result <- LiveActions.resource(runtime).use { actions =>
            for
              initial <- actions.state("bank")
              _ <- actions.submit(
                "bank",
                ActionRequest(
                  "attempt",
                  WorkspaceCommand.Financing(FinancingAction.Approve),
                  initial.hcursor.get[String]("version").toOption.get
                )
              )
              job <- finalJob(actions)
              submissions <- calls.get
            yield job -> submissions
          }
        yield result).timeout(5.seconds).unsafeRunSync()
        assertEquals(result._1.hcursor.get[String]("outcome").toOption, Some(expected))
        assertEquals(result._2, 1)
    }
  }

  test("a namesake template from another package cannot supply the live application") {
    val rogue =
      application.copy(template = application.template.toBuilder.setPackageId("b" * 64).build())
    val accepted = Vector(rogue, application).filter(catalog.accepts)
    assertEquals(accepted, Vector(application))
    assertEquals(
      LiveSnapshot(Vector(rogue).filter(catalog.accepts), Vector.empty).application,
      None
    )
  }

  private def finalJob(actions: LiveActions): IO[Json] = actions.state("bank").flatMap { state =>
    state.hcursor
      .get[Vector[Json]]("jobs")
      .toOption
      .get
      .find(!_.hcursor.get[String]("outcome").contains("pending")) match
      case Some(value) => IO.pure(value)
      case None        => IO.sleep(10.millis) *> IO.defer(finalJob(actions))
  }

  private final class Stub(
      failRead: Ref[IO, Boolean],
      observed: Deferred[IO, Unit],
      calls: Ref[IO, Int],
      readFailure: Status,
      response: IO[Json]
  ) extends ParticipantLedger:
    val party = "bank-party"
    val user = "bank"
    def active(readParty: String = party): IO[Vector[ActiveContract]] =
      failRead.getAndSet(false).flatMap { fail =>
        if fail then observed.complete(()) *> IO.raiseError(readFailure.asRuntimeException())
        else IO.pure(Vector(application))
      }
    def events: IO[Vector[Json]] = IO.pure(Vector.empty)
    def exercise(
        contract: ActiveContract,
        choice: String,
        argument: ValueOuterClass.Value,
        commandId: String,
        actAs: String = party
    ): IO[Json] = calls.update(_ + 1) *> response

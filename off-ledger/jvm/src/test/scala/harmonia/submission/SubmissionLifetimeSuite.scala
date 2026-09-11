package harmonia.submission

import cats.effect.{Deferred, IO}
import cats.effect.unsafe.implicits.global
import cats.syntax.all.*
import harmonia.workspace.WorkspaceCommand
import harmonia.financing.FinancingAction
import munit.FunSuite
import scala.concurrent.duration.*

class SubmissionLifetimeSuite extends FunSuite:
  test("closing the submission owner cancels an in-flight wait") {
    val check = for
      started <- Deferred[IO, Unit]
      cancelled <- Deferred[IO, Unit]
      _ <- Submissions
        .resource { (_, _, _) =>
          IO.pure(
            PreparedSubmission(
              "a" * 64,
              Some(
                (started.complete(()) *> IO.never[SubmissionResult])
                  .onCancel(cancelled.complete(()).void)
              )
            )
          )
        }
        .use { submissions =>
          submissions.submit(
            "bank",
            ActionRequest("one", WorkspaceCommand.Financing(FinancingAction.Approve), "a" * 64)
          ) *> started.get
        }
      _ <- cancelled.get
    yield ()
    check.timeout(5.seconds).unsafeRunSync()
  }

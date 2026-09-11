package harmonia.ledger

import cats.effect.{IO, Resource}
import cats.syntax.all.*
import harmonia.files.ArtifactFiles
import harmonia.processes.ManagedProcess
import java.nio.file.Path
import java.net.ServerSocket
import scala.concurrent.duration.*

final case class CantonSandbox(port: Int)

object CantonSandbox:
  def resource(root: Path, artifacts: Path, dar: Path): Resource[IO, CantonSandbox] =
    val portFile = artifacts.resolve("ledger.port")
    val command = List(
      root.resolve("scripts/daml").toString,
      "sandbox",
      "--port-file",
      portFile.toString,
      "--dar",
      dar.toString
    )
    val portOptions = List(
      "--port",
      "--admin-api-port",
      "--sequencer-public-port",
      "--sequencer-admin-port",
      "--mediator-admin-port",
      "--json-api-port"
    )
    val freePorts = List
      .fill(portOptions.size)(Resource.fromAutoCloseable(IO.blocking(new ServerSocket(0))))
      .sequence
      .use(sockets => IO.pure(sockets.map(_.getLocalPort)))
    harmonia.ledger.lifecycle.LedgerLease
      .resource(root)
      .evalMap(_ => freePorts)
      .flatMap { ports =>
        val arguments =
          portOptions.zip(ports).flatMap((option, port) => List(option, port.toString))
        // The SDK also writes rotating detail logs relative to its working directory.
        ManagedProcess.start(command ++ arguments, artifacts, artifacts.resolve("canton.log"))
      }
      .evalMap { process =>
        def awaitPort: IO[CantonSandbox] =
          ArtifactFiles.exists(portFile).flatMap {
            case true => ArtifactFiles.read(portFile).map(text => CantonSandbox(text.trim.toInt))
            case false =>
              IO.raiseUnless(process.isAlive)(
                RuntimeException(s"Canton exited. See $artifacts/canton.log")
              ) *>
                IO.sleep(200.millis) *> IO.defer(awaitPort)
          }
        awaitPort.timeout(90.seconds)
      }

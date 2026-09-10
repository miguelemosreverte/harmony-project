package harmonia.app

import cats.effect.{ExitCode, IO, IOApp}
import harmonia.files.ArtifactFiles
import harmonia.ledger.{CantonSandbox, DamlScript}
import harmonia.stories.run.CheckStories
import java.nio.file.Path

object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    val root = Path.of(sys.env.getOrElse("HARMONIA_ROOT", "..")).toAbsolutePath.normalize()
    args match
      case "check" :: stories => CheckStories.run(root, stories)
      case List("smoke") =>
        val dar = root.resolve("on-ledger/smoke/.daml/dist/harmonia-smoke-0.1.0.dar")
        for
          artifacts <- ArtifactFiles.createRun(root, "smoke")
          _ <- IO.println(s"Starting local Canton. Evidence: $artifacts")
          output <- CantonSandbox.resource(root, artifacts, dar).use { ledger =>
            DamlScript.run(root, ledger, dar, "Smoke:smoke", artifacts)
          }
          observed <- ArtifactFiles.read(output)
          _ <- IO.println(s"Observed from the ledger:\n$observed")
        yield ExitCode.Success
      case _ =>
        IO.println("Usage: scripts/harmonia smoke | check [story-directory ...]").as(ExitCode.Error)

package harmonia.tools

import cats.effect.{ExitCode, IO, IOApp}
import harmonia.files.ArtifactFiles
import harmonia.ledger.{CantonSandbox, DamlScript}
import harmonia.ledger.network.CantonNetwork
import harmonia.stories.run.CheckStories
import java.nio.file.Path

object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    val root = Path.of(sys.env.getOrElse("HARMONIA_ROOT", ".")).toAbsolutePath.normalize()
    args match
      case List("release-check") =>
        harmonia.release.CheckRelease.run(root).as(ExitCode.Success)
      case List("release-verify", directory) =>
        harmonia.release.ReleaseManifest.verify(root.resolve(directory)).as(ExitCode.Success)
      case List("portable-check") =>
        harmonia.verification.CheckPortableProject.run(root, None).as(ExitCode.Success)
      case List("portable-check", archive) =>
        harmonia.verification.CheckPortableProject
          .run(root, Some(root.resolve(archive)))
          .as(ExitCode.Success)
      case List("boundaries-check") =>
        harmonia.verification.CheckBoundaries.run(root).as(ExitCode.Success)
      case List("builder-check") =>
        harmonia.packages.verify.CheckBuilder.run(root).as(ExitCode.Success)
      case List("composer-check") =>
        harmonia.composition.verify.CheckComposer.run(root).as(ExitCode.Success)
      case List("live") => harmonia.tools.ReaderSandbox.run(root).as(ExitCode.Success)
      case List("live-attach", source, output) =>
        harmonia.demo.Demo
          .reconnect(root, root.resolve(source), root.resolve(output))
          .use { server =>
            IO.println(s"Product: http://127.0.0.1:${server.port}/") *> IO.never
          }
          .as(ExitCode.Success)
      case List("live-check") => harmonia.live.verify.CheckLive.run(root).as(ExitCode.Success)
      case List("bindings-check") =>
        harmonia.bindings.verify.CheckBindings.run(root).as(ExitCode.Success)
      case List("bindings-check", mapping, expected, output) =>
        harmonia.bindings.verify.CheckBinding
          .run(root, root.resolve(mapping), root.resolve(expected), root.resolve(output))
          .as(ExitCode.Success)
      case List("packages-check") =>
        harmonia.packages.verify.CheckPackages.run(root).as(ExitCode.Success)
      case "check" :: stories => CheckStories.run(root, stories)
      case List("network-smoke") =>
        for
          artifacts <- ArtifactFiles.createRun(root, "network")
          _ <- IO.println(s"Starting three participant nodes. Evidence: $artifacts")
          _ <- CantonNetwork
            .resource(
              root,
              artifacts,
              root.resolve("harness/ledger/tests/.daml/dist/harmonia-tests-0.1.0.dar")
            )
            .use { network => IO.println(s"Connected: ${network.participants}") }
        yield ExitCode.Success
      case List("smoke") =>
        val dar = root.resolve("harness/ledger/tests/.daml/dist/harmonia-tests-0.1.0.dar")
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
        IO.println(
          "Usage: scripts/harmonia release-check | release-verify directory | book-links directory | live | live-check | composer-check | builder-check | boundaries-check | portable-check [archive] | generate-bindings mapping output | bindings-check | smoke | network-smoke | resolve-packages [manifest] | packages-check | check [story-directory ...] | book run-directory | export-book run-directory output-directory | serve-book directory"
        ).as(ExitCode.Error)

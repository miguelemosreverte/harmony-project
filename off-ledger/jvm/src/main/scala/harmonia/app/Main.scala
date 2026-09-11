package harmonia.app

import cats.effect.{ExitCode, IO, IOApp}
import harmonia.files.ArtifactFiles
import harmonia.book.{ExportBook, ServeBook}
import harmonia.ledger.{CantonSandbox, DamlScript}
import harmonia.ledger.network.CantonNetwork
import harmonia.stories.run.CheckStories
import java.nio.file.Path

object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    val root = Path.of(sys.env.getOrElse("HARMONIA_ROOT", "..")).toAbsolutePath.normalize()
    args match
      case List("builder-check") =>
        harmonia.builder.verify.CheckBuilder.run(root).as(ExitCode.Success)
      case List("composer-check") =>
        harmonia.composer.verify.CheckComposer.run(root).as(ExitCode.Success)
      case List("live")       => harmonia.live.run.LiveRuntime.serve(root).as(ExitCode.Success)
      case List("live-check") => harmonia.live.verify.CheckLive.run(root).as(ExitCode.Success)
      case List("generate-bindings", mapping, output) =>
        harmonia.bindings.generate.GenerateBinding
          .run(root, root.resolve(mapping), root.resolve(output))
          .as(ExitCode.Success)
      case List("bindings-check") =>
        harmonia.bindings.verify.CheckBindings.run(root).as(ExitCode.Success)
      case List("bindings-check", mapping, expected, output) =>
        harmonia.bindings.verify.CheckBinding
          .run(root, root.resolve(mapping), root.resolve(expected), root.resolve(output))
          .as(ExitCode.Success)
      case List("packages-check") =>
        harmonia.packages.verify.CheckPackages.run(root).as(ExitCode.Success)
      case List("resolve-packages") =>
        harmonia.packages.resolve.ResolvePackages
          .run(root, root.resolve("packages/inputs.md"))
          .as(ExitCode.Success)
      case List("resolve-packages", manifest) =>
        harmonia.packages.resolve.ResolvePackages
          .run(root, root.resolve(manifest))
          .as(ExitCode.Success)
      case List("export-book", run, output) =>
        ExportBook.write(root, root.resolve(run), root.resolve(output)).as(ExitCode.Success)
      case List("book", run) =>
        for
          output <- ArtifactFiles.createRun(root, "book")
          _ <- ExportBook.write(root, root.resolve(run), output)
          _ <- ServeBook.serve(output)
        yield ExitCode.Success
      case List("serve-book", directory) =>
        ServeBook.serve(root.resolve(directory)).as(ExitCode.Success)
      case "check" :: stories => CheckStories.run(root, stories)
      case List("network-smoke") =>
        for
          artifacts <- ArtifactFiles.createRun(root, "network")
          _ <- IO.println(s"Starting three participant nodes. Evidence: $artifacts")
          _ <- CantonNetwork
            .resource(
              root,
              artifacts,
              root.resolve("on-ledger/smoke/.daml/dist/harmonia-smoke-0.1.0.dar")
            )
            .use { network => IO.println(s"Connected: ${network.participants}") }
        yield ExitCode.Success
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
        IO.println(
          "Usage: scripts/harmonia live | live-check | composer-check | builder-check | generate-bindings mapping output | bindings-check | smoke | network-smoke | resolve-packages [manifest] | packages-check | check [story-directory ...] | book run-directory | export-book run-directory output-directory | serve-book directory"
        ).as(ExitCode.Error)

package harmonia.packages.verify

import cats.effect.IO
import cats.syntax.all.*
import harmonia.files.ArtifactFiles
import harmonia.ledger.DamlScript
import harmonia.ledger.network.CantonNetwork
import harmonia.packages.inspect.InspectDar
import harmonia.packages.resolve.ResolvePackages
import harmonia.processes.ManagedProcess
import harmonia.stories.read.MarkdownYaml
import harmonia.stories.compare.CompareResults
import io.circe.Json
import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters.*

object CheckPackages:
  def run(root: Path): IO[Unit] = for
    resolved <- ResolvePackages.run(root, root.resolve("packages/inputs.md"))
    artifacts <- ArtifactFiles.createRun(root, "packages")
    _ <- ArtifactFiles.write(artifacts.resolve("resolved.json"), resolved.spaces2)
    _ <- Vector("metadata-receipts", "package-example").traverse_ { name =>
      ManagedProcess.run(
        List(
          root.resolve("scripts/daml").toString,
          "build",
          "--package-root",
          root.resolve("on-ledger").resolve(name).toString
        ),
        root,
        artifacts.resolve(s"build-$name.log")
      )
    }
    dar = root.resolve("on-ledger/package-example/.daml/dist/harmonia-package-example-0.1.0.dar")
    scenarioText <- ArtifactFiles.read(root.resolve("on-ledger/package-example/input.md"))
    scenario <- IO.fromEither(
      MarkdownYaml.read(scenarioText, "Scenario").left.map(RuntimeException(_))
    )
    reference <- IO.fromEither(scenario.hcursor.get[String]("reference"))
    expectedText <- ArtifactFiles.read(root.resolve("on-ledger/package-example/expected.md"))
    expected <- IO.fromEither(
      MarkdownYaml.read(expectedText, "Result").left.map(RuntimeException(_))
    )
    _ <- ArtifactFiles.write(artifacts.resolve("input.md"), scenarioText)
    _ <- ArtifactFiles.write(artifacts.resolve("expected.md"), expectedText)
    _ <- ArtifactFiles.write(artifacts.resolve("input.json"), Json.fromString(reference).noSpaces)
    metadata = root.resolve(".artifacts/packages/inputs/metadata.dar")
    expectedHash <- IO.fromEither(
      resolved.hcursor.downField("inputs").downField("metadata").get[String]("sha256")
    )
    _ <- Vector("first", "repeat").traverse_ { label =>
      val run = artifacts.resolve(label)
      CantonNetwork.resource(root, run.resolve("network"), dar, Vector(metadata)).use { network =>
        for
          output <- DamlScript.runNetwork(
            root,
            network.configuration,
            dar,
            "PackageExample:run",
            run,
            artifacts.resolve("input.json")
          )
          text <- ArtifactFiles.read(output)
          actual <- IO.fromEither(io.circe.parser.parse(text))
          differences = CompareResults.compare(expected, actual)
          _ <- ArtifactFiles.write(
            run.resolve("actual.md"),
            MarkdownYaml.render("Observed package import", "Result", actual)
          )
          _ <- ArtifactFiles.write(run.resolve("diff.md"), CompareResults.markdown(differences))
          _ <- IO.raiseWhen(differences.nonEmpty)(
            RuntimeException(
              s"Pinned package execution differed from its committed expectation; see $run"
            )
          )
          downloads <- IO.blocking {
            val stream = Files.list(run.resolve("network/downloaded"))
            try stream.iterator().asScala.filter(_.toString.endsWith(".dar")).toVector
            finally stream.close()
          }
          _ <- IO.raiseUnless(downloads.size == 1)(
            RuntimeException("Expected one administrator-downloaded DAR")
          )
          downloadedHash <- InspectDar.digest(downloads.head)
          _ <- IO.raiseUnless(downloadedHash == expectedHash)(
            RuntimeException("Participant download changed the original archive bytes")
          )
          _ <- ArtifactFiles.write(
            run.resolve("verification.json"),
            Json
              .obj(
                "matched" -> Json.True,
                "admin_download_sha256" -> Json.fromString(downloadedHash),
                "original_bytes_preserved" -> Json.True
              )
              .spaces2
          )
        yield ()
      }
    }
    _ <- IO.println(
      s"PASS pinned package imports on two fresh networks and administrator DAR retrieval: $artifacts"
    )
  yield ()

package harmonia.bindings.verify

import cats.effect.IO
import cats.syntax.all.*
import harmonia.bindings.generate.{GenerateBinding, GeneratedProject}
import harmonia.files.ArtifactFiles
import harmonia.ledger.{CantonSandbox, DamlScript}
import harmonia.files.MarkdownYaml
import harmonia.stories.compare.{CompareResults, Difference}
import io.circe.Json
import java.nio.file.Path

final case class BindingObservation(artifacts: Path, differences: Vector[Difference])

object CheckBinding:
  def run(root: Path, mapping: Path, expectedPath: Path, output: Path): IO[Unit] = for
    expectedMarkdown <- ArtifactFiles.read(expectedPath)
    project <- GenerateBinding.run(root, mapping, output)
    observed <- verifyBuilt(root, project, expectedMarkdown)
    _ <- IO.raiseWhen(observed.differences.nonEmpty)(
      RuntimeException(
        s"Generated binding differs from its committed expectation. See ${observed.artifacts}/diff.md"
      )
    )
    _ <- IO.println(
      s"PASS generated binding against unchanged source DAR ${project.sourceDigest}: ${observed.artifacts}"
    )
  yield ()

  def verifyBuilt(
      root: Path,
      project: GeneratedProject,
      expectedMarkdown: String
  ): IO[BindingObservation] = for
    expected <- IO.fromEither(
      MarkdownYaml.read(expectedMarkdown, "Result").left.map(RuntimeException(_))
    )
    output = project.directory
    artifacts <- ArtifactFiles.createRun(root, "bindings")
    _ <- ArtifactFiles.write(artifacts.resolve("expected.md"), expectedMarkdown)
    inputMarkdown <- ArtifactFiles.read(output.resolve("mapping.md"))
    _ <- ArtifactFiles.write(artifacts.resolve("input.md"), inputMarkdown)
    rawPath <- CantonSandbox
      .resource(root, artifacts, project.example)
      .use(ledger => DamlScript.run(root, ledger, project.example, "Example:run", artifacts))
    raw <- ArtifactFiles.read(rawPath).flatMap(value => IO.fromEither(io.circe.parser.parse(value)))
    booleans <- Vector(
      "unauthorizedReader" -> "unauthorized_reader",
      "readerVisible" -> "reader_visible",
      "originalActive" -> "original_active",
      "repeatedRejected" -> "repeated_rejected"
    ).traverse { (from, to) =>
      IO.fromEither(raw.hcursor.get[Boolean](from)).map(value => to -> Json.fromBoolean(value))
    }
    texts <- Vector("sourceStatus" -> "source_status", "workflowStatus" -> "workflow_status")
      .traverse { (from, to) =>
        IO.fromEither(raw.hcursor.get[String](from)).map(value => to -> Json.fromString(value))
      }
    count <- IO
      .fromEither(raw.hcursor.get[Json]("activeSources"))
      .flatMap(value =>
        IO.fromOption(
          value.asNumber.flatMap(_.toInt).orElse(value.asString.flatMap(_.toIntOption))
        )(RuntimeException("Invalid source count"))
      )
    actual = Json.fromFields(booleans ++ texts :+ ("active_sources" -> Json.fromInt(count)))
    differences = CompareResults.compare(expected, actual)
    _ <- ArtifactFiles.write(
      artifacts.resolve("actual.md"),
      MarkdownYaml.render("Observed generated binding", "Result", actual)
    )
    _ <- ArtifactFiles.write(artifacts.resolve("diff.md"), CompareResults.markdown(differences))
    manifest <- ArtifactFiles.read(output.resolve("generation.json"))
    _ <- ArtifactFiles.write(artifacts.resolve("generation.json"), manifest)
    _ <- harmonia.stories.run.RunProvenance.write(
      root,
      artifacts.resolve("input.md"),
      artifacts.resolve("expected.md"),
      project.example,
      artifacts,
      differences.isEmpty,
      "one local Canton participant, generated library and separate executable example"
    )
    provenance <- ArtifactFiles
      .read(artifacts.resolve("run.json"))
      .flatMap(value => IO.fromEither(io.circe.parser.parse(value)))
    _ <- ArtifactFiles.write(
      artifacts.resolve("run.json"),
      provenance
        .mapObject(
          _.remove("story_format")
            .add("mapping_format", Json.fromInt(1))
            .add("artifact_kind", Json.fromString("generated-binding"))
        )
        .spaces2
    )
  yield BindingObservation(artifacts, differences)

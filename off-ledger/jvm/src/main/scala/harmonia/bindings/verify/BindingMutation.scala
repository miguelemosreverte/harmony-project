package harmonia.bindings.verify

import cats.effect.IO
import cats.syntax.all.*
import harmonia.bindings.generate.GenerateBinding
import harmonia.files.ArtifactFiles
import harmonia.packages.inspect.InspectDar
import harmonia.processes.ManagedProcess
import io.circe.Json
import java.nio.file.Path

object BindingMutation:
  def verify(root: Path): IO[Unit] = for
    expected <- ArtifactFiles.read(root.resolve("packages/mappings/approval-expected.md"))
    artifacts <- ArtifactFiles.createRun(root, "binding-mutation")
    project <- GenerateBinding.run(
      root,
      root.resolve("packages/mappings/financing.md"),
      artifacts.resolve("project")
    )
    source = project.directory.resolve("library/daml/GeneratedFinancing.daml")
    original <- ArtifactFiles.read(source)
    anchor = "replacement <- exercise source Source.Approve"
    _ <- IO.raiseUnless(
      original.contains(anchor) && original.indexOf(anchor) == original.lastIndexOf(anchor)
    )(RuntimeException("The deliberate source-call mutation needs exactly one reviewed anchor"))
    _ <- ArtifactFiles.write(source, original.replace(anchor, "let replacement = source"))
    _ <- Vector("library", "example").traverse_ { name =>
      ManagedProcess.run(
        List(
          root.resolve("scripts/daml").toString,
          "build",
          "--package-root",
          project.directory.resolve(name).toString
        ),
        root,
        artifacts.resolve(s"mutated-$name.log")
      )
    }
    sourceHash <- InspectDar.digest(source)
    libraryHash <- InspectDar.digest(project.library)
    exampleHash <- InspectDar.digest(project.example)
    unchanged <- InspectDar.digest(project.directory.resolve("vendor/source.dar"))
    _ <- IO.raiseUnless(unchanged == project.sourceDigest)(
      RuntimeException("The mutation must leave the source DAR untouched")
    )
    baselineText <- ArtifactFiles.read(project.directory.resolve("generation.json"))
    baseline <- IO.fromEither(io.circe.parser.parse(baselineText))
    _ <- ArtifactFiles.write(artifacts.resolve("generation-baseline.json"), baselineText)
    recorded = baseline.mapObject { obj =>
      obj
        .add("mutation", Json.fromString("skip the real source choice"))
        .add(
          "files",
          obj("files").get
            .mapObject(_.add("library/daml/GeneratedFinancing.daml", Json.fromString(sourceHash)))
        )
        .add(
          "artifacts",
          obj("artifacts").get.mapObject(
            _.add("library_dar", Json.fromString(libraryHash))
              .add("example_dar", Json.fromString(exampleHash))
          )
        )
    }
    _ <- ArtifactFiles.write(project.directory.resolve("generation.json"), recorded.spaces2)
    observed <- CheckBinding.verifyBuilt(root, project, expected)
    paths = observed.differences.map(_.path).toSet
    _ <- IO.raiseUnless(Set("$.source_status", "$.original_active").subsetOf(paths))(
      RuntimeException(
        "The golden failed to detect the compiled binding skipping its source action"
      )
    )
    _ <- ArtifactFiles.write(
      artifacts.resolve("verification.md"),
      s"# Deliberate binding regression detected\n\nThe mutated adapter compiled and executed on Canton, but skipped its source choice. The independent golden reported ${observed.differences.size} business differences at ${observed.artifacts}. The source DAR stayed unchanged. Infrastructure failures do not satisfy this check.\n"
    )
    _ <- IO.println(
      s"PASS golden caught compiled binding regression (${observed.differences.size} differences): $artifacts; observations: ${observed.artifacts}"
    )
  yield ()

package harmonia.bindings.generate

import cats.effect.IO
import cats.syntax.all.*
import harmonia.bindings.read.BindingFormat
import harmonia.bindings.inspect.TemplateShapeReader
import harmonia.files.ArtifactFiles
import harmonia.packages.resolve.ResolvePackages
import harmonia.packages.inspect.InspectDar
import harmonia.processes.ManagedProcess
import io.circe.Json
import java.nio.file.{Files, Path, StandardCopyOption}
import java.security.MessageDigest

final case class GeneratedProject(
    directory: Path,
    library: Path,
    example: Path,
    sourceDigest: String
)

object GenerateBinding:
  private val owner = "harmonia-typed-binding/v1"

  def run(root: Path, mappingPath: Path, output: Path): IO[GeneratedProject] = for
    markdown <- ArtifactFiles.read(mappingPath)
    mapping <- IO.fromEither(BindingFormat.read(markdown).left.map(RuntimeException(_)))
    resolved <- ResolvePackages.run(root, root.resolve("packages/inputs.md"))
    input <- IO.fromOption(resolved.hcursor.downField("inputs").downField(mapping.source).focus)(
      RuntimeException(
        s"Unknown pinned source alias ${mapping.source}; add a reviewed identity to packages/inputs.md"
      )
    )
    sourcePath <- IO.fromEither(input.hcursor.get[String]("file"))
    digest <- IO.fromEither(input.hcursor.get[String]("sha256"))
    prettyPath = root.resolve(".artifacts/packages/cache").resolve(digest).resolve("main.daml-lf")
    prettySize <- IO.blocking(Files.size(prettyPath))
    _ <- IO.raiseWhen(prettySize > 16 * 1024 * 1024)(
      RuntimeException("LF inspection exceeds the 16 MiB limit")
    )
    pretty <- ArtifactFiles.read(prettyPath)
    shape <- IO.fromEither(TemplateShapeReader.read(pretty, mapping).left.map(RuntimeException(_)))
    files <- IO.fromEither(GenerateSources(mapping, shape).left.map(RuntimeException(_)))
    exists <- ArtifactFiles.exists(output)
    _ <-
      if !exists then IO.unit
      else
        for
          marker <- ArtifactFiles
            .read(output.resolve("generation.json"))
            .flatMap(value => IO.fromEither(io.circe.parser.parse(value)))
          _ <- IO.raiseUnless(marker.hcursor.get[String]("generator").contains(owner))(
            RuntimeException(s"Output is not an owned generated project: $output")
          )
        yield ()
    _ <- files.traverse_(file => ArtifactFiles.write(output.resolve(file.path), file.content))
    _ <- ArtifactFiles.write(output.resolve("mapping.md"), markdown)
    _ <- Vector(
      root.resolve(sourcePath) -> "source.dar",
      root.resolve(
        "on-ledger/interfaces/.daml/dist/harmonia-interfaces-0.1.0.dar"
      ) -> "interfaces.dar",
      root.resolve("on-ledger/core/.daml/dist/harmonia-core-0.1.0.dar") -> "core.dar"
    ).traverse_ { (source, name) =>
      IO.blocking {
        Files.createDirectories(output.resolve("vendor"))
        Files.copy(
          source,
          output.resolve("vendor").resolve(name),
          StandardCopyOption.REPLACE_EXISTING
        )
        ()
      }
    }
    _ <- ArtifactFiles.write(
      output.resolve("generation.json"),
      Json
        .obj(
          "generator" -> Json.fromString(owner),
          "sdk" -> Json.fromString("3.4.11"),
          "mapping_sha256" -> Json.fromString(hash(markdown)),
          "source" -> input,
          "files" -> Json.fromFields(
            files.map(file => file.path -> Json.fromString(hash(file.content)))
          )
        )
        .spaces2
    )
    _ <- Vector("library", "example").traverse_ { name =>
      ManagedProcess.run(
        List(
          root.resolve("scripts/daml").toString,
          "build",
          "--package-root",
          output.resolve(name).toString
        ),
        root,
        output.resolve(s"build-$name.log")
      )
    }
    after <- InspectDar.digest(root.resolve(sourcePath))
    copied <- InspectDar.digest(output.resolve("vendor/source.dar"))
    _ <- IO.raiseUnless(after == digest && copied == digest)(
      RuntimeException("Source DAR identity changed during generation")
    )
    artifactDigests <- Vector(
      "library_dar" -> output.resolve(
        s"library/.daml/dist/${GenerateSources.packageName(mapping)}-0.1.0.dar"
      ),
      "example_dar" -> output.resolve(
        s"example/.daml/dist/${GenerateSources.packageName(mapping)}-example-0.1.0.dar"
      ),
      "interfaces_dar" -> output.resolve("vendor/interfaces.dar"),
      "core_dar" -> output.resolve("vendor/core.dar")
    ).traverse { (name, path) =>
      InspectDar.digest(path).map(value => name -> Json.fromString(value))
    }
    manifest <- ArtifactFiles
      .read(output.resolve("generation.json"))
      .flatMap(value => IO.fromEither(io.circe.parser.parse(value)))
    _ <- ArtifactFiles.write(
      output.resolve("generation.json"),
      manifest.mapObject(_.add("artifacts", Json.fromFields(artifactDigests))).spaces2
    )
    _ <- IO.println(s"Generated and compiled typed binding: $output")
  yield GeneratedProject(
    output,
    output.resolve(s"library/.daml/dist/${GenerateSources.packageName(mapping)}-0.1.0.dar"),
    output.resolve(s"example/.daml/dist/${GenerateSources.packageName(mapping)}-example-0.1.0.dar"),
    digest
  )

  private def hash(text: String): String = MessageDigest
    .getInstance("SHA-256")
    .digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8))
    .map(value => f"${value & 0xff}%02x")
    .mkString

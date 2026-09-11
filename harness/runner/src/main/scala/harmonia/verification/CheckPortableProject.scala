package harmonia.verification

import cats.effect.{IO, Resource}
import cats.syntax.all.*
import harmonia.bindings.generate.GeneratedProject
import harmonia.bindings.verify.CheckBinding
import harmonia.packages.workspace.ProjectArchive
import harmonia.files.ArtifactFiles
import harmonia.packages.inspect.InspectDar
import harmonia.processes.ManagedProcess
import io.circe.Json
import java.nio.file.{Files, Path}
import java.util.zip.ZipFile
import scala.jdk.CollectionConverters.*

object CheckPortableProject:
  def run(root: Path, provided: Option[Path]): IO[Unit] = for
    archive <- provided.fold(defaultArchive(root))(IO.pure)
    output <- ArtifactFiles.createRun(root, "portable-project")
    _ <- IO.println(
      s"Rebuilding a portable project with empty build directories. Evidence: $output"
    )
    project <- unpack(archive, output)
    expected <- ArtifactFiles.read(root.resolve("product/packages/mappings/approval-expected.md"))
    _ <- Vector("library", "example").traverse_(name =>
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
    )
    manifest <- ArtifactFiles
      .read(output.resolve("generation.json"))
      .flatMap(value => IO.fromEither(io.circe.parser.parse(value)))
    _ <- Vector(
      "library_dar" -> project.library,
      "example_dar" -> project.example,
      "interfaces_dar" -> output.resolve("vendor/interfaces.dar"),
      "core_dar" -> output.resolve("vendor/core.dar")
    ).traverse_ { (name, path) =>
      InspectDar
        .digest(path)
        .flatMap(hash =>
          IO.raiseUnless(manifest.hcursor.downField("artifacts").get[String](name).contains(hash))(
            RuntimeException(s"Freshly built $name differs from the archived manifest")
          )
        )
    }
    _ <- InspectDar
      .digest(output.resolve("vendor/source.dar"))
      .flatMap(hash =>
        IO.raiseUnless(hash == project.sourceDigest)(
          RuntimeException("Portable source DAR changed")
        )
      )
    observed <- CheckBinding.verifyBuilt(root, project, expected)
    _ <- IO.raiseWhen(observed.differences.nonEmpty)(
      RuntimeException(s"Portable project differs: ${observed.artifacts}/diff.md")
    )
    _ <- ArtifactFiles.write(
      output.resolve("verification.json"),
      Json
        .obj(
          "archive" -> Json.fromString(archive.toString),
          "ledger_evidence" -> Json.fromString(observed.artifacts.toString),
          "empty_build_directories" -> Json.fromBoolean(true),
          "compiled_digests_match" -> Json.fromBoolean(true),
          "golden_differences" -> Json.fromInt(observed.differences.size)
        )
        .spaces2
    )
    _ <- IO.println(
      s"PASS portable project: fresh compilation, identical DARs, and real ledger golden at ${observed.artifacts}"
    )
  yield ()

  private def defaultArchive(root: Path): IO[Path] =
    val directory = root.resolve(".artifacts/generated-financing")
    for
      manifest <- ArtifactFiles
        .read(directory.resolve("generation.json"))
        .flatMap(value => IO.fromEither(io.circe.parser.parse(value)))
      digest <- IO.fromEither(manifest.hcursor.downField("source").get[String]("sha256"))
      members <- IO.fromEither(manifest.hcursor.get[Map[String, String]]("files"))
      archive <- ProjectArchive.write(
        GeneratedProject(
          directory,
          directory.resolve("library/.daml/dist/harmonia-binding-generatedfinancing-0.1.0.dar"),
          directory.resolve(
            "example/.daml/dist/harmonia-binding-generatedfinancing-example-0.1.0.dar"
          ),
          digest,
          members.keys.toVector,
          manifest
        )
      )
    yield archive

  private def unpack(archive: Path, output: Path): IO[GeneratedProject] =
    Resource.fromAutoCloseable(IO.blocking(new ZipFile(archive.toFile))).use { zip =>
      IO.blocking {
        require(Files.size(archive) <= 16L * 1024 * 1024, "Portable archive exceeds 16 MiB")
        val entries = zip.entries().asScala.toVector
        require(
          entries.size <= 64 && entries.map(_.getName).distinct.size == entries.size,
          "Invalid portable project members"
        )
        val names = entries.map(_.getName)
        def dar(prefix: String): Path =
          val matches =
            names.filter(name => name.startsWith(prefix + "/.daml/dist/") && name.endsWith(".dar"))
          require(matches.size == 1, s"Expected one $prefix DAR"); output.resolve(matches.head)
        var expanded = 0L
        entries.foreach { entry =>
          val target = output.resolve(entry.getName).normalize()
          require(
            target.startsWith(output) && !entry.getName
              .startsWith("/") && !entry.getName.split('/').contains(".."),
            "Invalid portable project path"
          )
          require(!entry.isDirectory, "Portable archive must list files only")
          val stream = zip.getInputStream(entry)
          val bytes =
            try stream.readNBytes(32 * 1024 * 1024 + 1)
            finally stream.close()
          expanded += bytes.length
          require(expanded <= 32L * 1024 * 1024, "Portable project exceeds 32 MiB expanded")
          // Build outputs in the ZIP are evidence only. Rebuild from source and vendor inputs.
          if !entry.getName.split('/').contains(".daml") then
            Files.createDirectories(target.getParent); Files.write(target, bytes)
        }
        val manifest = io.circe.parser
          .parse(Files.readString(output.resolve("generation.json")))
          .fold(throw _, identity)
        GeneratedProject(
          output,
          dar("library"),
          dar("example"),
          manifest.hcursor.downField("source").get[String]("sha256").fold(throw _, identity),
          manifest.hcursor.get[Map[String, String]]("files").fold(throw _, identity).keys.toVector,
          manifest
        )
      }
    }

package harmonia.release

import cats.effect.IO
import cats.syntax.all.*
import harmonia.book.ExportBook
import harmonia.examples.Examples
import harmonia.files.ArtifactFiles
import harmonia.processes.ManagedProcess
import io.circe.Json
import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters.*

object CheckRelease:
  def run(root: Path): IO[Unit] = for
    work <- ArtifactFiles.createRun(root, "release")
    status <- ReleaseFiles.git(root, work.resolve("status.txt"), "status", "--porcelain")
    _ <- IO.raiseUnless(status.isEmpty)(
      RuntimeException("Commit the source before building a release")
    )
    revision <- ReleaseFiles.git(root, work.resolve("revision.txt"), "rev-parse", "HEAD")
    _ <- IO.blocking {
      require(
        Files.getFileStore(root).getUsableSpace > 900L * 1024 * 1024,
        "Release needs at least 900 MiB of available disk space"
      )
    }
    source = work.resolve("checkout")
    bundle = work.resolve(s"harmonia-${revision.take(12)}")
    _ <- IO.println(s"Clean release check: $work; one sequential ledger environment at a time")
    _ <- ReleaseFiles.cloneAt(root, source, revision, work.resolve("source-clone"))
    commands = Vector(
      "build" -> List("scripts/build"),
      "daml-test" -> List("scripts/daml", "test", "--package-root", "on-ledger/tests"),
      "scala-test" -> List(
        "bash",
        "-c",
        "source scripts/environment && cd off-ledger && sbt --batch jvm/test"
      )
    ) ++ Vector(
      "packages-check",
      "bindings-check",
      "check",
      "live-check",
      "composer-check",
      "builder-check",
      "boundaries-check",
      "portable-check"
    )
      .map(name => name -> List("scripts/harmonia", name))
    stages <- commands.traverse { (name, command) =>
      for
        start <- IO.monotonic
        _ <- IO.println(s"Release check: $name")
        _ <- ManagedProcess.run(command, source, work.resolve(s"checks/$name.log"))
        elapsed <- IO.monotonic.map(_ - start)
        _ <- IO.println(s"PASS $name (${elapsed.toSeconds}s)")
      yield Json.obj(
        "name" -> Json.fromString(name),
        "command" -> Json.fromValues(command.map(Json.fromString)),
        "seconds" -> Json.fromLong(elapsed.toSeconds),
        "passed" -> Json.True
      )
    }
    _ <- collectRecordings(source, work.resolve("recordings"), revision)
    _ <- AssembleRelease.run(source, bundle, revision, work)
    _ <- ExportBook.write(source, work.resolve("recordings"), bundle.resolve("book"))
    _ <- ReleaseManifest.write(bundle, revision, stages)
    _ <- ReleaseManifest.verify(bundle)
    archive = work.resolve(bundle.getFileName.toString + ".tar.gz")
    _ <- ManagedProcess.run(
      List("tar", "-czf", archive.toString, "-C", work.toString, bundle.getFileName.toString),
      root,
      work.resolve("archive.log")
    )
    digest <- ReleaseFiles.hash(archive)
    _ <- ArtifactFiles.write(work.resolve("archive.sha256"), s"$digest  ${archive.getFileName}\n")
    _ <- IO.println(s"PASS local release: $bundle\nArchive: $archive\nSHA-256: $digest")
  yield ()

  private def collectRecordings(source: Path, output: Path, revision: String): IO[Unit] = for
    files <- ReleaseFiles.files(source.resolve(".artifacts"))
    records = files.filter { path =>
      val relative = source.resolve(".artifacts").relativize(path)
      val first = relative.getName(0).toString
      relative.getNameCount == 3 && path.getFileName.toString == "run.json" &&
      Vector("check-", "live-check-", "composer-check-", "builder-check-", "boundaries-").exists(
        first.startsWith
      )
    }
    _ <- IO.raiseUnless(records.size == Examples.all.size)(
      RuntimeException(s"Expected ${Examples.all.size} release recordings, found ${records.size}")
    )
    _ <- IO.raiseUnless(records.map(_.getParent.getFileName.toString).toSet == Examples.ids)(
      RuntimeException("Missing, extra, or duplicate release story IDs")
    )
    _ <- records.traverse_ { run =>
      for
        json <- ArtifactFiles.read(run).flatMap(text => IO.fromEither(io.circe.parser.parse(text)))
        _ <- IO.raiseUnless(
          json.hcursor.get[String]("revision").contains(revision) && json.hcursor
            .get[Boolean]("worktree_dirty")
            .contains(false) && json.hcursor.get[Boolean]("matched").contains(true)
        )(
          RuntimeException(s"Invalid release recording: $run")
        )
        _ <- IO.blocking {
          Files.createDirectories(output)
          Files.createSymbolicLink(output.resolve(run.getParent.getFileName), run.getParent)
          ()
        }
      yield ()
    }
  yield ()

package harmonia.release

import cats.effect.IO
import cats.syntax.all.*
import harmonia.files.ArtifactFiles
import io.circe.Json
import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters.*

object ReleaseManifest:
  private def payload(path: Path): Boolean =
    !path.iterator().asScala.exists(_.toString == ".git") &&
      path.toString != "manifest.json"

  def write(bundle: Path, revision: String, stages: Vector[Json]): IO[Unit] = for
    files <- ReleaseFiles.files(bundle)
    entries <- files.filter(p => payload(bundle.relativize(p))).traverse { path =>
      ReleaseFiles.hash(path).map { digest =>
        bundle.relativize(path).toString -> Json.fromString(digest)
      }
    }
    _ <- ArtifactFiles.write(
      bundle.resolve("manifest.json"),
      Json
        .obj(
          "format" -> Json.fromInt(1),
          "source_revision" -> Json.fromString(revision),
          "recordings" -> Json.fromInt(32),
          "chapters" -> Json.fromInt(9),
          "sdk" -> Json.fromString("3.4.11"),
          "java" -> Json.fromString(Runtime.version().toString),
          "platform" -> Json.fromString(s"${sys.props("os.name")} ${sys.props("os.arch")}"),
          "environment" -> Json.fromString(
            "Clean Git checkout; installed SDK and dependency caches reused"
          ),
          "stages" -> Json.fromValues(stages),
          "files" -> Json.fromFields(entries)
        )
        .spaces2 + "\n"
    )
  yield ()

  def verify(bundle: Path): IO[Unit] = for
    manifest <- read(bundle.resolve("manifest.json"))
    revision <- IO.fromEither(manifest.hcursor.get[String]("source_revision"))
    entries <- IO.fromEither(manifest.hcursor.get[Map[String, String]]("files"))
    _ <- entries.toVector.traverse_ { (name, expected) =>
      val path = bundle.resolve(name).normalize()
      IO.raiseUnless(path.startsWith(bundle.normalize()) && !Path.of(name).isAbsolute)(
        RuntimeException(s"Invalid manifest path: $name")
      ) *> ReleaseFiles
        .hash(path)
        .flatMap(actual =>
          IO.raiseUnless(actual == expected)(RuntimeException(s"Release file changed: $name"))
        )
    }
    evidence <- read(bundle.resolve("book/evidence.json"))
    stories <- IO.fromEither(evidence.hcursor.get[Vector[Json]]("stories"))
    chapters <- IO.fromEither(evidence.hcursor.get[Vector[Json]]("chapters"))
    _ <- IO.raiseUnless(stories.size == 32 && chapters.size == 9)(
      RuntimeException("Incomplete release book")
    )
    _ <- stories.traverse_ { story =>
      val id = story.hcursor.get[String]("id").toOption.get
      read(bundle.resolve(s"book/evidence/$id/run.json")).flatMap { run =>
        IO.raiseUnless(
          run.hcursor.get[String]("revision").contains(revision) &&
            run.hcursor.get[Boolean]("worktree_dirty").contains(false) &&
            run.hcursor.get[Boolean]("matched").contains(true)
        )(RuntimeException(s"Recording $id does not prove the clean release revision"))
      }
    }
    logs <- ArtifactFiles.createRun(bundle.resolve("source"), "release-verification")
    head <- ReleaseFiles.git(
      bundle.resolve("source"),
      logs.resolve("head.txt"),
      "rev-parse",
      "HEAD"
    )
    status <- ReleaseFiles.git(
      bundle.resolve("source"),
      logs.resolve("status.txt"),
      "status",
      "--porcelain"
    )
    _ <- IO.raiseUnless(head == revision && status.isEmpty)(
      RuntimeException("Packaged source changed")
    )
    _ <- harmonia.book.verify.CheckBookLinks.run(bundle.resolve("source"), bundle.resolve("book"))
    _ <- IO.println(
      s"PASS release: ${entries.size} file hashes, 32 clean-revision recordings, nine chapters; $revision"
    )
  yield ()

  private def read(path: Path): IO[Json] =
    ArtifactFiles.read(path).flatMap(text => IO.fromEither(io.circe.parser.parse(text)))

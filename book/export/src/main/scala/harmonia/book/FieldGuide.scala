package harmonia.book

/** @book.slice
  *   book
  * @book.role
  *   Mount or read offline
  * @book.summary
  *   The exported directory can be read independently or mounted by the product HTTP server without
  *   importing book logic.
  */

import cats.effect.IO
import cats.syntax.all.*
import harmonia.files.ArtifactFiles
import io.circe.syntax.*
import java.nio.file.{Files, Path, StandardCopyOption}
import scala.jdk.CollectionConverters.*

/** Exports the designed book as files. The production server only mounts this directory. */
object FieldGuide:
  private val evidenceNames =
    Vector("input.md", "expected.md", "actual.md", "diff.md", "observation.json", "run.json")
  def evidenceFiles(output: Path, stories: Vector[RecordedStory]): IO[Map[String, String]] =
    stories
      .flatTraverse(story =>
        evidenceNames.traverse { name =>
          val relative = s"evidence/${story.id}/$name"
          ArtifactFiles.read(output.resolve(relative)).map(relative -> _)
        }
      )
      .map(_.toMap)

  def write(root: Path, output: Path, stories: Vector[RecordedStory] = Vector.empty): IO[Unit] = for
    _ <- Vector(
      "book",
      "docs",
      "examples",
      "product",
      "harness",
      "scripts",
      "project",
      "design/0.2"
    )
      .traverse_(directory => copyTree(root, root.resolve(directory), output.resolve("source")))
    _ <- Vector("README.md", "FOURTH-DRAFT.md", "build.sbt").traverse_(name =>
      copy(root.resolve(name), output.resolve("source").resolve(name))
    )
    _ <- copy(
      root.resolve("product/scene/target/scala-3.3.6/harmonia-scene-fastopt/main.js"),
      output.resolve("source/product/scene/target/scala-3.3.6/harmonia-scene-fastopt/main.js")
    )
    _ <- copy(
      root.resolve("book/browser/target/scala-3.3.6/harmonia-reader-fastopt/main.js"),
      output.resolve("source/book/browser/target/scala-3.3.6/harmonia-reader-fastopt/main.js")
    )
    _ <- ArtifactFiles.write(
      output.resolve("index.html"),
      """<!doctype html><html lang="en"><head><meta charset="utf-8"><title>Harmonia field guide</title><script src="guide-entry.js" defer></script></head><body><a href="source/design/0.2/book-overview.html">Open the field guide →</a></body></html>"""
    )
    _ <- ArtifactFiles.write(
      output.resolve("guide-entry.js"),
      "location.replace('source/design/0.2/book-overview.html' + location.search + location.hash);"
    )
    raw <- evidenceFiles(output, stories)
    _ <- raw.toVector.traverse_((name, text) =>
      ArtifactFiles.write(output.resolve("source/design/0.2").resolve(name), text)
    )
    _ <- ArtifactFiles.write(
      output.resolve("source/design/0.2/run-recordings.js"),
      "window.HarmoniaRunRecordings = " + stories
        .map(s => s.id -> s)
        .toMap
        .asJson
        .noSpaces
        .replace("<", "\\u003c") + ";\nwindow.HarmoniaRunEvidenceFiles = " + raw.asJson.noSpaces
        .replace("<", "\\u003c") + ";"
    )
    _ <- IO.whenA(stories.nonEmpty)(
      ArtifactFiles.write(
        output.resolve("source/design/0.2/context.js"),
        "window.HarmoniaLiveRoot = null; window.HarmoniaLaboratory = 'laboratory.html?story=financing-approved';"
      )
    )
  yield ()

  private def copyTree(root: Path, directory: Path, output: Path): IO[Unit] = IO
    .blocking {
      val stream = Files.walk(directory)
      try
        stream
          .iterator()
          .asScala
          .filter { path =>
            Files.isRegularFile(path) && !Files.isSymbolicLink(path) && (Set(
              "md",
              "scala",
              "daml",
              "yaml",
              "json",
              "sbt",
              "html",
              "css",
              "js",
              "mjs",
              "svg",
              "png",
              "pdf",
              "py",
              "yml",
              "sh",
              "properties"
            )(path.getFileName.toString.split('.').last) ||
              (root.relativize(path).startsWith("scripts") && !path.getFileName.toString.contains(
                "."
              ))) &&
            !path
              .iterator()
              .asScala
              .exists(part =>
                Set(".daml", "target", ".bsp", "__pycache__", ".git", ".artifacts")(part.toString)
              )
          }
          .toVector
      finally stream.close()
    }
    .flatMap(_.traverse_(path => copy(path, output.resolve(root.relativize(path)))))

  private def copy(source: Path, target: Path): IO[Unit] = IO.blocking {
    Files.createDirectories(target.getParent)
    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
    ()
  }

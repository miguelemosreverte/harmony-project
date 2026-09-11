package harmonia.book

import cats.effect.IO
import cats.syntax.all.*
import harmonia.files.ArtifactFiles
import harmonia.stories.read.{MarkdownYaml, StoryFormat}
import io.circe.Json
import java.nio.file.{Files, Path, StandardCopyOption}
import java.security.MessageDigest
import org.commonmark.node.{AbstractVisitor, Link}
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import scala.jdk.CollectionConverters.*

object ExportBook:
  def write(root: Path, run: Path, output: Path): IO[Unit] = for
    directories <- IO.blocking {
      val stream = Files.list(run)
      try
        stream
          .iterator()
          .asScala
          .filter(p => Files.isRegularFile(p.resolve("actual.md")))
          .toVector
          .sortBy(_.getFileName.toString)
      finally stream.close()
    }
    _ <- IO.raiseWhen(directories.isEmpty)(RuntimeException(s"No recorded stories in $run"))
    stories <- directories.traverse(directory => recorded(directory, output))
    chapters <- Vector(
      "01-first-story.md",
      "02-two-integration-paths.md",
      "03-participant-views.md",
      "04-progression.md",
      "05-financing-and-offer.md",
      "06-atomic-transfer.md",
      "07-generated-bindings.md"
    ).traverse { name =>
      ArtifactFiles.read(root.resolve("book").resolve(name)).flatMap { markdown =>
        CodeIncludes.expand(root.resolve("book"), markdown).map { expanded =>
          val document = Parser.builder().build().parse(expanded)
          document.accept(new AbstractVisitor {
            override def visit(link: Link): Unit =
              if !link.getDestination.contains(":") && !link.getDestination.startsWith("#") then
                link.setDestination("source/book/" + link.getDestination)
              visitChildren(link)
          })
          Json.obj(
            "id" -> Json.fromString(name),
            "title" -> Json.fromString(markdown.linesIterator.next().stripPrefix("# ")),
            "html" -> Json.fromString(
              HtmlRenderer.builder().escapeHtml(true).sanitizeUrls(true).build().render(document)
            )
          )
        }
      }
    }
    _ <- ArtifactFiles.write(
      output.resolve("evidence.json"),
      Json
        .obj("stories" -> Json.fromValues(stories), "chapters" -> Json.fromValues(chapters))
        .spaces2
    )
    _ <- copy(root.resolve("book/site/index.html"), output.resolve("index.html"))
    _ <- copy(root.resolve("book/site/book.css"), output.resolve("book.css"))
    _ <- copy(
      root.resolve("off-ledger/browser/target/scala-3.3.6/harmonia-book-fastopt/main.js"),
      output.resolve("main.js")
    )
    _ <- Vector("book", "docs", "stories", "on-ledger", "off-ledger", "packages", "evaluations")
      .traverse_ { directory =>
        IO.blocking {
          val stream = Files.walk(root.resolve(directory))
          try
            stream
              .iterator()
              .asScala
              .filter { path =>
                Files.isRegularFile(path) && Set("md", "scala", "daml", "yaml", "json", "sbt")(
                  path.getFileName.toString.split('.').last
                ) &&
                !path
                  .iterator()
                  .asScala
                  .exists(part => Set(".daml", "target", ".bsp")(part.toString))
              }
              .toVector
          finally stream.close()
        }.flatMap(
          _.traverse_(path => copy(path, output.resolve("source").resolve(root.relativize(path))))
        )
      }
    _ <- Vector("README.md", "PRD.md", "harmonia.md", "harmonia-architecture.html").traverse_(
      name => copy(root.resolve(name), output.resolve("source").resolve(name))
    )
    _ <- IO.println(s"Book exported: $output")
  yield ()

  private def recorded(directory: Path, output: Path): IO[Json] = for
    input <- ArtifactFiles.read(directory.resolve("input.md"))
    expected <- ArtifactFiles.read(directory.resolve("expected.md"))
    actual <- ArtifactFiles.read(directory.resolve("actual.md"))
    provenanceText <- ArtifactFiles.read(directory.resolve("run.json"))
    provenance <- IO.fromEither(io.circe.parser.parse(provenanceText))
    _ <- Vector(
      "input.md" -> "input_sha256",
      "expected.md" -> "expected_sha256",
      "actual.md" -> "actual_sha256",
      "observation.json" -> "observation_sha256"
    ).traverse_ { (name, field) =>
      for
        expectedDigest <- IO.fromEither(provenance.hcursor.get[String](field))
        digest <- IO.blocking(
          MessageDigest
            .getInstance("SHA-256")
            .digest(Files.readAllBytes(directory.resolve(name)))
            .map(b => f"${b & 0xff}%02x")
            .mkString
        )
        _ <- IO.raiseUnless(digest == expectedDigest)(
          RuntimeException(s"Recorded artifact changed: ${directory.resolve(name)}")
        )
      yield ()
    }
    scenario <- IO.fromEither(MarkdownYaml.read(input, "Scenario").left.map(RuntimeException(_)))
    baseline <- IO.fromEither(StoryFormat.result(expected).left.map(RuntimeException(_)))
    observed <- IO.fromEither(StoryFormat.result(actual).left.map(RuntimeException(_)))
    id = directory.getFileName.toString
    _ <- Vector("input.md", "expected.md", "actual.md", "diff.md", "run.json", "observation.json")
      .traverse_(name =>
        copy(directory.resolve(name), output.resolve("evidence").resolve(id).resolve(name))
      )
  yield Json.obj(
    "id" -> Json.fromString(id),
    "title" -> Json.fromString(input.linesIterator.next().stripPrefix("# ")),
    "description" -> Json.fromString(
      input.linesIterator.drop(1).takeWhile(_ != "## Scenario").mkString(" ").trim
    ),
    "input" -> scenario,
    "expected" -> baseline,
    "actual" -> observed,
    "provenance" -> provenance
  )

  private def copy(source: Path, target: Path): IO[Unit] = IO.blocking {
    Files.createDirectories(target.getParent)
    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
    ()
  }

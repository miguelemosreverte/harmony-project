package harmonia.stories.run

import cats.effect.{ExitCode, IO}
import cats.syntax.all.*
import harmonia.files.ArtifactFiles
import harmonia.bindings.inspect.SourceIdentity
import harmonia.ledger.CantonSandbox
import harmonia.ledger.network.CantonNetwork
import harmonia.stories.Story
import harmonia.stories.run.process.RunProcessStory
import harmonia.stories.run.purchase.RunPurchaseStory
import harmonia.stories.compare.CompareResults
import harmonia.stories.read.{MarkdownYaml, StoryFormat}
import io.circe.Json
import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters.*

object CheckStories:
  private final case class Prepared(
      directory: Path,
      story: Story,
      expected: Json,
      inputMarkdown: String,
      expectedMarkdown: String
  )

  def run(root: Path, requested: List[String]): IO[ExitCode] = for
    format <- ArtifactFiles.read(root.resolve("stories/format.yaml"))
    _ <- IO.raiseUnless(format.trim == "version: 1")(
      RuntimeException("Unsupported story format version")
    )
    directories <-
      if requested.nonEmpty then IO.pure(requested.map(root.resolve).toVector)
      else
        IO.blocking {
          val entries = Files.list(root.resolve("stories"))
          try
            entries
              .iterator()
              .asScala
              .filter(path => Files.isRegularFile(path.resolve("input.md")))
              .toVector
              .sortBy(_.getFileName.toString)
          finally entries.close()
        }
    _ <- IO.raiseWhen(directories.isEmpty)(RuntimeException("No stories found"))
    prepared <- directories.traverse(prepare)
    _ <- IO.raiseWhen(prepared.map(_.story.id).distinct.size != prepared.size)(
      RuntimeException("Story names must be unique within a run")
    )
    artifacts <- ArtifactFiles.createRun(root, "check")
    _ <- SourceIdentity.verify(root, artifacts)
    _ <- IO.println(s"Running ${prepared.size} stories against local Canton. Evidence: $artifacts")
    dar = root.resolve("on-ledger/smoke/.daml/dist/harmonia-smoke-0.1.0.dar")
    (purchases, remaining) = prepared.partition(_.story.workflow.contains("property-purchase"))
    (privateStories, ordinaryStories) = remaining.partition(
      _.story.workflow.contains("private-approval")
    )
    ordinaryChecks <-
      if ordinaryStories.isEmpty then IO.pure(Vector.empty[Boolean])
      else
        CantonSandbox.resource(root, artifacts, dar).use { ledger =>
          checkPrepared(
            root,
            ordinaryStories,
            artifacts,
            dar,
            (story, output) =>
              if story.workflow.exists(Set("sequential-approval", "branching-approval")) then
                new RunProcessStory(root, ledger, dar).run(story, output)
              else new RunStory(root, ledger, dar).run(story, output),
            "one participant, one synchronizer, separate party identities"
          )
        }
    privateChecks <-
      if privateStories.isEmpty then IO.pure(Vector.empty[Boolean])
      else
        CantonNetwork.resource(root, artifacts.resolve("network"), dar).use { network =>
          checkPrepared(
            root,
            privateStories,
            artifacts,
            dar,
            new RunPrivateStory(root, network, dar).run,
            "three independent participant nodes, one local JVM, one common synchronizer"
          )
        }
    purchaseChecks <-
      if purchases.isEmpty then IO.pure(Vector.empty[Boolean])
      else
        CantonNetwork
          .resource(
            root,
            artifacts.resolve("purchase-network"),
            dar,
            participantNames = Vector("bank", "buyer", "buyeragent", "selleragent")
          )
          .use { network =>
            checkPrepared(
              root,
              purchases,
              artifacts,
              dar,
              new RunPurchaseStory(root, network, dar).run,
              "four independent participant nodes, one local JVM, one common synchronizer"
            )
          }
    _ <- SourceIdentity.verify(root, artifacts)
  yield
    if (ordinaryChecks ++ privateChecks ++ purchaseChecks).forall(identity) then ExitCode.Success
    else ExitCode.Error

  private def checkPrepared(
      root: Path,
      prepared: Vector[Prepared],
      artifacts: Path,
      dar: Path,
      execute: (Story, Path) => IO[Json],
      topology: String
  ): IO[Vector[Boolean]] =
    prepared.traverse { item =>
      val output = artifacts.resolve(item.story.id)
      for
        _ <- ArtifactFiles.write(output.resolve("input.md"), item.inputMarkdown)
        _ <- ArtifactFiles.write(output.resolve("expected.md"), item.expectedMarkdown)
        actual <- execute(item.story, output)
        _ <- IO.fromEither(
          StoryFormat
            .result(MarkdownYaml.render("Observed", "Result", actual))
            .left
            .map(RuntimeException(_))
        )
        differences = CompareResults.compare(item.expected, actual)
        _ <- ArtifactFiles.write(
          output.resolve("actual.md"),
          MarkdownYaml.render(s"Observed ${item.story.id}", "Result", actual)
        )
        _ <- ArtifactFiles.write(output.resolve("diff.md"), CompareResults.markdown(differences))
        _ <- RunProvenance.write(
          root,
          output.resolve("input.md"),
          output.resolve("expected.md"),
          dar,
          output,
          differences.isEmpty,
          topology
        )
        _ <- IO.println(
          s"${if differences.isEmpty then "PASS" else "FAIL"} ${item.story.id}: ${differences.size} differences"
        )
        _ <-
          if differences.nonEmpty then IO.println(CompareResults.markdown(differences))
          else IO.unit
      yield differences.isEmpty
    }

  private def prepare(directory: Path): IO[Prepared] = for
    input <- ArtifactFiles.read(directory.resolve("input.md"))
    expected <- ArtifactFiles.read(directory.resolve("expected.md"))
    story <- IO.fromEither(
      StoryFormat
        .input(directory.getFileName.toString, input)
        .left
        .map(message => RuntimeException(s"$directory/input.md: $message"))
    )
    result <- IO.fromEither(
      StoryFormat
        .result(expected)
        .left
        .map(message => RuntimeException(s"$directory/expected.md: $message"))
    )
  yield Prepared(directory, story, result, input, expected)

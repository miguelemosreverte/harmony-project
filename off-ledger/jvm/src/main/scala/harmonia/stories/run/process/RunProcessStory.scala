package harmonia.stories.run.process

import cats.effect.IO
import cats.syntax.all.*
import harmonia.files.ArtifactFiles
import harmonia.ledger.{CantonSandbox, DamlScript}
import harmonia.stories.Story
import harmonia.stories.run.NormalizeAction
import io.circe.Json
import java.nio.file.Path

final class RunProcessStory(root: Path, ledger: CantonSandbox, dar: Path):
  def run(story: Story, artifacts: Path): IO[Json] =
    val module = if story.workflow.contains("branching-approval") then "Branching" else "Sequence"
    execute(story, artifacts, module)

  private def execute(story: Story, artifacts: Path, module: String): IO[Json] = for
    _ <- ArtifactFiles.write(artifacts.resolve("input.json"), story.scriptInput.spaces2)
    contextFile <- DamlScript.run(
      root,
      ledger,
      dar,
      s"$module:setup",
      artifacts.resolve("setup"),
      Some(artifacts.resolve("input.json"))
    )
    contextText <- ArtifactFiles.read(contextFile)
    context <- IO.fromEither(io.circe.parser.parse(contextText))
    observations <- story.actions.zipWithIndex.traverse { (action, index) =>
      val directory = artifacts.resolve(f"attempt-${index + 1}%02d")
      for
        _ <- ArtifactFiles.write(
          directory.resolve("input.json"),
          Json.obj("context" -> context, "attempt" -> action.json).spaces2
        )
        output <- DamlScript.run(
          root,
          ledger,
          dar,
          s"$module:act",
          directory,
          Some(directory.resolve("input.json"))
        )
        text <- ArtifactFiles.read(output)
        observed <- IO.fromEither(io.circe.parser.parse(text))
      yield observed
    }
    normalized <- observations.traverse { observation =>
      for
        step <- IO.fromEither(observation.hcursor.get[Json]("step"))
        base <- IO.fromEither(NormalizeAction(step).left.map(RuntimeException(_)))
        names = Vector(
          "review",
          "completed",
          "enabled"
        ) ++ (if module == "Branching" then Vector("branch", "closure", "skipped")
              else Vector.empty)
        fields <- names.traverse(name =>
          IO.fromEither(observation.hcursor.get[Json](name)).map(name -> _)
        )
      yield base.mapObject(obj =>
        fields.foldLeft(obj) { case (current, (name, value)) => current.add(name, value) }
      )
    }
    definition <- IO.fromEither(observations.last.hcursor.get[Json]("definition"))
    version <- IO.fromOption(
      definition.hcursor
        .downField("version")
        .focus
        .flatMap(value =>
          value.asNumber.flatMap(_.toInt).orElse(value.asString.flatMap(_.toIntOption))
        )
    )(RuntimeException("Invalid definition version"))
    _ <- ArtifactFiles.write(
      artifacts.resolve("observation.json"),
      Json.obj("setup" -> context, "attempts" -> Json.fromValues(observations)).spaces2
    )
  yield Json.obj(
    "definition" -> definition.mapObject(_.add("version", Json.fromInt(version))),
    "actions" -> Json.fromValues(normalized)
  )

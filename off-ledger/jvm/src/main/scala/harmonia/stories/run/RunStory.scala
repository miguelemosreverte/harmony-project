package harmonia.stories.run

import cats.effect.IO
import harmonia.files.ArtifactFiles
import harmonia.ledger.{CantonSandbox, DamlScript}
import harmonia.stories.Story
import io.circe.Json
import java.nio.file.Path

final class RunStory(root: Path, ledger: CantonSandbox, dar: Path):
  def run(story: Story, artifacts: Path): IO[Json] = for
    _ <- ArtifactFiles.write(artifacts.resolve("input.json"), story.scriptInput.spaces2)
    path <- DamlScript.run(
      root,
      ledger,
      dar,
      "Story:run",
      artifacts,
      Some(artifacts.resolve("input.json"))
    )
    raw <- ArtifactFiles.read(path)
    json <- IO.fromEither(io.circe.parser.parse(raw))
    actions <- IO.fromEither(
      json.asArray.toRight(RuntimeException("Ledger observations must be a list"))
    )
    normalized <- actions.foldLeft(IO.pure(Vector.empty[Json])) { (acc, action) =>
      for
        preceding <- acc
        fields <- IO.fromEither(normalize(action).left.map(RuntimeException(_)))
      yield preceding :+ fields
    }
  yield Json.obj("actions" -> Json.fromValues(normalized))

  private def normalize(action: Json): Either[String, Json] =
    val cursor = action.hcursor
    for
      id <- cursor.get[String]("id").left.map(_.message)
      outcome <- cursor.get[String]("outcome").left.map(_.message)
      reason <- cursor.get[Option[String]]("reason").left.map(_.message)
      application <- cursor.get[String]("application").left.map(_.message)
      consumed <- cursor.get[Boolean]("consumed").left.map(_.message)
      countJson <- cursor.downField("activeContracts").focus.toRight("Missing activeContracts")
      count <- countJson.asNumber
        .flatMap(_.toInt)
        .orElse(countJson.asString.flatMap(_.toIntOption))
        .toRight("Invalid activeContracts")
      visible <- cursor.get[Vector[String]]("visibleTo").left.map(_.message)
    yield Json.fromFields(
      Vector(
        "id" -> Json.fromString(id),
        "outcome" -> Json.fromString(outcome),
        "application" -> Json.fromString(application),
        "consumed" -> Json.fromBoolean(consumed),
        "active_contracts" -> Json.fromInt(count),
        "visible_to" -> Json.fromValues(visible.sorted.map(Json.fromString))
      ) ++ reason.toVector.map(value => "reason" -> Json.fromString(value))
    )

package harmonia.stories.run

import cats.effect.IO
import harmonia.files.ArtifactFiles
import harmonia.ledger.{CantonSandbox, DamlScript}
import harmonia.stories.financing.model.FinancingStory
import io.circe.Json
import java.nio.file.Path

final class RunStory(root: Path, ledger: CantonSandbox, dar: Path):
  def run(story: FinancingStory, artifacts: Path): IO[Json] = for
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
        fields <- IO.fromEither(NormalizeAction(action).left.map(RuntimeException(_)))
      yield preceding :+ fields
    }
  yield Json.obj("actions" -> Json.fromValues(normalized))

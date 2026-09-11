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
      if story.integration.contains("generated") then "Story:runGenerated" else "Story:run",
      artifacts,
      Some(artifacts.resolve("input.json"))
    )
    raw <- ArtifactFiles.read(path)
    json <- IO.fromEither(io.circe.parser.parse(raw))
    actions <- IO.fromEither(
      (if story.integration.contains("generated") then
         json.hcursor.get[Vector[Json]]("steps").left.map(error => RuntimeException(error.message))
       else json.asArray.toRight(RuntimeException("Ledger observations must be a list")))
    )
    normalized <- actions.foldLeft(IO.pure(Vector.empty[Json])) { (acc, action) =>
      for
        preceding <- acc
        fields <- IO.fromEither(NormalizeAction(action).left.map(RuntimeException(_)))
      yield preceding :+ fields
    }
    result = Json.obj("actions" -> Json.fromValues(normalized))
    observed <-
      if !story.integration.contains("generated") then IO.pure(result)
      else
        for
          evidence <- harmonia.bindings.observe.GeneratedEvidence.collect(ledger.port, json)
          _ <- ArtifactFiles.write(path, json.mapObject(_.add("bank_events", evidence._2)).spaces2)
        yield result.mapObject(_.add("integration", evidence._1))
  yield observed

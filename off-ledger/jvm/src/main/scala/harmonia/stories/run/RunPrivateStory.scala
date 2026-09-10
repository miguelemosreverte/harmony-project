package harmonia.stories.run

import cats.effect.IO
import cats.syntax.all.*
import harmonia.files.ArtifactFiles
import harmonia.ledger.DamlScript
import harmonia.ledger.network.CantonNetwork
import harmonia.stories.run.evidence.{ObserveParties, ObservingParty, ObservationScope}
import harmonia.stories.financing.model.FinancingStory
import io.circe.Json
import java.nio.file.Path

final class RunPrivateStory(root: Path, network: CantonNetwork, dar: Path):
  def run(story: FinancingStory, artifacts: Path): IO[Json] = for
    _ <- ArtifactFiles.write(artifacts.resolve("input.json"), story.scriptInput.spaces2)
    output <- DamlScript.runNetwork(
      root,
      network.configuration,
      dar,
      "Privacy:run",
      artifacts,
      artifacts.resolve("input.json")
    )
    raw <- ArtifactFiles.read(output)
    observed <- IO.fromEither(io.circe.parser.parse(raw))
    steps <- IO.fromEither(observed.hcursor.get[Vector[Json]]("steps"))
    actions <- steps.traverse(action =>
      IO.fromEither(NormalizeAction(action).left.map(RuntimeException(_)))
    )
    evidence <- ObserveParties.collect(
      network,
      observed,
      Vector(
        ObservingParty("bank", "bank", story.bank),
        ObservingParty("buyer", "buyer", story.buyer),
        ObservingParty("reviewer", "reviewer", story.reviewer.get)
      ),
      ObservationScope(
        "PrivateFinancing",
        "Harmonia.SharedProgress",
        "SharedProgress",
        story.privateDetails.get,
        true
      )
    )
    _ <- ArtifactFiles.write(
      output,
      observed.mapObject(_.add("participant_events", evidence.events)).spaces2
    )
  yield Json.obj("actions" -> Json.fromValues(actions), "visibility" -> evidence.visibility)

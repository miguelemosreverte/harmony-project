package harmonia.stories.run.purchase

import cats.effect.IO
import cats.syntax.all.*
import harmonia.files.ArtifactFiles
import harmonia.ledger.DamlScript
import harmonia.ledger.network.CantonNetwork
import harmonia.stories.Story
import harmonia.stories.run.NormalizeAction
import harmonia.stories.run.evidence.{ObserveParties, ObservingParty, ObservationScope}
import io.circe.Json
import java.nio.file.Path

final class RunPurchaseStory(root: Path, network: CantonNetwork, dar: Path):
  def run(story: Story, artifacts: Path): IO[Json] = for
    setup <- IO.fromOption(story.purchase)(RuntimeException("Missing purchase setup"))
    _ <- ArtifactFiles.write(artifacts.resolve("input.json"), story.scriptInput.spaces2)
    output <- DamlScript.runNetwork(
      root,
      network.configuration,
      dar,
      "Purchase:run",
      artifacts,
      artifacts.resolve("input.json")
    )
    text <- ArtifactFiles.read(output)
    observed <- IO.fromEither(io.circe.parser.parse(text))
    steps <- IO.fromEither(observed.hcursor.get[Vector[Json]]("steps"))
    actions <- steps.traverse { step =>
      for
        action <- IO.fromEither(step.hcursor.get[Json]("action"))
        base <- IO.fromEither(NormalizeAction(action).left.map(RuntimeException(_)))
        fields <- Vector("offer", "proposal", "completed").traverse(name =>
          IO.fromEither(step.hcursor.get[Json](name)).map(name -> _)
        )
        available <- IO.fromEither(step.hcursor.get[Boolean]("evidenceAvailable"))
        countJson <- IO.fromEither(step.hcursor.get[Json]("proposals"))
        count <- IO.fromOption(
          countJson.asNumber.flatMap(_.toInt).orElse(countJson.asString.flatMap(_.toIntOption))
        )(RuntimeException("Invalid proposal count"))
      yield base.mapObject(obj =>
        fields
          .foldLeft(obj) { case (current, (name, value)) => current.add(name, value) }
          .add("evidence_available", Json.fromBoolean(available))
          .add("proposals", Json.fromInt(count))
      )
    }
    evidence <- ObserveParties.collect(
      network,
      observed,
      Vector(
        ObservingParty("bank", "bank", story.bank),
        ObservingParty("buyer", "buyer", story.buyer),
        ObservingParty("buyeragent", "buyerAgent", setup.buyerAgent),
        ObservingParty("selleragent", "sellerAgent", setup.sellerAgent)
      ),
      ObservationScope("PropertyFinancing", "PropertyOffer", "Offer", setup.documents, false)
    )
    _ <- ArtifactFiles.write(
      output,
      observed.mapObject(_.add("participant_events", evidence.events)).spaces2
    )
  yield Json.obj("actions" -> Json.fromValues(actions), "visibility" -> evidence.visibility)

package harmonia.stories.run

import cats.effect.IO
import cats.syntax.all.*
import harmonia.files.ArtifactFiles
import harmonia.ledger.DamlScript
import harmonia.ledger.network.CantonNetwork
import harmonia.ledger.events.LedgerEvents
import harmonia.stories.Story
import io.circe.Json
import java.nio.file.Path

final class RunPrivateStory(root: Path, network: CantonNetwork, dar: Path):
  def run(story: Story, artifacts: Path): IO[Json] = for
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
    views <- IO.fromEither(observed.hcursor.get[Vector[Json]]("views"))
    participants = Vector(
      "bank" -> story.bank,
      "buyer" -> story.buyer,
      "reviewer" -> story.reviewer.get
    )
    events <- participants.traverse { (role, name) =>
      for
        party <- IO.fromEither(observed.hcursor.downField("actors").get[String](role))
        updates <- LedgerEvents.read(network.participants(role), party)
      yield name -> Json.fromValues(updates)
    }
    visibility <- participants.traverse { (_, name) =>
      for
        view <- IO.fromOption(views.find(_.hcursor.get[String]("name").contains(name)))(
          RuntimeException(s"Missing query observation for $name")
        )
        application <- IO.fromEither(view.hcursor.get[Boolean]("application"))
        progress <- IO.fromEither(view.hcursor.get[Boolean]("progress"))
        updates = events.toMap.apply(name)
        created = updates.asArray.get.toVector
          .flatMap(
            _.hcursor.downField("transaction").get[Vector[Json]]("events").getOrElse(Vector.empty)
          )
          .flatMap(_.hcursor.downField("created").focus)
        privateEvents = created.count(
          _.hcursor.downField("templateId").get[String]("moduleName").contains("PrivateFinancing")
        )
        sharedEvents = created.count(
          _.hcursor
            .downField("templateId")
            .get[String]("moduleName")
            .contains("Harmonia.SharedProgress")
        )
        _ <- IO.raiseUnless(sharedEvents > 0)(
          RuntimeException(
            s"No positive control in $name's event stream; privacy evidence is incomplete"
          )
        )
      yield name -> Json.obj(
        "application" -> Json.fromBoolean(application),
        "progress" -> Json.fromBoolean(progress),
        "private_events" -> Json.fromInt(privateEvents),
        "progress_events" -> Json.fromInt(sharedEvents),
        "private_payload_observed" -> Json.fromBoolean(
          containsText(updates, story.privateDetails.get)
        )
      )
    }
    _ <- ArtifactFiles.write(
      output,
      observed.mapObject(_.add("participant_events", Json.fromFields(events))).spaces2
    )
  yield Json.obj("actions" -> Json.fromValues(actions), "visibility" -> Json.fromFields(visibility))

  private def containsText(json: Json, value: String): Boolean =
    json.asString.contains(value) || json.asArray.exists(_.exists(containsText(_, value))) ||
      json.asObject.exists(_.values.exists(containsText(_, value)))

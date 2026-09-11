package harmonia.bindings.observe

import cats.effect.IO
import harmonia.ledger.events.LedgerEvents
import io.circe.Json

object GeneratedEvidence:
  def collect(port: Int, observation: Json): IO[(Json, Json)] = for
    bank <- IO.fromEither(observation.hcursor.get[String]("bank"))
    count <- IO
      .fromEither(observation.hcursor.get[Json]("activeBindings"))
      .flatMap(value =>
        IO.fromOption(
          value.asNumber.flatMap(_.toInt).orElse(value.asString.flatMap(_.toIntOption))
        )(RuntimeException("Invalid active binding count"))
      )
    updates <- LedgerEvents.read(port, bank)
    created = updates
      .flatMap(
        _.hcursor.downField("transaction").get[Vector[Json]]("events").getOrElse(Vector.empty)
      )
      .flatMap(_.hcursor.downField("created").focus)
      .count { event =>
        val template = event.hcursor.downField("templateId")
        template.get[String]("moduleName").contains("GeneratedFinancing") && template
          .get[String]("entityName")
          .contains("Adapter")
      }
    _ <- IO.raiseUnless(created > 0)(
      RuntimeException("No generated binding event in the actor's stream")
    )
  yield Json.obj(
    "path" -> Json.fromString("generated"),
    "active_bindings" -> Json.fromInt(count),
    "binding_creations" -> Json.fromInt(created)
  ) -> Json.fromValues(updates)

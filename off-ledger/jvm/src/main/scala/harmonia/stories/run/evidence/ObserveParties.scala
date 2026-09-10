package harmonia.stories.run.evidence

import cats.effect.IO
import cats.syntax.all.*
import harmonia.ledger.network.CantonNetwork
import harmonia.ledger.events.LedgerEvents
import io.circe.Json

final case class ObservingParty(node: String, actorField: String, name: String)
final case class ObservationScope(
    privateModule: String,
    progressModule: String,
    progressTemplate: String,
    privateText: String,
    requireSharedControl: Boolean
)
final case class PartyEvidence(visibility: Json, events: Json)

object ObserveParties:
  def collect(
      network: CantonNetwork,
      observation: Json,
      parties: Vector[ObservingParty],
      scope: ObservationScope
  ): IO[PartyEvidence] = for
    views <- IO.fromEither(observation.hcursor.get[Vector[Json]]("views"))
    events <- parties.traverse { party =>
      for
        identity <- IO.fromEither(
          observation.hcursor.downField("actors").get[String](party.actorField)
        )
        updates <- LedgerEvents.read(network.participants(party.node), identity)
      yield party.name -> Json.fromValues(updates)
    }
    visibility <- parties.traverse { party =>
      for
        view <- IO.fromOption(views.find(_.hcursor.get[String]("name").contains(party.name)))(
          RuntimeException(s"Missing query observation for ${party.name}")
        )
        application <- IO.fromEither(view.hcursor.get[Boolean]("application"))
        progress <- IO.fromEither(view.hcursor.get[Boolean]("progress"))
        updates = events.toMap.apply(party.name)
        created = updates.asArray.get.toVector
          .flatMap(
            _.hcursor.downField("transaction").get[Vector[Json]]("events").getOrElse(Vector.empty)
          )
          .flatMap(_.hcursor.downField("created").focus)
        privateEvents = created.count(
          _.hcursor.downField("templateId").get[String]("moduleName").contains(scope.privateModule)
        )
        sharedEvents = created.count { event =>
          val template = event.hcursor.downField("templateId")
          template.get[String]("moduleName").contains(scope.progressModule) && template
            .get[String]("entityName")
            .contains(scope.progressTemplate)
        }
        _ <- IO.raiseUnless(
          if scope.requireSharedControl then sharedEvents > 0 else created.nonEmpty
        )(
          RuntimeException(
            s"No positive control in ${party.name}'s event stream; evidence is incomplete"
          )
        )
      yield party.name -> Json.obj(
        "application" -> Json.fromBoolean(application),
        "progress" -> Json.fromBoolean(progress),
        "private_events" -> Json.fromInt(privateEvents),
        "progress_events" -> Json.fromInt(sharedEvents),
        "private_payload_observed" -> Json.fromBoolean(containsText(updates, scope.privateText))
      )
    }
  yield PartyEvidence(Json.fromFields(visibility), Json.fromFields(events))

  private def containsText(json: Json, value: String): Boolean =
    json.asString.contains(value) || json.asArray.exists(_.exists(containsText(_, value))) ||
      json.asObject.exists(_.values.exists(containsText(_, value)))

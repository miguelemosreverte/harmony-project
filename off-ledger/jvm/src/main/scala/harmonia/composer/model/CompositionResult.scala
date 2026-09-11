package harmonia.composer.model

import io.circe.Json

object CompositionResult:
  def read(json: Json): Either[String, Json] = for
    _ <- Composition.fields(json, Set("composition", "actions"))
    composition <- json.hcursor.get[Json]("composition").left.map(_.message)
    _ <- Composition.fields(composition, Set("name", "reference"))
    _ <- Composition.string(composition, "name", 80)
    _ <- Composition.string(composition, "reference", 80)
    actions <- json.hcursor.get[Vector[Json]]("actions").left.map(_.message)
    _ <- Either.cond(
      actions.nonEmpty && actions.size <= 100,
      (),
      "Composition results require 1–100 actions"
    )
    _ <- actions.foldLeft[Either[String, Unit]](Right(())) { (previous, action) =>
      for
        _ <- previous
        outcome <- Composition.string(action, "outcome", 20)
        _ <- Either.cond(
          Set("committed", "rejected").contains(outcome),
          (),
          "Expected a definite observed outcome"
        )
        _ <- Composition.fields(
          action,
          Set(
            "id",
            "outcome",
            "drafts",
            "instances",
            "workflow",
            "completed",
            "enabled",
            "sources"
          ) ++ Option.when(outcome == "rejected")("reason")
        )
        _ <- Composition.string(action, "id", 64)
        _ <-
          if outcome == "rejected" then Composition.string(action, "reason", 200).map(_ => ())
          else Right(())
        workflow <- Composition.string(action, "workflow", 20)
        _ <- Either.cond(
          Set("not-started", "waiting", "complete").contains(workflow),
          (),
          "Unknown composition state"
        )
        _ <- Either.cond(
          Vector("drafts", "instances").forall(field =>
            action.hcursor.get[Int](field).exists(n => n >= 0 && n <= 1)
          ),
          (),
          "Expected zero or one draft/instance per story"
        )
        _ <- Either.cond(
          Vector("completed", "enabled").forall(field =>
            action.hcursor
              .get[Vector[String]](field)
              .exists(values => values.size <= 4 && values.distinct.size == values.size)
          ),
          (),
          "Step lists must be unique and bounded"
        )
        _ <- Either.cond(
          action.hcursor
            .downField("sources")
            .focus
            .flatMap(_.asObject)
            .exists(fields =>
              fields.size <= 4 && fields.values
                .forall(_.asString.exists(Set("pending", "approved", "confirmed")))
            ),
          (),
          "Source states must be observed text values"
        )
      yield ()
    }
  yield json

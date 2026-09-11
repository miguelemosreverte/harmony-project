package harmonia.stories.run

import io.circe.Json

object NormalizeAction:
  def apply(action: Json): Either[String, Json] =
    val cursor = action.hcursor
    for
      id <- cursor.get[String]("id").left.map(_.message)
      outcome <- cursor.get[String]("outcome").left.map(_.message)
      reason <- cursor.get[Option[String]]("reason").left.map(_.message)
      application <- cursor.get[String]("application").left.map(_.message)
      workflow <- cursor.get[Option[String]]("workflow").left.map(_.message)
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
      ) ++ reason.toVector.map(value => "reason" -> Json.fromString(value)) ++
        workflow.toVector.map(value => "workflow" -> Json.fromString(value))
    )

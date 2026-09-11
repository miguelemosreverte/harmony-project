package harmonia.workspace

import io.circe.{Encoder, Json}

/** The same request is submitted, remembered by the browser, and decoded by HTTP. */
final case class ActionRequest(id: String, command: WorkspaceCommand, version: String)
object ActionRequest:
  def read(json: Json): Either[String, ActionRequest] = for
    _ <- Either.cond(
      json.asObject.exists(obj =>
        Set("id", "action", "version").subsetOf(obj.keys.toSet) &&
          (obj.keys.toSet -- Set("id", "action", "version", "input")).isEmpty
      ),
      (),
      "Expected id, action, version, and optional composition input"
    )
    id <- json.hcursor.get[String]("id").left.map(_.getMessage)
    action <- json.hcursor.get[String]("action").left.map(_.getMessage)
    command <- WorkspaceCommand.read(action, json.hcursor.downField("input").focus)
    version <- json.hcursor.get[String]("version").left.map(_.getMessage)
  yield ActionRequest(id, command, version)

  given Encoder.AsObject[ActionRequest] = Encoder.AsObject.instance { request =>
    val fields = io.circe.JsonObject(
      "id" -> Json.fromString(request.id),
      "action" -> Json.fromString(request.command.wire),
      "version" -> Json.fromString(request.version)
    )
    request.command.parameters.fold(fields)(value => fields.add("input", value))
  }

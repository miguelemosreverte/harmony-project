package harmonia.stories.read

import harmonia.stories.{Story, StoryAction}
import io.circe.{Json, JsonObject}

object StoryFormat:
  def input(id: String, markdown: String): Either[String, Story] = for
    json <- MarkdownYaml.read(markdown, "Scenario")
    root <- fields(json, "scenario", Set("setup", "actions"), Set("workflow", "integration"))
    workflow <- root("workflow").fold[Either[String, Option[String]]](Right(None)) { value =>
      text(value, "workflow").flatMap { name =>
        Either.cond(name == "approval", Some(name), "Unsupported workflow: " + name)
      }
    }
    integration <- root("integration").fold[Either[String, Option[String]]](Right(None)) { value =>
      text(value, "integration").flatMap { name =>
        Either.cond(
          name == "adapter" && workflow.nonEmpty,
          Some(name),
          "Adapter requires an approval workflow"
        )
      }
    }
    setup <- fields(root("setup").get, "setup", Set("application"))
    application <- fields(
      setup("application").get,
      "setup.application",
      Set("bank", "buyer", "status")
    )
    bank <- text(application("bank").get, "bank")
    buyer <- text(application("buyer").get, "buyer")
    status <- text(application("status").get, "status")
    _ <- Either.cond(bank != buyer, (), "Bank and buyer must be distinct parties")
    _ <- Either.cond(
      Set("pending", "approved")(status),
      (),
      "Application status must be pending or approved"
    )
    values <- root("actions").get.asArray.toRight("actions must be a list")
    _ <- Either.cond(
      values.nonEmpty && values.size <= 32,
      (),
      "A story must contain 1 to 32 actions"
    )
    actions <- values.foldLeft[Either[String, Vector[StoryAction]]](Right(Vector.empty)) {
      (acc, value) =>
        for
          preceding <- acc
          action <- fields(value, "action", Set("id", "actor", "action"))
          name <- text(action("id").get, "action.id")
          actor <- text(action("actor").get, s"$name.actor")
          choice <- text(action("action").get, s"$name.action")
          _ <- Either.cond(Set(bank, buyer)(actor), (), s"$name: unknown actor '$actor'")
          _ <- Either.cond(
            choice == "approve-financing",
            (),
            s"$name: unsupported action '$choice'"
          )
          _ <- Either.cond(!preceding.exists(_.id == name), (), s"Duplicate action id '$name'")
        yield preceding :+ StoryAction(name, actor, choice)
    }
  yield Story(id, bank, buyer, status, actions, workflow, integration)

  def result(markdown: String): Either[String, Json] = for
    json <- MarkdownYaml.read(markdown, "Result")
    root <- fields(json, "result", Set("actions"))
    values <- root("actions").get.asArray.toRight("result.actions must be a list")
    _ <- values.foldLeft[Either[String, Unit]](Right(())) { (acc, value) =>
      for
        _ <- acc
        action <- fields(
          value,
          "result action",
          Set("id", "outcome", "application", "consumed", "active_contracts", "visible_to"),
          Set("reason", "workflow")
        )
        _ <- text(action("id").get, "result action.id")
        outcome <- text(action("outcome").get, "result action.outcome")
        _ <- Either.cond(
          Set("committed", "rejected")(outcome),
          (),
          "Outcome must be committed or rejected"
        )
        _ <- text(action("application").get, "result action.application")
        _ <- action("workflow").fold[Either[String, Unit]](Right(())) { value =>
          text(value, "result action.workflow").flatMap { status =>
            Either.cond(Set("waiting", "complete")(status), (), "Unknown workflow status")
          }
        }
        _ <- action("consumed").get.asBoolean.toRight("consumed must be true or false")
        count <- action("active_contracts").get.asNumber
          .flatMap(_.toInt)
          .toRight("active_contracts must be an integer")
        _ <- Either.cond(count >= 0, (), "active_contracts must not be negative")
        visible <- action("visible_to").get.asArray.toRight("visible_to must be a list")
        _ <- Either.cond(
          visible.forall(_.asString.exists(_.nonEmpty)),
          (),
          "visible_to must contain party names"
        )
        _ <- Either.cond(
          visible.distinct.size == visible.size,
          (),
          "visible_to contains duplicate parties"
        )
        _ <- Either.cond(
          outcome != "rejected" || action("reason").exists(_.asString.exists(_.nonEmpty)),
          (),
          "A rejected result requires a reason"
        )
        _ <- Either.cond(
          outcome != "committed" || !action.contains("reason"),
          (),
          "A committed result cannot have a rejection reason"
        )
      yield ()
    }
  yield Json.obj("actions" -> Json.fromValues(values.map { value =>
    value.mapObject { obj =>
      obj.add(
        "visible_to",
        Json.fromValues(obj("visible_to").get.asArray.get.sortBy(_.asString.get))
      )
    }
  }))

  private def fields(
      value: Json,
      path: String,
      required: Set[String],
      optional: Set[String] = Set.empty
  ): Either[String, JsonObject] =
    value.asObject.toRight(s"$path must be a mapping").flatMap { obj =>
      val missing = required -- obj.keys.toSet
      val unknown = obj.keys.toSet -- required -- optional
      Either.cond(
        missing.isEmpty && unknown.isEmpty,
        obj,
        s"$path: missing fields [${missing.toList.sorted.mkString(", ")}]; unknown fields [${unknown.toList.sorted.mkString(", ")}]"
      )
    }

  private def text(value: Json, path: String): Either[String, String] =
    value.asString.filter(_.nonEmpty).toRight(s"$path must be nonempty text")

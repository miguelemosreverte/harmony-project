package harmonia.stories.read

import harmonia.stories.{Story, StoryAction}
import harmonia.stories.financing.model.FinancingStory
import io.circe.{Json, JsonObject}

object StoryFormat:
  def input(id: String, markdown: String): Either[String, Story] = for
    json <- MarkdownYaml.read(markdown, "Scenario")
    story <-
      if json.hcursor.get[String]("workflow").contains("property-purchase") then
        purchase.PurchaseFormat.input(id, json)
      else ordinary(id, json)
  yield story

  private def ordinary(id: String, json: Json): Either[String, Story] = for
    root <- fields(json, "scenario", Set("setup", "actions"), Set("workflow", "integration"))
    workflow <- root("workflow").fold[Either[String, Option[String]]](Right(None)) { value =>
      text(value, "workflow").flatMap { name =>
        Either.cond(
          Set("approval", "private-approval", "sequential-approval", "branching-approval")(name),
          Some(name),
          "Unsupported workflow: " + name
        )
      }
    }
    integration <- root("integration").fold[Either[String, Option[String]]](Right(None)) { value =>
      text(value, "integration").flatMap { name =>
        Either.cond(
          name == "adapter" && workflow.contains("approval"),
          Some(name),
          "Adapter requires an approval workflow"
        )
      }
    }
    setup <- fields(root("setup").get, "setup", Set("application"))
    application <- fields(
      setup("application").get,
      "setup.application",
      Set("bank", "buyer", "status"),
      if workflow.contains("private-approval") then Set("reviewer", "private_details")
      else Set.empty
    )
    bank <- text(application("bank").get, "bank")
    buyer <- text(application("buyer").get, "buyer")
    status <- text(application("status").get, "status")
    reviewer <- optionalText(application, "reviewer")
    privateDetails <- optionalText(application, "private_details")
    _ <- Either.cond(
      !workflow.contains("private-approval") || (reviewer.nonEmpty && privateDetails.nonEmpty),
      (),
      "Private approval requires reviewer and private_details"
    )
    _ <- Either.cond(
      reviewer.forall(name => name != bank && name != buyer),
      (),
      "Reviewer must be a distinct party"
    )
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
          action <- fields(
            value,
            "action",
            Set("id", "actor", "action"),
            if workflow.exists(Set("sequential-approval", "branching-approval")) then Set("request")
            else Set.empty
          )
          request <- optionalText(action, "request")
          name <- text(action("id").get, "action.id")
          actor <- text(action("actor").get, s"$name.actor")
          choice <- text(action("action").get, s"$name.action")
          _ <- Either.cond(
            (Set(bank, buyer) ++ reviewer)(actor),
            (),
            s"$name: unknown actor '$actor'"
          )
          _ <- Either.cond(
            choice == "approve-financing" || (workflow.contains(
              "private-approval"
            ) && Set("publish-approval", "forge-completion")(choice)) || (workflow.contains(
              "sequential-approval"
            ) && Set(
              "confirm-review",
              "wait",
              "reconnect",
              "forge-completion"
            )(choice)) || (workflow.contains("branching-approval") && Set(
              "choose-approve",
              "choose-decline",
              "confirm-review",
              "close-application",
              "complete-join",
              "wait",
              "reconnect"
            )(choice)),
            (),
            s"$name: unsupported action '$choice'"
          )
          _ <- Either.cond(!preceding.exists(_.id == name), (), s"Duplicate action id '$name'")
        yield preceding :+ StoryAction(name, actor, choice, request)
    }
  yield FinancingStory(
    id,
    bank,
    buyer,
    status,
    actions,
    workflow,
    integration,
    reviewer,
    privateDetails
  )

  def result(markdown: String): Either[String, Json] = for
    json <- MarkdownYaml.read(markdown, "Result")
    root <- fields(json, "result", Set("actions"), Set("visibility", "definition"))
    _ <- validateVisibility(root("visibility"))
    _ <- root("definition").fold[Either[String, Unit]](Right(())) { value =>
      for
        definition <- fields(value, "definition", Set("name", "version"))
        _ <- text(definition("name").get, "definition.name")
        _ <- definition("version").get.asNumber
          .flatMap(_.toInt)
          .filter(_ > 0)
          .toRight("Definition version must be positive")
      yield ()
    }
    values <- root("actions").get.asArray.toRight("result.actions must be a list")
    _ <- values.foldLeft[Either[String, Unit]](Right(())) { (acc, value) =>
      for
        _ <- acc
        action <- fields(
          value,
          "result action",
          Set("id", "outcome", "application", "consumed", "active_contracts", "visible_to"),
          Set(
            "reason",
            "workflow",
            "review",
            "completed",
            "enabled",
            "branch",
            "closure",
            "skipped",
            "offer",
            "proposal",
            "proposals",
            "evidence_available"
          )
        )
        _ <- text(action("id").get, "result action.id")
        outcome <- text(action("outcome").get, "result action.outcome")
        _ <- Either.cond(
          Set("committed", "rejected", "observed", "duplicate")(outcome),
          (),
          "Outcome must be committed, rejected, observed, or duplicate"
        )
        _ <- text(action("application").get, "result action.application")
        _ <- action("workflow").fold[Either[String, Unit]](Right(())) { value =>
          text(value, "result action.workflow").flatMap { status =>
            Either.cond(Set("waiting", "complete")(status), (), "Unknown workflow status")
          }
        }
        _ <- Vector("completed", "enabled", "skipped").foldLeft[Either[String, Unit]](Right(())) {
          (acc, name) =>
            acc.flatMap(_ =>
              action(name).fold[Either[String, Unit]](Right(())) { value =>
                value.asArray
                  .filter(values =>
                    values.forall(
                      _.asString.exists(_.nonEmpty)
                    ) && values.distinct.size == values.size
                  )
                  .toRight(s"$name must contain unique step names")
                  .map(_ => ())
              }
            )
        }
        _ <- Vector("review", "branch", "closure", "offer", "proposal")
          .foldLeft[Either[String, Unit]](Right(())) { (acc, name) =>
            acc.flatMap(_ =>
              action(name).fold[Either[String, Unit]](Right(()))(value =>
                text(value, name).map(_ => ())
              )
            )
          }
        _ <- action("proposals").fold[Either[String, Unit]](Right(()))(value =>
          value.asNumber
            .flatMap(_.toInt)
            .filter(_ >= 0)
            .toRight("proposals must be a nonnegative integer")
            .map(_ => ())
        )
        _ <- action("evidence_available").fold[Either[String, Unit]](Right(()))(value =>
          value.asBoolean.toRight("evidence_available must be boolean").map(_ => ())
        )
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
          outcome == "rejected" || !action.contains("reason"),
          (),
          "Only a rejected result can have a rejection reason"
        )
      yield ()
    }
  yield json.mapObject(
    _.add(
      "actions",
      Json.fromValues(values.map { value =>
        value.mapObject { obj =>
          obj.add(
            "visible_to",
            Json.fromValues(obj("visible_to").get.asArray.get.sortBy(_.asString.get))
          )
        }
      })
    )
  )

  private def optionalText(obj: JsonObject, name: String): Either[String, Option[String]] =
    obj(name).fold[Either[String, Option[String]]](Right(None))(value =>
      text(value, name).map(Some(_))
    )

  private def validateVisibility(value: Option[Json]): Either[String, Unit] =
    value.fold[Either[String, Unit]](Right(())) { json =>
      json.asObject.toRight("visibility must map party names to observations").flatMap { obj =>
        obj.toVector.foldLeft[Either[String, Unit]](Right(())) { case (acc, (party, view)) =>
          for
            _ <- acc
            fields <- fields(
              view,
              s"visibility.$party",
              Set(
                "application",
                "progress",
                "private_events",
                "progress_events",
                "private_payload_observed"
              )
            )
            _ <- Vector("application", "progress", "private_payload_observed")
              .foldLeft[Either[String, Unit]](Right(()))((acc, name) =>
                acc.flatMap(_ =>
                  fields(name).get.asBoolean.toRight(s"$party.$name must be boolean").map(_ => ())
                )
              )
            _ <- Vector("private_events", "progress_events").foldLeft[Either[String, Unit]](
              Right(())
            )((acc, name) =>
              acc.flatMap(_ =>
                fields(name).get.asNumber
                  .flatMap(_.toInt)
                  .filter(_ >= 0)
                  .toRight(s"$party.$name must be a nonnegative count")
                  .map(_ => ())
              )
            )
          yield ()
        }
      }
    }

  private[read] def fields(
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

  private[read] def text(value: Json, path: String): Either[String, String] =
    value.asString.filter(_.nonEmpty).toRight(s"$path must be nonempty text")

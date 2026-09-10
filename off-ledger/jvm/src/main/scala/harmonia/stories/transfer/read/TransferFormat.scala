package harmonia.stories.transfer.read

import cats.syntax.all.*
import harmonia.stories.{Story, StoryAction}
import harmonia.stories.transfer.model.{TransferSetup, TransferStory}
import harmonia.stories.read.StoryFormat.{fields, text}
import io.circe.Json
import scala.util.Try

object TransferFormat:
  def decimal(value: Json, label: String): Either[String, BigDecimal] =
    value.asString
      .flatMap(value => Try(BigDecimal(value)).toOption)
      .filter(value => value >= 0 && value <= BigDecimal("1000000000000") && value.scale <= 10)
      .toRight(
        s"$label must be a quoted decimal from 0 to 1000000000000 with at most ten decimal places"
      )

  def input(id: String, json: Json): Either[String, Story] = for
    root <- fields(json, "scenario", Set("workflow", "setup", "actions"))
    setup <- fields(root("setup").get, "setup", Set("trade", "destination_receipts"))
    trade <- fields(
      setup("trade").get,
      "trade",
      Set("buyer", "seller", "source", "destination", "settler", "asset", "quantity")
    )
    names <- Vector("buyer", "seller", "source", "destination", "settler", "asset").traverse(name =>
      text(trade(name).get, name)
    )
    quantity <- decimal(trade("quantity").get, "quantity")
    receipts <- text(setup("destination_receipts").get, "destination_receipts")
    parties = names.take(4)
    _ <- Either.cond(
      parties.distinct.size == 4 && parties.contains(names(4)),
      (),
      "Transfer requires four distinct parties and a settler drawn from those parties"
    )
    _ <- Either.cond(
      quantity > 0 && Set("accept", "reject")(receipts),
      (),
      "Use a positive quantity and destination_receipts: accept or reject"
    )
    values <- root("actions").get.asArray.toRight("actions must be a list")
    _ <- Either.cond(
      values.nonEmpty && values.size <= 32,
      (),
      "A story must contain 1 to 32 actions"
    )
    actions <- values.traverse { value =>
      for
        action <- fields(value, "action", Set("id", "actor", "action"))
        name <- text(action("id").get, "id")
        actor <- text(action("actor").get, "actor")
        choice <- text(action("action").get, "action")
        _ <- Either.cond(parties.contains(actor), (), s"$name: unknown actor $actor")
        _ <- Either.cond(
          Set(
            "agree-trade",
            "lock-position",
            "confirm-source",
            "prepare-destination",
            "confirm-destination",
            "withdraw-directly",
            "settle"
          )(choice),
          (),
          s"$name: unsupported transfer action $choice"
        )
      yield StoryAction(name, actor, choice)
    }
    _ <- Either.cond(
      actions.map(_.id).distinct.size == actions.size,
      (),
      "Action IDs must be unique"
    )
  yield TransferStory(
    id,
    actions,
    TransferSetup(names(0), names(1), names(2), names(3), names(4), names(5), quantity, receipts)
  )

  def result(json: Json): Either[String, Json] = for
    root <- fields(json, "transfer result", Set("actions", "settlement_transactions"))
    _ <- count(root("settlement_transactions").get, "settlement_transactions")
    actions <- root("actions").get.asArray.toRight("actions must be a list")
    normalized <- actions.traverse { value =>
      for
        action <- fields(
          value,
          "transfer action",
          Set(
            "id",
            "outcome",
            "trade",
            "source",
            "destination",
            "workflow",
            "active_workflows",
            "releases",
            "visible_to"
          ),
          Set("reason")
        )
        _ <- text(action("id").get, "id")
        outcome <- text(action("outcome").get, "outcome")
        _ <- Either.cond(
          Set("committed", "rejected")(outcome),
          (),
          "Transfer outcome must be committed or rejected"
        )
        _ <- Either.cond(
          (outcome == "rejected") == action.contains("reason"),
          (),
          "Only rejected actions must carry a reason"
        )
        _ <- action("reason").traverse(value => text(value, "reason"))
        trade <- text(action("trade").get, "trade")
        _ <- Either.cond(
          Set("proposed", "agreed", "source-confirmed", "ready", "settled")(trade),
          (),
          "Unknown trade state"
        )
        source <- fields(action("source").get, "source", Set("available", "locked"))
        _ <- decimal(source("available").get, "source.available")
        _ <- decimal(source("locked").get, "source.locked")
        _ <- decimal(action("destination").get, "destination")
        workflow <- text(action("workflow").get, "workflow")
        _ <- Either.cond(Set("waiting", "complete")(workflow), (), "Unknown workflow state")
        _ <- Vector("active_workflows", "releases").traverse(name => count(action(name).get, name))
        visible <- action("visible_to").get.asArray.toRight("visible_to must be a list")
        _ <- Either.cond(
          visible.forall(_.asString.exists(_.nonEmpty)) && visible.distinct.size == visible.size,
          (),
          "visible_to must contain unique party names"
        )
      yield value.mapObject(_.add("visible_to", Json.fromValues(visible.sortBy(_.asString.get))))
    }
  yield json.mapObject(_.add("actions", Json.fromValues(normalized)))

  private def count(value: Json, name: String): Either[String, Int] =
    value.asNumber.flatMap(_.toInt).filter(_ >= 0).toRight(s"$name must be a nonnegative integer")

package harmonia.stories.transfer.run

import cats.effect.IO
import cats.syntax.all.*
import harmonia.files.ArtifactFiles
import harmonia.ledger.DamlScript
import harmonia.ledger.network.CantonNetwork
import harmonia.ledger.events.LedgerEvents
import harmonia.stories.transfer.model.TransferStory
import harmonia.stories.transfer.read.TransferFormat
import io.circe.Json
import java.nio.file.Path

final class RunTransferStory(root: Path, network: CantonNetwork, dar: Path):
  def run(story: TransferStory, artifacts: Path): IO[Json] = for
    _ <- ArtifactFiles.write(artifacts.resolve("input.json"), story.scriptInput.spaces2)
    output <- DamlScript.runNetwork(
      root,
      network.configuration,
      dar,
      "Transfer:run",
      artifacts,
      artifacts.resolve("input.json")
    )
    raw <- ArtifactFiles.read(output).flatMap(value => IO.fromEither(io.circe.parser.parse(value)))
    settler <- IO.fromEither(raw.hcursor.get[String]("settler"))
    updates <- LedgerEvents.read(
      network.participants(story.setup.nodes(story.setup.settler)),
      settler
    )
    steps <- IO.fromEither(raw.hcursor.get[Vector[Json]]("steps"))
    normalized <- steps.traverse(normalize)
    position <- IO.fromEither(raw.hcursor.get[Option[String]]("position"))
    transactions = updates.flatMap(_.hcursor.downField("transaction").focus)
    settlements = transactions.filter { transaction =>
      val events = transaction.hcursor.get[Vector[Json]]("events").getOrElse(Vector.empty)
      val retired = position.exists { cid =>
        events.exists { event =>
          val exercise = event.hcursor.downField("exercised")
          event.hcursor.downField("archived").get[String]("contractId").contains(cid) ||
          (exercise.get[String]("contractId").contains(cid) && exercise
            .get[Boolean]("consuming")
            .contains(true))
        }
      }
      val creates = events.flatMap(_.hcursor.downField("created").focus)
      def created(module: String, template: String): Boolean = creates.exists { event =>
        val id = event.hcursor.downField("templateId")
        id.get[String]("moduleName").contains(module) && id
          .get[String]("entityName")
          .contains(template)
      }
      retired && created("DestinationCustody", "Holding") && created(
        "AtomicTransfer",
        "Trade"
      ) && created("Harmonia.Process.Engine", "ProcessInstance")
    }
    _ <- IO.raiseUnless(transactions.nonEmpty)(
      RuntimeException("Settler event stream has no positive control")
    )
    actual = Json.obj(
      "actions" -> Json.fromValues(normalized),
      "settlement_transactions" -> Json.fromInt(settlements.size)
    )
    validated <- IO.fromEither(TransferFormat.result(actual).left.map(RuntimeException(_)))
    _ <- ArtifactFiles.write(
      output,
      raw
        .mapObject(
          _.add("settler_events", Json.fromValues(updates)).add(
            "settlement_transaction_ids",
            Json.fromValues(
              settlements.flatMap(_.hcursor.get[String]("updateId").toOption).map(Json.fromString)
            )
          )
        )
        .spaces2
    )
  yield validated

  private def normalize(step: Json): IO[Json] = for
    fields <- Vector("id", "outcome", "trade", "workflow").traverse(name =>
      IO.fromEither(step.hcursor.get[String](name)).map(name -> Json.fromString(_))
    )
    quantities <- Vector("available", "locked", "destination").traverse { name =>
      IO.fromEither(step.hcursor.get[Json](name))
        .flatMap(value =>
          IO.fromEither(
            TransferFormat
              .decimal(value.asNumber.fold(value)(number => Json.fromString(number.toString)), name)
              .left
              .map(RuntimeException(_))
          )
        )
        .map(value => name -> Json.fromString(value.bigDecimal.stripTrailingZeros.toPlainString))
    }
    counts <- Vector("activeWorkflows" -> "active_workflows", "releases" -> "releases").traverse {
      (raw, name) =>
        IO.fromEither(step.hcursor.get[Json](raw))
          .flatMap { value =>
            IO.fromOption(
              value.asNumber.flatMap(_.toInt).orElse(value.asString.flatMap(_.toIntOption))
            )(RuntimeException(s"Invalid observed $raw"))
          }
          .map(value => name -> Json.fromInt(value))
    }
    visible <- IO.fromEither(step.hcursor.get[Vector[String]]("visibleTo"))
    reason <- IO.fromEither(step.hcursor.get[Option[String]]("reason"))
    q = quantities.toMap
  yield Json.fromFields(
    fields ++ counts ++ Vector(
      "source" -> Json.obj("available" -> q("available"), "locked" -> q("locked")),
      "destination" -> q("destination"),
      "visible_to" -> Json.fromValues(visible.sorted.map(Json.fromString))
    ) ++ reason.map(value =>
      "reason" -> Json.fromString(
        if value == "application-rejected" && step.hcursor
            .get[String]("rawError")
            .toOption
            .exists(_.contains("Destination rejected receipt"))
        then "destination-rejected"
        else value
      )
    )
  )

package harmonia.composer.verify

import cats.effect.IO
import cats.syntax.all.*
import harmonia.composer.model.{Composition, CompositionResult}
import harmonia.files.ArtifactFiles
import harmonia.live.http.LiveServer
import harmonia.live.run.LiveRuntime
import harmonia.live.verify.SessionRequests.*
import harmonia.stories.compare.CompareResults
import harmonia.stories.read.MarkdownYaml
import harmonia.stories.run.RunProvenance
import io.circe.Json
import java.nio.file.Path

object CheckComposer:
  def run(root: Path): IO[Unit] = for
    artifacts <- ArtifactFiles.createRun(root, "composer-check")
    _ <- IO.println(s"Checking authenticated composition. Evidence: $artifacts")
    setup <- LiveRuntime.readInput(root)
    _ <- Vector("composer-direct", "composer-generated").traverse_ { id =>
      for
        input <- ArtifactFiles.read(root.resolve(s"evaluations/$id/input.md"))
        expectedMarkdown <- ArtifactFiles.read(root.resolve(s"evaluations/$id/expected.md"))
        scenario <- read(input, "Scenario")
        _ <- IO.fromEither(
          Composition
            .fields(scenario, Set("workflow", "plan", "actions"))
            .left
            .map(IllegalArgumentException(_))
        )
        _ <- IO.raiseUnless(scenario.hcursor.get[String]("workflow").contains("composed-process"))(
          IllegalArgumentException("Unknown composition story workflow")
        )
        plan <- IO.fromEither(scenario.hcursor.get[Json]("plan"))
        _ <- IO.fromEither(Composition.read(plan).left.map(IllegalArgumentException(_)))
        expectedJson <- read(expectedMarkdown, "Result")
        expected <- IO.fromEither(
          CompositionResult.read(expectedJson).left.map(IllegalArgumentException(_))
        )
        output = artifacts.resolve(id)
        _ <- ArtifactFiles.write(output.resolve("input.md"), input)
        _ <- ArtifactFiles.write(output.resolve("expected.md"), expectedMarkdown)
        result <- LiveRuntime
          .resource(root, artifacts.resolve(s"network-$id"), setup)
          .flatMap(runtime =>
            LiveServer
              .resource(root, artifacts.resolve(s"private-$id"), runtime)
              .map(server => runtime -> server)
          )
          .use { (runtime, server) =>
            execute(server, scenario, output).flatTap { _ =>
              for
                events <- runtime.participants.toVector.traverse { (name, participant) =>
                  participant.ledger.events.map(values => name -> Json.arr(values*))
                }
                recorded <- ArtifactFiles
                  .read(output.resolve("observation.json"))
                  .flatMap(value => IO.fromEither(io.circe.parser.parse(value)))
                _ <- ArtifactFiles.write(
                  output.resolve("observation.json"),
                  recorded
                    .deepMerge(Json.obj("participant_events" -> Json.fromFields(events)))
                    .spaces2
                )
              yield ()
            }
          }
        differences = CompareResults.compare(expected, result)
        _ <- ArtifactFiles.write(
          output.resolve("actual.md"),
          MarkdownYaml.render("Observed composition", "Result", result)
        )
        _ <- ArtifactFiles.write(output.resolve("diff.md"), CompareResults.markdown(differences))
        _ <- RunProvenance.write(
          root,
          output.resolve("input.md"),
          output.resolve("expected.md"),
          root.resolve("on-ledger/smoke/.daml/dist/harmonia-smoke-0.1.0.dar"),
          output,
          differences.isEmpty,
          "three authenticated participants; bank proposal and buyer consent; core-managed source actions"
        )
        _ <- IO.raiseUnless(differences.isEmpty)(
          RuntimeException(s"Composition differs: $output/diff.md")
        )
        _ <- IO.println(s"PASS $id: ${differences.size} differences")
      yield ()
    }
  yield ()

  private def execute(server: LiveServer, scenario: Json, output: Path): IO[Json] = for
    plan <- IO.fromEither(scenario.hcursor.get[Json]("plan"))
    reference <- IO.fromEither(plan.hcursor.get[String]("reference"))
    attempts <- IO.fromEither(scenario.hcursor.get[Vector[Json]]("actions"))
    _ <- IO.raiseUnless(attempts.nonEmpty && attempts.size <= 32)(
      IllegalArgumentException("Use one to thirty-two composition attempts")
    )
    _ <- IO.raiseUnless(
      attempts.flatMap(_.hcursor.get[String]("id").toOption).distinct.size == attempts.size
    )(IllegalArgumentException("Composition attempt identifiers must be unique"))
    observations <- attempts.traverse { attempt =>
      for
        id <- IO.fromEither(attempt.hcursor.get[String]("id"))
        actor <- IO.fromEither(attempt.hcursor.get[String]("actor"))
        action <- IO.fromEither(attempt.hcursor.get[String]("action"))
        _ <- IO.fromEither(
          Composition
            .fields(
              attempt,
              Set("id", "actor", "action") ++ (if action == "advance" then Set("step")
                                               else Set.empty[String])
            )
            .left
            .map(IllegalArgumentException(_))
        )
        _ <- IO.raiseUnless(
          Set("bank", "buyer").contains(actor) && id.matches("[a-zA-Z0-9-]{1,64}")
        )(IllegalArgumentException("Unknown actor or invalid attempt identifier"))
        _ <- IO.raiseUnless(Set("propose", "accept", "advance").contains(action))(
          IllegalArgumentException("Unknown composer story action")
        )
        before <- state(server, actor)
        parameters =
          if action == "propose" then plan
          else
            Json
              .obj("reference" -> Json.fromString(reference))
              .deepMerge(
                if action == "advance" then
                  Json.obj("step" -> attempt.hcursor.downField("step").focus.get)
                else Json.obj()
              )
        request = Json.obj(
          "id" -> Json.fromString(id),
          "action" -> Json.fromString("compose-" + action),
          "version" -> before.hcursor.downField("version").focus.get,
          "input" -> parameters
        )
        _ <- post(server, actor, request)
        job <- awaitJob(server, actor, id)
        outcome <- IO.fromEither(job.hcursor.get[String]("outcome"))
        _ <- IO.raiseUnless(Set("committed", "rejected").contains(outcome))(
          RuntimeException(s"Unexpected composition outcome: $outcome")
        )
        _ <-
          if outcome == "committed" then
            awaitCondition((state(server, "bank"), state(server, "buyer")).mapN { (bank, buyer) =>
              normalized(bank) == normalized(buyer)
            })
          else IO.unit
        after <- state(server, "bank")
        _ <- ArtifactFiles.write(
          output.resolve(s"action-$id.json"),
          Json.obj("request" -> request, "job" -> job, "after" -> after).spaces2
        )
      yield normalized(after)
        .deepMerge(Json.obj("id" -> Json.fromString(id), "outcome" -> Json.fromString(outcome)))
        .deepMerge(
          if outcome == "rejected" then Json.obj("reason" -> Json.fromString("ledger-rejected"))
          else Json.obj()
        )
    }
    finalState <- state(server, "bank")
    processes = finalState.hcursor
      .downField("composer")
      .get[Vector[Json]]("processes")
      .getOrElse(Vector.empty)
    process <- IO.fromOption(
      processes.find(_.hcursor.get[String]("reference").contains(reference))
    )(RuntimeException("Final process is missing"))
    result = Json.obj(
      "composition" -> Json.obj(
        "name" -> process.hcursor.downField("name").focus.get,
        "reference" -> process.hcursor.downField("reference").focus.get
      ),
      "actions" -> Json.arr(observations*)
    )
    _ <- ArtifactFiles.write(
      output.resolve("observation.json"),
      Json.obj("result" -> result, "final_state" -> finalState).spaces2
    )
  yield result

  private def normalized(state: Json): Json =
    val composer = state.hcursor.downField("composer")
    val drafts = composer.get[Vector[Json]]("drafts").getOrElse(Vector.empty)
    val processes = composer.get[Vector[Json]]("processes").getOrElse(Vector.empty)
    val process = processes.headOption.getOrElse(Json.Null).hcursor
    val steps = process.get[Vector[Json]]("steps").getOrElse(Vector.empty)
    def ids(field: String) = Json.arr(
      steps
        .filter(_.hcursor.get[Boolean](field).contains(true))
        .map(_.hcursor.downField("id").focus.get)*
    )
    Json.obj(
      "drafts" -> Json.fromInt(drafts.size),
      "instances" -> Json.fromInt(processes.size),
      "workflow" -> Json.fromString(
        if processes.isEmpty then "not-started"
        else if process.get[Boolean]("complete").contains(true) then "complete"
        else "waiting"
      ),
      "completed" -> ids("completed"),
      "enabled" -> ids("enabled"),
      "sources" -> Json.fromFields(
        steps.map(step =>
          step.hcursor.get[String]("id").toOption.get -> step.hcursor.downField("status").focus.get
        )
      )
    )
  private def read(markdown: String, section: String): IO[Json] =
    IO.fromEither(MarkdownYaml.read(markdown, section).left.map(IllegalArgumentException(_)))

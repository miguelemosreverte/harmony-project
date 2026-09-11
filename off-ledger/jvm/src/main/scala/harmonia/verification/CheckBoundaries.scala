package harmonia.verification

import cats.effect.IO
import cats.syntax.all.*
import harmonia.files.ArtifactFiles
import harmonia.ledger.{CantonSandbox, DamlScript}
import harmonia.live.http.LiveServer
import harmonia.live.run.LiveRuntime
import harmonia.live.ledger.{LedgerValue as V}
import harmonia.live.verify.SessionRequests.*
import harmonia.stories.compare.CompareResults
import harmonia.stories.read.MarkdownYaml
import harmonia.stories.run.RunProvenance
import io.circe.Json
import io.grpc.Status
import java.nio.file.Path

object CheckBoundaries:
  def run(root: Path): IO[Unit] = for
    artifacts <- ArtifactFiles.createRun(root, "boundaries")
    output = artifacts.resolve("execution-boundaries")
    _ <- IO.println(s"Checking execution bounds and competing advances. Evidence: $artifacts")
    input <- ArtifactFiles.read(root.resolve("evaluations/execution-boundaries/input.md"))
    baseline <- ArtifactFiles.read(root.resolve("evaluations/execution-boundaries/expected.md"))
    scenario <- IO.fromEither(
      MarkdownYaml.read(input, "Scenario").leftMap(IllegalArgumentException(_))
    )
    expected <- IO.fromEither(
      MarkdownYaml.read(baseline, "Result").leftMap(IllegalArgumentException(_))
    )
    _ <- IO.raiseUnless(scenario.hcursor.get[String]("ledger_script").contains("Boundaries:run"))(
      IllegalArgumentException("Unknown boundary script")
    )
    dar = root.resolve("on-ledger/smoke/.daml/dist/harmonia-smoke-0.1.0.dar")
    _ <- ArtifactFiles.write(output.resolve("input.md"), input)
    _ <- ArtifactFiles.write(output.resolve("expected.md"), baseline)
    core <- CantonSandbox.resource(root, artifacts.resolve("core"), dar).use { ledger =>
      DamlScript
        .run(root, ledger, dar, "Boundaries:run", artifacts.resolve("core"))
        .flatMap(ArtifactFiles.read)
        .flatMap(value => IO.fromEither(io.circe.parser.parse(value)))
    }
    setup <- LiveRuntime.readInput(root)
    race <- LiveRuntime
      .resource(root, artifacts.resolve("race-network"), setup)
      .flatMap(runtime =>
        LiveServer
          .resource(root, artifacts.resolve("private"), runtime)
          .map(server => runtime -> server)
      )
      .use { (runtime, server) =>
        compete(runtime, server, scenario.hcursor.downField("race").focus.get, output)
      }
    result = Json.obj("core" -> core, "race" -> race)
    differences = CompareResults.compare(expected, result)
    rawRace <- ArtifactFiles
      .read(output.resolve("race-transactions.json"))
      .flatMap(value => IO.fromEither(io.circe.parser.parse(value)))
    _ <- ArtifactFiles.write(
      output.resolve("observation.json"),
      Json.obj("result" -> result, "race" -> rawRace).spaces2
    )
    _ <- ArtifactFiles.write(
      output.resolve("actual.md"),
      MarkdownYaml.render("Observed execution boundaries", "Result", result)
    )
    _ <- ArtifactFiles.write(output.resolve("diff.md"), CompareResults.markdown(differences))
    _ <- RunProvenance.write(
      root,
      output.resolve("input.md"),
      output.resolve("expected.md"),
      dar,
      output,
      differences.isEmpty,
      "real ledger boundary script; two concurrent authenticated Ledger API commands against one process contract"
    )
    _ <- IO.raiseUnless(differences.isEmpty)(
      RuntimeException(s"Execution boundary mismatch: $output/diff.md")
    )
    _ <- IO.println(s"PASS execution boundaries: ${differences.size} differences")
  yield ()

  private def compete(
      runtime: LiveRuntime,
      server: LiveServer,
      plan: Json,
      output: Path
  ): IO[Json] =
    val bank = runtime.participants("bank").ledger
    val parsed = harmonia.composer.model.Composition
      .read(plan)
      .fold(message => throw IllegalArgumentException(message), identity)
    require(
      parsed.steps.size == 1 && parsed.steps.head.actor == "bank" && parsed.steps.head.action == "approve-financing",
      "The race requires one direct bank approval"
    )
    val reference = parsed.reference
    val stepName = parsed.steps.head.id
    def command(actor: String, id: String, action: String, input: Json): IO[Unit] = for
      before <- state(server, actor)
      _ <- post(
        server,
        actor,
        Json.obj(
          "id" -> Json.fromString(id),
          "action" -> Json.fromString(action),
          "version" -> before.hcursor.downField("version").focus.get,
          "input" -> input
        )
      )
      job <- awaitJob(server, actor, id)
      _ <- IO.raiseUnless(job.hcursor.get[String]("outcome").contains("committed"))(
        RuntimeException(s"Race setup did not commit: $job")
      )
    yield ()
    for
      _ <- command("bank", "race-propose", "compose-propose", plan)
      _ <- awaitCondition(
        state(server, "buyer").map(
          _.hcursor.downField("composer").get[Vector[Json]]("drafts").exists(_.nonEmpty)
        )
      )
      _ <- command(
        "buyer",
        "race-accept",
        "compose-accept",
        Json.obj("reference" -> Json.fromString(reference))
      )
      _ <- awaitCondition(
        bank
          .active()
          .map(
            _.exists(c =>
              runtime.catalog.accepts(c) && c.template.getEntityName == "ProcessInstance" && c
                .text("reference") == reference
            )
          )
      )
      before <- bank.active()
      process <- IO.fromOption(
        before.find(c =>
          runtime.catalog.accepts(c) && c.template.getEntityName == "ProcessInstance" && c.text(
            "reference"
          ) == reference
        )
      )(RuntimeException("Race process is missing"))
      source <- IO.fromOption(
        before.find(c =>
          runtime.catalog.accepts(c) && c.template.getModuleName == "Financing" && c.text(
            "reference"
          ) == reference
        )
      )(RuntimeException("Race source is missing"))
      argument = (id: String) =>
        V.record(
          "step" -> V.text(stepName),
          "actor" -> V.party(bank.party),
          "request" -> V.text(id)
        )
      pair <- (
        bank.exercise(process, "AdvanceStep", argument("race-a"), "race-a").attempt,
        bank.exercise(process, "AdvanceStep", argument("race-b"), "race-b").attempt
      ).parTupled
      outcomes = Vector(pair._1, pair._2)
      errors = outcomes.collect { case Left(error) => Status.fromThrowable(error).getCode }
      _ <- IO.raiseUnless(
        errors.forall(code =>
          Set(
            Status.Code.ABORTED,
            Status.Code.NOT_FOUND,
            Status.Code.INVALID_ARGUMENT,
            Status.Code.FAILED_PRECONDITION
          ).contains(code)
        )
      )(RuntimeException(s"Unexpected race failure, not a definitive conflict: $errors"))
      after <- bank.active()
      events <- bank.events
      processes = after.filter(c =>
        runtime.catalog.accepts(c) && c.template.getEntityName == "ProcessInstance" && c.text(
          "reference"
        ) == reference
      )
      applications = after.filter(c =>
        runtime.catalog.accepts(c) && c.template.getModuleName == "Financing" && c.text(
          "reference"
        ) == reference && c.text("status") == "approved"
      )
      completed = processes.flatMap(c =>
        V.fields(c).hcursor.get[Vector[String]]("completed").getOrElse(Vector.empty)
      )
      transactions = events.count(event =>
        event.hcursor
          .downField("transaction")
          .get[String]("commandId")
          .exists(Set("race-a", "race-b"))
      )
      _ <- ArtifactFiles.write(
        output.resolve("race-transactions.json"),
        Json
          .obj(
            "events" -> Json.arr(events*),
            "submissions" -> Json.arr(
              outcomes.map(
                _.fold(
                  error =>
                    Json.obj(
                      "error" -> Json.fromString(Status.fromThrowable(error).getCode.toString)
                    ),
                  identity
                )
              )*
            )
          )
          .spaces2
      )
    yield Json.obj(
      "committed" -> Json.fromInt(outcomes.count(_.isRight)),
      "conflicting" -> Json.fromInt(errors.size),
      "approved_sources" -> Json.fromInt(applications.size),
      "active_processes" -> Json.fromInt(processes.size),
      "completed_steps" -> Json.fromInt(completed.size),
      "committed_transactions" -> Json.fromInt(transactions),
      "original_source_archived" -> Json.fromBoolean(!after.exists(_.id == source.id))
    )

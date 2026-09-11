package harmonia.live.verify

import cats.effect.IO
import cats.syntax.all.*
import harmonia.files.ArtifactFiles
import harmonia.live.http.LiveServer
import harmonia.live.ledger.LiveLedger
import harmonia.live.run.{LiveRuntime, LiveParticipant}
import harmonia.live.state.LiveSnapshot
import harmonia.stories.financing.model.FinancingStory
import harmonia.stories.read.{MarkdownYaml, StoryFormat}
import harmonia.stories.compare.CompareResults
import harmonia.stories.run.RunProvenance
import io.circe.Json
import io.grpc.Status
import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.nio.file.Path
import scala.concurrent.duration.*

object CheckLive:
  import SessionRequests.*
  def run(root: Path): IO[Unit] = for
    artifacts <- ArtifactFiles.createRun(root, "live-check")
    output = artifacts.resolve("live-handoff")
    input <- ArtifactFiles.read(root.resolve("evaluations/live-handoff/input.md"))
    expectedMarkdown <- ArtifactFiles.read(root.resolve("evaluations/live-handoff/expected.md"))
    securityMarkdown <- ArtifactFiles.read(
      root.resolve("evaluations/live-handoff/security-expected.md")
    )
    story <- LiveRuntime.parseInput(input)
    expected <- IO.fromEither(
      StoryFormat.result(expectedMarkdown).left.map(IllegalArgumentException(_))
    )
    securityExpected <- IO.fromEither(
      MarkdownYaml.read(securityMarkdown, "Result").left.map(IllegalArgumentException(_))
    )
    _ <- ArtifactFiles.write(output.resolve("input.md"), input)
    _ <- ArtifactFiles.write(output.resolve("expected.md"), expectedMarkdown)
    _ <- ArtifactFiles.write(output.resolve("security-expected.md"), securityMarkdown)
    _ <- IO.println(s"Checking authenticated handoff and UI bypass. Evidence: $artifacts")
    observed <- LiveRuntime
      .resource(root, artifacts.resolve("network"), story)
      .flatMap(runtime =>
        LiveServer
          .resource(root, artifacts.resolve("private"), runtime)
          .map(server => runtime -> server)
      )
      .use { (runtime, server) =>
        exercise(runtime, server, story, output)
      }
    (actual, security) = observed
    differences = CompareResults.compare(expected, actual)
    securityDifferences = CompareResults.compare(securityExpected, security)
    _ <- ArtifactFiles.write(
      output.resolve("actual.md"),
      MarkdownYaml.render("Authenticated handoff observations", "Result", actual)
    )
    _ <- ArtifactFiles.write(
      output.resolve("security-actual.md"),
      MarkdownYaml.render("Observed identity boundaries", "Result", security)
    )
    _ <- ArtifactFiles.write(output.resolve("diff.md"), CompareResults.markdown(differences))
    _ <- ArtifactFiles.write(
      output.resolve("security-diff.md"),
      CompareResults.markdown(securityDifferences)
    )
    _ <- RunProvenance.write(
      root,
      output.resolve("input.md"),
      output.resolve("expected.md"),
      root.resolve("on-ledger/smoke/.daml/dist/harmonia-smoke-0.1.0.dar"),
      output,
      differences.isEmpty && securityDifferences.isEmpty,
      "three independent participants; restricted JWT users; independent HTTP sessions; one common synchronizer"
    )
    _ <- IO.raiseUnless(differences.isEmpty && securityDifferences.isEmpty)(
      RuntimeException(s"Live acceptance differs: see $output/diff.md and security-diff.md")
    )
    _ <- IO.println(
      s"PASS authenticated handoff, direct API authority, stale views, repeat protection, and reconnect: $output"
    )
  yield ()

  private def exercise(
      runtime: LiveRuntime,
      server: LiveServer,
      story: FinancingStory,
      output: Path
  ): IO[(Json, Json)] =
    val labels = Map("bank" -> story.bank, "buyer" -> story.buyer, "reviewer" -> story.reviewer.get)
    val sessions = labels.map(_.swap)
    val bank = runtime.participants("bank").ledger
    val buyer = runtime.participants("buyer").ledger
    for
      initialBank <- LiveSnapshot.read(bank, runtime.catalog)
      initialBuyer <- state(server, "buyer")
      readBypass <- buyer
        .active(bank.party)
        .attempt
        .map(_.fold(e => Status.fromThrowable(e).getCode.toString, _ => "SUCCEEDED"))
      actBypass <- buyer
        .exercise(
          initialBank.application.get,
          "Approve",
          LiveLedger.emptyArgument,
          "buyer-forged-bank",
          bank.party
        )
        .attempt
        .map(_.fold(e => Status.fromThrowable(e).getCode.toString, _ => "SUCCEEDED"))
      missingSession <- http(server, "", "GET", "/api/state", None).map(_._1)
      overrideRequest = action("override", "approve-financing", initialBank.version).deepMerge(
        Json.obj("actor" -> Json.fromString("bank"))
      )
      overrideHttp <- http(
        server,
        server.capabilities("buyer"),
        "POST",
        "/api/actions",
        Some(overrideRequest)
      ).map(_._1)
      observations <- story.actions.foldLeft(
        IO.pure(Vector.empty[Json] -> Map.empty[String, Json])
      ) { (acc, step) =>
        for
          previous <- acc
          before <- LiveSnapshot.read(bank, runtime.catalog)
          snapshot <- state(server, sessions(step.actor))
          request = action(
            step.id,
            step.action,
            snapshot.hcursor.get[String]("version").toOption.get
          )
          _ <- post(server, sessions(step.actor), request)
          job <- awaitJob(server, sessions(step.actor), step.id)
          outcome = job.hcursor.get[String]("outcome").getOrElse("missing")
          _ <- IO.raiseUnless(Set("committed", "rejected").contains(outcome))(
            RuntimeException(s"Unexpected command outcome $outcome for ${step.id}")
          )
          _ <-
            if outcome == "committed" && step.action == "approve-financing" then
              awaitCondition(LiveSnapshot.read(buyer, runtime.catalog).map(_.proof.nonEmpty))
            else IO.unit
          _ <-
            if outcome == "committed" && step.action == "publish-approval" then
              awaitCondition(
                runtime.participants.values.toVector
                  .traverse(p => LiveSnapshot.read(p.ledger, runtime.catalog))
                  .map(_.forall(_.workflow == "complete"))
              )
            else IO.unit
          after <- LiveSnapshot.read(bank, runtime.catalog)
          views <- runtime.participants.toVector.sortBy(_._1).traverse { (name, participant) =>
            LiveSnapshot.read(participant.ledger, runtime.catalog).map(name -> _)
          }
          fields = Vector(
            "id" -> Json.fromString(step.id),
            "outcome" -> Json.fromString(outcome),
            "application" -> Json.fromString(
              after.application.map(_.text("status")).getOrElse("missing")
            ),
            "workflow" -> Json.fromString(after.workflow),
            "consumed" -> Json.fromBoolean(
              before.application.map(_.id) != after.application.map(_.id)
            ),
            "active_contracts" -> Json.fromInt(
              after.contracts.count(_.template.getModuleName == "PrivateFinancing")
            ),
            "visible_to" -> Json.arr(views.collect {
              case (name, view) if view.application.nonEmpty => Json.fromString(labels(name))
            }*)
          ) ++
            Option.when(outcome == "rejected")("reason" -> Json.fromString("ledger-rejected"))
          observed = Json.fromFields(fields)
          _ <- ArtifactFiles.write(
            output.resolve(s"http-${step.id}.json"),
            Json.obj("request" -> request, "job" -> job, "state" -> snapshot).spaces2
          )
        yield (previous._1 :+ observed, previous._2.updated(step.id, request))
      }
      staleRequest = action(
        "stale-continuation",
        "publish-approval",
        initialBuyer.hcursor.get[String]("version").toOption.get
      )
      _ <- post(server, "buyer", staleRequest)
      stale <- awaitJob(server, "buyer", "stale-continuation")
      beforeRepeat <- bank.events
      repeated <- post(server, "bank", observations._2("bank-approval"))
      afterRepeat <- bank.events
      reconnect <- state(server, "buyer")
      finalViews <- runtime.participants.toVector.sortBy(_._1).traverse { (name, participant) =>
        participant.ledger.events.map(name -> _)
      }
      privateVisible = finalViews.collect {
        case (name, events) if events.exists(_.noSpaces.contains(story.privateDetails.get)) =>
          Json.fromString(name)
      }
      security = Json.obj(
        "buyer_read_as_bank" -> Json.fromString(readBypass),
        "buyer_act_as_bank" -> Json.fromString(actBypass),
        "missing_session_http" -> Json.fromInt(missingSession),
        "actor_override_http" -> Json.fromInt(overrideHttp),
        "stale_submission" -> stale.hcursor
          .get[String]("outcome")
          .toOption
          .fold(Json.Null)(Json.fromString),
        "repeated_request" -> repeated.hcursor
          .get[String]("outcome")
          .toOption
          .fold(Json.Null)(Json.fromString),
        "repeated_request_added_transactions" -> Json.fromInt(afterRepeat.size - beforeRepeat.size),
        "reconnected_workflow" -> reconnect.hcursor
          .get[String]("workflow")
          .toOption
          .fold(Json.Null)(Json.fromString),
        "private_payload_visible_to" -> Json.arr(privateVisible*)
      )
      actual = Json.obj("actions" -> Json.arr(observations._1*))
      _ <- ArtifactFiles.write(
        output.resolve("observation.json"),
        Json
          .obj(
            "actual" -> actual,
            "security" -> security,
            "participant_events" -> Json.fromFields(
              finalViews.map((name, events) => name -> Json.arr(events*))
            )
          )
          .spaces2
      )
    yield actual -> security

  private def action(id: String, name: String, version: String): Json = Json.obj(
    "id" -> Json.fromString(id),
    "action" -> Json.fromString(name),
    "version" -> Json.fromString(version)
  )

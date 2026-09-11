package harmonia.demo

import harmonia.app.Connections

import cats.effect.{IO, Resource}
import cats.syntax.all.*
import harmonia.files.ArtifactFiles
import harmonia.ledger.auth.LocalCredentials
import harmonia.ledger.client.{LiveLedger, ParticipantLedger, TemplateCatalog}
import harmonia.ledger.DamlScript
import harmonia.ledger.network.{CantonNetwork, NetworkAuthorization}
import io.circe.Json
import java.nio.file.Path
import java.util.UUID
import harmonia.stories.financing.model.FinancingStory
import harmonia.stories.read.StoryFormat

object Demo:
  def resource(root: Path, artifacts: Path, story: FinancingStory): Resource[IO, Connections] =
    val names = Vector("bank", "buyer", "reviewer")
    val dar = root.resolve("harness/ledger/demo/.daml/dist/harmonia-demo-0.1.0.dar")
    for
      catalog <- Resource.eval(
        TemplateCatalog.load(root, dar, artifacts.resolve("template-catalog"))
      )
      normal <- Resource.eval(
        names.traverse(name => LocalCredentials.create.map(name -> _)).map(_.toMap)
      )
      bootstrap <- Resource.eval(
        names.traverse(name => LocalCredentials.create.map(name -> _)).map(_.toMap)
      )
      tokens <- Resource.eval(
        names.traverse(name => bootstrap(name).token("bootstrap").map(name -> _)).map(_.toMap)
      )
      auth = NetworkAuthorization(
        names
          .map(name =>
            name -> s"[${normal(name).configuration(false)}, ${bootstrap(name).configuration(true)}]"
          )
          .toMap,
        tokens
      )
      network <- CantonNetwork.resource(
        root,
        artifacts,
        dar,
        inspectDars = Vector(
          root.resolve(
            "product/ledger/applications/legacy-financing/.daml/dist/legacy-financing-0.1.0.dar"
          )
        ),
        authorization = Some(auth)
      )
      _ <- Resource.eval(
        ArtifactFiles.write(
          artifacts.resolve("setup/input.json"),
          Json
            .obj(
              "namespace" -> Json.fromString("live-" + UUID.randomUUID().toString.take(8)),
              "bankName" -> Json.fromString(story.bank),
              "buyerName" -> Json.fromString(story.buyer),
              "reviewerName" -> Json.fromString(story.reviewer.get),
              "privateDetails" -> Json.fromString(story.privateDetails.get),
              "initialStatus" -> Json.fromString(story.status)
            )
            .noSpaces
        )
      )
      result <- Resource.eval(
        DamlScript.runNetwork(
          root,
          network.configuration,
          dar,
          "LiveSetup:run",
          artifacts.resolve("setup"),
          artifacts.resolve("setup/input.json")
        )
      )
      setup <- Resource.eval(
        ArtifactFiles.read(result).flatMap(s => IO.fromEither(io.circe.parser.parse(s)))
      )
      participants <- names.traverse { name =>
        for
          party <- Resource.eval(IO.fromEither(setup.hcursor.get[String](name)))
          ledger <- LiveLedger.resource(
            network.participants(name),
            party,
            name,
            normal(name).token(name)
          )
        yield name -> ledger
      }
    yield Connections(participants.toMap, artifacts.resolve("downloaded"), catalog)

  def serve(root: Path): IO[Unit] = for
    artifacts <- ArtifactFiles.createRun(root, "live")
    story <- readInput(root)
    _ <- resource(root, artifacts, story)
      .flatMap(runtime => harmonia.app.http.LiveServer.resource(root, artifacts, runtime))
      .use { server =>
        IO.println(
          s"Live viewer: http://127.0.0.1:${server.port}/. Open a provisioned participant link from $artifacts/sessions.json. Ctrl-C stops the disposable network."
        ) *> IO.never
      }
  yield ()

  def readInput(root: Path): IO[FinancingStory] =
    ArtifactFiles
      .read(root.resolve("examples/evaluations/live-handoff/input.md"))
      .flatMap(parseInput)

  def parseInput(markdown: String): IO[FinancingStory] =
    IO.fromEither(StoryFormat.input("live-handoff", markdown).left.map(IllegalArgumentException(_)))
      .flatMap {
        case story: FinancingStory
            if story.workflow.contains("private-approval") && story.status == "pending" =>
          IO.pure(story)
        case _ =>
          IO.raiseError(
            IllegalArgumentException("Live sessions require a pending private approval story")
          )
      }

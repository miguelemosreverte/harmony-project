package harmonia.ledger.network

import cats.effect.{IO, Resource}
import cats.syntax.all.*
import harmonia.files.ArtifactFiles
import harmonia.processes.ManagedProcess
import io.circe.Json
import java.net.ServerSocket
import java.nio.file.Path
import scala.concurrent.duration.*

final case class NetworkAuthorization(
    configuration: Map[String, String],
    bootstrapTokens: Map[String, String]
):
  override def toString: String = "NetworkAuthorization(<redacted>)"

final case class CantonNetwork(participants: Map[String, Int], configuration: Path)

object CantonNetwork:
  private val names = Vector("bank", "buyer", "reviewer")

  def resource(
      root: Path,
      artifacts: Path,
      dar: Path,
      inspectDars: Vector[Path] = Vector.empty,
      participantNames: Vector[String] = names,
      authorization: Option[NetworkAuthorization] = None
  ): Resource[IO, CantonNetwork] =
    require(
      participantNames.nonEmpty && participantNames.size <= 4 && participantNames.distinct.size == participantNames.size && participantNames
        .forall(_.matches("[a-z][a-z0-9]*")),
      "Use one to four distinct participant names "
    )
    val first = participantNames.head
    val count = participantNames.size
    val freePorts = List
      .fill(count * 3 + 3)(Resource.fromAutoCloseable(IO.blocking(new ServerSocket(0))))
      .sequence
      .use(sockets => IO.pure(sockets.map(_.getLocalPort).toVector))
    for
      ports <- Resource.eval(freePorts)
      config = artifacts.resolve("network.conf")
      bootstrap = artifacts.resolve("bootstrap.canton")
      ready = artifacts.resolve("network.ready")
      participantConfig = artifacts.resolve("participants.json")
      endpoints = participantNames.zipWithIndex.map((name, index) => name -> ports(index * 3)).toMap
      _ <- Resource.eval {
        val participants = participantNames.zipWithIndex
          .map { (name, index) =>
            s"""$name {
             | storage.type = memory
             | ledger-api.port = ${ports(index * 3)}
             | ledger-api.address = "127.0.0.1"
             | ${authorization
                .map(a => "ledger-api.auth-services = " + a.configuration(name))
                .getOrElse("")}
             | admin-api.port = ${ports(index * 3 + 1)}
             | admin-api.address = "127.0.0.1"
             | http-ledger-api.port = ${ports(index * 3 + 2)}
             | http-ledger-api.address = "127.0.0.1"
             |}""".stripMargin
          }
          .mkString("\n")
        val configuration = s"""canton {
          | participants { $participants }
          | sequencers.sequencer1 {
          |   admin-api.address = "127.0.0.1"
          |   public-api.address = "127.0.0.1"
          |   storage.type = memory
          |   admin-api.port = ${ports(count * 3)}
          |   public-api.port = ${ports(count * 3 + 1)}
          |   sequencer { type = reference, config.storage.type = memory }
          | }
          | mediators.mediator1 { admin-api.address = "127.0.0.1", storage.type = memory, admin-api.port = ${ports(
                                count * 3 + 2
                              )} }
          |}""".stripMargin
        val retrieval = inspectDars.zipWithIndex
          .map { (source, index) =>
            s"""val inputDar$index = $first.dars.upload(${quote(source.toString)})
             |$first.dars.download(inputDar$index, ${quote(
                artifacts.resolve("downloaded").toString
              )})
             |""".stripMargin
          }
          .mkString("\n")
        val script = s"""import com.digitalasset.canton.config.RequireTypes.PositiveInt
          |import com.digitalasset.canton.version.ProtocolVersion
          |val parameters = StaticSynchronizerParameters.defaults(sequencer1.config.crypto, ProtocolVersion.forSynchronizer, topologyChangeDelay = NonNegativeFiniteDuration.Zero)
          |bootstrap.synchronizer("harmonia", Seq(sequencer1), Seq(mediator1), Seq(sequencer1, mediator1), PositiveInt.one, parameters)
          |participants.local.foreach { participant =>
          |  participant.synchronizers.connect_local(sequencer1, "harmonia")
          |  participant.dars.upload(${quote(dar.toString)})
          |}
          |${participantNames
                         .filterNot(_ == first)
                         .map(name => s"$first.health.ping($name)")
                         .mkString("\n")}
          |$retrieval
          |java.nio.file.Files.writeString(java.nio.file.Path.of(${quote(
                         ready.toString
                       )}), participants.local.map(p => p.id.toString).mkString("\\n"))
          |""".stripMargin
        val json = Json.obj(
          "default_participant" -> endpoint(
            endpoints(first),
            authorization.map(_.bootstrapTokens(first))
          ),
          "participants" -> Json.fromFields(
            endpoints.toVector.map((name, port) =>
              name -> endpoint(port, authorization.map(_.bootstrapTokens(name)))
            )
          ),
          "party_participants" -> Json.obj()
        )
        IO.blocking(java.nio.file.Files.createDirectories(artifacts.resolve("downloaded"))) *>
          harmonia.ledger.auth.LocalCredentials.privateWrite(config, configuration) *> ArtifactFiles
            .write(bootstrap, script) *>
          harmonia.ledger.auth.LocalCredentials.privateWrite(participantConfig, json.spaces2)
      }
      process <- ManagedProcess.start(
        List(
          root.resolve("scripts/canton").toString,
          "daemon",
          "-c",
          config.toString,
          "--bootstrap",
          bootstrap.toString,
          "--log-file-name",
          artifacts.resolve("network-detail.log").toString
        ),
        root,
        artifacts.resolve("network.log")
      )
      _ <- Resource.eval {
        def awaitReady: IO[Unit] = ArtifactFiles.exists(ready).flatMap {
          case true => IO.unit
          case false =>
            IO.raiseUnless(process.isAlive)(
              RuntimeException(s"Network exited. See $artifacts/network.log")
            ) *>
              IO.sleep(200.millis) *> IO.defer(awaitReady)
        }
        awaitReady.timeout(120.seconds)
      }
    yield CantonNetwork(endpoints, participantConfig)

  private def endpoint(port: Int, token: Option[String]): Json =
    Json
      .obj("host" -> Json.fromString("127.0.0.1"), "port" -> Json.fromInt(port))
      .deepMerge(token.fold(Json.obj())(t => Json.obj("access_token" -> Json.fromString(t))))
  private def quote(value: String): String = Json.fromString(value).noSpaces

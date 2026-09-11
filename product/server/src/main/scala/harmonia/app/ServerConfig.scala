package harmonia.app

import cats.effect.{IO, Resource}
import cats.syntax.all.*
import harmonia.app.http.LiveServer
import harmonia.files.ArtifactFiles
import harmonia.ledger.client.{LiveLedger, TemplateCatalog}
import io.circe.Decoder
import java.nio.file.{Files, Path}

final case class ParticipantConfig(port: Int, party: String, user: String, tokenFile: String)
object ParticipantConfig:
  given Decoder[ParticipantConfig] = Decoder
    .forProduct4("port", "party", "user", "token_file")(
      ParticipantConfig.apply
    )
    .emap(p =>
      Either.cond(
        p.port > 0 && p.port <= 65535 && p.party.nonEmpty && p.user.nonEmpty && p.tokenFile.nonEmpty,
        p,
        "A participant needs a local port, party, user, and token file"
      )
    )

final case class ServerConfig(
    catalogDar: String,
    packageExports: String,
    participants: Map[String, ParticipantConfig]
)
object ServerConfig:
  given Decoder[ServerConfig] = Decoder
    .forProduct3("catalog_dar", "package_exports", "participants")(
      ServerConfig.apply
    )
    .emap(c =>
      Either.cond(
        c.participants.keySet == Set(
          "bank",
          "buyer",
          "reviewer"
        ) && c.catalogDar.nonEmpty && c.packageExports.nonEmpty,
        c,
        "Configure Bank, Buyer, Reviewer, the catalog DAR, and package exports"
      )
    )

  def serve(root: Path, path: Path): IO[Unit] = for
    text <- ArtifactFiles.read(path)
    config <- IO.fromEither(io.circe.parser.decode[ServerConfig](text))
    output <- ArtifactFiles.createRun(root, "server")
    catalog <- TemplateCatalog.load(
      root,
      root.resolve(config.catalogDar),
      output.resolve("catalog")
    )
    _ <- config.participants.toVector
      .traverse { (name, p) =>
        LiveLedger
          .resource(
            p.port,
            p.party,
            p.user,
            ArtifactFiles.read(root.resolve(p.tokenFile)).map(_.trim)
          )
          .map(name -> _)
      }
      .flatMap { ledgers =>
        LiveServer.resource(
          root,
          output,
          Connections(ledgers.toMap, root.resolve(config.packageExports), catalog)
        )
      }
      .use(server =>
        IO.println(
          s"Service: http://127.0.0.1:${server.port}/; participant sessions: $output/sessions.json"
        ) *> IO.never
      )
  yield ()

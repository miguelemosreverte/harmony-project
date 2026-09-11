package harmonia.app

import cats.effect.IO
import cats.syntax.all.*
import harmonia.app.http.LiveServer
import harmonia.files.ArtifactFiles
import harmonia.ledger.client.{LiveLedger, TemplateCatalog}
import io.circe.{Decoder, Encoder}
import harmonia.protocol.JsonCodec
import java.nio.file.Path

final case class ParticipantConfig(port: Int, party: String, user: String, tokenFile: String)
object ParticipantConfig:
  private val fields = JsonCodec.derived[ParticipantConfig]
  given Encoder.AsObject[ParticipantConfig] = fields
  given Decoder[ParticipantConfig] = fields
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
    participants: Map[String, ParticipantConfig],
    book: Option[String] = None
)
object ServerConfig:
  private val fields = JsonCodec.derived[ServerConfig]
  given Encoder.AsObject[ServerConfig] = fields
  given Decoder[ServerConfig] = fields
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

  def serve(root: Path, path: Path, directory: Option[Path] = None): IO[Unit] = for
    text <- ArtifactFiles.read(path)
    config <- IO.fromEither(io.circe.parser.decode[ServerConfig](text))
    output <- directory.fold(ArtifactFiles.createRun(root, "server"))(IO.pure)
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
          Connections(ledgers.toMap, root.resolve(config.packageExports), catalog),
          config.book.map(root.resolve)
        )
      }
      .use(server =>
        IO.println(
          s"Service: http://127.0.0.1:${server.port}/; participant sessions: $output/sessions.json"
        ) *> IO.never
      )
  yield ()

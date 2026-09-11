package harmonia.packages.inspect

import cats.effect.IO
import harmonia.files.ArtifactFiles
import harmonia.processes.ManagedProcess
import io.circe.{Decoder, Json}
import java.nio.file.{Files, Path}
import java.security.MessageDigest

final case class InspectedDar(packageId: String, lf: String, packages: Map[String, Json])
object InspectedDar:
  private val metadata = Decoder.forProduct2("main_package_id", "packages")(
    (id: String, packages: Map[String, Json]) => (id, packages)
  )

  def read(json: Json, packageId: String, lf: String): Either[Throwable, InspectedDar] = for
    fields <- metadata.decodeJson(json)
    (id, packages) = fields
    _ <- Either.cond(
      id == packageId,
      (),
      RuntimeException("Structured package identity disagrees with compiler metadata")
    )
  yield InspectedDar(id, lf, packages)

object InspectDar:
  val maximumBytes = 8 * 1024 * 1024

  def digest(path: Path): IO[String] = IO.blocking {
    require(
      Files.size(path) <= maximumBytes,
      s"DAR exceeds the $maximumBytes byte input limit: $path"
    )
    MessageDigest
      .getInstance("SHA-256")
      .digest(Files.readAllBytes(path))
      .map(b => f"${b & 0xff}%02x")
      .mkString
  }

  def inspect(root: Path, dar: Path, output: Path): IO[InspectedDar] = for
    main <- LfArchive.read(dar)
    _ <- ManagedProcess.run(
      List(root.resolve("scripts/daml").toString, "damlc", "inspect-dar", dar.toString, "--json"),
      root,
      output.resolve("packages.json")
    )
    text <- ArtifactFiles.read(output.resolve("packages.json"))
    json <- IO.fromEither(io.circe.parser.parse(text))
    inspection <- IO.fromEither(InspectedDar.read(json, main.id, main.version))
  yield inspection

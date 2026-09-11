package harmonia.packages.inspect

import cats.effect.IO
import harmonia.files.ArtifactFiles
import harmonia.processes.ManagedProcess
import io.circe.Json
import java.nio.file.{Files, Path}
import java.security.MessageDigest

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

  def inspect(root: Path, dar: Path, output: Path): IO[Json] = for
    main <- LfArchive.read(dar)
    _ <- ManagedProcess.run(
      List(root.resolve("scripts/daml").toString, "damlc", "inspect-dar", dar.toString, "--json"),
      root,
      output.resolve("packages.json")
    )
    text <- ArtifactFiles.read(output.resolve("packages.json"))
    json <- IO.fromEither(io.circe.parser.parse(text))
    inspectedId <- IO.fromEither(json.hcursor.get[String]("main_package_id"))
    _ <- IO.raiseUnless(inspectedId == main.id)(
      RuntimeException("Structured package identity disagrees with compiler metadata")
    )
  yield json.mapObject(_.add("lf", Json.fromString(main.version)))

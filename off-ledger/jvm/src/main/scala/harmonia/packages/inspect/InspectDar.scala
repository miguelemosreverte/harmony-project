package harmonia.packages.inspect

import cats.effect.{IO, Resource}
import harmonia.files.ArtifactFiles
import harmonia.processes.ManagedProcess
import io.circe.Json
import java.nio.file.{Files, Path}
import java.security.MessageDigest
import java.util.zip.ZipFile
import scala.jdk.CollectionConverters.*

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
    _ <- Resource.fromAutoCloseable(IO.blocking(new ZipFile(dar.toFile))).use { zip =>
      IO.blocking {
        val entries = zip.entries().asScala.toVector
        require(
          entries.size <= 2048 && entries.map(_.getName).distinct.size == entries.size,
          "DAR has too many entries or duplicate names"
        )
        require(
          entries.forall(_.getSize >= 0) && entries.map(_.getSize).sum <= 32L * 1024 * 1024,
          "DAR exceeds the 32 MiB expanded size limit"
        )
        require(zip.getEntry("META-INF/MANIFEST.MF") != null, "DAR is missing its manifest")
      }
    }
    _ <- ManagedProcess.run(
      List(root.resolve("scripts/daml").toString, "damlc", "inspect-dar", dar.toString, "--json"),
      root,
      output.resolve("packages.json")
    )
    _ <- ManagedProcess.run(
      List(
        root.resolve("scripts/daml").toString,
        "damlc",
        "inspect",
        dar.toString,
        "-o",
        output.resolve("main.daml-lf").toString
      ),
      root,
      output.resolve("inspect.log")
    )
    text <- ArtifactFiles.read(output.resolve("packages.json"))
    json <- IO.fromEither(io.circe.parser.parse(text))
    pretty <- ArtifactFiles.read(output.resolve("main.daml-lf"))
    lf <- IO.fromOption(
      pretty.linesIterator.find(_.startsWith("daml-lf ")).map(_.stripPrefix("daml-lf "))
    )(RuntimeException("Compiler did not report the DAR's Daml-LF version"))
  yield json.mapObject(_.add("lf", Json.fromString(lf)))

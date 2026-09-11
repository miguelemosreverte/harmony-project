package harmonia.files

import cats.effect.IO
import java.nio.file.{Files, Path}

object ArtifactFiles:
  def read(path: Path): IO[String] = IO.blocking(Files.readString(path))

  def write(path: Path, content: String): IO[Unit] = IO.blocking {
    Files.createDirectories(path.getParent)
    Files.writeString(path, content)
    ()
  }

  def exists(path: Path): IO[Boolean] = IO.blocking(Files.isRegularFile(path))

  def createRun(root: Path, name: String): IO[Path] = IO.blocking {
    val directory = root.resolve(".artifacts")
    Files.createDirectories(directory)
    Files.createTempDirectory(directory, s"$name-")
  }

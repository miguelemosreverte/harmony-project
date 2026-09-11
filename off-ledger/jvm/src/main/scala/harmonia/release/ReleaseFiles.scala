package harmonia.release

import cats.effect.IO
import cats.syntax.all.*
import harmonia.files.ArtifactFiles
import harmonia.processes.ManagedProcess
import java.nio.file.{Files, Path, StandardCopyOption}
import java.security.MessageDigest
import scala.jdk.CollectionConverters.*

private[release] object ReleaseFiles:
  def files(directory: Path): IO[Vector[Path]] = IO.blocking {
    val stream = Files.walk(directory)
    try stream.iterator().asScala.filter(Files.isRegularFile(_)).toVector.sortBy(_.toString)
    finally stream.close()
  }

  def copy(from: Path, to: Path): IO[Unit] = IO.blocking {
    Files.createDirectories(to.getParent)
    Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING)
    ()
  }

  def hash(path: Path): IO[String] = IO.blocking {
    val digest = MessageDigest.getInstance("SHA-256")
    val input = Files.newInputStream(path)
    val buffer = new Array[Byte](65536)
    try
      var size = input.read(buffer)
      while size >= 0 do
        digest.update(buffer, 0, size)
        size = input.read(buffer)
      digest.digest().map(b => f"${b & 0xff}%02x").mkString
    finally input.close()
  }

  def git(root: Path, output: Path, args: String*): IO[String] =
    ManagedProcess.run("git" :: args.toList, root, output) *> ArtifactFiles.read(output).map(_.trim)

  def cloneAt(from: Path, to: Path, revision: String, logs: Path): IO[Unit] =
    ManagedProcess.run(
      List("git", "clone", "--no-hardlinks", "--quiet", from.toString, to.toString),
      from,
      logs.resolve("clone.log")
    ) *> git(to, logs.resolve("checkout.log"), "checkout", "--detach", revision).void *>
      git(to, logs.resolve("remove-origin.log"), "remote", "remove", "origin").void

  def copySelected(from: Path, to: Path)(keep: Path => Boolean): IO[Unit] =
    files(from).flatMap(_.filter(keep).traverse_(p => copy(p, to.resolve(from.relativize(p)))))

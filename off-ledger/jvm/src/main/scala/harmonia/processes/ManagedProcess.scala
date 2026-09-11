package harmonia.processes

import cats.effect.{IO, Resource}
import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters.*

object ManagedProcess:
  def start(command: List[String], directory: Path, log: Path): Resource[IO, Process] =
    Resource.make(IO.blocking {
      Files.createDirectories(log.getParent)
      new ProcessBuilder(command.asJava)
        .directory(directory.toFile)
        .redirectErrorStream(true)
        .redirectOutput(log.toFile)
        .start()
    })(stop)

  def run(
      command: List[String],
      directory: Path,
      log: Path,
      pidFile: Option[Path] = None
  ): IO[Unit] =
    start(command, directory, log).use { process =>
      IO.blocking {
        pidFile.foreach(path => Files.writeString(path, process.pid().toString)); ()
      } *>
        IO.interruptible(process.waitFor()).flatMap { exit =>
          IO.raiseWhen(exit != 0)(RuntimeException(s"Command failed ($exit). See $log"))
        }
    }

  private def stop(process: Process): IO[Unit] = IO.blocking {
    val stream = process.descendants()
    val descendants =
      try stream.iterator().asScala.toList.reverse
      finally stream.close()
    descendants.foreach(_.destroy())
    process.destroy()
    if !process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS) then
      process.destroyForcibly()
      process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)
    descendants.filter(_.isAlive).foreach { child =>
      child.destroyForcibly()
      child.onExit().get(5, java.util.concurrent.TimeUnit.SECONDS)
    }
    ()
  }

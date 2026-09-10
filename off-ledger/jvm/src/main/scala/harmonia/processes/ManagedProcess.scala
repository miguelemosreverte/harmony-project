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

  def run(command: List[String], directory: Path, log: Path): IO[Unit] =
    start(command, directory, log).use { process =>
      IO.interruptible(process.waitFor()).flatMap { exit =>
        IO.raiseWhen(exit != 0)(RuntimeException(s"Command failed ($exit). See $log"))
      }
    }

  private def stop(process: Process): IO[Unit] = IO.blocking {
    val descendants = process.descendants()
    try descendants.iterator().asScala.toList.reverse.foreach(_.destroy())
    finally descendants.close()
    process.destroy()
    if !process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS) then process.destroyForcibly()
    ()
  }

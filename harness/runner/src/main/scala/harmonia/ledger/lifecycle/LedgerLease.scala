package harmonia.ledger.lifecycle

import cats.effect.{IO, Resource}
import java.nio.channels.{FileChannel, OverlappingFileLockException}
import java.nio.file.{Files, Path, StandardOpenOption}

/** One disposable ledger JVM per workspace, including live demos and checks. */
object LedgerLease:
  def resource(root: Path): Resource[IO, Unit] = for
    channel <- Resource.fromAutoCloseable(IO.blocking {
      val path = root.resolve(".artifacts/ledger.lock")
      Files.createDirectories(path.getParent)
      FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
    })
    _ <- Resource.make(IO.blocking {
      val lock =
        try Option(channel.tryLock())
        catch case _: OverlappingFileLockException => None
      lock.getOrElse(
        throw IllegalStateException(
          "A ledger environment is already running in this workspace. Stop the live demo before starting another ledger check."
        )
      )
    })(lock => IO.blocking(lock.release()))
  yield ()

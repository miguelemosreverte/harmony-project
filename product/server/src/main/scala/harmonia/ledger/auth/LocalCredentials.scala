package harmonia.ledger.auth

import cats.effect.IO
import java.nio.file.{Files, Path}
import java.nio.file.attribute.PosixFilePermissions
import java.security.SecureRandom
import java.util.Base64

/** Browser session keys and private local credential files. */
object LocalCredentials:
  private def encode(bytes: Array[Byte]): String =
    Base64.getUrlEncoder.withoutPadding().encodeToString(bytes)
  def random: IO[String] = IO {
    val bytes = new Array[Byte](32); new SecureRandom().nextBytes(bytes); encode(bytes)
  }
  def privateWrite(path: Path, text: String): IO[Unit] = IO.blocking {
    Files.createDirectories(path.getParent)
    if !Files.exists(path) then
      Files.createFile(
        path,
        PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"))
      )
    Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rw-------"))
    Files.writeString(path, text)
    ()
  }

package harmonia.ledger.auth

import cats.effect.IO
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Path}
import java.nio.file.attribute.PosixFilePermissions
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import io.circe.Json

/** Disposable loopback evaluation credentials. Never included in recorded evidence. */
final class LocalCredentials private (secret: String):
  def token(user: String): IO[String] = IO {
    val now = Instant.now().getEpochSecond
    val header = LocalCredentials.encode("""{"alg":"HS256","typ":"JWT"}""".getBytes(UTF_8))
    val claims = Json.obj(
      "sub" -> Json.fromString(user),
      "scope" -> Json.fromString("daml_ledger_api"),
      "iat" -> Json.fromLong(now),
      "exp" -> Json.fromLong(now + 300)
    )
    val unsigned = header + "." + LocalCredentials.encode(claims.noSpaces.getBytes(UTF_8))
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(new SecretKeySpec(secret.getBytes(UTF_8), "HmacSHA256"))
    unsigned + "." + LocalCredentials.encode(mac.doFinal(unsigned.getBytes(UTF_8)))
  }
  def configuration(privileged: Boolean): String =
    val authority =
      if privileged then
        ", privileged = true, access-level = wildcard, target-scope = daml_ledger_api"
      else ""
    s"{ type = unsafe-jwt-hmac-256, secret = \"$secret\"$authority }"
  override def toString: String = "LocalCredentials(<redacted>)"

object LocalCredentials:
  private def encode(bytes: Array[Byte]): String =
    Base64.getUrlEncoder.withoutPadding().encodeToString(bytes)
  def random: IO[String] = IO {
    val bytes = new Array[Byte](32); new SecureRandom().nextBytes(bytes); encode(bytes)
  }
  def create: IO[LocalCredentials] = random.map(new LocalCredentials(_))
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

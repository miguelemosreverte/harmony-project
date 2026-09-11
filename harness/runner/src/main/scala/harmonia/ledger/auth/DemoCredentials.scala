package harmonia.ledger.auth

import cats.effect.IO
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Instant
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import io.circe.Json

/** Disposable loopback evaluation credentials. Never included in recorded evidence. */
final class DemoCredentials private (secret: String):
  def token(user: String): IO[String] = IO {
    val now = Instant.now().getEpochSecond
    val header = DemoCredentials.encode("""{"alg":"HS256","typ":"JWT"}""".getBytes(UTF_8))
    val claims = Json.obj(
      "sub" -> Json.fromString(user),
      "scope" -> Json.fromString("daml_ledger_api"),
      "iat" -> Json.fromLong(now),
      "exp" -> Json.fromLong(now + 300)
    )
    val unsigned = header + "." + DemoCredentials.encode(claims.noSpaces.getBytes(UTF_8))
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(new SecretKeySpec(secret.getBytes(UTF_8), "HmacSHA256"))
    unsigned + "." + DemoCredentials.encode(mac.doFinal(unsigned.getBytes(UTF_8)))
  }
  def configuration(privileged: Boolean): String =
    val authority =
      if privileged then
        ", privileged = true, access-level = wildcard, target-scope = daml_ledger_api"
      else ""
    s"{ type = unsafe-jwt-hmac-256, secret = \"$secret\"$authority }"
  override def toString: String = "DemoCredentials(<redacted>)"

object DemoCredentials:
  private def encode(bytes: Array[Byte]): String =
    Base64.getUrlEncoder.withoutPadding().encodeToString(bytes)
  def create: IO[DemoCredentials] = LocalCredentials.random.map(new DemoCredentials(_))

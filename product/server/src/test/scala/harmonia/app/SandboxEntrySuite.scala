package harmonia.app

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.daml.ledger.api.v2.ValueOuterClass.Value
import harmonia.app.http.LiveServer
import harmonia.ledger.client.{ActiveContract, ParticipantLedger, TemplateCatalog}
import io.circe.Json
import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.nio.file.{Files, Path}
import munit.FunSuite

class SandboxEntrySuite extends FunSuite:
  private val untouched = new ParticipantLedger:
    val party = "unused"
    val user = "unused"
    def active(readParty: String): IO[Vector[ActiveContract]] =
      IO.raiseError(AssertionError("Entry read ledger state"))
    def events: IO[Vector[Json]] = IO.raiseError(AssertionError("Entry read ledger events"))
    def exercise(
        contract: ActiveContract,
        choice: String,
        argument: Value,
        commandId: String,
        actAs: String
    ): IO[Json] =
      IO.raiseError(AssertionError("Entry submitted a ledger command"))

  private def inspect(sandbox: Boolean)(check: LiveServer => Unit): Unit =
    val root = Path.of(".").toAbsolutePath.normalize()
    val output = Files.createTempDirectory("harmonia-entry-")
    val runtime = Connections(
      Map("bank" -> untouched, "buyer" -> untouched, "reviewer" -> untouched),
      output,
      TemplateCatalog(Map.empty)
    )
    LiveServer
      .resource(root, output, runtime, sandbox = sandbox)
      .use(server => IO.blocking(check(server)))
      .unsafeRunSync()

  private def request(
      server: LiveServer,
      path: String,
      method: String = "GET",
      origin: Option[String] = None,
      json: Boolean = true,
      bearer: Option[String] = None
  ) =
    val builder = HttpRequest.newBuilder(URI.create(s"http://127.0.0.1:${server.port}$path"))
    origin.foreach(builder.header("Origin", _))
    if json then builder.header("Content-Type", "application/json")
    bearer.foreach(value => builder.header("Authorization", "Bearer " + value))
    val request =
      if method == "POST" then builder.POST(HttpRequest.BodyPublishers.ofString("{}")).build()
      else builder.GET().build()
    HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())

  test("configured services expose no self-service participant access") {
    inspect(false) { server =>
      assertEquals(request(server, "/api/entry").body(), "{\"sandbox\":false}")
      assertEquals(
        request(server, "/api/sandbox/entry/bank", "POST", Some(s"http://127.0.0.1:${server.port}"))
          .statusCode(),
        404
      )
      assertEquals(request(server, "/api/state").statusCode(), 401)
    }
  }

  test("sandbox entry is same-origin, explicit, role-scoped and performs no ledger work") {
    inspect(true) { server =>
      val origin = s"http://127.0.0.1:${server.port}"
      assertEquals(request(server, "/api/entry").body(), "{\"sandbox\":true}")
      for actor <- Vector("bank", "buyer", "reviewer") do
        val response = request(server, "/api/sandbox/entry/" + actor, "POST", Some(origin))
        assertEquals(response.statusCode(), 200)
        val capability = io.circe.parser
          .parse(response.body())
          .toOption
          .get
          .hcursor
          .get[String]("capability")
          .toOption
          .get
        assert(capability == server.capabilities(actor), "Role receives only its own capability")
        assertEquals(response.headers().firstValue("Cache-Control").get(), "no-store")
      assertEquals(request(server, "/api/sandbox/entry/bank").statusCode(), 405)
      assertEquals(request(server, "/api/sandbox/entry/bank", "POST").statusCode(), 403)
      assertEquals(
        request(server, "/api/sandbox/entry/bank", "POST", Some("https://example.invalid"))
          .statusCode(),
        403
      )
      assertEquals(
        request(server, "/api/sandbox/entry/bank", "POST", Some(origin), json = false).statusCode(),
        403
      )
      assertEquals(
        request(server, "/api/sandbox/entry/admin", "POST", Some(origin)).statusCode(),
        400
      )
      assertEquals(
        request(server, "/api/builder", bearer = Some(server.capabilities("buyer"))).statusCode(),
        403
      )
      assertEquals(request(server, "/api/state").statusCode(), 401)
    }
  }

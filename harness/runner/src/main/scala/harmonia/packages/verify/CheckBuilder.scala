package harmonia.packages.verify

import harmonia.packages.workspace.*
import cats.effect.IO
import cats.syntax.all.*
import harmonia.files.ArtifactFiles
import harmonia.app.http.LiveServer
import harmonia.demo.Demo
import harmonia.app.Connections
import harmonia.live.verify.SessionRequests
import harmonia.packages.read.PackageManifest
import harmonia.stories.compare.CompareResults
import harmonia.files.MarkdownYaml
import harmonia.stories.run.RunProvenance
import io.circe.Json
import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.nio.file.{Files, Path}
import java.security.MessageDigest
import java.util.zip.ZipInputStream

object CheckBuilder:
  def run(root: Path): IO[Unit] = for
    artifacts <- ArtifactFiles.createRun(root, "builder-check")
    output = artifacts.resolve("package-builder")
    _ <- IO.println(s"Checking package input builder. Evidence: $artifacts")
    input <- ArtifactFiles.read(root.resolve("examples/evaluations/package-builder/input.md"))
    baseline <- ArtifactFiles.read(root.resolve("examples/evaluations/package-builder/expected.md"))
    scenario <- IO.fromEither(
      MarkdownYaml.read(input, "Scenario").leftMap(IllegalArgumentException(_))
    )
    expected <- IO.fromEither(
      MarkdownYaml.read(baseline, "Result").leftMap(IllegalArgumentException(_))
    )
    _ <- IO.fromEither(
      harmonia.packages.workspace.BuilderResult.read(expected).leftMap(IllegalArgumentException(_))
    )
    _ <- ArtifactFiles.write(output.resolve("input.md"), input)
    _ <- ArtifactFiles.write(output.resolve("expected.md"), baseline)
    setup <- Demo.readInput(root)
    observed <- Demo
      .resource(root, artifacts.resolve("network"), setup)
      .flatMap(runtime => LiveServer.resource(root, artifacts.resolve("private"), runtime))
      .use(server => execute(root, server, scenario, output))
    differences = CompareResults.compare(expected, observed)
    _ <- ArtifactFiles.write(
      output.resolve("actual.md"),
      MarkdownYaml.render("Observed package builder", "Result", observed)
    )
    _ <- ArtifactFiles.write(output.resolve("diff.md"), CompareResults.markdown(differences))
    _ <- RunProvenance.write(
      root,
      output.resolve("input.md"),
      output.resolve("expected.md"),
      root.resolve("harness/ledger/demo/.daml/dist/harmonia-demo-0.1.0.dar"),
      output,
      differences.isEmpty,
      "authenticated builder; actual uploaded and participant-exported DARs; compiled downloadable project"
    )
    _ <- IO.raiseUnless(differences.isEmpty)(RuntimeException(s"Builder differs: $output/diff.md"))
    _ <- IO.println(s"PASS package-builder: ${differences.size} differences")
  yield ()

  private def execute(root: Path, server: LiveServer, scenario: Json, output: Path): IO[Json] = for
    manifest <- ArtifactFiles.read(root.resolve("product/packages/inputs.md"))
    pins <- IO.fromEither(PackageManifest.read(manifest).leftMap(IllegalArgumentException(_)))
    attempts <- IO.fromEither(scenario.hcursor.get[Vector[Json]]("actions"))
    actions <- attempts.traverse { attempt =>
      for
        id <- IO.fromEither(attempt.hcursor.get[String]("id"))
        actor <- IO.fromEither(attempt.hcursor.get[String]("actor"))
        action <- IO.fromEither(attempt.hcursor.get[String]("action"))
        before <- state(server)
        request <- action match
          case "upload" =>
            for
              alias <- IO.fromEither(attempt.hcursor.get[String]("source"))
              pin <- IO.fromOption(pins.find(_.name == alias))(
                IllegalArgumentException("Unknown upload input")
              )
              bytes <- IO.blocking(Files.readAllBytes(root.resolve(pin.source)))
            yield ("/api/builder/upload", bytes)
          case "malformed" =>
            IO.pure(
              "/api/builder/upload" -> "This is not a DAR".getBytes(
                java.nio.charset.StandardCharsets.UTF_8
              )
            )
          case "oversized" => IO.pure("/api/builder/upload" -> new Array[Byte](8 * 1024 * 1024 + 1))
          case "retrieve" =>
            IO.pure(
              "/api/builder/retrieve" -> Json
                .obj("source" -> attempt.hcursor.downField("source").focus.get)
                .noSpaces
                .getBytes(java.nio.charset.StandardCharsets.UTF_8)
            )
          case "generate" =>
            val latest = before.hcursor.get[Vector[Json]]("inputs").toOption.get.last
            IO.pure(
              "/api/builder/generate" -> Json
                .obj("id" -> latest.hcursor.downField("id").focus.get)
                .noSpaces
                .getBytes(java.nio.charset.StandardCharsets.UTF_8)
            )
          case _ => IO.raiseError(IllegalArgumentException("Unknown builder story action"))
        response <- http(server, actor, "POST", request._1, request._2)
        after <- state(server)
        _ <- ArtifactFiles.write(
          output.resolve(s"action-$id.json"),
          Json
            .obj(
              "http" -> Json.fromInt(response._1),
              "response" -> Json.fromString(
                new String(response._2, java.nio.charset.StandardCharsets.UTF_8)
              ),
              "state" -> after
            )
            .spaces2
        )
        entries <- IO.fromEither(after.hcursor.get[Vector[Json]]("inputs"))
        row = Json.obj(
          "id" -> Json.fromString(id),
          "http" -> Json.fromInt(response._1),
          "inputs" -> Json.fromInt(entries.size)
        )
      yield
        if response._1 != 200 then row
        else
          val last = entries.last.hcursor
          row.deepMerge(
            Json.obj(
              "supported" -> last.downField("can_generate").focus.get,
              "available_live" -> last.downField("available_live").focus.get,
              "compiled" -> last.downField("compiled").focus.get
            )
          )
    }
    finalState <- state(server)
    entries <- IO.fromEither(finalState.hcursor.get[Vector[Json]]("inputs"))
    compiled <- IO.fromOption(entries.find(_.hcursor.get[Boolean]("compiled").contains(true)))(
      RuntimeException("No project was compiled")
    )
    download <- http(
      server,
      "bank",
      "GET",
      "/api/builder/project/" + compiled.hcursor.get[String]("id").toOption.get,
      Array.emptyByteArray
    )
    _ <- IO.raiseUnless(download._1 == 200)(RuntimeException("Compiled project download failed"))
    _ <- IO.blocking(Files.write(output.resolve("project.zip"), download._2))
    files <- IO.blocking {
      val zip = new ZipInputStream(new java.io.ByteArrayInputStream(download._2))
      try
        Iterator
          .continually(zip.getNextEntry)
          .takeWhile(_ != null)
          .map(entry => entry.getName -> zip.readAllBytes())
          .toMap
      finally zip.close()
    }
    generation <- IO.fromEither(
      io.circe.parser.parse(
        new String(files("generation.json"), java.nio.charset.StandardCharsets.UTF_8)
      )
    )
    digests <- IO.fromEither(generation.hcursor.get[Map[String, String]]("artifacts"))
    checks = Json.obj(
      "source_unchanged" -> Json.fromBoolean(
        hash(files("vendor/source.dar")) == pins.find(_.name == "legacy-financing").get.sha256
      ),
      "compiled_artifacts_match" -> Json.fromBoolean(digests.forall { (name, digest) =>
        val candidates = files.filter((path, _) =>
          name match
            case "library_dar"    => path.startsWith("library/.daml/dist/")
            case "example_dar"    => path.startsWith("example/.daml/dist/")
            case "interfaces_dar" => path == "vendor/interfaces.dar"
            case "core_dar"       => path == "vendor/core.dar"
            case _                => false
        )
        candidates.size == 1 && hash(candidates.head._2) == digest
      } && digests.keySet == Set("library_dar", "example_dar", "interfaces_dar", "core_dar"))
    )
    result = Json.obj(
      "builder" -> Json.fromString("package-inputs"),
      "actions" -> Json.arr(actions*),
      "download" -> checks
    )
    _ <- ArtifactFiles.write(
      output.resolve("observation.json"),
      Json
        .obj(
          "result" -> result,
          "final_state" -> finalState,
          "download_sha256" -> Json.fromString(hash(download._2))
        )
        .spaces2
    )
  yield result

  private def hash(bytes: Array[Byte]): String =
    MessageDigest.getInstance("SHA-256").digest(bytes).map(b => f"${b & 0xff}%02x").mkString
  private def state(server: LiveServer): IO[Json] =
    SessionRequests.request(server, "bank", "GET", "/api/builder", None)
  private def http(
      server: LiveServer,
      actor: String,
      method: String,
      path: String,
      bytes: Array[Byte]
  ): IO[(Int, Array[Byte])] = IO.interruptible {
    val request = HttpRequest
      .newBuilder(URI.create(s"http://127.0.0.1:${server.port}$path"))
      .timeout(java.time.Duration.ofSeconds(100))
      .header("Authorization", "Bearer " + server.capabilities(actor))
      .method(method, HttpRequest.BodyPublishers.ofByteArray(bytes))
      .build()
    val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofByteArray())
    response.statusCode() -> response.body()
  }

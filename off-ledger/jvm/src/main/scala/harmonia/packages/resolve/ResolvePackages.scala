package harmonia.packages.resolve

import cats.effect.{IO, Resource}
import cats.syntax.all.*
import harmonia.files.ArtifactFiles
import harmonia.packages.read.{PackageInput, PackageManifest}
import harmonia.packages.inspect.InspectDar
import io.circe.Json
import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.nio.file.{Files, Path, StandardCopyOption}
import java.time.Duration
import scala.concurrent.duration.*

object ResolvePackages:
  def run(root: Path, manifest: Path): IO[Json] = for
    text <- ArtifactFiles.read(manifest)
    inputs <- IO.fromEither(PackageManifest.read(text).left.map(RuntimeException(_)))
    records <- inputs.traverse(input => resolve(root, input).map(input.name -> _))
    result = Json.obj("sdk" -> Json.fromString("3.4.11"), "inputs" -> Json.fromFields(records))
    _ <- ArtifactFiles.write(root.resolve(".artifacts/packages/resolved.json"), result.spaces2)
    _ <- IO.println(s"Resolved ${inputs.size} pinned DAR inputs: .artifacts/packages/resolved.json")
  yield result

  private def resolve(root: Path, input: PackageInput): IO[Json] =
    val directory = root.resolve(".artifacts/packages/cache").resolve(input.sha256)
    val cached = directory.resolve("source.dar")
    val alias = root.resolve(".artifacts/packages/inputs").resolve(input.name + ".dar")
    for
      _ <- IO.blocking(Files.createDirectories(directory))
      exists <- ArtifactFiles.exists(cached)
      _ <- if exists then IO.unit else acquire(root, input, cached)
      digest <- InspectDar.digest(cached)
      _ <- IO.raiseUnless(digest == input.sha256)(
        RuntimeException(
          s"${input.name}: DAR digest mismatch. Expected ${input.sha256}, found $digest. Inspect $cached; the input was not accepted."
        )
      )
      // Local inputs are checked on every run, even when the content cache exists.
      _ <-
        if input.source.startsWith("https:") then IO.unit
        else
          InspectDar
            .digest(root.resolve(input.source))
            .flatMap(value =>
              IO.raiseUnless(value == input.sha256)(
                RuntimeException(
                  s"${input.name}: local source changed; review its identity explicitly"
                )
              )
            )
      inspection <- InspectDar.inspect(root, cached, directory)
      actualId <- IO.fromEither(inspection.hcursor.get[String]("main_package_id"))
      actualLf <- IO.fromEither(inspection.hcursor.get[String]("lf"))
      _ <- IO.raiseUnless(actualId == input.packageId && actualLf == input.lf)(
        RuntimeException(
          s"${input.name}: incompatible package. Expected ${input.packageId} / LF ${input.lf}; found $actualId / LF $actualLf"
        )
      )
      packages <- IO.fromEither(inspection.hcursor.get[Json]("packages"))
      _ <- IO.blocking {
        Files.createDirectories(alias.getParent)
        Files.copy(cached, alias, StandardCopyOption.REPLACE_EXISTING)
      }
    yield Json.obj(
      "source" -> Json.fromString(input.source),
      "sha256" -> Json.fromString(digest),
      "package_id" -> Json.fromString(actualId),
      "lf" -> Json.fromString(actualLf),
      "file" -> Json.fromString(root.relativize(alias).toString),
      "packages" -> packages
    )

  private def acquire(root: Path, input: PackageInput, destination: Path): IO[Unit] =
    val temporary = Resource.make(
      IO.blocking(Files.createTempFile(destination.getParent, "download-", ".dar"))
    )(p => IO.blocking(Files.deleteIfExists(p)).void)
    temporary.use { path =>
      val copy =
        if input.source.startsWith("https:") then download(input.source, path)
        else
          IO.blocking {
            val source = root.resolve(input.source)
            require(
              Files.isRegularFile(source),
              s"Local DAR is unavailable: $source. Build its source package first."
            )
            require(
              Files.size(source) <= InspectDar.maximumBytes,
              "Local DAR exceeds the 8 MiB limit"
            )
            Files.copy(source, path, StandardCopyOption.REPLACE_EXISTING); ()
          }
      for
        _ <- copy.timeout(60.seconds)
        digest <- InspectDar.digest(path)
        _ <- IO.raiseUnless(digest == input.sha256)(
          RuntimeException(
            s"${input.name}: downloaded or local bytes do not match the committed digest; no cache entry was accepted"
          )
        )
        _ <- IO.blocking(Files.move(path, destination, StandardCopyOption.REPLACE_EXISTING))
      yield ()
    }

  private def download(source: String, destination: Path): IO[Unit] =
    val client = HttpClient
      .newBuilder()
      .connectTimeout(Duration.ofSeconds(15))
      .followRedirects(HttpClient.Redirect.NEVER)
      .build()
    val request =
      HttpRequest.newBuilder(URI.create(source)).timeout(Duration.ofSeconds(45)).GET().build()
    Resource
      .fromAutoCloseable(
        IO.interruptible(client.send(request, HttpResponse.BodyHandlers.ofInputStream())).map {
          response =>
            if response.statusCode() != 200 then
              response.body().close()
              throw RuntimeException(
                s"DAR source returned HTTP ${response.statusCode()}: $source. Restore the pinned input or provide its verified local copy; no newer version was selected."
              )
            response.body()
        }
      )
      .use { stream =>
        IO.interruptible {
          val bytes = stream.readNBytes(InspectDar.maximumBytes + 1)
          require(bytes.length <= InspectDar.maximumBytes, "Downloaded DAR exceeds the 8 MiB limit")
          Files.write(destination, bytes); ()
        }
      }

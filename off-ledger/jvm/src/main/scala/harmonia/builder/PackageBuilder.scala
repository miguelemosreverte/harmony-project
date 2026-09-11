package harmonia.builder

import cats.effect.{IO, Ref, Resource}
import cats.effect.std.Semaphore
import cats.syntax.all.*
import harmonia.bindings.generate.GenerateBinding
import harmonia.files.ArtifactFiles
import harmonia.packages.inspect.InspectDar
import harmonia.packages.read.{PackageInput, PackageManifest}
import harmonia.packages.resolve.ResolvePackages
import io.circe.Json
import java.nio.file.{Files, Path}
import java.util.UUID
import scala.jdk.CollectionConverters.*
import scala.concurrent.duration.*

private final case class BuilderInput(
    id: String,
    directory: Path,
    record: Json,
    project: Option[Path] = None
)

final class PackageBuilder private (
    root: Path,
    directory: Path,
    exports: Path,
    pins: Vector[PackageInput],
    inputs: Ref[IO, Vector[BuilderInput]],
    gate: Semaphore[IO]
):
  private val mappings =
    Map("legacy-financing" -> "financing", "primitive-approval" -> "primitive-approval")

  def state: IO[Json] = inputs.get.map(values =>
    Json.obj(
      "inputs" -> Json.arr(values.map(_.record)*),
      "remaining" -> Json.fromInt(8 - values.size),
      "sources" -> Json.arr(
        pins.map(pin =>
          Json.obj("id" -> Json.fromString(pin.name), "source" -> Json.fromString(pin.source))
        )*
      )
    )
  )

  def upload(bytes: IO[Array[Byte]]): IO[Json] = exclusive {
    add("local upload", bytes, None)
  }

  def retrieve(name: String): IO[Json] = exclusive {
    if name == "participant" then
      for
        pin <- IO.fromOption(pins.find(_.name == "legacy-financing"))(
          IllegalArgumentException("Legacy input is not configured")
        )
        paths <- IO.blocking {
          val stream = Files.list(exports)
          try
            stream.iterator().asScala.filter(p => p.getFileName.toString.endsWith(".dar")).toVector
          finally stream.close()
        }
        _ <- IO.raiseWhen(paths.size > 16)(
          IllegalArgumentException("Too many participant export candidates")
        )
        matches <- paths.traverse(p => InspectDar.digest(p).map(hash => p -> hash))
        source <- IO.fromOption(matches.find(_._2 == pin.sha256).map(_._1))(
          IllegalArgumentException(
            "The participant export does not match the committed legacy DAR identity"
          )
        )
        result <- add(
          "participant administrator export",
          IO.blocking(Files.readAllBytes(source)),
          Some(pin)
        )
      yield result
    else
      for
        pin <- IO.fromOption(pins.find(_.name == name))(
          IllegalArgumentException("Choose a committed package source")
        )
        resolved <- ResolvePackages.resolve(root, pin)
        file <- IO.fromEither(resolved.hcursor.get[String]("file"))
        result <- add(pin.source, IO.blocking(Files.readAllBytes(root.resolve(file))), Some(pin))
      yield result
  }

  def generate(id: String): IO[Json] = exclusive {
    for
      entry <- find(id)
      _ <- entry.project match
        case Some(_) => IO.unit
        case None =>
          for
            alias <- IO.fromEither(
              entry.record.hcursor
                .get[String]("matched_source")
                .leftMap(_ =>
                  IllegalArgumentException(
                    "No reviewed mapping matches these archive bytes. Inspect the package and add a pinned source and typed mapping before rebuilding."
                  )
                )
            )
            mapping <- IO.fromOption(mappings.get(alias))(
              IllegalArgumentException(
                "This package has no supported action mapping. Metadata types are not an executable action."
              )
            )
            project <- GenerateBinding.run(
              root,
              root.resolve(s"packages/mappings/$mapping.md"),
              entry.directory.resolve("project")
            )
            manifestText <- ArtifactFiles.read(project.directory.resolve("generation.json"))
            manifest <- IO.fromEither(io.circe.parser.parse(manifestText))
            archive <- ProjectArchive.write(project, manifest)
            updated = entry.copy(
              project = Some(archive),
              record = entry.record.deepMerge(
                Json.obj("compiled" -> Json.fromBoolean(true), "generation" -> manifest)
              )
            )
            _ <- inputs.update(_.map(value => if value.id == id then updated else value))
          yield ()
      result <- state
    yield result
  }

  def download(id: String): IO[Path] = find(id).flatMap(entry =>
    IO.fromOption(entry.project)(
      IllegalArgumentException("Generate the project before downloading it")
    )
  )

  private def find(id: String): IO[BuilderInput] = inputs.get.flatMap(values =>
    IO.fromOption(values.find(_.id == id))(IllegalArgumentException("Unknown inspected package"))
  )

  private def add(
      origin: String,
      bytes: IO[Array[Byte]],
      expected: Option[PackageInput]
  ): IO[Json] = for
    values <- inputs.get
    _ <- IO.raiseWhen(values.size >= 8)(
      IllegalArgumentException("This builder accepts eight inputs per evaluation")
    )
    data <- bytes
    _ <- IO.raiseWhen(data.isEmpty || data.length > InspectDar.maximumBytes)(
      IllegalArgumentException("Upload a non-empty DAR no larger than 8 MiB")
    )
    id = UUID.randomUUID().toString
    target = directory.resolve("inputs").resolve(id)
    result <- (for
      _ <- IO.blocking {
        Files.createDirectories(target); Files.write(target.resolve("source.dar"), data); ()
      }
      digest <- InspectDar.digest(target.resolve("source.dar"))
      inspection <- InspectDar.inspect(root, target.resolve("source.dar"), target)
      packageId <- IO.fromEither(inspection.hcursor.get[String]("main_package_id"))
      lf <- IO.fromEither(inspection.hcursor.get[String]("lf"))
      _ <- IO.raiseUnless(Set("2.1", "2.2").contains(lf))(
        IllegalArgumentException("Supported Daml-LF versions are 2.1 and 2.2")
      )
      _ <- expected.traverse_(pin =>
        IO.raiseUnless(pin.sha256 == digest && pin.packageId == packageId && pin.lf == lf)(
          IllegalArgumentException("Retrieved DAR identity does not match its committed pin")
        )
      )
      pin = pins.find(p => p.sha256 == digest && p.packageId == packageId && p.lf == lf)
      supported = pin.exists(p => mappings.contains(p.name))
      available = pin.exists(_.name == "legacy-financing")
      record = Json.obj(
        "id" -> Json.fromString(id),
        "origin" -> Json.fromString(origin),
        "sha256" -> Json.fromString(digest),
        "package_id" -> Json.fromString(packageId),
        "lf" -> Json.fromString(lf),
        "matched_source" -> pin.fold(Json.Null)(p => Json.fromString(p.name)),
        "can_generate" -> Json.fromBoolean(supported),
        "available_live" -> Json.fromBoolean(available),
        "compiled" -> Json.fromBoolean(false),
        "packages" -> inspection.hcursor.downField("packages").focus.getOrElse(Json.obj()),
        "diagnostic" -> Json.fromString(
          if available then "Reviewed generated approval is available in the workflow action menu."
          else if supported then
            "A reviewed primitive-action mapping can generate a portable project. Register its typed action and rebuild to add it to the live composer."
          else
            "No reviewed executable mapping matches this input. Package inspection does not make an action available; add a pinned source and supported typed mapping, then rebuild."
        )
      )
      _ <- ArtifactFiles.write(target.resolve("input.json"), record.spaces2)
      _ <- inputs.update(_ :+ BuilderInput(id, target, record))
      result <- state
    yield result).onError(_ => remove(target)).onCancel(remove(target))
  yield result

  private def exclusive[A](operation: IO[A]): IO[A] = Resource
    .make(
      gate.tryAcquire.flatMap(ok =>
        IO.raiseUnless(ok)(
          IllegalArgumentException("Another package operation is running; wait for it to finish")
        )
      )
    )(_ => gate.release)
    .use(_ => operation.timeout(90.seconds))
    .adaptError {
      case error: IllegalArgumentException => error
      case scala.util.control.NonFatal(error) =>
        IllegalArgumentException(
          "Package inspection or compilation failed: " + Option(error.getMessage)
            .getOrElse(error.getClass.getSimpleName)
            .take(300)
        )
    }

  private def remove(path: Path): IO[Unit] = IO.blocking {
    if Files.exists(path) then
      val stream = Files.walk(path)
      try stream.iterator().asScala.toVector.sortBy(_.getNameCount).reverse.foreach(Files.delete)
      finally stream.close()
  }

object PackageBuilder:
  def create(root: Path, directory: Path, exports: Path): IO[PackageBuilder] = for
    markdown <- ArtifactFiles.read(root.resolve("packages/inputs.md"))
    pins <- IO.fromEither(PackageManifest.read(markdown).leftMap(IllegalArgumentException(_)))
    inputs <- Ref.of[IO, Vector[BuilderInput]](Vector.empty)
    gate <- Semaphore[IO](1)
  yield new PackageBuilder(root, directory, exports, pins, inputs, gate)

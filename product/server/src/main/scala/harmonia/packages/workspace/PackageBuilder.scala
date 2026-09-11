package harmonia.packages.workspace

import cats.effect.{IO, Ref, Resource}
import cats.effect.std.Semaphore
import cats.syntax.all.*
import harmonia.bindings.generate.GenerateBinding
import harmonia.files.ArtifactFiles
import harmonia.packages.inspect.InspectDar
import harmonia.packages.read.{PackageInput, PackageManifest}
import harmonia.packages.resolve.ResolvePackages
import io.circe.Json
import io.circe.syntax.*
import harmonia.packages.{PackageState, PackageSource}
import java.nio.file.{Files, Path}
import java.util.UUID
import scala.jdk.CollectionConverters.*
import scala.concurrent.duration.*

final class PackageBuilder private (
    root: Path,
    directory: Path,
    exports: Path,
    pins: Vector[PackageInput],
    inputs: Ref[IO, Vector[BuilderInput]],
    gate: Semaphore[IO]
):
  def state: IO[PackageState] = inputs.get.map(values =>
    PackageState(
      values.map(_.view),
      8 - values.size,
      pins.map(pin => PackageSource(pin.name, pin.source))
    )
  )

  def upload(bytes: IO[Array[Byte]]): IO[PackageState] = exclusive {
    add("local upload", bytes, None)
  }

  def retrieve(name: String): IO[PackageState] = exclusive {
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
        result <- add(pin.source, IO.blocking(Files.readAllBytes(resolved.file)), Some(pin))
      yield result
  }

  def generate(id: String): IO[PackageState] = exclusive {
    for
      entry <- find(id)
      _ <- entry.project match
        case Some(_) => IO.unit
        case None =>
          for
            _ <- IO.raiseWhen(entry.source.isEmpty)(
              IllegalArgumentException(
                "No reviewed mapping matches these archive bytes. Inspect the package and add a pinned source and typed mapping before rebuilding."
              )
            )
            binding <- IO.fromOption(entry.binding)(
              IllegalArgumentException(
                "This package has no supported action mapping. Metadata types are not an executable action."
              )
            )
            project <- GenerateBinding.run(
              root,
              root.resolve(s"product/packages/mappings/${binding.mapping}.md"),
              entry.directory.resolve("project")
            )
            archive <- ProjectArchive.write(project)
            updated = entry.copy(project = Some(CompiledProject(archive, project.manifest)))
            _ <- inputs.update(_.map(value => if value.id == id then updated else value))
          yield ()
      result <- state
    yield result
  }

  def download(id: String): IO[Path] = find(id).flatMap(entry =>
    IO.fromOption(entry.project.map(_.archive))(
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
  ): IO[PackageState] = for
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
      packageId = inspection.packageId
      lf = inspection.lf
      _ <- IO.raiseUnless(Set("2.1", "2.2").contains(lf))(
        IllegalArgumentException("Supported Daml-LF versions are 2.1 and 2.2")
      )
      _ <- expected.traverse_(pin =>
        IO.raiseUnless(pin.sha256 == digest && pin.packageId == packageId && pin.lf == lf)(
          IllegalArgumentException("Retrieved DAR identity does not match its committed pin")
        )
      )
      pin = pins.find(p => p.sha256 == digest && p.packageId == packageId && p.lf == lf)
      entry = BuilderInput(id, target, origin, digest, inspection, pin)
      _ <- ArtifactFiles.write(target.resolve("input.json"), entry.view.asJson.spaces2)
      _ <- inputs.update(_ :+ entry)
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
    markdown <- ArtifactFiles.read(root.resolve("product/packages/inputs.md"))
    pins <- IO.fromEither(PackageManifest.read(markdown).leftMap(IllegalArgumentException(_)))
    inputs <- Ref.of[IO, Vector[BuilderInput]](Vector.empty)
    gate <- Semaphore[IO](1)
  yield new PackageBuilder(root, directory, exports, pins, inputs, gate)

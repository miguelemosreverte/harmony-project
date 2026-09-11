package harmonia.bindings.inspect

import cats.effect.IO
import harmonia.files.ArtifactFiles
import harmonia.processes.ManagedProcess
import io.circe.Json
import java.nio.file.{Files, Path}
import java.security.MessageDigest

object SourceIdentity:
  def verify(root: Path, artifacts: Path): IO[Unit] =
    val source = root.resolve("on-ledger/applications/legacy-financing")
    val dar = source.resolve(".daml/dist/legacy-financing-0.1.0.dar")
    val inspection = artifacts.resolve("source-inspection.json")
    for
      lockText <- ArtifactFiles.read(source.resolve("identity.json"))
      lock <- IO.fromEither(io.circe.parser.parse(lockText))
      expectedDigest <- IO.fromEither(lock.hcursor.get[String]("dar_sha256"))
      expectedPackage <- IO.fromEither(lock.hcursor.get[String]("package_id"))
      digest <- IO.blocking {
        MessageDigest
          .getInstance("SHA-256")
          .digest(Files.readAllBytes(dar))
          .map(byte => f"${byte & 0xff}%02x")
          .mkString
      }
      _ <- IO.raiseUnless(digest == expectedDigest)(
        RuntimeException(
          s"Source DAR changed: expected $expectedDigest, found $digest. Review the source and its identity explicitly."
        )
      )
      _ <- ManagedProcess.run(
        List(root.resolve("scripts/daml").toString, "damlc", "inspect-dar", dar.toString, "--json"),
        root,
        inspection
      )
      observedText <- ArtifactFiles.read(inspection)
      observed <- IO.fromEither(io.circe.parser.parse(observedText))
      actualPackage <- IO.fromEither(observed.hcursor.get[String]("main_package_id"))
      _ <- IO.raiseUnless(actualPackage == expectedPackage)(
        RuntimeException("Source package identity changed")
      )
      packages <- IO.fromEither(observed.hcursor.get[Map[String, Json]]("packages"))
      _ <- IO.raiseWhen(
        packages.values.exists(
          _.hcursor.get[String]("name").toOption.exists(_.startsWith("harmonia-"))
        )
      )(
        RuntimeException("Unchanged source must not depend on Harmonia packages")
      )
    yield ()

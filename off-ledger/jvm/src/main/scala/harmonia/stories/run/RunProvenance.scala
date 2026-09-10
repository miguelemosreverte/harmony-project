package harmonia.stories.run

import cats.effect.IO
import harmonia.files.ArtifactFiles
import harmonia.processes.ManagedProcess
import io.circe.Json
import java.nio.file.{Files, Path}
import java.security.MessageDigest

object RunProvenance:
  def write(
      root: Path,
      input: Path,
      expected: Path,
      dar: Path,
      artifacts: Path,
      matched: Boolean
  ): IO[Unit] = for
    _ <- ManagedProcess.run(
      List("git", "rev-parse", "HEAD"),
      root,
      artifacts.resolve("revision.txt")
    )
    revision <- ArtifactFiles.read(artifacts.resolve("revision.txt"))
    _ <- ManagedProcess.run(
      List("git", "status", "--porcelain"),
      root,
      artifacts.resolve("worktree.txt")
    )
    worktree <- ArtifactFiles.read(artifacts.resolve("worktree.txt"))
    inputHash <- digest(input)
    expectedHash <- digest(expected)
    darHash <- digest(dar)
    actualHash <- digest(artifacts.resolve("actual.md"))
    observationHash <- digest(artifacts.resolve("observation.json"))
    timestamp <- IO.realTimeInstant
    _ <- ArtifactFiles.write(
      artifacts.resolve("run.json"),
      Json
        .obj(
          "mode" -> Json.fromString("live-canton"),
          "topology" -> Json.fromString(
            "one participant, one synchronizer, separate party identities"
          ),
          "revision" -> Json.fromString(revision.trim),
          "worktree_dirty" -> Json.fromBoolean(worktree.nonEmpty),
          "recorded_at" -> Json.fromString(timestamp.toString),
          "story_format" -> Json.fromInt(1),
          "java" -> Json.fromString(Runtime.version().toString),
          "configured_sdk" -> Json.fromString("3.4.11"),
          "input_sha256" -> Json.fromString(inputHash),
          "expected_sha256" -> Json.fromString(expectedHash),
          "dar_sha256" -> Json.fromString(darHash),
          "actual_sha256" -> Json.fromString(actualHash),
          "observation_sha256" -> Json.fromString(observationHash),
          "matched" -> Json.fromBoolean(matched)
        )
        .spaces2 + "\n"
    )
  yield ()

  private def digest(path: Path): IO[String] = IO.blocking {
    MessageDigest
      .getInstance("SHA-256")
      .digest(Files.readAllBytes(path))
      .map(byte => f"${byte & 0xff}%02x")
      .mkString
  }

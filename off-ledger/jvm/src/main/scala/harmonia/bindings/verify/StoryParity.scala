package harmonia.bindings.verify

import cats.effect.IO
import cats.syntax.all.*
import harmonia.files.ArtifactFiles
import harmonia.stories.read.{MarkdownYaml, StoryFormat}
import harmonia.stories.compare.CompareResults
import io.circe.Json
import java.nio.file.Path

object StoryParity:
  private val pairs = Vector("approved", "rejected")

  def verify(artifacts: Path, requireAll: Boolean): IO[Unit] = for
    checked <- pairs.traverse { suffix =>
      val direct = artifacts.resolve(s"workflow-$suffix")
      val generated = artifacts.resolve(s"generated-$suffix")
      for
        leftExists <- ArtifactFiles.exists(direct.resolve("actual.md"))
        rightExists <- ArtifactFiles.exists(generated.resolve("actual.md"))
        available = leftExists && rightExists
        _ <- IO.raiseWhen(requireAll && !available)(
          RuntimeException(s"The regular suite requires both $suffix participation paths")
        )
        result <-
          if !available then IO.pure(None)
          else
            for
              directInput <- scenario(direct.resolve("input.md"))
              generatedInput <- scenario(generated.resolve("input.md"))
              _ <- IO.raiseUnless(directInput == generatedInput.mapObject(_.remove("integration")))(
                RuntimeException(
                  s"$suffix parity stories must differ only in their integration path"
                )
              )
              directActual <- readResult(direct.resolve("actual.md"))
              generatedActual <- readResult(generated.resolve("actual.md"))
              differences = CompareResults.compare(
                directActual,
                generatedActual.mapObject(_.remove("integration"))
              )
              _ <- ArtifactFiles.write(
                artifacts.resolve(s"parity-$suffix.md"),
                CompareResults.markdown(differences)
              )
              _ <- IO.raiseWhen(differences.nonEmpty)(
                RuntimeException(s"Direct/generated $suffix business outcomes diverged")
              )
            yield Some(
              s"$suffix: identical business outcomes, permission results, and visibility; generated contract counts remain explicit"
            )
      yield result
    }
    _ <- ArtifactFiles.write(
      artifacts.resolve("parity.md"),
      "# Participation parity\n\n" + checked.flatten.map(value => s"- $value").mkString("\n") + "\n"
    )
    _ <-
      if checked.flatten.nonEmpty then
        IO.println(s"PASS direct/generated parity for ${checked.flatten.size} scenarios")
      else IO.unit
  yield ()

  private def scenario(path: Path): IO[Json] = ArtifactFiles
    .read(path)
    .flatMap(text =>
      IO.fromEither(MarkdownYaml.read(text, "Scenario").left.map(RuntimeException(_)))
    )
  private def readResult(path: Path): IO[Json] = ArtifactFiles
    .read(path)
    .flatMap(text => IO.fromEither(StoryFormat.result(text).left.map(RuntimeException(_))))

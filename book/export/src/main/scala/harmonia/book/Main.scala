package harmonia.book

import cats.effect.{ExitCode, IO, IOApp}
import harmonia.files.ArtifactFiles
import java.nio.file.Path

object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    val root = Path.of(sys.env.getOrElse("HARMONIA_ROOT", ".")).toAbsolutePath.normalize()
    val operation = args match
      case List("serve-book", directory) => ServeBook.serve(root.resolve(directory))
      case List("export-book", run, output) =>
        ExportBook.write(root, root.resolve(run), root.resolve(output))
      case List("book-links", directory) => verify.CheckBookLinks.run(root, root.resolve(directory))
      case List("book", run) =>
        ArtifactFiles.createRun(root, "book").flatMap { output =>
          ExportBook.write(root, root.resolve(run), output) *> ServeBook.serve(output)
        }
      case _ =>
        IO.raiseError(
          IllegalArgumentException("Expected book, export-book, serve-book, or book-links")
        )
    operation.as(ExitCode.Success)

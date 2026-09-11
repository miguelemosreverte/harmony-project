package harmonia.book

import cats.effect.IO
import cats.syntax.all.*
import harmonia.files.ArtifactFiles
import java.nio.file.{Files, Path}

/** Embed a reviewed source specimen without keeping another copy in chapter prose. */
object CodeIncludes:
  private val directive = "(?m)^\\{\\{code: ([A-Za-z0-9_./-]+)\\}\\}$".r

  def expand(book: Path, markdown: String): IO[String] = for
    root <- IO.blocking(book.toRealPath())
    matches = directive.findAllMatchIn(markdown).toVector
    _ <- IO.raiseWhen(matches.size > 8)(
      RuntimeException("A chapter can include at most eight code specimens")
    )
    replacements <- matches.traverse { entry =>
      for
        path <- IO.blocking(root.resolve(entry.group(1)).toRealPath())
        _ <- IO.raiseUnless(
          path.startsWith(root) && Set("daml", "scala")(path.getFileName.toString.split('.').last)
        )(RuntimeException("Code specimen must be a Daml or Scala file inside the book"))
        size <- IO.blocking(Files.size(path))
        _ <- IO.raiseWhen(size > 65536)(RuntimeException("Code specimen exceeds 64 KiB"))
        text <- ArtifactFiles.read(path)
        fence = "`" * ("`+".r.findAllIn(text).map(_.length).foldLeft(3)(math.max) + 1)
        language = path.getFileName.toString.split('.').last
      yield (entry.start, entry.end, s"$fence$language\n$text\n$fence")
    }
  yield replacements.reverse.foldLeft(markdown) { case (text, (start, end, replacement)) =>
    text.take(start) + replacement + text.drop(end)
  }

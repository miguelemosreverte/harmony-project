package harmonia.book.verify

import cats.effect.IO
import cats.syntax.all.*
import harmonia.files.ArtifactFiles
import io.circe.Json
import java.net.URI
import java.nio.file.{Files, Path, FileVisitResult, SimpleFileVisitor}
import java.nio.file.attribute.BasicFileAttributes
import org.commonmark.node.{AbstractVisitor, Link, Image}
import org.commonmark.parser.Parser
import scala.collection.mutable.ArrayBuffer

object CheckBookLinks:
  def run(root: Path, book: Path): IO[Unit] = for
    evidence <- ArtifactFiles
      .read(book.resolve("evidence.json"))
      .flatMap(value => IO.fromEither(io.circe.parser.parse(value)))
    chapters <- IO.fromEither(evidence.hcursor.get[Vector[Json]]("chapters"))
    sourceProblems <- IO.blocking {
      val problems = ArrayBuffer.empty[String]
      val parser = Parser.builder().build()
      Files.walkFileTree(
        root,
        new SimpleFileVisitor[Path] {
          override def preVisitDirectory(
              path: Path,
              attributes: BasicFileAttributes
          ): FileVisitResult =
            if Set(".git", ".artifacts", ".daml", "target", "node_modules", ".bsp").contains(
                path.getFileName.toString
              )
            then FileVisitResult.SKIP_SUBTREE
            else FileVisitResult.CONTINUE
          override def visitFile(path: Path, attributes: BasicFileAttributes): FileVisitResult =
            if path.toString.endsWith(".md") && path != root.resolve("docs/proposal/harmonia.md")
            then
              parser
                .parse(Files.readString(path))
                .accept(new AbstractVisitor {
                  override def visit(link: Link): Unit =
                    check(path.getParent, link.getDestination, path.toString, problems)
                  override def visit(image: Image): Unit =
                    check(path.getParent, image.getDestination, path.toString, problems)
                })
            FileVisitResult.CONTINUE
        }
      )
      problems.toVector
    }
    bookProblems <- IO.blocking {
      val problems = ArrayBuffer.empty[String]
      val attribute = "(?:href|src)=\"([^\"]+)\"".r
      val html = Files.readString(book.resolve("index.html")) + chapters
        .map(_.hcursor.get[String]("html").getOrElse(""))
        .mkString
      attribute
        .findAllMatchIn(html)
        .foreach(m => check(book, m.group(1).replace("&amp;", "&"), "exported book", problems))
      Vector("main.js", "book.css", "evidence.json").foreach(name =>
        check(book, name, "book assets", problems)
      )
      evidence.hcursor.get[Vector[Json]]("stories").getOrElse(Vector.empty).foreach { story =>
        val id = story.hcursor.get[String]("id").toOption.get
        val operation =
          story.hcursor.downField("presentation").get[String]("operation").fold(throw _, identity)
        check(book, "source/" + operation, "recording operation", problems)
        Vector("input.md", "expected.md", "actual.md", "diff.md", "observation.json", "run.json")
          .foreach(name => check(book, s"evidence/$id/$name", "recorded story", problems))
      }
      problems.toVector
    }
    problems = sourceProblems ++ bookProblems
    _ <- IO.raiseWhen(problems.nonEmpty)(
      IllegalArgumentException(problems.mkString("Broken local links:\n", "\n", ""))
    )
    _ <- IO.println(
      s"PASS source/document links, ${chapters.size} exported chapters, all recording links, and browser assets"
    )
  yield ()

  private def check(
      base: Path,
      destination: String,
      owner: String,
      problems: ArrayBuffer[String]
  ): Unit =
    if destination.nonEmpty && !destination.startsWith("#") then
      try
        val uri = URI.create(destination)
        if !uri.isAbsolute then
          val path = base.resolve(uri.getPath).normalize()
          if !Files.exists(path) then problems += s"$owner -> $destination"
      catch case _: IllegalArgumentException => problems += s"$owner -> invalid URI $destination"

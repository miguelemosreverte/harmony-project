package harmonia.tools

import cats.effect.IO
import harmonia.book.{FieldGuide, ServeBook}
import harmonia.demo.Demo
import harmonia.files.ArtifactFiles
import harmonia.ledger.auth.LocalCredentials
import java.nio.file.Path

/** Development orchestration owns the relationship between two independent applications. */
object ReaderSandbox:
  def run(root: Path): IO[Unit] = for
    output <- ArtifactFiles.createRun(root, "field-guide")
    _ <- FieldGuide.write(root, output)
    _ <- Demo.viewer(root).use { (artifacts, product) =>
      val origin = s"http://127.0.0.1:${product.port}/"
      ArtifactFiles.write(
        output.resolve("source/design/0.2/context.js"),
        s"window.HarmoniaLiveRoot = '$origin';"
      ) *>
        ServeBook.resource(output).use { port =>
          val book = s"http://127.0.0.1:$port/"
          val pages = Vector("bank", "buyer", "reviewer")
          val writes = pages.zipWithIndex.foldLeft(IO.unit) { case (before, (actor, index)) =>
            val next = pages.lift(index + 1).map(_ + ".html").getOrElse("book.html")
            val purpose = Map(
              "bank" -> "Approve the private financing case.",
              "buyer" -> "Use the approval to continue.",
              "reviewer" -> "Observe the shared result."
            )(actor)
            val html = page(
              actor.capitalize,
              purpose,
              s"${origin}#session=${product.capabilities(actor)}",
              "Enter as " + actor.capitalize,
              next,
              if index < 2 then "Next participant →" else "Read the book →"
            )
            before *> LocalCredentials.privateWrite(artifacts.resolve(actor + ".html"), html)
          }
          writes *> LocalCredentials.privateWrite(
            artifacts.resolve("open.html"),
            page(
              "Make the handoff",
              "Start with Bank. Each participant uses a separate tab.",
              "bank.html",
              "Open the sandbox →",
              book,
              "Read the book →"
            )
          ) *>
            LocalCredentials.privateWrite(
              artifacts.resolve("book.html"),
              page(
                "The field guide",
                "Four ways to understand the product.",
                book,
                "Begin reading →",
                "bank.html",
                "Return to Bank →"
              )
            ) *>
            IO.println(
              s"Product: $origin\nBook: $book\nParticipant launcher: $artifacts/open.html"
            ) *> IO.never
        }
    }
  yield ()

  private def page(
      title: String,
      purpose: String,
      first: String,
      firstLabel: String,
      second: String,
      secondLabel: String
  ): String =
    s"""<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>$title · Harmonia</title><style>body{font:18px system-ui;max-width:650px;margin:8vh auto;padding:24px;color:#172638;background:#f5f7fa}a{display:block;padding:18px;margin:16px 0;background:white;border:1px solid #dce3eb;border-radius:12px;color:#275ee3;text-decoration:none}p{line-height:1.6}</style><main><p>HARMONIA · LOCAL SANDBOX</p><h1>$title</h1><p>$purpose</p><a target="_blank" rel="noopener" href="$first">$firstLabel</a><a href="$second">$secondLabel</a></main></html>"""

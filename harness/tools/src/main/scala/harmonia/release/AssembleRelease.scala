package harmonia.release

import cats.effect.IO
import cats.syntax.all.*
import harmonia.files.ArtifactFiles
import harmonia.examples.Examples
import java.nio.file.{Files, Path}

private[release] object AssembleRelease:
  def run(source: Path, bundle: Path, revision: String, work: Path): IO[Unit] = for
    _ <- ReleaseFiles.cloneAt(
      source,
      bundle.resolve("source"),
      revision,
      work.resolve("bundle-clone")
    )
    classpaths <- Vector("service", "bookExport", "tools").traverse { target =>
      ArtifactFiles
        .read(source.resolve(s".artifacts/classpaths/$target.txt"))
        .map(text => target -> text.trim.split(java.io.File.pathSeparator).toVector.map(Path.of(_)))
    }
    jars = classpaths.flatMap(_._2).distinct
    _ <- IO.raiseUnless(jars.map(_.getFileName).distinct.size == jars.size)(
      RuntimeException("Conflicting runtime JAR names")
    )
    _ <- jars.traverse_(p => ReleaseFiles.copy(p, bundle.resolve("lib").resolve(p.getFileName)))
    _ <- classpaths.traverse_ { (target, paths) =>
      ArtifactFiles.write(
        bundle.resolve(s"classpaths/$target.txt"),
        paths.map(p => "lib/" + p.getFileName).mkString(java.io.File.pathSeparator)
      )
    }
    _ <- Vector("product/ledger", "harness/ledger").traverse_ { folder =>
      ReleaseFiles.copySelected(source.resolve(folder), bundle.resolve("source").resolve(folder))(
        p => p.toString.contains("/.daml/dist/") && p.toString.endsWith(".dar")
      )
    }
    _ <- Vector(
      "product/web/target/scala-3.3.6/harmonia-web-fastopt/main.js",
      "book/browser/target/scala-3.3.6/harmonia-reader-fastopt/main.js"
    ).traverse_(js => ReleaseFiles.copy(source.resolve(js), bundle.resolve("source").resolve(js)))
    _ <- ReleaseFiles.copySelected(
      source.resolve(".artifacts/packages/cache"),
      bundle.resolve("source/.artifacts/packages/cache")
    )(
      _.getFileName.toString == "source.dar"
    )
    _ <- ReleaseFiles.copySelected(source.resolve(".artifacts"), bundle.resolve("evidence")) {
      path =>
        Set(
          "input.md",
          "expected.md",
          "actual.md",
          "diff.md",
          "observation.json",
          "run.json",
          "security-expected.md",
          "security-actual.md",
          "security-diff.md",
          "verification.json",
          "verification.md",
          "generation.json",
          "generation-baseline.json",
          "parity.md"
        ).contains(path.getFileName.toString)
    }
    _ <- ReleaseFiles.copySelected(work.resolve("checks"), bundle.resolve("checks"))(_ => true)
    _ <- Vector("product", "book", "live", "verify").traverse_(mode => launcher(bundle, mode))
    _ <- ArtifactFiles.write(
      bundle.resolve("README.md"),
      s"""# Harmonia local evaluation

Source revision: `$revision`. This bundle contains ${Examples.chapters.size} chapters, ${Examples.all.size} fresh recordings, the compiled Scala application, Daml packages, source history, and retained verification evidence.

- `./run-product serve /absolute/configuration.json`: run the product against configured local participants; see `source/product/README.md`. Its classpath excludes book and harness JARs.
- `./run-book`: open the printed local URL. Java 17 is sufficient; the book needs no ledger.
- `./run-live`: start disposable authenticated participant sessions. Install Daml SDK 3.4.11 first; see `source/book/setup.md`. Ctrl-C releases the network.
- `./run-verify`: verify payload hashes, source identity, recording provenance, and local book links.
- `cd source && scripts/check`: rebuild and rerun the complete sequential suite with Java 17, Daml SDK 3.4.11, sbt, ripgrep, and network access for missing dependencies.

`manifest.json` records the tested platform and checks. `checks/` holds their logs; `evidence/` holds selected raw results, comparisons, and generation records. `book/evidence/` contains the reader-facing recordings. Historical absolute paths in observations identify where execution occurred; use the corresponding bundled files for offline inspection.

Recorded playback starts with a 128 MiB heap cap; live tools use 512 MiB, Canton 2 GiB, and Daml Script 512 MiB. One ledger environment may run per workspace. This is a local reference implementation; see `source/docs/capabilities.md` and `source/docs/compatibility.md` for demonstrated limits.

The clean checkout reused installed tools and dependency caches. Archive timestamps and run metadata vary; generated sources and DAR identities have separate determinism checks. Public repository, project license, external evaluation, and adoption remain pending. This archive is a local evaluation delivery, not a public distribution authorization.
"""
    )
    _ <- ReleaseFiles.copy(
      source.resolve("docs/release/changelog.md"),
      bundle.resolve("CHANGELOG.md")
    )
  yield ()

  private def launcher(bundle: Path, mode: String): IO[Unit] =
    val (target, main, args) = mode match
      case "product" => ("service", "harmonia.app.Main", "\"$@\"")
      case "book"    => ("bookExport", "harmonia.book.Main", "serve-book \"$harmonia_bundle/book\"")
      case "live"    => ("tools", "harmonia.tools.Main", "live")
      case "verify"  => ("tools", "harmonia.tools.Main", "release-verify \"$harmonia_bundle\"")
    val heap = if mode == "live" || mode == "product" then "512m" else "128m"
    val path = bundle.resolve(s"run-$mode")
    ArtifactFiles.write(
      path,
      s"""#!/usr/bin/env bash
set -euo pipefail
harmonia_bundle="$$(cd "$$(dirname "$${BASH_SOURCE[0]}")" && pwd)"
export HARMONIA_ROOT="$$harmonia_bundle/source"
if [[ -z "$${JAVA_HOME:-}" && "$$(uname -s)" == Darwin ]]; then
  export JAVA_HOME="$$(/usr/libexec/java_home -v 17)"
fi
cd "$$harmonia_bundle"
exec "$${JAVA_HOME:-/usr}/bin/java" -Xms32m -Xmx$heap -XX:ActiveProcessorCount=4 -cp "$$(cat "$$harmonia_bundle/classpaths/$target.txt")" $main $args
"""
    ) *> IO.blocking {
      Files.setPosixFilePermissions(
        path,
        java.nio.file.attribute.PosixFilePermissions.fromString("rwxr-xr-x")
      ); ()
    }

package harmonia.release

import cats.effect.IO
import cats.syntax.all.*
import harmonia.files.ArtifactFiles
import java.nio.file.{Files, Path}

private[release] object AssembleRelease:
  def run(source: Path, bundle: Path, revision: String, work: Path): IO[Unit] = for
    _ <- ReleaseFiles.cloneAt(
      source,
      bundle.resolve("source"),
      revision,
      work.resolve("bundle-clone")
    )
    classpath <- ArtifactFiles.read(source.resolve("off-ledger/target/runtime-classpath"))
    jars = classpath.trim.split(java.io.File.pathSeparator).toVector.map(Path.of(_))
    _ <- IO.raiseUnless(jars.map(_.getFileName).distinct.size == jars.size)(
      RuntimeException("Duplicate runtime JAR names")
    )
    _ <- jars.traverse_(p => ReleaseFiles.copy(p, bundle.resolve("lib").resolve(p.getFileName)))
    _ <- ReleaseFiles.copySelected(source.resolve("on-ledger"), bundle.resolve("source/on-ledger"))(
      p => p.toString.contains("/.daml/dist/") && p.toString.endsWith(".dar")
    )
    js = Path.of("off-ledger/browser/target/scala-3.3.6/harmonia-book-fastopt/main.js")
    _ <- ReleaseFiles.copy(source.resolve(js), bundle.resolve("source").resolve(js))
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
    _ <- Vector("book", "live", "verify").traverse_(mode => launcher(bundle, mode))
    _ <- ArtifactFiles.write(
      bundle.resolve("README.md"),
      s"""# Harmonia local evaluation

Source revision: `$revision`. This bundle contains nine chapters, 32 fresh recordings, the compiled Scala application, Daml packages, source history, and retained verification evidence.

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
    val args = mode match
      case "book"   => "serve-book \"$harmonia_bundle/book\""
      case "live"   => "live"
      case "verify" => "release-verify \"$harmonia_bundle\""
    val heap = if mode == "live" then "512m" else "128m"
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
exec "$${JAVA_HOME:-/usr}/bin/java" -Xms32m -Xmx$heap -XX:ActiveProcessorCount=4 -cp "$$harmonia_bundle/lib/*" harmonia.app.Main $args
"""
    ) *> IO.blocking {
      Files.setPosixFilePermissions(
        path,
        java.nio.file.attribute.PosixFilePermissions.fromString("rwxr-xr-x")
      ); ()
    }

# Build a local evaluation bundle

From a clean committed checkout, run:

```sh
scripts/harmonia release-check
```

The command uses a 128 MiB orchestration JVM. It clones the exact source revision into an owned artifact directory, builds from empty project build directories, and runs the same ordered stages as `scripts/check`. Stages execute sequentially and stop on the first failure. Each ledger environment closes before the next starts. The existing installed SDK and dependency caches are reused; this does not claim a fresh operating-system installation or production-scale testing.

Only after all stages pass does packaging collect 32 matching recordings from that revision. It creates a separate source checkout, compiled runtime JARs and DARs, browser assets, pinned input DARs, the exported book, stage logs, and selected verification files. Temporary ledger configuration, HMAC keys, participant capability links, process logs, and participant databases stay in the private build directory. The bundle's source history has no remote configured.

The output directory has `run-product`, `run-book`, `run-live`, and `run-verify` launchers, `README.md`, `CHANGELOG.md`, and a SHA-256 manifest. A compressed archive and its separate digest sit beside it. Recorded playback requires Java 17; live evaluation also requires the pinned SDK. Source rebuilds require the tools in [setup](../../book/setup.md). Native launchers target macOS/Linux; the currently tested platform is recorded in the manifest.

The manifest identifies the exact software, chapter, and recording revision. File hashes detect payload changes; recorded artifacts retain their own hashes and source provenance. The archive itself is an execution artifact: timestamps, allocated parties, contract IDs, and run metadata vary. Deterministic generated sources and DARs are checked separately against pinned identities. Rebuilding requires dependency access when caches are empty.

Use `run-verify` after unpacking. It verifies every declared payload hash, the clean source revision, all 32 passing recording revisions, nine chapters, and local source/book links. Files produced by subsequent local executions under `source/.artifacts` and Git's mutable metadata are outside the payload hash set.

The source checkout keeps full history for review, including earlier verification notes whose absolute paths describe past local runs. The new bundle's `checks`, `evidence`, and `book/evidence` directories are the current release evidence. Public distribution awaits an explicit project license and publication destination; upstream package identity does not imply permission to redistribute it.

Each launcher selects its own relative classpath from `classpaths/`. The product classpath excludes book and harness JARs. `run-product serve /absolute/configuration.json` connects to existing local participants; `run-live` provisions the disposable demonstration separately. See the [product configuration](../../product/README.md).

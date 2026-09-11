# Run the local baseline

The first working version creates a financing application, verifies that its buyer cannot approve it, and lets the bank approve it. The final contract is queried under both party identities.

## Requirements

- macOS or Linux with Java 17 or later and sbt available.
- Daml SDK **3.4.11**, including its Canton runtime, compiler, and script runner.
- Scala **3.3.6**, Cats Effect **3.6.3**, and sbt **1.12.5** are pinned in the build and resolved by sbt.

The launcher looks for the Daml assistant at `~/.daml/bin/daml`. Set `HARMONIA_DAML` if it is installed elsewhere. With an installed assistant, run `daml install 3.4.11` to add the pinned SDK. This baseline uses the assistant bundled with the 3.4 SDK; migration to newer DPM releases is a separately tested toolchain change.

## Build and execute

From the repository root:

```sh
scripts/build
scripts/harmonia smoke
scripts/harmonia check
```

The Scala program starts an isolated Canton sandbox on dynamically assigned ports, uploads the sample DAR, runs Daml Script against its real Ledger API, and prints the queried result. It owns and stops the sandbox when the run ends. Run it again to start a fresh local ledger.

Each run retains `canton.log`, `script.log`, and `observation.json` in its printed `.artifacts/smoke-.../` directory. That directory is ignored by Git. An error points to its evidence rather than being reported as a successful business outcome.

## What this proves

This is a real local Canton transaction with an application authorization check. Both parties are hosted by one local participant. It is not yet the reusable Harmonia core, a cross-application workflow, or evidence of privacy between separately operated participants. Those capabilities have their own later acceptance gates.

The [first story chapter](01-first-story.md) supplies execution from readable Markdown and compares its observed result with a committed golden. Run `scripts/check` to build, run focused tests, and execute the complete current story collection.

## Separate participants

`scripts/harmonia network-smoke` starts three independently identified participant nodes on a common local synchronizer, uploads the story DAR, checks connectivity, and stops the owned nodes. The generated configuration and node IDs remain in the printed evidence directory. `scripts/harmonia check stories/private-approval` exercises the authority and privacy story in that topology.

## Reproduce package inputs

The build resolves the [committed package manifest](../packages/inputs.md). Its first run downloads a small upstream Splice metadata DAR pinned to a full commit and SHA-256; later runs verify cached bytes. The local legacy DAR is built first and checked against its committed identity. See [package setup and failure diagnostics](../packages/README.md) for offline copies and supported inputs.

`scripts/harmonia packages-check` builds the mixed-version import example, compares two real executions with its Markdown expectation, and verifies administrator DAR retrieval. `scripts/check` includes this verification. Its local participant administrator endpoints are part of the disposable test environment; production business credentials are a separate boundary.

## Memory and process ownership

The launcher compiles an immutable application JAR when sources change, then exits sbt before starting the command. Repeated commands with unchanged sources launch Java directly. Build and verification stages run sequentially.

| Process | Initial heap | Maximum heap | Lifetime |
| --- | --- | --- | --- |
| sbt compiler | 128 MiB | 1 GiB | Build or focused tests |
| Scala command | 32 MiB | 512 MiB | One command or live server |
| Canton, including up to four participants | 128 MiB | 2 GiB | One owned ledger environment |
| Daml Script runner | 32 MiB | 512 MiB | One script |

These are Java heap limits, not total process memory: native buffers, code, thread stacks, and JVM metadata also use RAM. Each JVM is limited to four visible processors to bound worker creation. A workspace lease rejects a second live demo or ledger check while the first owns its network. Stop the live demo with Ctrl-C before checking another ledger story. Ordinary cancellation closes owned child processes and the lease. Forced termination of the owner with SIGKILL cannot run cleanup.

Keep one book preview open and replace it when changing the exported book. For a small preview with a stable address:

```sh
HARMONIA_TOOLS_HEAP=128m HARMONIA_BOOK_PORT=56007 scripts/harmonia serve-book .artifacts/book-purchase
```

Omit `HARMONIA_BOOK_PORT` to choose an available port. `HARMONIA_TOOLS_HEAP` and `HARMONIA_CANTON_HEAP` override the respective maximum heaps for an explicit local experiment. The default four-participant transfer was verified with the limits above; see [memory verification](../docs/verification/15a-memory.md).

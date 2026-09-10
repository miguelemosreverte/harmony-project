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
```

The Scala program starts an isolated Canton sandbox on dynamically assigned ports, uploads the sample DAR, runs Daml Script against its real Ledger API, and prints the queried result. It owns and stops the sandbox when the run ends. Run it again to start a fresh local ledger.

Each run retains `canton.log`, `script.log`, and `observation.json` in its printed `.artifacts/smoke-.../` directory. That directory is ignored by Git. An error points to its evidence rather than being reported as a successful business outcome.

## What this proves

This is a real local Canton transaction with an application authorization check. Both parties are hosted by one local participant. It is not yet the reusable Harmonia core, a cross-application workflow, or evidence of privacy between separately operated participants. Those capabilities have their own later acceptance gates.

The next increment will supply this execution from a readable Markdown story and compare its observed result with a committed golden.

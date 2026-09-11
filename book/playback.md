# Explore a recorded execution

The browser edition lets you read nine chapters and walk through their actual execution evidence. The application is written in Scala.js. It reads a bundle exported by the Scala/JVM tools; it does not simulate application rules or infer success from an expected file.

## Open the book

Build and run the current stories:

```sh
scripts/build
scripts/harmonia check
```

The check prints its evidence directory. Open that exact run, replacing the example name below:

```sh
scripts/book .artifacts/check-RUN
```

Open the local URL printed by the command. Ctrl-C stops the book server. The server binds only to the local machine and serves the generated book directory. No ledger is started for playback.

To retain a standalone bundle without starting a server:

```sh
scripts/harmonia export-book .artifacts/check-RUN .artifacts/my-book
scripts/harmonia serve-book .artifacts/my-book
```

The bundle contains the compiled browser application, chapters, relevant source files, and selected evidence. It can also be served by an ordinary static HTTP server. No CDN or external font is required.

## Take a short walkthrough

1. Choose **Workflow approval**. The first attempt is Alice's rejected approval; the application is still pending.
2. Select **Next attempt**. Northbank's approval produces an approved application and a completed workflow.
3. Compare the committed expectation and ledger observation in the detail table. Contract consumption, active application count, and visibility appear alongside the workflow status.
4. Open the input, expectation, actual result, diff, or raw observations from the evidence panel. The provenance link identifies the recording's source revision, worktree state, topology, and hashes.
5. Read a chapter using the navigation, then use its recorded-example buttons to return directly to the relevant story. Source links point to files included with this edition.

## Explore a failing comparison

Create a copy of `stories/workflow-approved/` at `.artifacts/experiments/wrong-workflow/`. In the copied `expected.md`, change the successful action's `workflow: complete` to `workflow: waiting`. This is the `wrong-workflow` experiment: its input performs a valid bank approval, but its copied expectation says the workflow should remain waiting. Run the normal story and that experiment together:

```sh
scripts/harmonia check stories/workflow-approved .artifacts/experiments/wrong-workflow
```

The command should return a failure because the copied expectation is intentionally wrong. Open the printed run with `scripts/book`. Choose the regression experiment. The workflow row shows **waiting** as the expectation and **complete** as the observation; the complete diff remains available below it.

A passing badge means that the recording matches its accompanying baseline. It does not mean that playback has just contacted a ledger. The interface labels every run as recorded evidence.

## Preserve the connection to the original run

New runs save copies of the exact authored input and expectation used during execution. The exporter verifies those copies, the normalized actual result, and raw observations against the recorded hashes. If one changes, export fails. Older runs created before these snapshots were introduced must be rerun before export.

The JVM and browser compile the same pure structural comparator from `off-ledger/shared/`. YAML parsing, filesystem access, and ledger integration stay on the JVM. The browser owns presentation and selection state.

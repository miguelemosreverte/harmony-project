# A story the ledger can prove

Alice needs Northbank to approve her financing application. The business rule is simple: Alice can see the application, but only the bank can approve it.

Read the [story input](../examples/stories/financing-approved/input.md) and [committed expectation](../examples/stories/financing-approved/expected.md). They describe two attempts in order: Alice tries first, then Northbank.

## Run the story

After [setup](setup.md), run:

```sh
scripts/harmonia check examples/stories/financing-approved
```

The Scala runner creates a local Canton environment, translates the input into a Daml Script argument, and submits the attempts. The script queries the contracts after each attempt. The comparator then checks those observations against the expected file.

```mermaid
flowchart LR
  pending[Pending application] --> attempt[Alice attempts approval]
  attempt --> rejected[Rejected: application remains pending]
  rejected --> bank[Northbank approves]
  bank --> approved[Approved application]
```

This diagram explains the committed scenario. The run's `actual.md` records what the ledger actually did; `diff.md` records any disagreement.

## Read the evidence

The terminal prints the run directory. Inside the story's subdirectory:

| File | What it tells you |
| --- | --- |
| `input.json` | The argument sent to the Daml script, derived solely from the Markdown input |
| `observation.json` | Raw script observations, including real contract identifiers and rejection details |
| `actual.md` | The normalized, human-readable result |
| `diff.md` | The fields where expectation and observation disagree |
| `run.json` | Execution mode, source revision, worktree state, toolchain information, and artifact hashes |

In `actual.md`, a rejected attempt should leave the input contract unconsumed. Successful approval should consume the pending contract and leave one approved application visible to Alice and Northbank.

The [already-approved story](../examples/stories/already-approved/input.md) tests another boundary: bank authority alone is insufficient when the application is already approved.

## Try a disagreement

Copy the story to a new directory under `.artifacts/`, preserving its `input.md` and `expected.md`. In the copied expectation, change the bank attempt's expected application state from `approved` to `pending`.

Run `scripts/harmonia check` with that copied directory as its argument. The action should still approve the application, but the check must fail and identify the differing field. The command does not update the golden to make the test pass.

Editing only the explanatory prose leaves the scenario unchanged. Adding an unsupported action or unknown actor fails validation before ledger execution.

## Follow the code

- [Financing application](../on-ledger/applications/financing/daml/Financing.daml): its signatory, observer, controller, and transition condition.
- [Ledger-driving script](../on-ledger/tests/daml/Story.daml): submits actions and queries their effects.
- [Scala execution slice](../off-ledger/jvm/src/main/scala/harmonia/stories/run/RunStory.scala): translates input and retains observations without receiving the expectation.
- [Pure comparison](../off-ledger/shared/src/main/scala/harmonia/stories/compare/CompareResults.scala): checks complete result structures and reports differences.

## Let the workflow advance with the application

The [workflow story](../examples/stories/workflow-approved/input.md) adds one line, `workflow: approval`, to select the core-managed path. Its [expectation](../examples/stories/workflow-approved/expected.md) observes both application and workflow state.

```sh
scripts/harmonia check examples/stories/workflow-approved examples/stories/workflow-rejected
```

Alice's attempt leaves the workflow waiting. Northbank's approval changes the application to approved and the workflow to complete in a single transaction. The core calls a common action interface; the financing application supplies its implementation and retains its own approval rule.

The [rollback story](../examples/stories/workflow-rejected/input.md) starts with an already-approved application. Even Northbank cannot approve it again. The failure leaves that application active and the workflow waiting. Daml rolls back the consuming workflow exercise with the failed child action.

Follow the [common interface](../on-ledger/interfaces/daml/Harmonia/Action.daml), [core transition](../on-ledger/core/daml/Harmonia/Workflow.daml), and financing implementation above. Neither the common interface nor the core imports the financing application.

These examples run on one local participant. Separate-participant privacy requires a different topology and its own observations. An adapter for an unchanged application is the next integration experiment.

## Find the live operation

The private handoff in [Chapter 3](03-participant-views.md) uses the same approval idea with separate participant sessions. Its Scala entry point is [the financing operation](../off-ledger/jvm/src/main/scala/harmonia/financing/Financing.scala). It accepts a [typed financing action and observation](../off-ledger/shared/src/main/scala/harmonia/financing/FinancingState.scala); the authenticated ledger client submits the actual Daml choice. [Workspace command decoding](../off-ledger/shared/src/main/scala/harmonia/workspace/WorkspaceCommand.scala) translates external action names at the API boundary.

# Third-commit golden proof

Verified locally on 2026-09-10, using the pinned runtime described in the [baseline report](02-runtime.md).

## Passing observations

`scripts/harmonia check` executed two stories against an actual Canton sandbox and exited successfully:

- `already-approved`: the bank's repeated approval was rejected, the contract remained active and unconsumed, and no second application appeared.
- `financing-approved`: the buyer's attempt was rejected without a state change; the bank's attempt consumed the pending application and produced the approved application.

Both results matched their independently authored Markdown expectations with zero differences. Evidence from the first fully passing run is retained locally in `.artifacts/check-11659292359615719296/`.

## Deliberate regression

An isolated copy of the financing story was placed in `.artifacts/experiments/wrong-expectation/`. Only the expected final application status was changed from `approved` to `pending`. The committed story files were untouched.

The live command exited with status **1** and retained this exact difference:

| Field | Expected | Actual |
| --- | --- | --- |
| `$.actions[1].application` | `pending` | `approved` |

Evidence is in `.artifacts/check-6028100898739911627/wrong-expectation/`, including the actual result, diff, raw ledger observations, and run provenance.

## Focused checks

Ten Scala tests passed, covering prose independence, ambiguous Markdown blocks, duplicate YAML fields, unknown fields/actors/actions, custom tags, YAML indirection, non-text keys, meaningful action order, unordered visibility, numeric/string serialization, and unexpected effects. Some tests cover multiple related assertions.

The actual runner receives only the parsed story. The expected result enters the pure comparator after execution. Runtime errors outside the explicitly recognized business rejection categories abort the run.

## Scope

The proof demonstrates source-application behavior and the Markdown golden pipeline on a real local ledger. It does not yet demonstrate the reusable Harmonia interface/core or privacy across separate participant nodes. The next commits implement those independent requirements.

The aggregate `scripts/check` passed, including both builds, Daml checks, all ten Scala tests, and both live goldens. Its live evidence is in `.artifacts/check-2336112448916527918/`.

Run `scripts/check` to rebuild and repeat the current suite. Local run artifacts are intentionally ignored by Git; the committed inputs, expectations, code, and commands make the experiment reproducible.

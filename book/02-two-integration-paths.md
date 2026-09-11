# Two ways an application can participate

Northbank already owns an approval application. Harmonia needs to coordinate its action without owning the application's business rules.

If the application can be developed with Harmonia in mind, it implements the common action interface. [Chapter 1](01-first-story.md) follows this direct path. If the application already exists as a DAR, a separate typed adapter implements the interface and calls the original application choice.

## Compare the stories

| Path | Input | Committed expectation |
| --- | --- | --- |
| Direct | [Workflow approval](../examples/stories/workflow-approved/input.md) | [Result](../examples/stories/workflow-approved/expected.md) |
| Adapter | [Adapted approval](../examples/stories/adapter-approved/input.md) | [Result](../examples/stories/adapter-approved/expected.md) |
| Adapter failure | [Already approved](../examples/stories/adapter-rejected/input.md) | [Rollback](../examples/stories/adapter-rejected/expected.md) |

```sh
scripts/harmonia check examples/stories/workflow-approved examples/stories/adapter-approved examples/stories/adapter-rejected
```

The adapter input adds `integration: adapter`. The business observations are the same: the buyer's attempt is rejected, the bank's valid action approves the application and completes the workflow, and a failed application action preserves the waiting workflow.

## Inspect what changed

The [source application](../product/ledger/applications/legacy-financing/daml/LegacyFinancing.daml) contains no Harmonia imports. Its [identity record](../product/ledger/applications/legacy-financing/identity.json) pins the artifact that the suite checks. The [adapter](../product/ledger/bindings/daml/FinancingBinding.daml) knows the source template and choice; the [core](../product/ledger/core/daml/Harmonia/Workflow.daml) only knows the common action interface.

The adapter has a contract of its own, pointing to the source contract. Approval replaces both contracts. A workflow failure replaces neither. The golden counts application contracts; raw observations identify the source contracts used during execution.

## Try an experiment

Copy an adapter story under `.artifacts/`, change the acting party, and run the copy. An assignment cannot override the source application's approval rule. Keep the expected file unchanged to see exactly how the result differs.

The [architecture decision](../docs/architecture/001-application-integration.md) explains package ownership, consuming behavior, and the currently supported binding shape. Generating this adapter automatically is a later step, after this hand-written pattern is proven.

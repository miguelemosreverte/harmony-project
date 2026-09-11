# Direct-interface workflow proof

Verified locally on 2026-09-10 with `scripts/check`. All four live Canton goldens, the Daml smoke checks, and ten Scala tests passed. Evidence: `.artifacts/check-16254007642544499401/`.

The two new stories observe the shared workflow alongside its independent application:

- `workflow-approved`: the buyer cannot advance the bank's step; the bank approves the application and completes the workflow in the same transaction.
- `workflow-rejected`: the already-approved application rejects another approval; the consuming workflow advance rolls back, leaving the original application active and the workflow waiting.

A copy under `.artifacts/experiments/wrong-workflow/` expects the successful workflow to remain waiting. The live run exited with status 1 at `$.actions[1].workflow`, expecting `waiting` and observing `complete`. Its evidence is in `.artifacts/check-8395785116419346857/`.

The common API and core are separate packages. The core imports only the API. Financing independently implements that API and retains its controller and application-specific validation. `ExecuteAction` is nonconsuming because its implementation delegates consumption to the actual application choice.

This increment proves one assigned transition on one participant. General graph execution, independent workflow continuation, and separate-participant privacy remain later acceptance items.

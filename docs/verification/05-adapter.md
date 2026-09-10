# Unchanged-source adapter proof

The source application was compiled before the adapter on 2026-09-10. Its identity was recorded in `on-ledger/legacy-financing/identity.json`. Subsequent builds reproduced the same DAR SHA-256 and package ID. Package inspection found no Harmonia dependency in that source DAR.

`scripts/check` passed all six live Canton stories, Daml smoke checks, and ten Scala checks. Evidence: `.artifacts/check-8497592380592558092/`. The suite checks the source identity before and after execution; `source-inspection.json` retains the inspected package contents.

The added `adapter-approved` and `adapter-rejected` stories have the same business expectations as their direct-interface counterparts. The buyer's attempt fails, the bank's valid approval completes the workflow, and a source validation failure leaves the original source contract active and the workflow waiting. Raw observations identify the actual legacy source contracts.

The [architecture decision](../architecture/001-application-integration.md) records the dependency graph and supported binding shape. [Chapter 2](../../book/02-two-integration-paths.md) provides the reader's walkthrough. Automatic adapter generation is a later increment.

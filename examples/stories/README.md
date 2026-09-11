# Executable stories

Each directory contains a human-authored `input.md` and `expected.md`. The directory names the story. `format.yaml` records the format version once for this collection.

The first supported setup is one financing application with distinct bank and buyer parties and an explicit `pending` or `approved` state. `actions` is an ordered list of uniquely named attempts; the initial action vocabulary is `approve-financing`. Each actor must name a party in the setup. A story contains between one and 32 actions.

The expected result records the observed outcome and application state after every attempt. `consumed` says whether that attempt's input contract was archived; `active_contracts` counts the bank's active application contracts. `visible_to` names the parties that successfully queried the resulting/current contract. It is an unordered set; action order is meaningful.

A rejection includes `reason`. Unknown transport/ledger errors fail execution instead of becoming an expected rejection. The initial observation scope is the financing application template and its two parties, on one local participant; it makes no separate-participant privacy claim.

```sh
scripts/harmonia check
scripts/harmonia check stories/financing-approved
```

The Scala runner parses the designated Markdown/YAML blocks and validates their fields before starting Canton. It translates only the input to a Daml Script argument. That script submits real commands and queries their effects. Expected files are read by the comparator, never by the execution program.

Runs write raw observations, normalized `actual.md`, `diff.md`, and `run.json` provenance under `.artifacts/`. The command exits unsuccessfully on a mismatch and never rewrites expectations. Review intentional changes by editing the expected file and rerunning; there is no automatic baseline-acceptance command.

The optional scenario field `workflow: approval` runs the financing action through the Harmonia core. Without it, the story exercises the source application directly. Workflow results add a `workflow` status beside the observed application status.

An approval workflow can select `integration: adapter` to exercise the unchanged legacy application through a typed binding. Omitting the field selects the direct interface implementation. Both paths observe the same business fields.

The `private-approval` workflow adds a reviewer and synthetic private details to the application setup. `approve-financing` issues a signed result; `publish-approval` asks the buyer-owned shared workflow to consume it. Its result adds a `visibility` mapping of party-specific queries and observed event counts.

The `sequential-approval` workflow adds an independent buyer review. A `request` identifier makes retries explicit; otherwise the action ID is used. Its result reports the published definition version, review state, and completed/enabled steps. `observed` denotes a read with no transition, and `duplicate` denotes an acknowledged request with no repeated application effect.

The `branching-approval` workflow adds `choose-approve`, `choose-decline`, `close-application`, and `complete-join`. It supports the same explicit request receipts. Results add `branch`, `closure`, and `skipped`. The active application count covers financing plus the two review/closure contracts; `consumed` continues to describe the financing contract. Selecting a branch alone performs no financing action. Unselected contracts remain unchanged, and skipped workflow steps are distinct from completed steps.

The `property-purchase` workflow has four distinct parties. Its application setup adds `documents` and the bank's selected `bank_decision`; its offer setup names `buyer_agent`, `seller_agent`, `property`, and the evidence fixture. `financing` binds the actual produced result. The explicit invalid fixtures create signed test results with a wrong issuer, subject, buyer, or continuation; `missing` deliberately passes no result.

Purchase results distinguish the current offer/proposal from the total active proposal count. `evidence_available` asks whether the bound result is still active; it does not count every result visible to the buyer. Completed steps are queried from the domain workflow instances. `active_contracts` covers financing applications, offers, and proposals; `consumed` still refers to the financing application. Participant visibility covers the private financing module and offer template. The reuse story prepares a second current offer while retaining the first proposal on the ledger.

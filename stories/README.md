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

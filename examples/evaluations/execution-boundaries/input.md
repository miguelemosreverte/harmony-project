# Execute the supported limits and compete for one step

The ledger script exercises the actual core and composer bounds. A separate
authenticated network then submits two distinct commands against the same
observed process contract. Both requests use the bank's own credentials.

## Scenario

```yaml
ledger_script: Boundaries:run
race:
  name: Competing approvals
  reference: race-17
  steps:
    - {id: approval, role: lender, actor: bank, action: approve-financing}
```

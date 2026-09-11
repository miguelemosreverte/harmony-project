# Authenticated handoff observations

## Result

```yaml
actions:
  - id: approval-required
    outcome: rejected
    application: pending
    workflow: waiting
    consumed: false
    active_contracts: 1
    visible_to:
      - Northbank
    reason: ledger-rejected
  - id: bank-approval
    outcome: committed
    application: approved
    workflow: waiting
    consumed: true
    active_contracts: 1
    visible_to:
      - Northbank
  - id: observer-cannot-continue
    outcome: rejected
    application: approved
    workflow: waiting
    consumed: false
    active_contracts: 1
    visible_to:
      - Northbank
    reason: ledger-rejected
  - id: buyer-continuation
    outcome: committed
    application: approved
    workflow: complete
    consumed: false
    active_contracts: 1
    visible_to:
      - Northbank
```

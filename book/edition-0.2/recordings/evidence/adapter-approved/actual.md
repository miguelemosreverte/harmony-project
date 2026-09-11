# Observed adapter-approved

## Result

```yaml
actions:
  - id: buyer-attempt
    outcome: rejected
    application: pending
    consumed: false
    active_contracts: 1
    visible_to:
      - Alice
      - Northbank
    reason: unauthorized
    workflow: waiting
  - id: bank-approval
    outcome: committed
    application: approved
    consumed: true
    active_contracts: 1
    visible_to:
      - Alice
      - Northbank
    workflow: complete
```

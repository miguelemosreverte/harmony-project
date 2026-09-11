# Observed financing-approved

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
  - id: bank-approval
    outcome: committed
    application: approved
    consumed: true
    active_contracts: 1
    visible_to:
      - Alice
      - Northbank
```

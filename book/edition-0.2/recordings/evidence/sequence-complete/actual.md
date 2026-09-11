# Observed sequence-complete

## Result

```yaml
definition:
  name: approval-review
  version: 1
actions:
  - id: bank-approval
    outcome: committed
    application: approved
    consumed: true
    active_contracts: 2
    visible_to:
      - Alice
      - Northbank
    workflow: waiting
    review: pending
    completed:
      - financing
    enabled:
      - review
  - id: buyer-review
    outcome: committed
    application: approved
    consumed: false
    active_contracts: 2
    visible_to:
      - Alice
      - Northbank
    workflow: complete
    review: confirmed
    completed:
      - financing
      - review
    enabled: []
```

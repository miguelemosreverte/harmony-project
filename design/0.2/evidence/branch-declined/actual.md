# Observed branch-declined

## Result

```yaml
definition:
  name: decision-review
  version: 1
actions:
  - id: choose-decline
    outcome: committed
    application: pending
    consumed: false
    active_contracts: 3
    visible_to:
      - Alice
      - Northbank
    workflow: waiting
    review: pending
    completed:
      - decision
    enabled:
      - closure
    branch: decline
    closure: pending
    skipped:
      - financing
      - review
  - id: unselected-financing
    outcome: rejected
    application: pending
    consumed: false
    active_contracts: 3
    visible_to:
      - Alice
      - Northbank
    reason: application-rejected
    workflow: waiting
    review: pending
    completed:
      - decision
    enabled:
      - closure
    branch: decline
    closure: pending
    skipped:
      - financing
      - review
  - id: record-closure
    outcome: committed
    application: pending
    consumed: false
    active_contracts: 3
    visible_to:
      - Alice
      - Northbank
    workflow: waiting
    review: pending
    completed:
      - decision
      - closure
    enabled:
      - join
    branch: decline
    closure: confirmed
    skipped:
      - financing
      - review
  - id: join-completes
    outcome: committed
    application: pending
    consumed: false
    active_contracts: 3
    visible_to:
      - Alice
      - Northbank
    workflow: complete
    review: pending
    completed:
      - decision
      - closure
      - join
    enabled: []
    branch: decline
    closure: confirmed
    skipped:
      - financing
      - review
```

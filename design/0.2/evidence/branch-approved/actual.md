# Observed branch-approved

## Result

```yaml
definition:
  name: decision-review
  version: 1
actions:
  - id: join-before-decision
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
    completed: []
    enabled:
      - decision
    branch: undecided
    closure: pending
    skipped: []
  - id: choose-approval
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
      - financing
      - review
    branch: approve
    closure: pending
    skipped:
      - closure
  - id: cannot-change-decision
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
      - financing
      - review
    branch: approve
    closure: pending
    skipped:
      - closure
  - id: unselected-closure
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
      - financing
      - review
    branch: approve
    closure: pending
    skipped:
      - closure
  - id: bank-approval
    outcome: committed
    application: approved
    consumed: true
    active_contracts: 3
    visible_to:
      - Alice
      - Northbank
    workflow: waiting
    review: pending
    completed:
      - decision
      - financing
    enabled:
      - review
    branch: approve
    closure: pending
    skipped:
      - closure
  - id: join-still-waits
    outcome: rejected
    application: approved
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
      - financing
    enabled:
      - review
    branch: approve
    closure: pending
    skipped:
      - closure
  - id: buyer-review
    outcome: committed
    application: approved
    consumed: false
    active_contracts: 3
    visible_to:
      - Alice
      - Northbank
    workflow: waiting
    review: confirmed
    completed:
      - decision
      - financing
      - review
    enabled:
      - join
    branch: approve
    closure: pending
    skipped:
      - closure
  - id: join-completes
    outcome: committed
    application: approved
    consumed: false
    active_contracts: 3
    visible_to:
      - Alice
      - Northbank
    workflow: complete
    review: confirmed
    completed:
      - decision
      - financing
      - review
      - join
    enabled: []
    branch: approve
    closure: pending
    skipped:
      - closure
  - id: join-retry
    outcome: duplicate
    application: approved
    consumed: false
    active_contracts: 3
    visible_to:
      - Alice
      - Northbank
    workflow: complete
    review: confirmed
    completed:
      - decision
      - financing
      - review
      - join
    enabled: []
    branch: approve
    closure: pending
    skipped:
      - closure
  - id: new-join-request
    outcome: rejected
    application: approved
    consumed: false
    active_contracts: 3
    visible_to:
      - Alice
      - Northbank
    reason: application-rejected
    workflow: complete
    review: confirmed
    completed:
      - decision
      - financing
      - review
      - join
    enabled: []
    branch: approve
    closure: pending
    skipped:
      - closure
```

# The selected path determines what is required

Skipped steps are distinct from completed steps. The decision records the bank's
selection; approval itself still requires the financing application action.

## Result

```yaml
definition:
  name: decision-review
  version: 1
actions:
  - id: join-before-decision
    outcome: rejected
    reason: application-rejected
    application: pending
    review: pending
    closure: pending
    branch: undecided
    workflow: waiting
    completed: []
    skipped: []
    enabled: [decision]
    consumed: false
    active_contracts: 3
    visible_to: [Alice, Northbank]
  - id: choose-approval
    outcome: committed
    application: pending
    review: pending
    closure: pending
    branch: approve
    workflow: waiting
    completed: [decision]
    skipped: [closure]
    enabled: [financing, review]
    consumed: false
    active_contracts: 3
    visible_to: [Alice, Northbank]
  - id: cannot-change-decision
    outcome: rejected
    reason: application-rejected
    application: pending
    review: pending
    closure: pending
    branch: approve
    workflow: waiting
    completed: [decision]
    skipped: [closure]
    enabled: [financing, review]
    consumed: false
    active_contracts: 3
    visible_to: [Alice, Northbank]
  - id: unselected-closure
    outcome: rejected
    reason: application-rejected
    application: pending
    review: pending
    closure: pending
    branch: approve
    workflow: waiting
    completed: [decision]
    skipped: [closure]
    enabled: [financing, review]
    consumed: false
    active_contracts: 3
    visible_to: [Alice, Northbank]
  - id: bank-approval
    outcome: committed
    application: approved
    review: pending
    closure: pending
    branch: approve
    workflow: waiting
    completed: [decision, financing]
    skipped: [closure]
    enabled: [review]
    consumed: true
    active_contracts: 3
    visible_to: [Alice, Northbank]
  - id: join-still-waits
    outcome: rejected
    reason: application-rejected
    application: approved
    review: pending
    closure: pending
    branch: approve
    workflow: waiting
    completed: [decision, financing]
    skipped: [closure]
    enabled: [review]
    consumed: false
    active_contracts: 3
    visible_to: [Alice, Northbank]
  - id: buyer-review
    outcome: committed
    application: approved
    review: confirmed
    closure: pending
    branch: approve
    workflow: waiting
    completed: [decision, financing, review]
    skipped: [closure]
    enabled: [join]
    consumed: false
    active_contracts: 3
    visible_to: [Alice, Northbank]
  - id: join-completes
    outcome: committed
    application: approved
    review: confirmed
    closure: pending
    branch: approve
    workflow: complete
    completed: [decision, financing, review, join]
    skipped: [closure]
    enabled: []
    consumed: false
    active_contracts: 3
    visible_to: [Alice, Northbank]
  - id: join-retry
    outcome: duplicate
    application: approved
    review: confirmed
    closure: pending
    branch: approve
    workflow: complete
    completed: [decision, financing, review, join]
    skipped: [closure]
    enabled: []
    consumed: false
    active_contracts: 3
    visible_to: [Alice, Northbank]
  - id: new-join-request
    outcome: rejected
    reason: application-rejected
    application: approved
    review: confirmed
    closure: pending
    branch: approve
    workflow: complete
    completed: [decision, financing, review, join]
    skipped: [closure]
    enabled: []
    consumed: false
    active_contracts: 3
    visible_to: [Alice, Northbank]
```

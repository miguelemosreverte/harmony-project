# The selected path determines what is required

Skipped steps are distinct from completed steps. The decision records the bank's
selection; approval itself still requires the financing application action.

## Result

```yaml
definition:
  name: decision-review
  version: 1
actions:
  - id: choose-decline
    outcome: committed
    application: pending
    review: pending
    closure: pending
    branch: decline
    workflow: waiting
    completed: [decision]
    skipped: [financing, review]
    enabled: [closure]
    consumed: false
    active_contracts: 3
    visible_to: [Alice, Northbank]
  - id: unselected-financing
    outcome: rejected
    reason: application-rejected
    application: pending
    review: pending
    closure: pending
    branch: decline
    workflow: waiting
    completed: [decision]
    skipped: [financing, review]
    enabled: [closure]
    consumed: false
    active_contracts: 3
    visible_to: [Alice, Northbank]
  - id: record-closure
    outcome: committed
    application: pending
    review: pending
    closure: confirmed
    branch: decline
    workflow: waiting
    completed: [decision, closure]
    skipped: [financing, review]
    enabled: [join]
    consumed: false
    active_contracts: 3
    visible_to: [Alice, Northbank]
  - id: join-completes
    outcome: committed
    application: pending
    review: pending
    closure: confirmed
    branch: decline
    workflow: complete
    completed: [decision, closure, join]
    skipped: [financing, review]
    enabled: []
    consumed: false
    active_contracts: 3
    visible_to: [Alice, Northbank]
```

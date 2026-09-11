# Both application effects happen once

The workflow records completed and enabled steps. Waiting and reconnecting
change no contracts; retrying the same request returns the existing state.

## Result

```yaml
definition:
  name: approval-review
  version: 1
actions:
  - id: bank-approval
    outcome: committed
    application: approved
    review: pending
    workflow: waiting
    completed: [financing]
    enabled: [review]
    consumed: true
    active_contracts: 2
    visible_to: [Alice, Northbank]
  - id: buyer-review
    outcome: committed
    application: approved
    review: confirmed
    workflow: complete
    completed: [financing, review]
    enabled: []
    consumed: false
    active_contracts: 2
    visible_to: [Alice, Northbank]
```

# Observed sequence-resumed

## Result

```yaml
definition:
  name: approval-review
  version: 1
actions:
  - id: forged-completion
    outcome: rejected
    application: pending
    consumed: false
    active_contracts: 2
    visible_to:
      - Alice
      - Northbank
    reason: unauthorized
    workflow: waiting
    review: pending
    completed: []
    enabled:
      - financing
  - id: premature-review
    outcome: rejected
    application: pending
    consumed: false
    active_contracts: 2
    visible_to:
      - Alice
      - Northbank
    reason: application-rejected
    workflow: waiting
    review: pending
    completed: []
    enabled:
      - financing
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
  - id: same-request-again
    outcome: duplicate
    application: approved
    consumed: false
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
  - id: completed-step-again
    outcome: rejected
    application: approved
    consumed: false
    active_contracts: 2
    visible_to:
      - Alice
      - Northbank
    reason: application-rejected
    workflow: waiting
    review: pending
    completed:
      - financing
    enabled:
      - review
  - id: waiting-for-alice
    outcome: observed
    application: approved
    consumed: false
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
  - id: fresh-client
    outcome: observed
    application: approved
    consumed: false
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

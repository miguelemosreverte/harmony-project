# Observed private-approval

## Result

```yaml
actions:
  - id: cannot-forge-completion
    outcome: rejected
    application: pending
    consumed: false
    active_contracts: 1
    visible_to:
      - Northbank
    reason: unauthorized
    workflow: waiting
  - id: buyer-cannot-read-or-approve
    outcome: rejected
    application: pending
    consumed: false
    active_contracts: 1
    visible_to:
      - Northbank
    reason: not-visible
    workflow: waiting
  - id: approval-is-required
    outcome: rejected
    application: pending
    consumed: false
    active_contracts: 1
    visible_to:
      - Northbank
    reason: application-rejected
    workflow: waiting
  - id: bank-approval
    outcome: committed
    application: approved
    consumed: true
    active_contracts: 1
    visible_to:
      - Northbank
    workflow: waiting
  - id: reviewer-cannot-continue
    outcome: rejected
    application: approved
    consumed: false
    active_contracts: 1
    visible_to:
      - Northbank
    reason: unauthorized
    workflow: waiting
  - id: buyer-continuation
    outcome: committed
    application: approved
    consumed: false
    active_contracts: 1
    visible_to:
      - Northbank
    workflow: complete
visibility:
  Northbank:
    application: true
    progress: true
    private_events: 2
    progress_events: 2
    private_payload_observed: true
  Alice:
    application: false
    progress: true
    private_events: 0
    progress_events: 2
    private_payload_observed: false
  Olivia:
    application: false
    progress: true
    private_events: 0
    progress_events: 2
    private_payload_observed: false
```

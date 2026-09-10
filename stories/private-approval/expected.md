# Share the result without the private application

The bank approves privately. Alice then consumes its signed result to
complete shared progress. Olivia can observe this handoff but cannot
perform it. Current-contract queries and full participant event histories
must agree about the private boundary.

## Result

```yaml
actions:
  - id: buyer-cannot-read-or-approve
    outcome: rejected
    reason: not-visible
    application: pending
    workflow: waiting
    consumed: false
    active_contracts: 1
    visible_to: [Northbank]
  - id: approval-is-required
    outcome: rejected
    reason: application-rejected
    application: pending
    workflow: waiting
    consumed: false
    active_contracts: 1
    visible_to: [Northbank]
  - id: bank-approval
    outcome: committed
    application: approved
    workflow: waiting
    consumed: true
    active_contracts: 1
    visible_to: [Northbank]
  - id: reviewer-cannot-continue
    outcome: rejected
    reason: unauthorized
    application: approved
    workflow: waiting
    consumed: false
    active_contracts: 1
    visible_to: [Northbank]
  - id: buyer-continuation
    outcome: committed
    application: approved
    workflow: complete
    consumed: false
    active_contracts: 1
    visible_to: [Northbank]
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

# Only the scoped result enables the offer

These observations cover the financing application, current offer, resulting
proposals, completed domain steps, and each participant's permitted view.

## Result

```yaml
actions:
  - id: bank-assessment
    outcome: committed
    application: approved
    offer: not-opened
    proposal: none
    proposals: 0
    evidence_available: true
    workflow: waiting
    completed: [assessment]
    consumed: true
    active_contracts: 1
    visible_to: [Alice, Northbank]
  - id: open-offer
    outcome: committed
    application: approved
    offer: waiting
    proposal: none
    proposals: 0
    evidence_available: true
    workflow: waiting
    completed: [assessment]
    consumed: false
    active_contracts: 2
    visible_to: [Alice, Northbank]
  - id: invalid-evidence
    outcome: rejected
    reason: application-rejected
    application: approved
    offer: waiting
    proposal: none
    proposals: 0
    evidence_available: true
    workflow: waiting
    completed: [assessment]
    consumed: false
    active_contracts: 2
    visible_to: [Alice, Northbank]
visibility:
  Alice:
    application: true
    progress: true
    private_events: 3
    progress_events: 1
    private_payload_observed: true
  Ben:
    application: false
    progress: true
    private_events: 0
    progress_events: 1
    private_payload_observed: false
  Northbank:
    application: true
    progress: false
    private_events: 3
    progress_events: 0
    private_payload_observed: true
  Sofia:
    application: false
    progress: true
    private_events: 0
    progress_events: 1
    private_payload_observed: false
```

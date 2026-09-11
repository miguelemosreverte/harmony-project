# Observed purchase-rejected

## Result

```yaml
actions:
  - id: bank-assessment
    outcome: committed
    application: rejected
    consumed: true
    active_contracts: 1
    visible_to:
      - Alice
      - Northbank
    workflow: waiting
    offer: not-opened
    proposal: none
    completed:
      - assessment
    evidence_available: true
    proposals: 0
  - id: open-offer
    outcome: committed
    application: rejected
    consumed: false
    active_contracts: 2
    visible_to:
      - Alice
      - Northbank
    workflow: waiting
    offer: waiting
    proposal: none
    completed:
      - assessment
    evidence_available: true
    proposals: 0
  - id: rejected-financing
    outcome: rejected
    application: rejected
    consumed: false
    active_contracts: 2
    visible_to:
      - Alice
      - Northbank
    reason: application-rejected
    workflow: waiting
    offer: waiting
    proposal: none
    completed:
      - assessment
    evidence_available: true
    proposals: 0
visibility:
  Northbank:
    application: true
    progress: false
    private_events: 3
    progress_events: 0
    private_payload_observed: true
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
  Sofia:
    application: false
    progress: true
    private_events: 0
    progress_events: 1
    private_payload_observed: false
```

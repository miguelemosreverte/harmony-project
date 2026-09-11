# Observed purchase-reused-proof

## Result

```yaml
actions:
  - id: bank-assessment
    outcome: committed
    application: approved
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
    application: approved
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
  - id: make-proposal
    outcome: committed
    application: approved
    consumed: false
    active_contracts: 3
    visible_to:
      - Alice
      - Northbank
    workflow: waiting
    offer: proposed
    proposal: draft
    completed:
      - assessment
      - proposal
    evidence_available: false
    proposals: 1
  - id: open-second-offer
    outcome: committed
    application: approved
    consumed: false
    active_contracts: 4
    visible_to:
      - Alice
      - Northbank
    workflow: waiting
    offer: waiting
    proposal: none
    completed:
      - assessment
    evidence_available: false
    proposals: 1
  - id: reuse-consumed-result
    outcome: rejected
    application: approved
    consumed: false
    active_contracts: 4
    visible_to:
      - Alice
      - Northbank
    reason: not-visible
    workflow: waiting
    offer: waiting
    proposal: none
    completed:
      - assessment
    evidence_available: false
    proposals: 1
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
    progress_events: 3
    private_payload_observed: true
  Ben:
    application: false
    progress: true
    private_events: 0
    progress_events: 3
    private_payload_observed: false
  Sofia:
    application: false
    progress: true
    private_events: 0
    progress_events: 3
    private_payload_observed: false
```

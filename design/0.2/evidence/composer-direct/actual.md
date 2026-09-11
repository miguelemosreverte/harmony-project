# Observed composition

## Result

```yaml
composition:
  name: Offer checks
  reference: home-17
actions:
  - id: proposal
    outcome: committed
    drafts: 1
    instances: 0
    workflow: not-started
    completed: []
    enabled: []
    sources: {}
  - reason: ledger-rejected
    id: partner-consent-required
    outcome: rejected
    drafts: 1
    instances: 0
    workflow: not-started
    completed: []
    enabled: []
    sources: {}
  - id: acceptance
    outcome: committed
    drafts: 0
    instances: 1
    workflow: waiting
    completed: []
    enabled:
      - approval
    sources:
      approval: pending
      review: pending
  - reason: ledger-rejected
    id: review-too-early
    outcome: rejected
    drafts: 0
    instances: 1
    workflow: waiting
    completed: []
    enabled:
      - approval
    sources:
      approval: pending
      review: pending
  - id: approval
    outcome: committed
    drafts: 0
    instances: 1
    workflow: waiting
    completed:
      - approval
    enabled:
      - review
    sources:
      approval: approved
      review: pending
  - reason: ledger-rejected
    id: wrong-reviewer
    outcome: rejected
    drafts: 0
    instances: 1
    workflow: waiting
    completed:
      - approval
    enabled:
      - review
    sources:
      approval: approved
      review: pending
  - id: review
    outcome: committed
    drafts: 0
    instances: 1
    workflow: complete
    completed:
      - approval
      - review
    enabled: []
    sources:
      approval: approved
      review: confirmed
```

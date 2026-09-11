# Observed transfer-missing-agreement

## Result

```yaml
actions:
  - id: seller-locks
    outcome: committed
    trade: proposed
    workflow: waiting
    active_workflows: 0
    releases: 0
    source:
      available: '0'
      locked: '10'
    destination: '0'
    visible_to:
      - Alice
      - Destination
      - Seller
      - Source
  - id: source-too-early
    outcome: rejected
    trade: proposed
    workflow: waiting
    active_workflows: 0
    releases: 0
    source:
      available: '0'
      locked: '10'
    destination: '0'
    visible_to:
      - Alice
      - Destination
      - Seller
      - Source
    reason: application-rejected
  - id: settle
    outcome: rejected
    trade: proposed
    workflow: waiting
    active_workflows: 0
    releases: 0
    source:
      available: '0'
      locked: '10'
    destination: '0'
    visible_to:
      - Alice
      - Destination
      - Seller
      - Source
    reason: application-rejected
settlement_transactions: 0
```

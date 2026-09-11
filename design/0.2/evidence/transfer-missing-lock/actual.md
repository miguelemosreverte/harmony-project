# Observed transfer-missing-lock

## Result

```yaml
actions:
  - id: seller-agrees
    outcome: committed
    trade: agreed
    workflow: waiting
    active_workflows: 0
    releases: 0
    source:
      available: '10'
      locked: '0'
    destination: '0'
    visible_to:
      - Alice
      - Destination
      - Seller
      - Source
  - id: source-without-lock
    outcome: rejected
    trade: agreed
    workflow: waiting
    active_workflows: 0
    releases: 0
    source:
      available: '10'
      locked: '0'
    destination: '0'
    visible_to:
      - Alice
      - Destination
      - Seller
      - Source
    reason: application-rejected
  - id: settle
    outcome: rejected
    trade: agreed
    workflow: waiting
    active_workflows: 0
    releases: 0
    source:
      available: '10'
      locked: '0'
    destination: '0'
    visible_to:
      - Alice
      - Destination
      - Seller
      - Source
    reason: application-rejected
settlement_transactions: 0
```

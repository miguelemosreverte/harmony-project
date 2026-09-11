# Observed transfer-missing-readiness

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
  - id: seller-locks
    outcome: committed
    trade: agreed
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
  - id: source-confirms
    outcome: committed
    trade: source-confirmed
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
  - id: destination-without-permit
    outcome: rejected
    trade: source-confirmed
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
    trade: source-confirmed
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

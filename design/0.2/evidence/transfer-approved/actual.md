# Observed transfer-approved

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
  - id: destination-prepares
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
  - id: destination-confirms
    outcome: committed
    trade: ready
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
  - id: bypass-coordinator
    outcome: rejected
    trade: ready
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
    reason: unauthorized
  - id: wrong-settler
    outcome: rejected
    trade: ready
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
    outcome: committed
    trade: settled
    workflow: complete
    active_workflows: 1
    releases: 0
    source:
      available: '0'
      locked: '0'
    destination: '10'
    visible_to:
      - Alice
      - Destination
      - Seller
      - Source
  - id: settle-again
    outcome: rejected
    trade: settled
    workflow: complete
    active_workflows: 1
    releases: 0
    source:
      available: '0'
      locked: '0'
    destination: '10'
    visible_to:
      - Alice
      - Destination
      - Seller
      - Source
    reason: application-rejected
settlement_transactions: 1
```

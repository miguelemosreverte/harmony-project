# Expected transfer: missing agreement

Quantities are synthetic TEST units. Visibility refers to the shared trade. The transaction count requires source retirement, destination receipt, trade settlement, and workflow completion in one observed transaction.

## Result

```yaml
actions:
  - id: seller-locks
    outcome: committed
    trade: proposed
    source: {available: "0", locked: "10"}
    destination: "0"
    workflow: waiting
    active_workflows: 0
    releases: 0
    visible_to: [Alice, Destination, Seller, Source]
  - id: source-too-early
    outcome: rejected
    reason: application-rejected
    trade: proposed
    source: {available: "0", locked: "10"}
    destination: "0"
    workflow: waiting
    active_workflows: 0
    releases: 0
    visible_to: [Alice, Destination, Seller, Source]
  - id: settle
    outcome: rejected
    reason: application-rejected
    trade: proposed
    source: {available: "0", locked: "10"}
    destination: "0"
    workflow: waiting
    active_workflows: 0
    releases: 0
    visible_to: [Alice, Destination, Seller, Source]
settlement_transactions: 0
```

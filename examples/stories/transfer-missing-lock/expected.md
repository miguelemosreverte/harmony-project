# Expected transfer: missing lock

Quantities are synthetic TEST units. Visibility refers to the shared trade. The transaction count requires source retirement, destination receipt, trade settlement, and workflow completion in one observed transaction.

## Result

```yaml
actions:
  - id: seller-agrees
    outcome: committed
    trade: agreed
    source: {available: "10", locked: "0"}
    destination: "0"
    workflow: waiting
    active_workflows: 0
    releases: 0
    visible_to: [Alice, Destination, Seller, Source]
  - id: source-without-lock
    outcome: rejected
    reason: application-rejected
    trade: agreed
    source: {available: "10", locked: "0"}
    destination: "0"
    workflow: waiting
    active_workflows: 0
    releases: 0
    visible_to: [Alice, Destination, Seller, Source]
  - id: settle
    outcome: rejected
    reason: application-rejected
    trade: agreed
    source: {available: "10", locked: "0"}
    destination: "0"
    workflow: waiting
    active_workflows: 0
    releases: 0
    visible_to: [Alice, Destination, Seller, Source]
settlement_transactions: 0
```

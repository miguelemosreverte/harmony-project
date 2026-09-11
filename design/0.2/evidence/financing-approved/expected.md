# Only the bank can approve

The rejected attempt leaves the application intact. The bank's approval
replaces it with an approved application, visible to both parties.

## Result

```yaml
actions:
  - id: buyer-attempt
    outcome: rejected
    reason: unauthorized
    application: pending
    consumed: false
    active_contracts: 1
    visible_to: [Alice, Northbank]
  - id: bank-approval
    outcome: committed
    application: approved
    consumed: true
    active_contracts: 1
    visible_to: [Alice, Northbank]
```

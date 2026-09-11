# Two authenticated sessions complete the handoff

A rejected command leaves the application and shared progress unchanged.
Only the bank can see the private application. The buyer consumes the signed
result and completes the shared workflow.

## Result

```yaml
actions:
  - id: approval-required
    outcome: rejected
    reason: ledger-rejected
    application: pending
    workflow: waiting
    consumed: false
    active_contracts: 1
    visible_to: [Northbank]
  - id: bank-approval
    outcome: committed
    application: approved
    workflow: waiting
    consumed: true
    active_contracts: 1
    visible_to: [Northbank]
  - id: observer-cannot-continue
    outcome: rejected
    reason: ledger-rejected
    application: approved
    workflow: waiting
    consumed: false
    active_contracts: 1
    visible_to: [Northbank]
  - id: buyer-continuation
    outcome: committed
    application: approved
    workflow: complete
    consumed: false
    active_contracts: 1
    visible_to: [Northbank]
```

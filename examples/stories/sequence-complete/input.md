# Approval followed by review

Northbank approves the financing. Alice can then confirm her independent
review. Both steps belong to version 1 of the published approval workflow.

## Scenario

```yaml
workflow: sequential-approval
setup:
  application:
    bank: Northbank
    buyer: Alice
    status: pending
actions:
  - id: bank-approval
    actor: Northbank
    action: approve-financing
  - id: buyer-review
    actor: Alice
    action: confirm-review
```

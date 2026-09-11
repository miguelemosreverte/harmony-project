# Adapted workflow approval

Alice needs a bank approval before she can continue with a purchase.
She first tries to approve her own application; Northbank then acts.

## Scenario

```yaml
workflow: approval
integration: adapter
setup:
  application:
    bank: Northbank
    buyer: Alice
    status: pending
actions:
  - id: buyer-attempt
    actor: Alice
    action: approve-financing
  - id: bank-approval
    actor: Northbank
    action: approve-financing
```

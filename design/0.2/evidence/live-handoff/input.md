# Approve privately, continue in another session

The bank, buyer, and Olivia each connect with credentials restricted to their
own party on a separate participant. The browser cannot select another identity.
The live viewer loads this setup; the acceptance runner also performs these actions.

## Scenario

```yaml
workflow: private-approval
setup:
  application:
    bank: Northbank
    buyer: Alice
    reviewer: Olivia
    status: pending
    private_details: "Synthetic income: 90000; internal rating: B"
actions:
  - id: approval-required
    actor: Alice
    action: publish-approval
  - id: bank-approval
    actor: Northbank
    action: approve-financing
  - id: observer-cannot-continue
    actor: Olivia
    action: publish-approval
  - id: buyer-continuation
    actor: Alice
    action: publish-approval
```

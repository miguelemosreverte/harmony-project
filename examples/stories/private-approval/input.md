# A private approval, a shared next step

Northbank keeps its application details private. Alice needs the bank's
signed approval to continue, while Olivia can observe shared progress.
Each party is hosted on a separate participant node.

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
  - id: cannot-forge-completion
    actor: Alice
    action: forge-completion
  - id: buyer-cannot-read-or-approve
    actor: Alice
    action: approve-financing
  - id: approval-is-required
    actor: Alice
    action: publish-approval
  - id: bank-approval
    actor: Northbank
    action: approve-financing
  - id: reviewer-cannot-continue
    actor: Olivia
    action: publish-approval
  - id: buyer-continuation
    actor: Alice
    action: publish-approval
```

# The approved branch waits for both applications

The bank selects one path. The join waits for every required action in that path.
Unselected application contracts remain unchanged.

## Scenario

```yaml
workflow: branching-approval
setup:
  application:
    bank: Northbank
    buyer: Alice
    status: pending
actions:
  - id: join-before-decision
    actor: Alice
    action: complete-join
  - id: choose-approval
    actor: Northbank
    action: choose-approve
    request: decision-1
  - id: cannot-change-decision
    actor: Northbank
    action: choose-decline
    request: decision-1
  - id: unselected-closure
    actor: Northbank
    action: close-application
  - id: bank-approval
    actor: Northbank
    action: approve-financing
  - id: join-still-waits
    actor: Alice
    action: complete-join
  - id: buyer-review
    actor: Alice
    action: confirm-review
  - id: join-completes
    actor: Alice
    action: complete-join
    request: join-1
  - id: join-retry
    actor: Alice
    action: complete-join
    request: join-1
  - id: new-join-request
    actor: Alice
    action: complete-join
```

# The declined branch records closure

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
  - id: choose-decline
    actor: Northbank
    action: choose-decline
  - id: unselected-financing
    actor: Northbank
    action: approve-financing
  - id: record-closure
    actor: Northbank
    action: close-application
  - id: join-completes
    actor: Alice
    action: complete-join
```

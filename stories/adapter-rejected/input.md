# Adapted workflow rollback

An approved application cannot be approved again. Even the bank must
respect the application's current state.

## Scenario

```yaml
workflow: approval
integration: adapter
setup:
  application:
    bank: Northbank
    buyer: Alice
    status: approved
actions:
  - id: repeated-approval
    actor: Northbank
    action: approve-financing
```

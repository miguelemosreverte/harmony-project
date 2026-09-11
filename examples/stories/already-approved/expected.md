# Existing approval is preserved

The failed action neither consumes the existing contract nor creates
another application.

## Result

```yaml
actions:
  - id: repeated-approval
    outcome: rejected
    reason: application-rejected
    application: approved
    consumed: false
    active_contracts: 1
    visible_to: [Alice, Northbank]
```

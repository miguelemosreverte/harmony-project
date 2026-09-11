# Existing approval is preserved

The failed action neither consumes the existing contract nor creates
another application. The workflow remains waiting because its application action failed.

## Result

```yaml
actions:
  - id: repeated-approval
    outcome: rejected
    reason: application-rejected
    application: approved
    workflow: waiting
    consumed: false
    active_contracts: 1
    visible_to: [Alice, Northbank]
```

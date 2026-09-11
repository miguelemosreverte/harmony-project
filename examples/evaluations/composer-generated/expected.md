# The chosen order governs both applications

The generated integration has the same source-action and core-progression
obligations as a direct action. Its extra companion contract is not counted
as another workflow instance.

## Result

```yaml
composition: {name: Review before legacy approval, reference: home-18}
actions:
  - id: proposal
    outcome: committed
    drafts: 1
    instances: 0
    workflow: not-started
    completed: []
    enabled: []
    sources: {}
  - id: acceptance
    outcome: committed
    drafts: 0
    instances: 1
    workflow: waiting
    completed: []
    enabled: [inspection]
    sources: {inspection: pending, financing: pending}
  - id: inspection
    outcome: committed
    drafts: 0
    instances: 1
    workflow: waiting
    completed: [inspection]
    enabled: [financing]
    sources: {inspection: confirmed, financing: pending}
  - id: financing
    outcome: committed
    drafts: 0
    instances: 1
    workflow: complete
    completed: [inspection, financing]
    enabled: []
    sources: {inspection: confirmed, financing: approved}
```

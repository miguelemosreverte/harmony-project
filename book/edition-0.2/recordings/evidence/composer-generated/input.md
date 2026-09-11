# Change the order and use an unchanged application

The buyer reviews first. The bank then approves through a generated adapter
against the unchanged legacy application. A reader chooses these actions and
their order; no generated Daml needs editing.

## Scenario

```yaml
workflow: composed-process
plan:
  name: Review before legacy approval
  reference: home-18
  steps:
    - id: inspection
      role: inspector
      actor: buyer
      action: confirm-review
    - id: financing
      role: lender
      actor: bank
      action: approve-generated
actions:
  - {id: proposal, actor: bank, action: propose}
  - {id: acceptance, actor: buyer, action: accept}
  - {id: inspection, actor: buyer, action: advance, step: inspection}
  - {id: financing, actor: bank, action: advance, step: financing}
```

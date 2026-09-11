# Propose, consent, then act

The bank proposes an approval followed by the buyer's review. No source
application exists until the buyer accepts the exact plan. The core then
rejects an early review and an action attempted by the wrong party.

## Scenario

```yaml
workflow: composed-process
plan:
  name: Offer checks
  reference: home-17
  steps:
    - id: approval
      role: lender
      actor: bank
      action: approve-financing
    - id: review
      role: reviewer
      actor: buyer
      action: confirm-review
actions:
  - {id: proposal, actor: bank, action: propose}
  - {id: partner-consent-required, actor: bank, action: accept}
  - {id: acceptance, actor: buyer, action: accept}
  - {id: review-too-early, actor: buyer, action: advance, step: review}
  - {id: approval, actor: bank, action: advance, step: approval}
  - {id: wrong-reviewer, actor: bank, action: advance, step: review}
  - {id: review, actor: buyer, action: advance, step: review}
```

# The offer rejects wrong buyer evidence

Alice's documents are shared with Northbank. Ben owns the buyer-side offer
application; Sofia receives proposals through the seller-side continuation.
The evidence selection is explicit so invalid-certificate experiments stay visible.

## Scenario

```yaml
workflow: property-purchase
setup:
  application:
    bank: Northbank
    buyer: Alice
    status: pending
    documents: "Income evidence #7"
    bank_decision: approved
  offer:
    buyer_agent: Ben
    seller_agent: Sofia
    property: riverside-17
    evidence: wrong-buyer
actions:
  - id: bank-assessment
    actor: Northbank
    action: assess-financing
  - id: open-offer
    actor: Alice
    action: open-offer
  - id: invalid-evidence
    actor: Alice
    action: make-proposal
```

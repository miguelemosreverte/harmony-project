# A bank result becomes an independently owned offer

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
    evidence: financing
actions:
  - id: wrong-assessor
    actor: Alice
    action: assess-financing
  - id: bank-assessment
    actor: Northbank
    action: assess-financing
  - id: cannot-forge-proposal
    actor: Alice
    action: forge-proposal
  - id: open-offer
    actor: Alice
    action: open-offer
  - id: make-proposal
    actor: Alice
    action: make-proposal
  - id: receipt-must-wait
    actor: Sofia
    action: receive-proposal
  - id: buyer-agent-relays
    actor: Ben
    action: relay-proposal
  - id: seller-agent-receives
    actor: Sofia
    action: receive-proposal
```

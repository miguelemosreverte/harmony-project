# Transfer: missing readiness

Destination confirmation needs the buyer’s receiving request and the custodian’s prepared permit.

## Scenario

```yaml
workflow: atomic-transfer
setup:
  trade:
    buyer: Alice
    seller: Seller
    source: Source
    destination: Destination
    settler: Alice
    asset: TEST
    quantity: "10"
  destination_receipts: accept
actions:
  - id: seller-agrees
    actor: Seller
    action: agree-trade
  - id: seller-locks
    actor: Seller
    action: lock-position
  - id: source-confirms
    actor: Source
    action: confirm-source
  - id: destination-without-permit
    actor: Destination
    action: confirm-destination
  - id: settle
    actor: Alice
    action: settle
```

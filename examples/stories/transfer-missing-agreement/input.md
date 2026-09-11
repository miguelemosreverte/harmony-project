# Transfer: missing agreement

Locking a position does not replace the seller’s agreement to the trade.

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
  - id: seller-locks
    actor: Seller
    action: lock-position
  - id: source-too-early
    actor: Source
    action: confirm-source
  - id: settle
    actor: Alice
    action: settle
```

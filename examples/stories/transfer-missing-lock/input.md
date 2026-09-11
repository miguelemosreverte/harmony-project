# Transfer: missing lock

The source cannot confirm an unlocked position; settlement preserves the available balance.

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
  - id: source-without-lock
    actor: Source
    action: confirm-source
  - id: settle
    actor: Alice
    action: settle
```

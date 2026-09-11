# Transfer: source settler

The source custodian can serve as Settler when every party has granted the required consent.

## Scenario

```yaml
workflow: atomic-transfer
setup:
  trade:
    buyer: Alice
    seller: Seller
    source: Source
    destination: Destination
    settler: Source
    asset: TEST
    quantity: "7.125"
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
  - id: destination-prepares
    actor: Destination
    action: prepare-destination
  - id: destination-confirms
    actor: Destination
    action: confirm-destination
  - id: settle
    actor: Source
    action: settle
```

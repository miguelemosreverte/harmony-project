# Transfer: final leg rejected

The destination rejects its receipt after source withdrawal is attempted. The source remains locked and no destination holding or workflow completion survives.

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
  destination_receipts: reject
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
    actor: Alice
    action: settle
```

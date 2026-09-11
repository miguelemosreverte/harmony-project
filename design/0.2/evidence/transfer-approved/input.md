# Transfer: approved

All parties consent, Alice settles, and a second settlement is rejected.

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
  - id: destination-prepares
    actor: Destination
    action: prepare-destination
  - id: destination-confirms
    actor: Destination
    action: confirm-destination
  - id: bypass-coordinator
    actor: Alice
    action: withdraw-directly
  - id: wrong-settler
    actor: Seller
    action: settle
  - id: settle
    actor: Alice
    action: settle
  - id: settle-again
    actor: Alice
    action: settle
```

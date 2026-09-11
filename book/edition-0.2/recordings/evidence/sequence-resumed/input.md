# A workflow survives retries and a restart

A forged completion and a premature review must fail. The bank's request
can be retried safely. Alice then reconnects and completes her review using
progress recovered from the ledger.

## Scenario

```yaml
workflow: sequential-approval
setup:
  application:
    bank: Northbank
    buyer: Alice
    status: pending
actions:
  - id: forged-completion
    actor: Northbank
    action: forge-completion
  - id: premature-review
    actor: Alice
    action: confirm-review
  - id: bank-approval
    actor: Northbank
    action: approve-financing
    request: approval-1
  - id: same-request-again
    actor: Northbank
    action: approve-financing
    request: approval-1
  - id: completed-step-again
    actor: Northbank
    action: approve-financing
  - id: waiting-for-alice
    actor: Alice
    action: wait
  - id: fresh-client
    actor: Alice
    action: reconnect
  - id: buyer-review
    actor: Alice
    action: confirm-review
```

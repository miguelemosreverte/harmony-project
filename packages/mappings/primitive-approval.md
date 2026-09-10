# Primitive choice arguments

This source-only fixture exercises all five supported primitive types. The choice checks each supplied value before approving. It shares the [approval expectation](approval-expected.md) with the empty-argument financing mapping.

## Mapping

```yaml
binding: GeneratedPrimitiveApproval
source:
  package: primitive-approval
  module: PrimitiveApproval
  template: Application
  choice: Approve
roles:
  actor: bank
  readers: [buyer]
subject: reference
arguments:
  message: reviewed
  days: 2
  authorized: true
  cost: "1.25"
  approver: Northbank
result: replacement
observe: status
example:
  bank: Northbank
  buyer: Alice
  reference: application
  status: pending
  counter: 0
  enabled: true
  budget: "10"
```

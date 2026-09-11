# Integrate the existing financing approval

The source is the pinned `legacy-financing` package in [the input manifest](../inputs.md). Types come from that DAR. This mapping names the business roles and the eligible choice; the example supplies the source's initial values.

## Mapping

```yaml
binding: GeneratedFinancing
source:
  package: legacy-financing
  module: LegacyFinancing
  template: Application
  choice: Approve
roles:
  actor: bank
  readers: [buyer]
subject: reference
arguments: {}
result: replacement
observe: status
example:
  bank: Northbank
  buyer: Alice
  reference: application
  status: pending
```

`arguments` is empty because the inspected approval choice has no fields. `replacement` requires a consuming choice returning a contract of the same source template. The adapter checks the source's actor, reader fields, and subject before invoking it. The runnable example observes `status` and the actual source/core effects; the [expectation](approval-expected.md) is independently authored.

# Bring a compiled application

The bank supplies the unchanged legacy DAR, compares it with the participant's
actual administrator export, and builds its reviewed adapter. Metadata can be
inspected but has no executable action mapping. Buyer credentials cannot operate
the builder, and malformed archives never become accepted inputs.

## Scenario

```yaml
workflow: package-builder
actions:
  - {id: local, actor: bank, action: upload, source: legacy-financing}
  - {id: participant, actor: bank, action: retrieve, source: participant}
  - {id: compile, actor: bank, action: generate}
  - {id: metadata, actor: bank, action: retrieve, source: metadata}
  - {id: unsupported, actor: bank, action: generate}
  - {id: wrong-party, actor: buyer, action: retrieve, source: legacy-financing}
  - {id: malformed, actor: bank, action: malformed}
  - {id: oversized, actor: bank, action: oversized}
```

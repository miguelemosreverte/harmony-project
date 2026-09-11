# Observed package builder

## Result

```yaml
builder: package-inputs
actions:
  - supported: true
    available_live: true
    compiled: false
    id: local
    http: 200
    inputs: 1
  - supported: true
    available_live: true
    compiled: false
    id: participant
    http: 200
    inputs: 2
  - supported: true
    available_live: true
    compiled: true
    id: compile
    http: 200
    inputs: 2
  - supported: false
    available_live: false
    compiled: false
    id: metadata
    http: 200
    inputs: 3
  - id: unsupported
    http: 400
    inputs: 3
  - id: wrong-party
    http: 403
    inputs: 3
  - id: malformed
    http: 400
    inputs: 3
  - id: oversized
    http: 400
    inputs: 3
download:
  source_unchanged: true
  compiled_artifacts_match: true
```

# Bounds, disclosure, and one winning advance

Every numeric limit below is observed from queried ledger state. A conflicting
request must fail definitively; a lost connection cannot satisfy this baseline.

## Result

```yaml
core:
  maximum_steps: 16
  approved_sources: 16
  maximum_join_prerequisites: 14
  join_complete: true
  graph_overflow_rejected: true
  stale_contract_rejected: true
  request_collision_rejected: true
  duplicate_kept_identity: true
  missing_disclosure_rejected: true
  maximum_composer_steps: 4
  composition_approved_sources: 4
  composition_overflow_rejected: true
  maximum_proposals: 8
  proposal_overflow_rejected: true
race:
  committed: 1
  conflicting: 1
  approved_sources: 1
  active_processes: 1
  completed_steps: 1
  committed_transactions: 1
  original_source_archived: true
```

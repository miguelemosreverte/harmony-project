# Identity and reconnect boundaries

These checks bypass the browser controls. Read and act-as attempts go directly
to the Ledger API using the buyer's token. HTTP attempts use the same endpoints
as the browser. Retrying an existing request must not create another transaction.

## Result

```yaml
buyer_read_as_bank: PERMISSION_DENIED
buyer_act_as_bank: PERMISSION_DENIED
missing_session_http: 401
actor_override_http: 400
stale_submission: stale
repeated_request: committed
repeated_request_added_transactions: 0
reconnected_workflow: complete
private_payload_visible_to: [bank]
```

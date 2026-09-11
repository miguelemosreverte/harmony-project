# The product

Harmonia coordinates independently owned Daml applications. Contracts authorize the actions; the Scala service observes participant state, submits supported commands, and exposes a typed HTTP interface. The live browser is a client of that interface.

| Directory | Read it for |
| --- | --- |
| `ledger/` | Interfaces, process execution, application choices, consent, and adapters |
| `api/` | Public commands, observations, and response types shared with the browser |
| `server/` | Feature operations, ledger transport, request handling, and package generation |
| `web/` | Live financing, workflow editor, and package interface |
| `packages/` | Pinned application identities and reviewed mappings |

## Follow one operation

1. [FinancingState](api/src/main/scala/harmonia/financing/FinancingState.scala) names supported actions and observations.
2. [FinancingObservation](server/src/main/scala/harmonia/financing/FinancingObservation.scala) decodes visible contracts.
3. [Financing](server/src/main/scala/harmonia/financing/Financing.scala) selects the ledger choice.
4. [Workspace](server/src/main/scala/harmonia/app/workspace/Workspace.scala) supplies connections and owns submission coordination.
5. [LiveServer](server/src/main/scala/harmonia/app/http/LiveServer.scala) encodes the response for the browser.

`Connections` contains supplied ledger clients and their catalog. It does not create a network or read a story. `Resource` closes the clients, HTTP executor, and submission supervisor when their owner stops.

For complete request-to-effect paths, read the [fifth-draft guide](../docs/fifth-draft/reading-guide.md). `ActionRequest` is shared with the browser, including remembered retries. Package resolution returns verified facts directly; generation returns its member list and manifest for archive assembly. JSON is encoded at transport and evidence boundaries.

## Build and run

From the repository root, `scripts/build-product` builds product contracts, service, and browser. It does not build the book or harness. `scripts/product serve configuration.json [state-directory]` connects to existing local participants; it does not provision them.

```json
{
  "catalog_dar": "path/to/application-assembly.dar",
  "package_exports": "path/to/verified-dar-exports",
  "participants": {
    "bank": {"port": 5001, "party": "bank-party", "user": "bank-user", "token_file": "bank.token"},
    "buyer": {"port": 5002, "party": "buyer-party", "user": "buyer-user", "token_file": "buyer.token"},
    "reviewer": {"port": 5003, "party": "reviewer-party", "user": "reviewer-user", "token_file": "reviewer.token"}
  }
}
```

Ports refer to the supported local Ledger API connections. Token files supply existing participant credentials. The catalog DAR must contain the supported application packages; package exports contain the verified legacy DAR used by package retrieval. Paths resolve from the repository root. The optional state directory controls where the service writes its local state; otherwise it creates a fresh run directory. The service prints its address and the private file containing browser session links.

The separate harness command `scripts/demo live` provisions the demonstrated local network. The book and full verification instructions are outside this product directory.

Public case classes use `JsonCodec` to derive the established snake-case HTTP fields. Special envelopes, such as the flattened workspace response, remain explicit. Composition validation operates on the typed plan and is shared by the editor and HTTP boundary.

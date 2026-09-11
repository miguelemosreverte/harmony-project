# Ledger ownership

Read `interfaces`, then the particular application and choice used by your example. `core` depends only on the common interfaces. Applications own their business transitions and remain separate compiled packages.

| Directory | Responsibility |
| --- | --- |
| `interfaces/` | Common action, result, and transfer vocabulary |
| `core/` | Workflow, shared progress, and process execution |
| `applications/` | Independently owned financing, review, property, and custody applications |
| `bindings/` | Handwritten adapter for an unchanged legacy application |
| `composition/` | Consented proposal and process creation |
| `demo/` | Standalone authenticated live setup, with no dependency on the test assembly |
| `tests/` | Daml Script integration, branching, privacy, transfer, and boundary tests |
| `fixtures/` | Primitive generation and imported metadata examples |

Each package's `daml.yaml` declares its dependencies. [The build order](../../scripts/ledger-packages) keeps those dependencies explicit. Generated adapter projects live under `.artifacts/`, importing pinned application and Harmonia API DARs. Source application identities stay pinned in [the input manifest](../packages/inputs.md).

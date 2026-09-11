# Local evaluation edition

The first implementation provides a core-managed workflow engine, direct interfaces and generated typed adapters, private result handoff, and two complete reference workflows: financing to property offer, and a four-party atomic transfer.

The Scala application includes authenticated live participant sessions, a consented workflow editor, a bounded DAR builder, golden verification, and a nine-chapter interactive book. Thirty-two release recordings cover business behavior, authorization, composition, builder inputs, and execution limits. Compiled-regression and generation-determinism checks accompany them.

Runtime processes now have explicit lifetimes and heap limits. The recorded book runs directly in one 128 MiB JVM; build tools exit before applications start. A workspace lease prevents overlapping local ledger environments.

This is a local evaluation edition. Public publication, a project source-code license, outside-team feedback, and adoption acceptance are not completed by this delivery.

The second draft separates typed financing/composition operations from submission delivery, replaces pretty-printed LF parsing with structured protobuf inspection, and groups independently owned applications, live setup, tests, and examples. The reader opens at Chapter 1, keeps navigation context, and provides an evidence/source inspector. Live editors stay mounted through refresh. Golden contents and pinned source DAR bytes are preserved. The second-draft acceptance record identifies its fresh complete release evidence.

The third draft constructs shared workspace, package, and book models directly on the server. Financing observations are decoded once, composition has its own command family and payload schemas, and prepared ledger exercises have named fields. Missing required ledger data fails explicitly. Package generation uses typed stored facts; raw metadata and complete golden evidence are preserved. The existing book presentation and business expectations remain unchanged.

The fourth draft separates product, book, and development harness into explicit build targets and entry points. The service receives configured participant connections and runs without book or harness classes. The composition editor validates typed plans directly; shared response codecs remove repeated field lists while preserving the HTTP contract. Product/browser assets and release launchers have distinct ownership. Earlier plans move under `docs/history/`; current guides lead with the product. All original golden files and Daml source bytes remain unchanged.

# Local evaluation edition

The first implementation provides a core-managed workflow engine, direct interfaces and generated typed adapters, private result handoff, and two complete reference workflows: financing to property offer, and a four-party atomic transfer.

The Scala application includes authenticated live participant sessions, a consented workflow editor, a bounded DAR builder, golden verification, and a nine-chapter interactive book. Thirty-two release recordings cover business behavior, authorization, composition, builder inputs, and execution limits. Compiled-regression and generation-determinism checks accompany them.

Runtime processes now have explicit lifetimes and heap limits. The recorded book runs directly in one 128 MiB JVM; build tools exit before applications start. A workspace lease prevents overlapping local ledger environments.

This is a local evaluation edition. Public publication, a project source-code license, outside-team feedback, and adoption acceptance are not completed by this delivery.

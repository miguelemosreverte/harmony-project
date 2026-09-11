# Maintenance boundaries

The repository owns the core workflow contracts, reference application packages, typed adapter generator, Scala orchestration and browser, story format, independent expectations, and book. Existing application choices retain business authority. An integration publisher is trusted to publish valid bindings; the reference does not prove safety against a malicious publisher within its own trusted role.

Keep changes organized by feature. Use pure types and functions for validation and comparison, `IO` at effectful boundaries, and `Resource` for subprocesses, channels, servers, and temporary live environments. Avoid background build servers and overlapping ledger runs. Recheck process cleanup and memory limits after lifecycle changes.

Treat each committed expected result as a reviewed promise. A failing golden is evidence to investigate. Do not regenerate expectations as part of normal verification. Preserve actual output and diffs, distinguish failed observation from business rejection, and add focused negative cases for new authority, privacy, and atomicity behavior.

Pinned SDK, protocol dependencies, package identities, and supported mapping shapes are deliberate compatibility boundaries. An upgrade needs compilation plus relevant actual-ledger stories, participant visibility checks, generator determinism, portable project reproduction, and a new clean release. Previously recorded evidence keeps its original revision; do not relabel it as fresh execution.

The live reference uses disposable local networks and limited synthetic workloads. Durable business hosting, production deployment, a general workflow language, arbitrary-DAR automation, third-party support commitments, and external milestone acceptance are outside the delivered maintenance promise.

After a release, retain the source revision, archive digest, payload manifest, required-check logs, and golden observations together. Archive source links are stable; later documentation may link back to them but cannot change what that revision proved. Public publication and license decisions belong in a separate recorded decision when made.

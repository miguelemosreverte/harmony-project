# Reproducible package input proof

Verified locally on 2026-09-10. The [manifest](../../product/packages/inputs.md) resolves the unchanged local legacy DAR and an upstream Splice metadata DAR pinned to a full commit, archive SHA-256, main package ID, and Daml-LF version. The resolver records every included dependency identity.

`scripts/check` passed sixteen Scala tests, Daml definition/smoke checks, the package reproduction check, and all eleven workflow goldens. Workflow evidence is `.artifacts/check-16196452810007767555/`; the first package reproduction is `.artifacts/packages-6328852912847288170/`.

The receipt application was then separated from its test scripts. The final `scripts/build` completed without the template/script dependency warning, and `scripts/harmonia packages-check` passed again at `.artifacts/packages-18382140955707526061/`. Both fresh networks produced the committed approval and typed receipt. Their `first/` and `repeat/` directories retain actual Markdown, diffs, raw observations, node configuration, logs, and downloaded DARs.

On both networks, Canton administrator `dars.download` returned the originally uploaded metadata DAR. Its SHA-256 remained `455eb160cb5abd4ae9918a6fbb9dad471f721adda39f0e5c76feef08d05637fc`. The reference imports LF 2.1 metadata alongside the LF 2.2 legacy application with SDK 3.4.11.

Two separate negative manifests exited unsuccessfully with the required diagnostics: a mismatched main package ID reported incompatibility, and an unavailable pinned URL reported HTTP 404. Inputs, logs, exit codes, and assertions are retained in `.artifacts/package-failures/`. Pure checks additionally reject mutable URLs, unsupported LF versions, credentials in URLs, incomplete identities, and unknown fields.

The [package guide](../../product/packages/README.md) distinguishes package payloads from full DAR downloads, describes the administrator boundary and offline reproduction, and records resolver limits. No external participant or production administration service was contacted; retrieval was verified on disposable local nodes.

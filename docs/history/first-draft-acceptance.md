# First-draft local technical acceptance

The implementation plan is complete through Step 20 for the local evaluation delivery. Public licensing, publication, external evaluation, and adoption acceptance are outside this implementation plan. The [verification record](verification/19-release.md) supplies the actual commands, environment, artifact paths, measurements, and limits behind this acceptance.

## Identified delivery

| Item | Identifier |
| --- | --- |
| Verified software, chapters, and fresh recordings | `dd5cf02754241730dc6cfed71e0b92216d657da0` |
| Verification record commit | `8189028` |
| Local archive | `.artifacts/release-13364464813494329764/harmonia-dd5cf0275424.tar.gz` |
| Archive SHA-256 | `6a13bc5f2b033ef81afdff7a6350de666f0b8329b200311d8db6a402f21bc1a2` |
| Payload manifest | `harmonia-dd5cf0275424/manifest.json`: 1,171 file hashes |
| Reader content | Nine chapters, 32 passing recorded examples |
| Final handoff documents | This documentation commit; see Git history and the progress record |

The source snapshot inside the archive precedes the final verification/acceptance documentation. Its planning status reflects that point in time. This acceptance applies to the exact revision above; later documentation does not change or relabel its recordings. Historical verification notes remain historical, with their original paths and scope.

## Accepted technical behavior

| Capability | Deciding evidence |
| --- | --- |
| Direct and unchanged-application participation | `workflow-*`, `adapter-*`, `generated-*`; two direct/generated parity checks |
| Progression, branches, and joins | `sequence-*`, `branch-*`; actual bound and stale/retry checks |
| Private result handoff and separate application ownership | `private-approval`, eight `purchase-*` cases, participant observations |
| Atomic four-party transfer | Six `transfer-*` cases, including final-leg rollback |
| Generated integration | Both generated projects compile/run; identical repeated manifests; compiled regression caught; portable rebuild passes |
| Authenticated live use | Restricted participant sessions; API bypass, stale/repeat, reconnect, and observation-failure checks |
| Reader-defined supported composition | Two consented composer goldens plus live editor/browser verification |
| Bounded package builder | Actual DAR upload/retrieval, inspection, supported compilation/download, and rejected unsupported/invalid inputs |
| Digestible delivery | Chapter-linked experiments, independent expected/actual inspection, source links, glossary, guides, and tested narrow layout |
| Local reproduction | Eleven sequential clean-checkout gates; 27 Scala tests and four Daml scripts; actual unpacked launchers and changed-file rejection |

These checks support the documented vocabulary and local topology in the [capability](../capabilities.md) and [compatibility](../compatibility.md) matrices. They do not establish arbitrary application integration, malicious-publisher safety, production availability, or general performance limits.

## Reader and developer handoff

Unpack the archive and run `./run-book` with Java 17. Open the printed loopback URL. The retained local preview uses `http://127.0.0.1:56007/`. The book needs no ledger. Use `./run-verify` to check the identified payload; it also needs Git for source verification.

For live evaluation, install Daml SDK 3.4.11 and use `./run-live`. Follow the provisioned participant links in the private local sessions file and Chapter 8's workflow. Ctrl-C releases the network. The relocated launcher was exercised from a path containing spaces, with all three authenticated state requests accepted and missing credentials rejected.

For development, follow [setup](../../book/setup.md), the [walkthrough](../developer-walkthrough.md), and [packaging](../release/packaging.md). `source/scripts/check` runs from the packaged source checkout with the required tools. The clean release reused installed SDK/dependency caches; rebuilding without them requires dependency access. The original third actual commit, `39d2143`, already proved a real ledger golden and a failing changed expectation.

The final process inspection found one retained book JVM, around 97 MiB resident with a 128 MiB heap cap, and no sbt or Canton process. The complete release check reached about 3.21 GiB aggregate Java RSS while its bounded transient tools ran sequentially. Heap caps are not total resident-memory caps.

## Ready for an external conversation

The [unpublished release description](../release/public-description.md), [demonstration outline](../release/demo-outline.md), [feedback template](../release/evaluator-feedback.md), [application-team guide](../release/adopter-guide.md), and [maintenance boundaries](../release/maintenance.md) are prepared. No message, publication, workshop, outside-team confirmation, or adoption claim has been made.

The public development destination, project license and upstream distribution conditions, evaluator contact, feedback, demonstrations, and adoption evidence sit outside the implementation checklist. The [original proposal](../proposal/harmonia.md) retains their context and conditions.

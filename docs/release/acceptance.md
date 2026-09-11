# Local technical acceptance

The third draft is complete on `third-draft`. Its [verification and handoff record](../third-draft/acceptance.md) identifies the exact software, fresh ledger evidence, hashed archive, relocated launchers, browser checks, and measured resource use.

| Item | Accepted delivery |
| --- | --- |
| Verified software revision | `070f499f045b196cfab66c8bd34dd6c86c94dc8f` |
| Release archive | `.artifacts/release-7433717228274830633/harmonia-070f499f045b.tar.gz` |
| Archive SHA-256 | `694e69671fc329dd9e8b3047f133ac8c0c9ad6df859a39e678663c48875c933b` |
| Verification | Eleven passing clean-checkout gates, 45 Scala tests, four Daml scripts |
| Book and evidence | Nine chapters, 32 passing fresh recordings, 1,292 payload hashes |
| Local preview | `http://127.0.0.1:56007/`, using the relocated packaged launcher |

Start with the [principles and completed plan](../../THIRD-DRAFT.md), then the [four operation traces](../third-draft/reading-guide.md). Named models now carry operations, observations, workspace state, package facts, and book recordings through their producers and consumers. The book presents the same capabilities through concrete stories, independent expectations, observed results, and contextual source inspection.

All 67 preserved example files and both pinned source DAR identities remain unchanged. The source snapshot precedes this final documentation commit; the archive and recordings retain their verified software revision. The [second-draft acceptance](../second-draft/acceptance.md) and [first-draft acceptance](first-draft-acceptance.md) retain prior deliveries. `main` and `first-draft` remain at `d58c113`; `second-draft` remains at `c971845`.

Unpack the archive and run `./run-book` with Java 17. `./run-verify` checks the payload and uses Git for source identity. `./run-live` also needs Daml SDK 3.4.11; Ctrl-C releases its disposable network. See [setup](../../book/setup.md), the [developer walkthrough](../developer-walkthrough.md), and [packaging](packaging.md) for reproduction.

Final process inspection found one book JVM at about 96 MiB RSS with a 128 MiB heap cap, and no live runtime, sbt, Script, or Canton process. The full release reached approximately 3.07 GiB sampled aggregate Java RSS, excluding the preserved preview and native processes. Detailed measurements and local disk cleanup are recorded in the third-draft acceptance.

Acceptance applies to the local behavior in the [capability](../capabilities.md) and [compatibility](../compatibility.md) matrices. The reading guide supports review; no independent human-review outcome, production availability, or external adoption is claimed.

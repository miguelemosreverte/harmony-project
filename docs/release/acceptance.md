# Local technical acceptance

The second draft is complete on `second-draft`. Its [verification and handoff record](../second-draft/acceptance.md) identifies the exact software, fresh ledger evidence, release archive, relocated launchers, browser checks, and measured resource use.

| Item | Accepted delivery |
| --- | --- |
| Verified software revision | `b1ed52f38553888696fd512c9558a18f395a0ec5` |
| Release archive | `.artifacts/release-3265764795549403592/harmonia-b1ed52f38553.tar.gz` |
| Archive SHA-256 | `c3ce4ca8aa3c4f685d882228409a8d0fffbf2b7c1facfb75738c1c22c01bb978` |
| Verification | Eleven passing clean-checkout gates, 36 Scala tests, four Daml scripts |
| Book and evidence | Nine chapters, 32 passing fresh recordings, 1,266 payload hashes |
| Local preview | `http://127.0.0.1:56007/`, using the relocated packaged launcher |

Start with the [principles and completed plan](../../SECOND-DRAFT.md), then the [three operation traces](../second-draft/reading-traces.md). The book introduces the same operations through concrete stories, independent expectations, observed results, and contextual source inspection. The [interaction record](../second-draft/interaction-checks.md) covers live recovery, editing, consent, generation, and narrow layouts.

All 67 moved example files and both pinned source DAR identities are preserved. The source snapshot precedes this final documentation commit; the archive and recordings retain their verified software revision. The original delivery remains available in the [first-draft acceptance](first-draft-acceptance.md), and both `main` and `first-draft` remain at `d58c113`.

Unpack the archive and run `./run-book` with Java 17. `./run-verify` checks the payload and uses Git for source identity. `./run-live` also needs Daml SDK 3.4.11; Ctrl-C releases its disposable network. See [setup](../../book/setup.md), the [developer walkthrough](../developer-walkthrough.md), and [packaging](packaging.md) for reproduction.

The final process inspection found one retained book JVM at about 95 MiB RSS with a 128 MiB heap cap, and no sbt or Canton process. The full release reached approximately 3.03 GiB sampled aggregate Java RSS, excluding the preserved preview and native processes. Detailed measurements and their limits are in the second-draft verification record.

Acceptance applies to the local behavior in the [capability](../capabilities.md) and [compatibility](../compatibility.md) matrices. Public licensing, publication, external evaluation, and adoption remain outside this implementation plan. No external delivery or human-review outcome is claimed.

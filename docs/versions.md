# Version history

The five `0.1` snapshots explored code quality while preserving the first draft's
behavioral expectations. The `0.2` line revisits the original product documents.
These are development references, not published package releases. The branches and
tags are backed up in the private `miguelemosreverte/harmony-project` repository.

| Previous branch | Archive branch | Annotated tag | Commit | Purpose |
| --- | --- | --- | --- | --- |
| first-draft | archive/0.1.1 | v0.1.1-draft | d58c113 | First working implementation and independent goldens |
| second-draft | archive/0.1.2 | v0.1.2-draft | c971845 | Clearer ownership and compiler-checked structure |
| third-draft | archive/0.1.3 | v0.1.3-draft | 5345651 | More intuitive typed paths and smaller responsibilities |
| fourth-draft | archive/0.1.4 | v0.1.4-draft | bef8ac5 | Minimal production code; separate book and harness |
| fifth-draft | archive/0.1.5 | v0.1.5-draft | 7c62e69 | Further typed internal data flow |

All five historical builds declared `0.1.0`. The numbered archive names identify
successive drafts; they do not retroactively change the contents of those builds.
The tags have a `-draft` prerelease suffix for that reason.

On 2026-09-11, `main` advanced by fast-forward from the first draft to the fourth
draft, as requested. `version/0.2.0` branches from that fourth-draft commit.
The fifth draft remains independently available; its code is not silently merged.

The active build is `0.2.0-SNAPSHOT`. A future `v0.2.0` tag requires actual release
validation. `v0.2.0-design.1` identifies the verified design and quotation checkpoint.
There is no `v0.2.0` stable release tag yet. Review the [0.2 plan](0.2/PLAN.md)
for the distinction between design completion and runtime implementation.

Historical documents retain their original branch names and evidence revisions.
Use this table to resolve those names; do not rewrite recorded history as if it
had been produced on the new branches.

`v0.2.0-design.2` preserves the second UX pass. `v0.2.0-infographic.1` identifies
shared HTML infographic scenes and verified integration with the existing live
financing server. Its [verification](0.2/verification-3.md) distinguishes live
commands, recorded stories, and the remaining 0.2 release work. `main` is unchanged.

`v0.2.0-reference.1` implements the approved desktop and mobile composition using
the original illustration pixels, a four-beat purchase carousel, and shared book/live
header and evidence drawers. Its [visual contract and checks](0.2/REFERENCE-FIDELITY.md)
include side-by-side browser captures and a real financing handoff. This remains a
checkpoint on `version/0.2.0`; it does not change `main` or declare a stable release.


`v0.2.0-reader.1` shares measured diagrams across live composition, package stages,
and the book. It adds four reader entrances, exact source browsing with authored
annotations, diagram review, original passages beside reusable companion views,
and URL-driven presentation recording. The [reader contract](0.2/READER-EXPERIENCE.md),
[browser checks](0.2/reader-browser.json), and [live checks](0.2/reader-live.json)
record its scope and verification. The original golden expectations are preserved.


`v0.2.0-reader.2` extends the shared visual reader to all 32 registered recordings.
It adds documented source groups, parsed Daml dependency maps, reviewed execution
handoffs, complete evidence URLs, offline artifact inspection, and stable A4 diagram
printing. The [acceptance audit](0.2/COMPLETION-AUDIT.md) records the full scope and
verification; the [delivery report](0.2/reader-delivery.json) verifies the final mounted
book and a fresh sandbox. `main` remains the fourth-draft baseline.


`v0.2.0-reader.3` replaces the reader controls with finite, authored routes and
a two-interaction page budget. It separates product and book hosting, uses generic
product module documentation, introduces one-question composition pages, and
provides a [page walkthrough](0.2/WALKTHROUGH.md) with desktop/mobile screenshots.
All 32 recordings and exact original quotations remain preserved. Live financing,
consent, execution, adapter compilation and recovery were exercised on a disposable
network. This is a reader checkpoint on `version/0.2.0`; `main` remains unchanged.

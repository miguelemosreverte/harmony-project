# First interactive book verification

Verified locally on 2026-09-10. Scala/JVM and Scala.js compile with the pinned versions in [ADR 002](../../architecture/002-recorded-book.md).

## Ledger evidence and export

The regular `scripts/check` passed: all six live Canton goldens, Daml smoke checks, ten Scala tests, formatting, and both runtime builds. Evidence: `.artifacts/check-3966401245811045529/`.

A separate five-story run contains four passing direct/adapter examples and the intentionally wrong workflow expectation. It correctly exited with status 1 and recorded `$.actions[1].workflow`: expected `waiting`, observed `complete`. Evidence: `.artifacts/check-13200557612085035243/`.

That mixed run was exported into `.artifacts/book-preview/`. Playback does not start or query a ledger. A copy of one recording with its `actual.md` altered was rejected during export with `Recorded artifact changed`, identifying the exact file; the experiment is retained under `.artifacts/tampered-recording/`.

## Browser walkthrough

The rendered book was exercised in a real browser:

- The normal workflow starts on Alice's rejected attempt; **Next attempt** selects Northbank and shows application `approved` and workflow `complete` in both columns.
- Selecting the regression experiment highlights the differing step automatically. Both the detail row and full diff show expected `waiting` and observed `complete`, with the mismatch count displayed in text.
- Both chapters render, and all 25 links from their rendered content returned HTTP 200 from the local bundle.
- Desktop playback was inspected at 1440 pixels wide. At 390 pixels wide, the document width remained 390 pixels; the graph scrolls within its panel and brings the selected step into view. Chapter navigation wraps and comparison headings remain legible.
- Source revision, dirty-worktree status, recording time, and participant topology are visible or linked as provenance. Evidence files are directly inspectable.

Final screenshots are retained locally at `.artifacts/verification/06-book-desktop.png` and `.artifacts/verification/06-book-mobile.png`. The browser bundle is reproducible using the [playback guide](../../../book/playback.md).

## Scope

This edition visualizes recorded attempts and their contract observations. General graph semantics, participant-specific privacy views, live authenticated execution, and the composer remain later items in the PRD. Local checks do not constitute a hosted CI run or a published release.

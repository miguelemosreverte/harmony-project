# Complete reader path

The book has nine chapters and 32 recorded examples. Every chapter offers matching experiment buttons. Chapter 9 displays the core boundary measurements and the competing-command result as two independent verification phases; the underlying input, expected result, actual result, raw observation, and provenance remain available.

Added the glossary, extension guide, troubleshooting, current repository/Scala architecture, developer walkthrough, and `scripts/demo`. The source link audit and exported asset/recording link audit pass. The imported proposal is archival; its originally missing diagram assets are not part of that audit. External URLs and Markdown heading anchors are not checked automatically.

## Verification

- JVM compilation, Scala formatting, and Scala.js linking passed: `.artifacts/build18-final.log` and `.artifacts/build18-navigation.log`.
- `scripts/harmonia book-links .artifacts/book-purchase` passed for the current source documents, nine exported chapters, browser assets, and all six files for each recording.
- Browser walkthrough selected all nine chapters. Matching demonstration counts were 2, 4, 2, 4, 8, 6, 2, 3, and 3. Boundary playback showed both phases and no expected/actual differences.
- At 390 pixels wide, chapter and experiment navigation transferred focus to the main content and scrolled it into view. The active chapter carries `aria-current`; code/table regions are keyboard focusable. Chapter and laboratory pages had no document overflow. Existing graph scrolling remains local to its panel. Status words and numeric comparisons accompany color.
- One direct Java book server remained, around 106 MiB resident with a 128 MiB heap cap; no ledger or sbt server remained after these checks.

This development export contains individually attributed recordings from earlier increments. Step 19 replaces that mixed development set with fresh checks from one clean source revision. This walkthrough is an internal review; external evaluator feedback remains pending.

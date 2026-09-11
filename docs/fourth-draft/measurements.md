# Fourth-draft measurements

The source snapshot is `0014c0d7a3eb8a31f16a2ac4ee81b1ef320d5948`. Earlier branch snapshots are first `d58c113`, second `c971845`, and third `5345651`. Counts use committed Git blobs and physical lines, including comments and blank lines. They exclude dependencies, compiler output, recordings, and other ignored artifacts. This final documentation is written after the measured software snapshot.

## Across the drafts

| Measure | First | Second | Third | Fourth |
| --- | ---: | ---: | ---: | ---: |
| Committed files | 325 | 366 | 378 | 398 |
| Repository lines | 19,885 | 21,912 | 22,527 | 23,080 |
| Scala files | 99 | 128 | 137 | 147 |
| Scala lines including tests | 9,524 | 10,733 | 11,109 | 11,343 |
| Scala lines excluding unit tests | 8,999 | 10,002 | 10,173 | 10,354 |
| Median Scala file | 74 | 63.5 | 63 | 56 |
| Largest Scala file | 477 | 358 | 358 | 360 |
| Scala files over 150 lines | 19 | 20 | 20 | 19 |
| Scala files over 300 lines | 2 | 1 | 1 | 1 |
| Passing Scala tests | 27 | 36 | 45 | 49 |
| Golden recordings / chapters | 32 / 9 | 32 / 9 | 32 / 9 | 32 / 9 |

The total Scala source grew by **234 lines (2.1%)** over draft three. This is not a repository-wide code reduction. Separate entry points, explicit service configuration, independent runtime launching, and their checks account for new code. The improvement is a smaller, isolated product reading surface plus specific reductions in repeated representations.

## What a product reviewer sees

| Product source | Files | Lines |
| --- | ---: | ---: |
| Scala server | 38 | 2,899 |
| Shared Scala API | 10 | 393 |
| Scala browser | 10 | 960 |
| **Product Scala, excluding tests** | **58** | **4,252** |
| Product Daml | 20 | 873 |

The largest product Scala file is the 261-line source generator. No product Scala file exceeds 300 lines. Book Scala occupies 1,818 lines including 101 test lines; harness Scala occupies 5,244 including 859 test lines. Product boundary tests add 29 lines. These groups sum to the complete 11,343-line Scala count.

Draft three had no separately compiled product. Comparing all its 10,173 non-test Scala lines with today's 4,252 product lines would confuse relocation with deletion. The service now cannot compile against book, scenario-runner, demo-credential, or release types, and its runtime classpath contains only the service as an internal application JAR. Those are verified dependency changes.

## Actual simplification and relocation

Five public response files—composition, financing, packages, ledger updates, and workspace snapshots—went from 275 lines to 201. Including the new 11-line compiler-derived codec helper, that is **63 fewer lines (23%)** for the same transport fields. Wire-contract tests and the authenticated live gate preserve their behavior.

The composition editor went from 152 to 147 lines. Its larger improvement is semantic: it edits a `Vector[PlannedStep]` and validates a `Composition`, removing an intermediate draft representation and internal JSON round-trip. `Connections` replaces the redundant participant wrapper and separates supplied clients from network provisioning.

The migration's 53 retained product Scala files totalled 4,227 lines in draft three and 4,119 after the move and edits. The 108-line difference includes 29 lines removed from the product credential helper when demo signing moved to the harness; that portion is relocation. Five new product files add 133 lines for the explicit entry points, connection configuration, supplied connections, and codec convention. This accounting gives the final 4,252 product lines without claiming that moving code deleted it.

Golden-result validators, scenario interpretation, Canton setup, Script execution, release assembly, and book rendering now have visible homes outside `product/`. The live browser owns a 105-line stylesheet and its own JavaScript entry point; recorded playback owns its own assets. Prior plans and evidence moved under `docs/history/`. All 67 original golden input/expectation files remain byte-identical.

## Reproduce and judge

Use `git ls-tree -r --name-only REVISION` to enumerate the snapshot and `git show REVISION:PATH` to read each blob. Count `splitlines()` per blob; restrict to `.scala` for Scala totals and exclude `/src/test/` for non-unit-test totals. The local calculation and per-file lineage are retained in `.artifacts/fourth-measure.py` and `.artifacts/fourth-metrics.json`; golden hashes are in `.artifacts/fourth-example-preservation.json` and the committed [move manifest](../history/second-draft/example-moves.json).

Readability is a human review judgment. These are my provisional design scores, not measured performance or an independent review result:

| Judgment, 0–10 | First | Second | Third | Fourth |
| --- | ---: | ---: | ---: | ---: |
| Ownership is obvious | 4 | 7 | 8 | 9 |
| Typed operations | 4 | 7 | 8 | 8 |
| Product/harness separation | 3 | 4 | 4 | 9 |
| Minimalism | 5 | 5 | 5 | 7 |

The [reading guide](reading-guide.md) makes those judgments easier to test by following real operations. The [acceptance record](acceptance.md) supplies execution evidence and resource measurements separately.

# Harmonia, fourth draft

**Status:** implementation in progress on `fourth-draft`.  
**Preserved third draft:** `5345651`; verified software `070f499f045b196cfab66c8bd34dd6c86c94dc8f`.

## Purpose

Make the product small, beautiful, and immediately readable. A reader should see its contracts, operations, public data, and effects without first understanding a book, test network, scenario parser, or release system. Preserve the demonstrated behavior and independent expectations.

## Principles

1. Prefer ordinary named values and direct functions. Keep a related operation together; a shorter file obtained only by scattering it is not a simplification.
2. Product, book, and harness have real compilation boundaries and entry points. Product cannot import either of the other two.
3. The service receives connections and configuration explicitly. Disposable networks, synthetic participants, and golden input belong to the harness.
4. The book owns its chapters, recording models, export, and reader. Recorded playback consumes evidence files; live interfaces use the public HTTP contract.
5. Keep meaningful types, explicit decoding failures, and `IO`/`Resource` ownership. Authentication, bounded inputs, and uncertain submission reconciliation are runtime behavior.
6. Delete redundant representations and forwarding layers when the resulting operation reads more directly. Do not pursue fewer lines through dense syntax or cryptic names.
7. Report moved code separately from actual reduction. Use the same preserved scenarios to verify the result.

## Intended ownership

| Directory | Responsibility |
| --- | --- |
| `product/ledger` | Common interfaces, workflow core, supported applications and integration contracts |
| `product/server`, `product/api`, `product/web` | Service and tools, shared public types, live browser application |
| `product/packages` | Pinned inputs and reviewed mappings |
| `book/` | Authored chapters, independent reader, recording models, exporter and styling |
| `harness/runner`, `harness/ledger` | Golden execution, demo provisioning, verification, ledger tests and fixtures |
| `harness/tools` | Development/release command dispatch and assembly |
| `examples/` | Existing readable inputs and independent expectations |
| `docs/` | Current reading guide and explicitly historical evidence |

The service and live browser compile separately. The runner uses the service; book export uses recorded-story interpretation from the runner; development tools compose runner and exporter. The recorded browser has its own entry point and build. Shared source directories contain only the types actually needed by both JVM and browser, not a second general utility framework.

## Baseline

Physical committed lines include comments and blank lines, excluding generated output and dependencies.

| Measure | Draft 1 | Draft 2 | Draft 3 |
| --- | ---: | ---: | ---: |
| Repository lines | 19,885 | 21,912 | 22,527 |
| Scala lines including tests | 9,524 | 10,733 | 11,109 |
| Scala files | 99 | 128 | 137 |
| Median Scala file | 74 | 63.5 | 63 |
| Verified Scala tests | 27 | 36 | 45 |

Draft three's preliminary package classification has 1,677 book lines, 4,590 story/test/verification/release lines, and 4,842 remaining Scala lines. The remainder includes demo setup, and some parsing under stories is used by package tools. This is a baseline classification, not a claim that those groups already compile independently. The migration record must account for this distinction.

## Ordered work and commits

- [x] FD01: preserve the third draft, inspect actual coupling, and record principles and baseline.
- [ ] FD02: introduce a plain service context and move demo preparation behind it; establish independent product/book/harness builds and entry points.
- [ ] FD03: make the third actual commit record a working slice, with compiler boundaries and a real ledger/browser check against the unchanged golden.
- [ ] FD04: finish the filesystem migration, update build/package paths, and simplify product state and operation wiring without losing supported behavior.
- [ ] FD05: provide independently runnable artifacts and a short product reading guide; update export, source links, release assembly, and architectural evidence.
- [ ] FD06: run the complete clean-checkout gate sequentially; verify relocation, browser controls, independent expectations, archive identity, and owned process cleanup.
- [ ] FD07: publish local measurements of moved/deleted code, preserve one bounded book preview, and commit final acceptance.

## Acceptance

The product compiles and launches without book or harness classes on its runtime classpath. The book has its own compiled browser artifact and launcher. Demo setup remains separately usable through the harness. No golden expectation is regenerated to make a change pass; all 67 preserved files and the 32 example identities remain intact.

The complete release repeats the existing ledger, package, composition, boundary, portability, and browser proofs. Both pinned source DAR identities remain unchanged. One heavy stage runs at a time; resource measurements distinguish Java RSS from heap caps and native/system memory. Existing verified archives and the third-draft preview remain available until replacement is verified.

A final guide follows one operation through a small number of meaningful owners and explains the public boundary. Measurements report source moved out of product separately from implementation deleted or added. Human readability remains a review judgment rather than a number inferred from compilation.

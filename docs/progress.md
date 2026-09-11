# Implementation progress

The [PRD](../PRD.md) defines the ordered work. This record reports actual commits and evidence, independently of publication or external adoption.

## Current step

Step 15 adds authenticated live participant sessions, observed state/history, confirmed command results, and recovery from lost responses. The live acceptance story and direct authority checks pass. Next: the bounded workflow composer and its DAR input path.

## Commit evidence

Actual commit identifiers are recorded in the next commit after they are created, avoiding self-referential hashes. Git history is the authoritative record of committed files.

| Plan item | Commit | Verified behavior |
| --- | --- | --- |
| 01 | `6e99049` | Planning documents, local links, imported source provenance, and source exclusions |
| 02 | `04bb37f` | [Runtime verification](verification/02-runtime.md): compiled Scala/Daml and real Canton create/reject/approve/query execution |
| 03 | `39d2143` | [Golden proof](verification/03-golden-proof.md): real observed results match, and a changed expectation fails |
| 04 | `af521b7` | [Direct workflow proof](verification/04-direct-workflow.md): four live goldens and an intentional workflow mismatch |
| 05 | `b0559cb` | [Unchanged-source proof](verification/05-adapter.md): six live goldens and source identity checked before and after execution |
| 06 | `dabea08` | [Recorded book verification](verification/06-book.md): browser walkthrough, narrow layout, artifact integrity, and the regular suite |
| 07 | `8c42b3e` | [Authority and privacy proof](verification/07-privacy.md): seven live goldens, participant event controls, and browser perspective views |
| 08 | `b8feb4f` | [Progression proof](verification/08-progression.md): nine live goldens, distinct client processes, equal final effects, and rejected forged completion |
| 09 | `68c1950` | [Branch and join proof](verification/09-branches.md): eleven live goldens, invalid-definition rejection, and both routes in Chapter 4 |
| 10 | `64c365d` | [Package reproduction proof](verification/10-packages.md): pinned local/upstream inputs, two fresh executions, exact DAR retrieval, and negative resolution checks |
| 11 | `ed9a686` | [Financing-to-offer proof](verification/11-financing-offer.md): nineteen live goldens, scoped single-use evidence, four participant views, and Chapter 5 |
| 12a | `3136d45` | [Model separation](verification/12a-story-models.md): full build and nineteen unchanged ledger goldens |
| 12b | `f701344` | [Atomic transfer proof](verification/12-atomic-transfer.md): twenty-five live goldens, exact quantities, transaction evidence, and complete rollback |
| 13 | `3e80cfe` | [Binding generation proof](verification/13-binding-generation.md): two compiled and executed generated projects, twenty-two Scala tests, unchanged DARs, and Chapter 7 |

| 14 | `44cc384` | [Participation parity proof](verification/14-participation-parity.md): twenty-seven ledger goldens, two equal business scenarios, deterministic generation, and a caught compiled regression |

| 15 | This increment | [Authenticated live proof](verification/15-live-sessions.md): separate sessions, direct API permission denial, stale/repeated requests, browser reconnect, and response-loss recovery |

## External decisions

- Public repository destination and source-code license remain unselected; no publication has occurred.
- The proposal's imported BPMN/SVG assets were not included in the downloaded files.
- External evaluator contact, adoption, and committee acceptance require separate evidence.

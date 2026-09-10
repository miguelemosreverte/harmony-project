# Implementation progress

The [PRD](../PRD.md) defines the ordered work. This record reports actual commits and evidence, independently of publication or external adoption.

## Current step

Commit 03: both Markdown golden stories passed against actual Canton, and an intentionally incorrect expectation produced exit status 1 with a precise diff. The aggregate `scripts/check` also passed. Record this increment, then implement the direct-interface core in Commit 04.

## Commit evidence

Actual commit identifiers are recorded in the next commit after they are created, avoiding self-referential hashes. Git history is the authoritative record of committed files.

| Plan item | Commit | Verified behavior |
| --- | --- | --- |
| 01 | `6e99049` | Planning documents, local links, imported source provenance, and source exclusions |
| 02 | `04bb37f` | [Runtime verification](verification/02-runtime.md): compiled Scala/Daml and real Canton create/reject/approve/query execution |
| 03 | This increment | [Golden proof](verification/03-golden-proof.md): real observed results match, and a changed expectation fails |

## External decisions

- Public repository destination and source-code license remain unselected; no publication has occurred.
- The proposal's imported BPMN/SVG assets were not included in the downloaded files.
- External evaluator contact, adoption, and committee acceptance require separate evidence.

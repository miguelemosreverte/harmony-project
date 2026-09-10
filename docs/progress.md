# Implementation progress

The [PRD](../PRD.md) defines the ordered work. This record reports actual commits and evidence, independently of publication or external adoption.

## Current step

Commit 04 proves the direct-interface workflow: application approval and workflow completion commit together; rejected authority and rejected application actions leave progress unchanged. Next: prove a typed adapter against an unchanged source DAR.

## Commit evidence

Actual commit identifiers are recorded in the next commit after they are created, avoiding self-referential hashes. Git history is the authoritative record of committed files.

| Plan item | Commit | Verified behavior |
| --- | --- | --- |
| 01 | `6e99049` | Planning documents, local links, imported source provenance, and source exclusions |
| 02 | `04bb37f` | [Runtime verification](verification/02-runtime.md): compiled Scala/Daml and real Canton create/reject/approve/query execution |
| 03 | `39d2143` | [Golden proof](verification/03-golden-proof.md): real observed results match, and a changed expectation fails |
| 04 | This increment | [Direct workflow proof](verification/04-direct-workflow.md): four live goldens and an intentional workflow mismatch |

## External decisions

- Public repository destination and source-code license remain unselected; no publication has occurred.
- The proposal's imported BPMN/SVG assets were not included in the downloaded files.
- External evaluator contact, adoption, and committee acceptance require separate evidence.

# Version 6 — fewer words, same mechanism

Recorded: 2026-09-14 UTC. Based on version 5 at `0d218ea`.
Status: browser inspected; ready for review.

---

## User direction

Miguel asked for simpler words and fewer of them. Keep the diagram's arrangement,
boundaries, relationships and approved palette. Use authored SVG; no image generation.

## Edit

Remove the repeated subtitle, secondary field lists and duplicate output labels.
Shorten the action and return labels to “Run” and “Update progress”. Keep the
distinction between an eligible action and permission to execute it.

| Visible wording | Exact architectural meaning |
| --- | --- |
| Bindings / Allowed actions | The Binding DAR declares eligible application templates and choices, step kinds and roles. |
| Running workflow | Workflow instance and its current state. |
| Party / Action | The assigned actor and target choice. |
| Action / Needs permission | A Daml choice, subject to the source application's required authority. |
| Handoff / Pass results | Core continuation carrying outputs to another workflow definition. |

The [version 5 source citations and qualifications](design-v5.md#2-architectural-message-and-sources)
still apply. This is proposed architecture, not a statement of production
completeness. The simplified diagram does not claim arbitrary dynamic choice
execution or unconditional atomic execution. The accessible SVG description
retains the precise terms behind the shorter labels.

The left column is [read-v6.md](read-v6.md), now a short explanation of the whole
mechanism. Technical qualifications remain in this design record.

---

## Evidence

| Visible diagram text | Version 5 | Version 6 |
| --- | ---: | ---: |
| Words | 90 | 44 |
| Labels | 29 | 24 |

Counts include visible SVG text nodes only, excluding accessibility descriptions
and CSS. A word is a whitespace-separated token containing a letter or digit;
standalone punctuation is excluded. The reduction is 46 words, approximately
51%. This measures the edit, not whether the new wording is intuitive.

The diagram's structure, component positions, type sizes, colors and connections
are preserved. Only the two shorter action/return captions were recentered.
Source: [06-architecture.svg](06-architecture.svg); page: [v6.html](v6.html).

```sh
.artifacts/book-tools/bin/python book/navigation/explorations/architecture-structure/render-review.py 6
open 'http://127.0.0.1:56202/book/navigation/explorations/architecture-structure/v6.html'
```

Inspected desktop (1440 × 900) and mobile (390 × 844) screenshots. The complete
prose now fits both sizes without scrolling. The full diagram fits its viewport;
mobile still uses pan/zoom to read diagram detail. No raster assets are used.

Browser checks passed for label collisions, text clipping, all 20 component
labels inside their containers, Core/continuation and application/action nesting,
two interaction surfaces, and zero browser exceptions or failed resources.
The pan/zoom implementation is unchanged from the previously validated version.

Evidence in `.artifacts/architecture-structure-v6-review/`:
`desktop-complete.png`, `mobile-complete.png`, `mobile-choice-detail.png`,
`checks.json`, and `native-geometry.json`.

---

## User review

Pending.

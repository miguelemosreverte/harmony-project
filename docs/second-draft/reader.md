# The second-draft book and live workspace

**Target experience, not yet implemented.** This document is the presentation brief for [the second-draft plan](../../SECOND-DRAFT.md). The current working book remains the behavioral and content reference.

## The first ten minutes

The book opens at a readable introduction to one real story. The reader meets the bank and buyer, sees the starting state, and steps through an approval and the shared continuation it enables. A short explanation accompanies the selected attempt. Expected/actual results, participant visibility, and code are available at that point in the story.

The reader should leave that first encounter able to explain who owns the decision, what another participant learns, what the result enables, and where the observed behavior can be checked. Terms such as binding, interface, and workflow instance arrive when the story needs them.

## Three clear workspaces

| Workspace | Primary task | Visible context |
| --- | --- | --- |
| Read | Follow a chapter and its guided story | Current chapter, concise explanation, selected attempt, next experiment |
| Inspect evidence | Understand why an outcome is supported | Selected event/state, expected versus actual, participant view, raw evidence and code links |
| Run live | Perform the authenticated participant's available work | Current session, observed state, eligible commands, owned draft/package inputs, submission status |

Reading and evidence inspection share the selected example, attempt, and saved participant perspective. Opening evidence preserves the reader's position; returning restores it. A stable navigation address identifies a chapter/example/attempt when feasible. It never contains live credentials. A recorded perspective selector is labeled as a view of saved observations. Live identity comes from the authenticated session and is not a role selector.

## Book layout

The desired desktop composition is:

```text
┌────────────────────┬─────────────────────────────────────────┐
│ Harmonia           │ Chapter title                           │
│                    │ Short explanation and the concrete story│
│ Chapters           │                                         │
│  1  First story    │ [Starting state] → [Attempt] → [Result] │
│  2  Participation  │                                         │
│  …                 │ What happened, and why it matters       │
│                    │                                         │
│ Examples           │ Previous attempt       Next attempt     │
│ Glossary           │ Inspect evidence · Read the operation   │
└────────────────────┴─────────────────────────────────────────┘
```

Evidence opens beside the story when there is enough width and in a dedicated, reversible view on narrow screens. Long comparisons and code have local scroll regions. The chapter remains readable without opening every technical detail. The example catalog supports discovery beyond the guided reading order.

Preserve the current book's warm paper, restrained green accents, and readable typography as the starting visual direction. Use generous spacing and a comfortable reading measure. Establish a small shared set of colors, spacing values, text styles, buttons, notices, and panels. The final refinement should be judged on actual rendered pages and interactions.

## Live layout and interaction

The live workspace has an identity header and clearly named financing, composition, and package areas. Each area explains the work available to that session. Submission feedback remains next to the action or draft it concerns. The applicable last observed state remains visible during a connection failure and is labeled as such.

An editor owns its draft input, validation messages, selection, and focus. Server observations update the relevant observed-state components without replacing the editor. Components have explicit mounting/disposal and stable identities. Polling, listeners, and pending fetches stop when their owning view closes.

Financing follows the operation and result directly. Composition explains proposal, partner consent, eligible action, and completion. Package work explains acquisition, inspection, reviewed mapping support, compilation, and download. Detailed package IDs and raw traces remain available when they help answer an inspection question.

## Shared explanatory language

- Show actor, action, and observed result together for the selected attempt.
- Explain expected rejection as part of the story, with its actual effects visible.
- Use words, symbols, and values alongside color for status and differences.
- Distinguish recorded execution, current live observation, pending submission, uncertain completion, and definite rejection.
- Retain explicit missing/hidden observations; a private value is not equivalent to an empty value.
- Present execution-bound checks as measurements and verification phases, with the observations that support them.
- Keep code excerpts tied to the exact source revision and the operation being explained.
- Make source, expected output, actual output, raw evidence, and provenance reachable without placing all of them in the first paragraph.

The browser renders evidence and sends commands. It never calculates a second authoritative workflow outcome. A presentation model may select and label observed facts; it must preserve the independent comparison and expose omissions or decoding failures.

## Interaction evidence required during implementation

Verify the migrated financing slice first, then apply the same review to composition, packages, transfer, and the completed book:

1. Follow the opening story from chapter to attempt, evidence, code, and back.
2. Navigate by keyboard and verify meaningful focus after transitions; distinguish focus from selection.
3. At desktop and 390-pixel width, read a chapter, scroll its graph/code/table locally, and inspect a difference without document overflow.
4. Edit and reorder a composition while observed state refreshes; confirm that input, selection, diagnostics, and focus survive.
5. Observe pending, definite rejection, missing prerequisites, stale state, transport failure, uncertain submission, and successful recovery without misleading success or rollback claims.
6. Navigate repeatedly and confirm disposed components stop their polling/listeners and no duplicate submissions occur.
7. Browse every required recording and verify its story, expected/actual comparison, source links, and provenance.

Capture representative rendered pages and the interaction outcomes at the revision being reviewed. Static layout images alone do not establish the state and lifecycle behavior above.

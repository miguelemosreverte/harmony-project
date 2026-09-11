# The proposed 0.2 experience

> Historical first-pass design record (`v0.2.0-design.1`). The second pass is
> governed by [UX.md](UX.md), which supersedes this visual hierarchy and olive palette.

The book introduces the product through a concrete use case. The application
workspace lets the authorized participant do the work. They share a restrained
visual vocabulary, with separate implementations and responsibilities.

## Principal screens

| Screen | Purpose | Image and adjacent HTML | Source intent |
| --- | --- | --- | --- |
| Book overview | Explain the product and offer a reading path | `design/0.2/book-overview.{png,html}` | Abstract, objective, system context |
| Use-case chapter | Follow financing into an offer; inspect claims and evidence | `design/0.2/book-chapter.{png,html}` | Proposal lines 133–141 |
| Source coverage | Inspect quotation inclusion and locate every passage | `design/0.2/coverage.{png,html}` | User's quotation KPI; original acceptance and knowledge transfer |
| Workflow workspace | Show the current actor, source owner, next action, and history | `design/0.2/application.{png,html}` | Dapp scope; M2–M6; visible actors and progression |
| Application integration | Select, inspect, map, and understand build/export boundaries | `design/0.2/application-builder.{png,html}` | Both participation paths; M4–M6 |

Original generated files are copied into the repository unchanged. The exact
prompts are in `design/0.2/prompts/`; provenance records their hashes and built-in
image-generation origin. Rendered screenshots are separate `.rendered.png` files.
The comparison page places each concept beside its actual HTML rendering.

## Visual decisions

- Warm ivory background, dark text, and restrained olive emphasis.
- A persistent chapter rail on desktop, a keyboard-operable chapter menu on small
  screens, and one obvious next action in each context.
- Serif titles and readable native sans-serif controls. Thin rules establish
  grouping; diagrams show ownership and progression before technical details.
- Each quote identifies its original document and line range. Full passages sit
  in expandable source panels, with source views and intact files one click away.
- Inputs, expectations, explanations, and source are separate tabs. Their names
  remain concrete; no internal parser or build machinery appears as a user task.

## Prototype interactions

The book chapter supports next/reset, illustrated participant views, and tabs with
arrow-key navigation. It starts at the approved financing step to match its image.
The full quotation chapter remains independently accessible from the Source tab.

The workspace supports bank approval or rejection, a subsequent buyer action,
seller completion, history, and source-application inspection. Selecting another
illustrated actor changes the eligible sample action. This is not authentication.
Expandable prototype controls show pending, refused, stale, and disconnected
states without suggesting a successful ledger submission.

The builder supports the reviewed sample and a deliberately unsupported shape.
Inspection, mapping, and build/export views explain what real implementation must
do. They never claim an archive was uploaded, compiled, executed, or installed.

## Runtime integration contract

The future product UI consumes typed service observations and submits commands.
An optimistic animation must not create observed success. Keep command pending,
ledger committed, denied, failed, stale, and disconnected states distinguishable.
The role is established by the real participant session, not a browser dropdown.

The future book consumes a typed recording with independent expectations. It must
show the story revision and source package identities. The citation/coverage
artifact remains book-owned; production does not import it.

## Fidelity and scope

The HTML follows the image composition, hierarchy, palette, and controls using
actual semantic DOM elements. Native fonts, select controls, line wrapping, and
the generated image's subtle paper shading are not pixel-identical. No generated
image is used as a background to fake an implemented interface. The comparison
artifacts make the remaining differences visible for review.

The overview uses “Start here” instead of the image's unmeasured “6 min”. The
chapter rail highlights only the current chapter, correcting the generated image's
extra Overview highlight. The coverage page adds the complete counting methodology
and product claim table below the initial view. These are deliberate copy and
interaction corrections, not claims of exact pixel equivalence.

This stage delivers a working design prototype and validated source traceability.
Live UI implementation, new ledger behavior, independent adopter evidence, and
release publication remain the later unchecked commits in [the plan](PLAN.md).

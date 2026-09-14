# Three illustrated passages

Recorded: 2026-09-14 UTC. Previous revision: `1bb5e2d`.

## User direction

Miguel asked for short text and images that build the explanation together:
contracts on Canton, a unified interface, and how Harmonia enables reusable
composition. The existing chapter has too much prose between too few images.

---

## Pass 1 — what each illustration contributes

1. Two application contracts on Canton establish the starting point. Financing
   and custody are illustrative domains; each keeps its own rules and permissions.
2. The retained `09-contract-composition.png` shows what Harmonia adds: a shared
   StepAction calling convention used by a workflow contract. Canton already
   supports transactions across applications; the interface makes the coordination
   reusable rather than granting Canton a missing transaction capability.
3. The request/result boundary shows an application action and workflow progress
   inside one ledger transaction. Off-ledger preparation is explained briefly in
   text. The image does not imply all steps of a workflow commit at once.

---

## Pass 2 — continuity and restraint

Keep the existing chapter typography and white reading column. Alternate short
prose and figures in ordinary document flow. Use the same bank, vault, document
cards and application panels throughout. The second figure is retained unchanged.
Each new figure has one specific job; no toolbar, carousel or new navigation is
needed to read it. The images are displayed uncropped.

The field-level StepAction explanation, request reconciliation detail and
continuation validation remain in the prior revision and source audit. This
opening explains the mechanism before those details. Direct implementation and
the supported adapter path remain visible in the shorter chapter.

---

## Pass 3 — source and visual references

The proposal's Abstract seeks composition "without bespoke pairwise integration".
Section 2 defines on-ledger workflow state and step execution; its Builder section
describes project generation for existing applications. The inspected
`Action.daml`, `Process/Engine.daml` and `GenerateBinding.scala` ground the
interface, per-step transaction and adapter descriptions. External Canton and
banking context uses the primary sources already checked in the textbook audit.

The visual reference for both new images is `09-contract-composition.png`, itself
derived directly from the approved workflow reference. This reuses its pictograms
and panels, with no new artistic direction.

---

## Exact opening-image prompt

```text
Use case: infographic-diagram.
Create an inline illustration for the opening of a Harmonia architecture textbook. The supplied image is the visual and pictogram reference. Keep its exact family of navy sans-serif labels, white document cards with pale blue lines, softly shaded small bank and vault pictograms, rounded pale blue and neutral panels, and thin spacing. This is another page of the same illustrated explanation, not a new style.

Show two independent Daml application contracts on Canton BEFORE introducing Harmonia's shared interface. Square composition, designed to stay readable at 346 CSS pixels wide. A subtle large rounded background enclosure is labeled "Canton" at the top. Inside it are two equal side-by-side application panels: left very pale blue with the same small bank pictogram and a white document card, right light neutral with the same small vault pictogram and a white document card. Use exactly these three labels in total: "Canton", "Financing", "Custody". Financing belongs to the left contract, Custody to the right. Labels should be at least 60-70 pixels high in a roughly 1024 pixel wide image so they remain clear on mobile. The document cards must be prominent; the bank and vault are small domain cues, not giant buildings.

There are NO arrows in this opening illustration. It establishes two contracts in their own application boundaries, using the same ledger technology. Do not imply the contracts cannot already transact together. Remove the workflow contract, all StepAction labels and all connectors from the reference's structure. Preserve the recognizable bank, vault, cards and panel palette. Make the composition compact, calm and balanced, with safe margins and no large empty bands. No extra words, logos, status checks, locks over the entire network, selectors, people, legends, or fine print. Pure white background outside the enclosure.
```

---

## Exact ledger-boundary prompt

```text
Use case: infographic-diagram.
Create a compact inline illustration for the final passage of the same Harmonia architecture textbook. The supplied image is the style reference: navy sans-serif labels, rounded pale-blue and light-neutral panels, white document pictograms with blue lines, soft shadows, blue dotted connectors. Match that family exactly. No bank or vault is needed here.

Meaning: off-ledger software submits a request and reads its confirmed result. On-ledger execution commits an application action and that workflow step's progress together, atomically. This is ONE application action plus its recorded progress, not all actions in a multi-step process.

Square 1024 by 1024 composition for a 346 CSS-pixel reading column. Use two broad horizontal regions stacked vertically:
- Upper light-neutral rounded region titled "Off-ledger". Inside it one small white request-document pictogram, labeled "Request".
- Lower pale-blue rounded region titled "On-ledger". Inside it ONE clearly bounded white rounded transaction enclosure, labeled "One transaction". Within that SAME enclosure are two equally sized small document pictograms with large labels beneath: "Application action" and "Workflow progress". Put each of these two labels on two lines, and use wide enough columns to avoid crowding. These are the two things committed together.

Exactly two blue dotted connectors occupy dedicated gaps at the left and right of the central composition. The LEFT connector travels from the upper request card down to the boundary of the ONE transaction enclosure, and has ONE downward arrowhead at that enclosure. The RIGHT connector travels from the transaction enclosure back up to the request area, and has ONE upward arrowhead. Arrows must stop outside boxes and must not touch text or intersect the On-ledger heading. Do not connect directly to just one of the two inner documents. These arrows express request and result; the surrounding prose explains their direction, so put NO words on the arrows.

Exact text only: "Off-ledger", "Request", "On-ledger", "One transaction", "Application action", "Workflow progress". Use large navy typography, about 60 pixels high in a 1024-pixel-wide image, readable after scaling to 346 CSS pixels. Prefer smaller icons and compact panels to smaller labels. Generous clearance at each arrow endpoint and around every label. No code, fields, StepAction label, people, process steps, status badges, buttons, logos, or legend. Pure white outer background.
```

## Generation and review

Both new images used the built-in `image_gen.imagegen` tool, referencing
`09-contract-composition.png`. That existing middle image remains unchanged.

Original outputs are preserved under
`/Users/miguel_lemos/.codex/generated_images/01a08cd4-eaa1-7bd2-83c1-bf9bf78e55de/`.

| Workspace image, beside this record | Started / finished (UTC, 2026-09-14) | Original output |
| --- | --- | --- |
| `10-canton-contracts.png` | 09:06:48 / 09:07:12 | `exec-a654811f-abae-4db1-87ee-e7fcfd080361.png` |
| `11-ledger-transaction.png` | 09:06:48 / 09:07:42 | `exec-b75ed533-6d60-4263-a3fb-1cfa63ca8a90.png` |

No raster edits or crops were applied.

- Opening SHA-256: `9ecbe330f280b7ae674e2694f7c6a3993f5f4c0dd089721db8c940ba62c5aace`.
- Boundary SHA-256: `5d54ac68d5eebcd419231fdaacd7230a71ee22145b4b38b20b5cffb642ef9d45`.
- Git revision: the commit introducing this record and the two images;
  locate it with `git log --diff-filter=A --format='%h %s' -- book/navigation/explorations/architecture-structure/textbook-passages.md`.

Assistant review: the bank, vault and document pictograms remain recognizable
across the first two figures. The opening's Canton enclosure identifies the
shared technology, not a deployment topology or a claim that all data is public.
In the final figure, both updates sit inside one transaction boundary. The two
arrowheads point down for submission and up for the result, without crossing
labels. There are no extra control-like elements or field lists. Further image
variants are not needed to review this explanatory sequence.

The chapter now alternates three short passages with three illustrations. Browser
checks passed at 1440×900 and 390×844: all three images load, preserve their natural
aspect ratios and fit the column; all passages and the ending are reachable;
there are two citation links, no buttons or selectors, no horizontal overflow
and no browser errors. Desktop/mobile screenshots were inspected, including the
first-to-second passage transition and the final adapter explanation. Evidence:
`.artifacts/architecture-passages-review/`.

The shorter opening does not claim complete coverage of the original proposal.
Its detailed sources and earlier explanations remain in `text-review.md` and Git
history. User review of this illustrated edition remains pending.

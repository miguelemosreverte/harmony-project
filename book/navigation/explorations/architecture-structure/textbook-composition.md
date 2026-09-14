# Contract composition — textbook revision 4

Recorded: 2026-09-14 UTC. Previous reading revision: `a7fe677`.

## User review

Miguel asked to follow the approved infographic style and focus the architecture
on the interface, contract composition, and the on-ledger/off-ledger division.
Scala and the browser do not need a component tour in this chapter.

---

## Pass 1 — the message

Independent application contracts expose a common action interface. Workflow
contracts can refer to that interface, invoke eligible actions and record
progress. The application's business authorization still applies. Off-ledger
tools prepare supported integration code and submit/read ledger requests.

Source: original proposal, Abstract and §2, Core Layer and Project Builder.
Actual call shape: `product/ledger/interfaces/daml/Harmonia/Action.daml`.
Composition: `product/ledger/core/daml/Harmonia/Process/Engine.daml`.
Direct implementation and separate adapter are alternative participation paths.

---

## Pass 2 — placement and scope

The illustration follows the interface explanation. Its labels are therefore
already meaningful. It shows the shared calling convention across distinct
application contracts, not a complete business story, a simultaneous call or
an atomic transaction spanning both applications.

Financing and custody are illustrative domains from the original proposal.
The illustration does not claim both are implemented through the exact same
example contract. Runtime checks and atomicity are described in the prose.

---

## Pass 3 — use the approved visual reference directly

Reference:
`design/0.2/infographic/narrative-v1/desktop-reference.png`.

Use rounded application panels, white document pictograms, navy labels and blue
dotted connections. Each application keeps its interface card inside its own
boundary. The previous picture of large buildings and books is retained in
`08-canton-textbook.png` but is superseded in the chapter.

The workflow is above the applications to leave space for two connections.
Repeated StepAction labels have a specific purpose: the reader sees the same
interface on different application contracts. No fields or arrow labels are
added because the prose explains them.

---

## Exact generation prompt

```text
Use case: infographic-diagram.
Create ONE inline textbook infographic about Harmonia contract composition. The supplied image is the approved STYLE reference: copy its rounded pale-blue and light-neutral application panels, white document pictograms with blue lines and gentle shadows, dark navy sans-serif labels, and thin dotted blue connectors. The picture must feel like another illustration from that exact book. Do not imitate the later monumental 3D bank-building illustration.

Content and meaning:
A workflow contract can invoke independently owned application contracts through the same shared Daml interface, StepAction. Each application retains its own business rules. This diagram shows invocation relationships, not a numbered sequence, not simultaneous settlement, not a network topology. The surrounding textbook explains actor, subject and ExecuteAction before this figure.

Layout: a compact, balanced landscape 3:2 figure on white, with no browser chrome. One smaller light-neutral rounded panel centered above two equal rounded application panels. The top panel contains a white document pictogram and the large label "Workflow contract". Below left is a pale-blue panel with a modest bank pictogram (use the scale and softly shaded style of the bank in the reference), titled "Financing". Below right is a light-neutral panel with a modest vault/document pictogram, titled "Custody". Within EACH lower panel, near its top, show a white rounded contract card with the SAME large label "StepAction". The two cards must have identical styling and scale. Each lower panel's pictogram sits beneath its StepAction card inside that same panel, communicating its own application implementation.

Draw exactly two thin dotted blue arrows from the lower-left and lower-right edges of the workflow panel respectively to the top edges of the two StepAction cards. Place the entire arrows in dedicated whitespace, terminating outside the cards with comfortable clearance. Do not connect the two application panels to each other. Do not put a separate shared server, central gateway or electrical plug between them.

Only these exact text labels: "Workflow contract", "Financing", "Custody", "StepAction" (twice). No other words or abbreviations. All five labels must be large and legible when the whole figure is displayed at 346 CSS pixels wide; use very large clean navy sans-serif, no small print. No tiny fonts on arrows, no field lists, no decorative legends. The two application panels and top workflow panel should fill the frame with modest safe margins; avoid large empty bands.

No people, no avatar initials, no use-case storyline, no numbered steps, no progress dock, no clickable-looking buttons, no logos, no green, no status ticks, no sockets, no coins, no base platform, no large photorealistic buildings. This is a calm technical illustration. Preserve the soft pictogram treatment and flat panel geometry of the approved reference.
```

## Generation and review

The first candidate, `09a-contract-composition.png`, matches the approved panels
and pictograms. Its raster labels are too small when scaled to a mobile reading
column. A second pass prioritizes label size in a square composition while
preserving the same content and relationships.

### Exact mobile-legibility refinement

```text
Edit the supplied Harmonia infographic for mobile legibility. Preserve its approved illustration style, palette, exact five labels, meaning, icons and three-panel hierarchy. This is a typography/layout refinement, not new content.

Produce a SQUARE 1024 by 1024 composition. Workflow contract stays above the two side-by-side application panels. Every label should be about 60 to 68 pixels tall in this 1024-pixel image, so that it reads at normal body-text size when the whole illustration is only 346 CSS pixels wide. Prioritize these large labels over icon size. Break "Workflow contract" into two centered lines if needed. Widen the white StepAction cards enough to comfortably fit "StepAction" at the requested large size; reduce their decorative document lines and height as needed. Both StepAction cards must be identical in shape and typography.

Keep exactly two dotted blue arrows, one to each StepAction card. Arrow starts must lie outside the workflow panel and clear all text; arrowheads must stop just outside each card. Keep the financing bank and custody vault as small supporting pictograms within their respective rounded panels. All labels and illustrations must be inside safe margins and have ample clearance. No extra words or icons. Exact labels: "Workflow contract", "StepAction", "StepAction", "Financing", "Custody". White background, navy sans-serif, pale-blue financing panel and light-neutral custody panel, soft white document cards. No new legend, fields, status marks, buttons, people, or small print.
```

### Generation provenance

Both calls used the built-in `image_gen.imagegen` tool. The original outputs are
preserved under `/Users/miguel_lemos/.codex/generated_images/01a08cd4-eaa1-7bd2-83c1-bf9bf78e55de/`.

| Candidate | Input reference | Started / finished (UTC, 2026-09-14) | Original output |
| --- | --- | --- | --- |
| `09a-contract-composition.png` | Approved `desktop-reference.png` named above | 08:48:39 / 08:49:29 | `exec-89df42c7-aadc-4582-8bb4-f3bf6800d1a6.png` |
| `09-contract-composition.png` — selected | `09a-contract-composition.png` | 08:50:28 / 08:51:12 | `exec-444ca2c2-1cd4-49ff-92f7-75e773c8602f.png` |

Both copies live beside this record; no raster editing was performed.

- First candidate SHA-256: `29236c5bb5639e0894f1c4156816b1949f25ea2b78c1752ca83f2ca2eaee883e`.
- Selected candidate SHA-256: `eb6c48c766b82b0bd8a4d9fe5589ab326db8b8e12f2b12044e39ee40cc4a72d1`.
- Git revision: the commit introducing this record and the selected image;
  find it with `git log --diff-filter=A --format='%h %s' -- book/navigation/explorations/architecture-structure/09-contract-composition.png`.

### Assistant review

The selected image preserves the approved application panels and document/bank
pictograms. The five labels are correct and remain readable in the mobile
column. The larger text and square composition resolve the first candidate's
legibility issue. Both arrows terminate above their target cards, without
crossing labels or pictograms. The picture is passive and contains no controls.

The image shows an invocation relationship; it does not establish that both
applications act in one transaction. The preceding paragraph limits the atomicity
claim to an application action and that step's recorded progress. The prose also
explains application authority; this is not inferred from the picture alone.

The 406-word chapter and selected image were checked at 1440×900 and 390×844.
All four sections render, image loading and natural aspect ratio pass, both
on-ledger and off-ledger responsibilities are present, and the chapter no longer
contains the Scala/browser/HTTP component tour. There are two citation links,
no buttons or selectors, no horizontal overflow and no browser errors.
Screenshots were visually inspected, including the illustration and off-ledger
section. Evidence: `.artifacts/architecture-composition-review/`.

User review remains pending.

# A textbook opening

Recorded: 2026-09-14 UTC. Parent revision: `f0d3e14`.

## Reader and message

Miguel asked to begin with Canton and its use by banks, explain coordination
across contracts, and introduce the shared interface from that need. He permits
inline generated illustrations again. This is a reading chapter, with the
established white, navy and pale blue design, not a replacement dashboard.

---

## Pass 1 — establish the facts

Canton already supports transactions across independently developed Daml
applications. Harmonia supplies reusable workflow rules and progress on that
ledger. The problem is the repeated work of implementing coordination, not the
existence of multiple contracts or a lack of Canton interoperability.

Lloyds' January 2026 announcement describes a pilot. It is evidence of banking
use of Canton, not evidence that Lloyds uses Harmonia. No network growth metric
is needed to explain the architecture.

---

## Pass 2 — let the prose carry the explanation

The chapter begins with the banking context, then independent application
actions, then the shared interface, then workflow enforcement and runtime
components. Each term is used after its introduction. The interface's actor,
subject and executable action are explained as a concrete call boundary.

The illustration belongs immediately after the Canton introduction. It shows
two institutions keeping separate records while agreeing on a transaction. It
does not attempt to explain the whole architecture before the reader has any
context. The generated buildings are generic and imply no bank endorsement.

---

## Pass 3 — visual constraints

Use the palette and restrained depth of `01-structure.png`. Preserve distinct
records, use only three large labels, and leave the explanatory caption in live
HTML. The illustration is conceptual, not a Canton node topology or a claim
that all contract data lives in a single shared database. It is displayed whole,
with its original aspect ratio, without cropping or interactive controls.

---

## Exact generation prompt

```text
Create one restrained editorial illustration for an engineering textbook about Canton. Use the supplied Harmonia image ONLY as a style reference: white, navy, ice blue, gentle depth, crisp geometry, generous whitespace. Do NOT reproduce its architecture diagram or any of its labels.

The paragraph this illustrates says: "Banks need to agree on transactions without exposing every customer's records. Canton lets organizations use shared smart contracts while controlling who sees each part of a transaction."

Make a simple wide 3:2 illustration on pure white. On the left, an elegant low-relief bank building behind its own navy-and-blue ledger book. On the right, a distinct modern securities custodian building behind its own separate ledger book. Both groups are equally prominent, comfortably separated. Between them, show exactly one small shared transaction document on a pale blue oval foundation, visually connecting the two institutions without merging the two books. Keep both books beside their respective institutions. This is a conceptual illustration of cooperation with separate records, not a network deployment topology.

Only three labels, exact spelling, in very large dark navy sans-serif: "Bank" below the left institution, "Custodian" below the right institution, "Canton" centered on the pale blue foundation beneath the shared document. Typography must remain clearly readable when the whole image is only 340 CSS pixels wide. Keep all subjects and labels inside generous safe margins. Avoid large empty vertical areas; compose tightly enough to sit within a textbook column. The bottom Canton foundation may gently span both institutions, but it must not look like one central database.

No arrows, no plug icons, no electricity, no padlocks, no extra text, no long explanatory title, no dashboards, no buttons, no company logos, no flags, no coins raining down, no people, no cartoon faces, no neon, no green. Quiet, precise, adult, approachable. Three objects of attention: bank with its book, custodian with its book, their shared transaction. All information beyond these simple relationships belongs to the surrounding live HTML prose.
```

## Generation and review

Tool: built-in `image_gen.imagegen`, with a referenced image for visual style.

- Reference: `book/navigation/explorations/architecture-structure/01-structure.png`.
- Started: 2026-09-14 07:45:12 UTC.
- Finished: 2026-09-14 07:46:02 UTC.
- Original: `/Users/miguel_lemos/.codex/generated_images/01a08cd4-eaa1-7bd2-83c1-bf9bf78e55de/exec-be266c5d-ceaf-4e9e-8f5c-9801fb2e3cb0.png`.
- Committed copy: `08-canton-textbook.png`; original preserved, no raster editing.
- SHA-256: `d4bf9b64f7250539ae5f9039a491211455431b30e445fd6ba3f63271617c1f42`.
- Git revision: the commit introducing this file and `08-canton-textbook.png`,
  retrievable with `git log --diff-filter=A --format='%h %s' -- book/navigation/explorations/architecture-structure/08-canton-textbook.png`.

Assistant review: the three labels are correct, the institutions and books remain
distinct, and the palette fits the existing reading page. At 390 px viewport
width the labels remain legible and the image is uncropped. The picture conveys
the banking context, not the internals of Canton or a full Harmonia architecture.
The surrounding prose carries the selective-visibility claim; a drawing of
separate books alone cannot explain the privacy protocol. No further image
iteration is justified before the user reviews this new textbook direction.

Desktop and mobile screenshots were inspected. Evidence is in
`.artifacts/architecture-textbook-review/`; all sections load, the image retains
its natural aspect ratio, and no horizontal overflow or browser errors were found.
The chapter contains two citation links and no buttons or selectors.

Miguel's review, 2026-09-14: the textbook direction is getting better; images
should follow the previously approved infographics. The architecture should
focus on the shared interface, composition and on-ledger/off-ledger division.
The next revision is documented in `textbook-composition.md`.

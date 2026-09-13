# The workflow as a document

Export what the recorded reader shows into JSON so a reviewer can inspect the
story without clicking through it. A workflow document should answer: what does
this demonstrate, what happened at each step, what did the reader see, and where
does the evidence come from?

The first comparison is `financing-approved` versus `purchase-approved`. Both
depict a bank signing an approval, but the purchase continues into a property
proposal. Their use of the same image must be visible in the documents.

## Read the documents

Start with [the comparison](documents/comparison.json), then open
[financing](documents/financing-approved.json) and
[purchase](documents/purchase-approved.json). The [catalog](documents/catalog.json)
lists all 32 scenarios, 130 observed steps and 55 illustrations. Each image lists
every story and step that uses it; `approval-signed` has 18 uses.

The exporter writes about half a megabyte of JSON and references the existing
image files. It uses one temporary browser tab and needs no additional JVM.

| File | Purpose |
| --- | --- |
| `capture.mjs` | Obtain the current book's records and Scala projections; compare the visible artwork and dialogue |
| `export.mjs` | Assemble documents, verify references, and check reproducibility |
| `documents/catalog.json` | Story inventory, image catalog, original passages and rendering dependencies |
| `documents/comparison.json` | The two approval examples, including their different next steps |
| `documents/<story>.json` | One complete ordered scenario |

The source explorer groups this exporter under **Workflow presentation documents**.
Generated JSON is excluded from the source-code catalog, just as recorded evidence
is: cataloguing its own dependency hashes would create a circular build.

## JSON conventions

`purpose` is the current recording's description, preserved verbatim. It is not
a newly inferred marketing claim. `steps` is ordered and each step has a stable
identity. Selecting `steps[1]` corresponds to `step=1` in a laboratory URL.

Each step records its actor and action, image identity, dialogue, diagram state,
observed values, independent expectation reference, and navigation. For example:

```json
{
  "id": "bank-approval",
  "actor": "Northbank",
  "action": "approve-financing",
  "open": "laboratory.html?story=financing-approved&step=1",
  "illustration": "approval-signed"
}
```

The image identity resolves through `catalog.illustrations`. Its entry contains
the image's repository path, SHA-256, dimensions, descriptive text and occurrences.
Bytes are not embedded into every step. Dialogue retains the renderer's speaker,
portrait identity and exact words.

The stable part of the diagram appears once in `diagram.shared`. Each step supplies
all varying fields in `step.diagram`. Reconstruct it with a shallow merge:

```js
const frame = {...workflow.diagram.shared, ...step.diagram};
frame.observation = step.observation;
const speech = ({speaker, portrait, text}) => ({name: speaker, portrait, text});
frame.conversation = {
  first: speech(step.dialogue[0]),
  second: speech(step.dialogue[1]),
  illustration: step.illustration
};
```

Pass that value to `renderHarmoniaScene` when `diagram.renderer` is `scene`, or
`renderHarmoniaDiagram` when it is `diagram`. Every reconstructed frame is checked
for equality with the original typed projector output. A step is independent:
do not accumulate changes from earlier steps.

`navigation.progress` lists the carousel colors/states in the same order as
`steps`. `previous` and `next` preserve the actual laboratory destinations, including
transitions into another scenario. File paths are relative to the repository root;
navigation URLs are relative to the selected book directory, not this JSON folder.

`recording.file` identifies the complete source record. `expected_pointer` selects
the independent expectation within it, using slash-separated object keys and array
indexes. The expectation is referenced instead of copied beside every observation.
Observed object keys are sorted consistently; action and dialogue arrays preserve
their meaningful order. A mismatch is exported as `matches_expectation: false`.

If the served book uses a different run, the exporter retains that exact record in
`documents/recordings/`. Historical evidence files are linked only when their
hashes match the selected record's provenance. Original source documents and
chapter-level passage assignments are in the catalog; there is no invented
step-to-quotation coverage claim.

The rendering manifest identifies the loaded styles, scripts and images. Their
served bytes must match the repository, except the two explicitly marked session
bindings. JSON carries the content and state; the referenced renderer and assets
provide its appearance. To reproduce the website outside this repository, those
dependencies must also be packaged. This export is a documentation format, not a
self-contained website archive.

## Generate or verify

Use an existing local Chrome debugging endpoint and the recorded laboratory:

```sh
node book/showcase/export.mjs \
  http://127.0.0.1:61322 \
  http://127.0.0.1:54724/source/design/0.2/laboratory.html
```

The ports belong to this running session. An alternative output directory may
follow the URL. Repeat the command with `--check` to require byte-identical JSON.
This check also revisits every observation and verifies the visible illustration,
dialogue, complete reconstructed frame, image contents and navigation references.

## What the study reveals

These are 32 recorded scenarios, not 32 separate customer journeys. The purchase
success recording includes eight attempts: five successful actions and three
refused shortcuts. The public chapter deliberately chooses the main handoffs;
the laboratory keeps all attempts for a reviewer.

The standalone financing example ends after Northbank approves. Its next arrow
opens the generated-adapter example. In the purchase recording, approval is
followed by a refused attempt to forge the proposal, before the ordinary offer
preparation. The JSON preserves that current behavior. This distinction helps
review whether a destination suits a product reader or a regression reviewer.

The current export covers the complete recorded laboratory. Authored chapter
introductions, their alternative-ending carousel and live workspaces require
separate export documents; they should keep their own purpose and provenance.
In particular, a live export would describe an observed session state rather than
promise that visiting its URL replays an earlier ledger transaction.

## Implementation record

- [x] Capture the existing reader's structured projections in an isolated tab.
  - [x] Read the selected book's recordings, including supplied run overrides.
  - [x] Use the compiled Scala projectors that already supply the UI.
  - [x] Check the displayed illustration and dialogue against each projection.
- [x] Export one readable document per workflow and one shared catalog.
  - [x] Preserve story purpose, ordered actions, observed values and navigation.
  - [x] Keep a stable diagram once; describe each step's varying fields separately.
  - [x] Reference images once by identity, path and hash; list every use.
  - [x] Link recording provenance, independent expectations and original sources.
  - [x] Record renderer dependencies needed to reproduce the presentation.
- [x] Verify the complete export and publish the two comparison links.
  - [x] Reconstruct every diagram and compare it with the existing projector.
  - [x] Check all image, source, evidence and navigation references.
  - [x] Repeat the export and require identical JSON.
  - [x] Commit and push the documents and exporter.

Verified on 2026-09-13: all 130 projected frames matched their reconstruction;
all 130 visible image/dialogue selections agreed; all 55 illustration hashes
matched the served files; a repeated export produced identical JSON. All 34 JSON
documents also matched byte-for-byte when requested from the running book server.
The edition's 28 source, quotation, recording and rendering checks also passed
after adding the exporter to the source explorer.

## Boundaries

This belongs to the book. It adds no product controls, ledger commands or new
execution model. The current approved illustration and infographic stay intact.
An illustration is explanatory artwork, not execution evidence. Reusing an image
does not mean two stories share a ledger transaction.

JSON references the existing image files rather than embedding their bytes or
copying them. Original source quotations retain their existing source identity;
an association with a chapter does not certify that its requirements are complete.

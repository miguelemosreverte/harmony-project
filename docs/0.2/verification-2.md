# Second UX pass — verification

The second pass follows [UX.md](UX.md) on `version/0.2.0`. It changes the design
edition and its documentation. It does not change product contracts or claim a
fresh ledger evaluation. `v0.2.0-design.1` preserves the previous interface.

## Problems reproduced and corrected

| Observed problem | Correction |
| --- | --- |
| Markdown headings, table pipes, and PlantUML appeared as ordinary prose | Real Markdown rendering; diagram programs and exact audit text are separate disclosures |
| Original HTML diagram labels were flattened into text fragments | Two supplied SVGs are rendered as isolated diagrams, with readable native sizing and a full-size link |
| A decorative HTML comment broke the exported component SVG | Export removes comments that are invalid XML; both SVGs are XML-validated and browser-loaded |
| Chapter 03 had two competing destinations | One canonical chapter; the old entry redirects while retaining query and fragment |
| The first screen exposed too much without a clear route | Five reader intentions, one expanded description, a named starting action, and a short route |
| Book prose concentrated on implementation progress | Narratives begin with Alice, Northbank, Ben, Sofia, or the four transfer parties and their goals |
| Sandbox, tabs, role, and builder state lived only in memory | Validated query state drives rendering; cold links and browser history reconstruct it |
| Workspace actions and the next actor were unclear | One current task, explicit ownership, refusal/recovery text, and a named handoff |
| Source audits and raw state dominated the page | Read/Try/Sources/Evidence modes and secondary disclosures |
| Olive green had no source-derived branding basis | Neutral blue default; optional source-derived dark appearance and warm Paper, with three reading sizes |
| Mobile readers reached too much setup before the sandbox action | Short introduction, one primary scenario selector, visible playback controls, person highlighting inside evidence |
| Print initially used the wrong scenario table and awkward source breaks | Selected recording table and current outcome, light print tokens, source pagination, and preserved interactive state |

During implementation, browser assertions also caught and helped correct an
appearance-selector event bubbling bug in the audience chooser. Navigation links
are refreshed when preferences change, while the design-review reproduction links
retain their explicitly captured appearance.

## Recorded evidence

Four preserved fourth-draft recordings are committed under
`book/edition-0.2/recordings/`. Their provenance names revision
`0014c0d7a3eb8a31f16a2ac4ee81b1ef320d5948`. The manifest records the original
export fingerprint and each selected recording's fingerprint.

The builder verifies the recording bytes, revision, input and expectation hashes
against the current committed Markdown stories, and equality of the recorded
expected/observed result. Every displayed action result must equal the corresponding
entry in that comparison. This prevents the presentation from inventing a result.

| Recording | Attempted actions | Refused actions |
| --- | ---: | ---: |
| Purchase approved | 8 | 3 |
| Purchase rejected | 3 | 1 |
| Transfer approved | 9 | 3 |
| Transfer final leg rejected | 6 | 1 |
| Total | 26 | 8 |

A committed negative assessment is displayed as a committed transaction with a
rejected financing decision. A refused final transfer displays ten source units
still locked, zero at the destination, and the unchanged waiting workflow.

## Checks actually run

- **19 Python checks passed** with `scripts/build-design`: deterministic generated
  output, every original quotation once and unchanged, local links/anchors,
  semantic Markdown, valid SVG, absent-attachment handling, source-style isolation,
  recording pins, and deliberate quote/source/recording/expectation corruption.
- **101 browser cases passed**, recorded in [browser-results-2.json](browser-results-2.json).
  These are cases, not a count of distinct assertions: several exercise many actions.
  They cover five reader routes, 26 recorded actions, workspace completion/refusal,
  disabled pending/refused/stale/disconnected states, builder supported/unsupported
  paths, evidence-tab keyboard navigation, appearance, Back/Forward, and cold replay.
- Layout checks covered **17 pages at four widths**: 390, 768, 1280, and 1536 pixels.
  All ten chapters' source passages were opened with large text at narrow width.
  The main source chapter was also checked under all nine theme/text combinations.
  No document-level horizontal overflow or broken eagerly loaded image was observed.
- URL checks covered invalid/repeated/unknown parameters, bounded steps, nested
  source disclosures, the legacy redirect, preference propagation, and exact design
  review links. The mobile route menu was opened and closed through its real control.
- Clipboard-denied recovery was tested with a temporary rejected clipboard adapter;
  the copyable fallback contained the exact current URL. The host clipboard was not
  overwritten. Native OS clipboard permission behavior remains browser-dependent.
- A `file://` cold link replayed the rejected purchase without the preview server.
  With JavaScript disabled, the browser accessibility tree still contained the
  narrative and static recorded-action tables.
- Browser exception collection reported **zero uncaught page exceptions** during
  the automated run. This is a scoped observation, not a claim that all bugs are absent.
- **Nine screenshots** are committed under `design/0.2/review-2/` and linked from
  [the design review](../../design/0.2/review.html). Desktop, mobile/large text,
  readable source, coverage, workspace, builder, Dark, and Paper were inspected.
  The original component diagram was additionally opened and visually inspected.
- A **four-page tagged PDF** was generated from the failed transfer. All four pages
  were visually inspected. A focused print recheck after pagination polish verified
  light colors, source block pagination, hidden inactive controls, and unchanged
  interactive state. [Open the PDF](../../design/0.2/review-2/transfer-rollback.pdf).

Reproduce browser checks using the command in
[the design README](../../design/0.2/README.md). The runner attaches to an explicitly
supplied local Chrome debugging endpoint and uses one existing owned preview tab.
No new JVM or ledger was started. The existing book JVM was left running.

## Limits and next product work

The recorded sandbox is interactive playback of actual historical evidence. It
cannot submit a new transaction or replace live participant authentication. The
workspace and builder remain design simulations. The Scala-backed production UI,
independent adopter integration, broader composer views, and external adoption
remain in the unchecked runtime portion of [PLAN.md](PLAN.md).

Quotation coverage remains **736/736 source units and 8,110/8,110 source words**
under the defined two-document denominator. The original documents are byte-identical.
This measures inclusion, not editorial quality, implementation acceptance, or
adoption. Four missing original diagram attachments remain explicitly identified.

Chrome was exercised here; equivalent Safari/Firefox behavior and a human
screen-reader review are not claimed. A reader's own device may paginate a PDF
slightly differently. The UX contract and saved artifacts make further review concrete.

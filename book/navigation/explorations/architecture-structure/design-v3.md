# Harmonia architecture — version 3

Status: version 3 inspected as a paired Markdown/image review; ready for Miguel.
Recorded: 2026-09-14 UTC.
Baseline: `b01d5ff`, branch `design/architecture-clarity-v2`.
Previous image and review: [version 2](design-v2.md).

---

## 1. User direction

Miguel preferred version 1's continuous left-to-right reading. Version 2 made
some relations more explicit at the cost of small labels and additional routing.
He asked how much text we could remove while making the architecture more intuitive.

The review now pairs the actual image with concise Markdown on its left. The
prose says what the reader should understand and why the spatial representation
helps. It is not the full design log or prompt. [read-v3.md](read-v3.md) is the
committed source for that visible prose; it is rendered as Markdown, not displayed
as raw syntax or manually duplicated in the page.

---

## 2. Design principles applied

The Apple reference is used for legibility, alignment, restraint and putting
primary content in view. Apple's [UI Design Dos and Don'ts](https://developer.apple.com/design/tips/)
recommends legible text and contrast, adequate spacing, undistorted images, and
alignment that conveys relationships. These are primary-source design references,
not a claim that this prototype is an Apple interface or passes an Apple audit.

Our concrete decisions:

- Remove every connector label. Keep short names on the objects themselves.
- Restore a continuous Dapp → Core → Applications reading direction.
- Use one ledger boundary and clear ownership groups instead of explanatory
  sentences around boxes.
- Show the two application participation modes together inside the application
  area, without drawing a false chain or separate branching routes.
- Place Binding DAR below the main relationship as a declaration. Builder
  produces it at build time; the declaration does not become a runtime service.
- Keep the contract structure visible as a second horizontal row. Leave role
  binding as instance data and continuation inside the Core grouping.
- Move source citations, qualifications and detailed relationship wording into
  the adjacent readable prose and this record.

The picture supplies spatial structure. The prose supplies precise meaning.
The review evaluates the pair, including whether the picture earns its space.

---

## 3. Source fidelity and scope

| Meaning retained | Source | Treatment |
| --- | --- | --- |
| “composition state stays on-ledger” | [Original §1, line 202][ledger] | Canton/Daml encloses Core and application packages. |
| Dapp is the interaction surface | [Original §1, lines 116–132][dapp] | Dapp remains outside the ledger, on the left. |
| Core owns state and step rules | [Original §1, lines 205–228][core] | Short Core label with “State · rules”. |
| Two participation paths, one model | [Original §1, lines 249–271][paths] | Two alternative package glyphs within a single application area; no edge between them. |
| Binding declares participation; Builder is build-time | [Original §1, lines 135–149][builder] and [230–250][binding] | A smaller declaration below the main connection, produced by Builder. |
| Application authority and visibility remain local | [Original §2, lines 394–453][authority] | Choice and authority sit in the application enclosure; the prose makes ownership explicit. |
| Definition, instance, assignment, role binding, continuation | [Original §2, lines 335–424][contracts] | A compact contract row and one continuation branch within Core. |
| Atomic execution needs suitable structure and authority | [Proposal, lines 66–74][atomic] | State the condition once in adjacent prose. |

[ledger]: https://github.com/miguelemosreverte/harmony-project/blob/b01d5ff/docs/proposal/harmonia-architecture.html#L202
[dapp]: https://github.com/miguelemosreverte/harmony-project/blob/b01d5ff/docs/proposal/harmonia-architecture.html#L116-L132
[core]: https://github.com/miguelemosreverte/harmony-project/blob/b01d5ff/docs/proposal/harmonia-architecture.html#L205-L228
[paths]: https://github.com/miguelemosreverte/harmony-project/blob/b01d5ff/docs/proposal/harmonia-architecture.html#L249-L271
[builder]: https://github.com/miguelemosreverte/harmony-project/blob/b01d5ff/docs/proposal/harmonia-architecture.html#L135-L149
[binding]: https://github.com/miguelemosreverte/harmony-project/blob/b01d5ff/docs/proposal/harmonia-architecture.html#L230-L250
[authority]: https://github.com/miguelemosreverte/harmony-project/blob/b01d5ff/docs/proposal/harmonia-architecture.html#L394-L453
[contracts]: https://github.com/miguelemosreverte/harmony-project/blob/b01d5ff/docs/proposal/harmonia-architecture.html#L335-L424
[atomic]: https://github.com/miguelemosreverte/harmony-project/blob/b01d5ff/docs/proposal/harmonia.md#L66-L74

This is the whole overview, not an exhaustive package or contract catalog.
Package-manager retrieval, reference-package contents and AppliesTo fields stay
in the original reference. The socket glyph represents the interface relationship;
final interface placement and binding mechanics remain open in the source.
The contract row illustrates the choice-exercise relationship, not every step kind.
No use-case scenario or production implementation claim is introduced.

---

## 4. Exact image prompt

The reference is version 1 for its approved palette, typography and visual
language. The content and arrangement change as specified below.

```text
Use case: infographic-diagram.
Create version 3 of the Harmonia ARCHITECTURE overview using the attached image as a visual-style reference. Preserve the white/pale-blue/navy palette, softly dimensional software pictograms and calm sans-serif typography. Recompose the COMPLETE architecture into two clear horizontal readings. The result should feel spare, precise and easy to grasp. Landscape 3:2, approximately 1800 × 1200. This image will be shown beside a prose explanation, so it must not contain paragraphs, disclaimers, source credits or tiny labels.

CRITICAL: ZERO TEXT ON OR BESIDE CONNECTORS. No creates, assigns, qualifies, selects, implements, binds, Ledger API, transactions/state or other arrow annotations. Put only short names on the pictured objects and large titles on boundaries. Every word must remain readable when the whole image is displayed about 1000 pixels wide. Use roughly 30–38 px type at 1536 px image width, never tiny type. No overall headline or Harmonia logo; the surrounding page supplies the title. Keep plenty of whitespace.

UPPER HALF: one continuous main left-to-right reading:
Dapp → Core → Application DARs.
The Dapp is a browser/app pictogram on the LEFT, OUTSIDE the ledger. Its only label is "Dapp". Core and Application DARs are inside one large pale-blue enclosure titled "Canton · Daml". The upper primary connection crosses from Dapp to Core, then continues from Core to the application area. Use a clear bidirectional Dapp/Core connector for submitting work and reading state; use one right-pointing Core/application connector with a small interface socket glyph on it. No words on either connector. The relationships are conceptual participation/execution, not package imports.
Core has a workflow pictogram, label "Core", and only the short supporting line "State · rules".
Application DARs is one clear responsibility area on the right, labeled "Application DARs". Inside are two equal small package glyphs, side by side, labeled "Direct" and "Generated", with a simple large word "or" between them. They are alternative participation modes, never sequential steps. Draw NO connector between these two glyphs and NO branching arrows to the individual glyphs. The main Core connection reaches their shared application area through the socket. No bank, house, money or other use-case imagery.

Below this main line, keep build tooling visibly secondary. Builder sits OUTSIDE the ledger, below Dapp on the left. It has a small tool pictogram labeled "Builder" and the short sublabel "Build time". One thin dashed arrow reaches a smaller folded declaration paper labeled "Binding DAR" INSIDE the Canton boundary, below the Core-to-application relationship. Attach that declaration to the interface socket with a very fine dotted vertical annotation line without an arrowhead or text. This declaration is not a third runtime service and the main horizontal runtime line must NOT pass through it. Do not add a service card behind the declaration. Avoid crossing any other connector or label.

LOWER HALF: architectural contract relationships, another continuous left-to-right reading. Above this part use the single heading "Contracts".
A subtle enclosure labeled "Core" contains three modest document/contract pictograms in a horizontal row: "Definition" → "Instance" → "Assigned step". Instance also contains the single short supporting line "Roles · parties". No field lists or edge labels. Continue the main arrow from Assigned step through a small unlabeled interface socket to a document labeled "Choice", INSIDE a separate right-side enclosure titled "Application". A small key inside this application enclosure is labeled "Own authority". The arrow reaches Choice, not the key.
Below Instance, still INSIDE the Core enclosure, one thin downward arrow reaches a document labeled "Continuation", then one rightward arrow reaches "Next definition", also inside Core. Keep this auxiliary branch visually subordinate to the main row. These are contracts and relationships, not use-case carousel steps. No numbered circles, progress bars, interface controls or human avatars.

Use ONLY these visible text labels, placed as described: "Dapp", "Canton · Daml", "Core", "State · rules", "Application DARs", "Direct", "or", "Generated", "Builder", "Build time", "Binding DAR", "Contracts", "Core", "Definition", "Instance", "Roles · parties", "Assigned step", "Application", "Choice", "Own authority", "Continuation", "Next definition".
No other visible words, no tiny lettering, no explanatory footers and no decorative labels. Preserve real ownership boundaries while simplifying. The first thing the viewer sees should be the calm horizontal Dapp/Core/application arrangement. Both halves remain visible together and aligned. All text and artwork must be uncropped.
```

Input image: `01-structure.png` from this directory, style reference only.

---

## 5. Artifact, text reduction and inspection

### Generation

- Tool: built-in image generation; one call using version 1 as a style reference.
- Exact prompt: section 4, extracted verbatim from the fenced block.
- Started: 2026-09-14 06:09:59 UTC.
- Returned: 2026-09-14 06:10:45 UTC.
- Image: [03-structure.png](03-structure.png), 1536 × 1024, 1,339,809 bytes.
- SHA-256: `e62ce064a598372ff31133663bb3272d113186815af4f04d5356d0daee49dea7`.
- Original output: `~/.codex/generated_images/01a08cd4-eaa1-7bd2-83c1-bf9bf78e55de/exec-64a28250-2dd5-4492-84a5-097ea970a3dc.png`.
- Review page: [v3.html](v3.html).

The design and exact prompt were committed before generation at
[f652746](https://github.com/miguelemosreverte/harmony-project/commit/f652746).

### What was removed

| Measure | Version 2 | Version 3 |
| --- | ---: | ---: |
| Visible words in the image | 131 | 31 |
| Nonempty lines in the label transcription | 54 | 22 |
| Text annotations on connectors | 11 | 0 |

This is 100 fewer image words, about 76% less. The counts use manual transcriptions
of the actual rendered images: [labels-v2.txt](labels-v2.txt) and
[labels-v3.txt](labels-v3.txt). A word is a whitespace-separated token containing
at least one letter; punctuation-only tokens and section numbers are excluded.
The transcription lines count text entries, not the image's physical line wrapping.

The adjacent Markdown is **115 words**, including headings and source quotations,
counted using the same rule. This is an image-text reduction, not a claim that the
entire new page has fewer words. The user expressly requested the prose alongside
the picture so the architectural meaning can be reviewed directly.

### Assistant inspection

The Dapp, Core and application area form a horizontal main relationship. Dapp
and Builder are outside Canton; Core, applications and the binding declaration
are inside. Direct and generated participation appear as alternatives without a
connector between them. The main path bypasses the declaration; Builder produces
it on the lower build-time path. The socket is the interface relationship.

The contract row keeps definition, instance, assignment and continuation in
Core, with choice and authority in the application boundary. The continuation
branch remains visible. There are no connector labels, tiny source credits or
use-case narratives in the image. The role/party label stays inside Instance.

I prefer this presentation for the requested reading: the enclosure and placement
carry more of the meaning, and the main relationship can be followed continuously.
The adjacent prose explains the precise interpretation instead of adding more
words to the lines. Whether the pairing is sufficiently intuitive remains the
subject of Miguel's review. This overview still omits the full contract schema
and cannot resolve the source's open packaging choices.

### Rendered Markdown and browser behavior

`read-v3.md` is rendered by the existing installed `markdown-it-py` package into
`v3.template.html`. The generated `v3.html` preserves headings, emphasis, paragraphs
and quotations. No hand-written duplicate prose is maintained in HTML.

Rebuild from the repository root:

```sh
.artifacts/book-tools/bin/python book/navigation/explorations/architecture-structure/render-v3.py
```

`review.css` supplies the paired layout. The existing `sheet.js` supplies image
pan/zoom, preserving its mounted DOM and deterministic `x`, `y`, `z` camera values.
`review.js` also preserves the prose scroll position through the `read` query
parameter. The body has two interaction surfaces: the prose and the diagram.
There are no added buttons, selectors or body links.

Inspected final screenshots at 1600 × 1050 and 1440 × 900: Markdown is on the left,
the complete diagram is on the right, and neither requires scrolling at those
sizes. Prose remains 16 CSS pixels. At 390 × 844, the two areas stack; prose can
scroll within its area and the image supports pinch/zoom. The mobile fit view is
an overview, not a claim that every raster label is readable at that scale.

Browser checks passed for formatted Markdown, full-image fit, both interaction
surfaces, no document overflow, persistent image DOM, shared camera and prose
position, touch pinch, Home-to-fit and a single uncropped printed page. No browser
exceptions or failed resources were recorded. Existing Chrome and Python were
reused; no JVM or new browser process was started.

Local evidence in `.artifacts/architecture-structure-v3-review/`:
`desktop-complete.png`, `laptop-paired.png`, `mobile-complete.png`,
`desktop-shared-camera.png`, `mobile-execution-detail.png`, `complete.pdf` and
`checks.json`. These checks establish rendering behavior; the architectural and
readability observations above are explicitly assistant judgments.

---

## 6. User review

Pending. Append actual feedback with the artifact and commit reviewed. The previous
approval to generate is not approval of the new image or side-by-side presentation.

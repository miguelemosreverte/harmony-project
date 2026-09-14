# One entrance, four perspectives

Open [the entrance](../index.html). The repository root forwards here, preserving
its query parameters. This is the shared website entrance; the standalone Canton
recording remains independently distributable.

## Reader journey

One question: **Where would you like to begin?**

| Path | What the reader wants | Opens |
| --- | --- | --- |
| User | Follow people using the product | Existing illustrated financing workflow |
| Investor / Canton demos | See composition, privacy and settlement work | Preserved Canton runs, with milestone evidence and JSON export |
| Architecture | Understand how the contracts compose | Three approved illustrated passages with source citations |
| Implementation | Read and assess the production code | Existing annotated source explorer and production file tree |

The entrance has two interaction surfaces: the established connected-dot carousel
and one **Open** action. Each cover has one short explanation and a preview.
Changing perspective keeps the covers, images, stage and action mounted in the
same positions. Images fit without cropping. The shortest phone layout omits the
incidental format note to keep the preview and action visible.

The carousel supports click, hover/focus, arrow keys and swipe. `?branch=user`,
`investor`, `architecture` or `implementation` reproduces the selected cover.
Browser history restores it. The header's **‹ Harmonia** link returns each reader
to its own cover; switching perspectives requires returning to this entrance.

## Files

- `../index.html`: the four authored covers and existing destinations.
- `home.css`, `home.js`: entrance layout and query-driven carousel selection.
- `return.css`, `return.js`: shared website return link, outside the body.
- `../investor/index.html`, `open-demo.js`: mount the archived demo presentation
  with website navigation. The saved log and its portable export stay unchanged.
- `prepare.py`: extract and hash-check the committed archive's event log into
  ignored `../investor/recorded/events.json`. No ledger or service is started.
- `assets/`: screenshots of the existing source explorer and Canton settlement
  diagram, captured on 2026-09-14. The other covers reuse the approved proposal
  illustration and architecture composition image directly.
- `check.mjs`: browser navigation and layout checks using an existing CDP browser.

Production modules have no dependency on this entrance. Source citations, code
annotations and diagram contents remain owned by their respective readers.

## Run

Use the existing scene and reader JavaScript bundles. In a fresh checkout, build
those first with the project's [toolchain](../setup.md):
`sbt --batch 'scene/fastLinkJS' 'reader/fastLinkJS'`.

From the repository root:

```sh
python3 book/home/prepare.py
python3 -m http.server 56202 --bind 127.0.0.1
```

If that server is already running, only preparation is needed. Then:

```sh
open 'http://127.0.0.1:56202/book/index.html?branch=investor'
```

Keep `book/investor/recorded/events.json` when copying the prepared website. It is
recovered from the committed archive, rather than committed a second time. The
existing Scala book exporter includes prepared JSON under `book/`; its historical
root entry remains the source-cited field guide.

## Verified behavior

```sh
node book/home/check.mjs http://127.0.0.1:61322 http://127.0.0.1:56202/
```

On 2026-09-14: 20 screenshots, four covers at three viewport sizes, and all four
paths opened and returned on desktop and phone. Cover and control positions stayed
fixed; the entrance fit without scrolling, including 320×568. Clicks, keyboard,
swipe, history, reload, invalid branch fallback and root redirect passed. The
hosted Canton recording advanced to its next recorded event. No browser errors
were reported. Local screenshots and detailed results are in
`.artifacts/home-review/`. This checks website navigation, not new ledger runs.

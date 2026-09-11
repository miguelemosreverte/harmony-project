# Infographic and live integration verification

2026-09-11 · `version/0.2.0` · checkpoint `v0.2.0-infographic.1`.
This is an integration checkpoint, not the complete 0.2 product release.

## What changed

The use-case chapters open on an HTML infographic with one caption and a carousel.
Keyboard arrows, swipe, and scene dots move through the business journey. Every
recorded attempt remains selectable under Evidence, including unauthorized probes.
The shared Scala.js renderer is in `product/scene/`; book projection stays in the
book, and live financing projection stays in `product/web/financing/`.

The Scala export now packages the source-cited guide and injects verified matching
recordings from the requested run. The detailed laboratory remains at
`laboratory.html`. The product server can mount the exported book as files without
compiling against book or harness code. The demo creates a private session launcher.

The actual financing UI submits the existing commands to the Scala server and
updates from participant ledger observations. Compose, Applications, and Evidence
have separate task surfaces. Polling and tab navigation preserve unfinished drafts.

## Evidence

| Check | Result |
| --- | --- |
| Scala unit suites | 50 passed across service, runner, and book export; includes the product compile boundary and mounted-path/symlink confinement |
| Scala.js | Shared scene, product browser, and detailed reader compile and link |
| Source/recording checks | 19 passed; original bytes and independent expectations preserved |
| Quotation inclusion | 736 / 736 defined source units; 8,110 / 8,110 source words |
| Infographic browser checks | 79 passed against a relocated export; 26 recorded actions across four scenarios |
| Actual live browser checks | 20 passed: bank approval, buyer continuation, observer visibility, refresh, draft retention, query navigation, book mount and return route |
| Independent live golden check | Passed authenticated handoff, authority bypass attempts, stale requests, repetition, and reconnect |
| Exported links | Passed source links, guide HTML/assets, nine laboratory chapters and recording links |
| Visual artifacts | Desktop/mobile captures, source rendering, dark/paper examples, and a PDF under `design/0.2/infographic/review/` |

Machine-readable browser evidence: [infographic](infographic-browser.json) and
[live integration](infographic-live.json). The live report preserves two initial
missing-favicon diagnostics. The final server supplies both icon routes; final
startup verification is recorded separately in [sandbox smoke](sandbox-smoke.json).
There were no JavaScript or Content Security Policy failures in the passing run.

The independent live golden artifacts are local under
`.artifacts/live-check-1514094193993048905/live-handoff/`.
The relocated export was generated from `.artifacts/check-5403854857099033244/`
into `.artifacts/infographic-export/`; its recordings retain their original run
identity. Exporting them is not a claim of rerunning those stories today.

Screenshots revealed and led to fixes for mobile actor/artifact overlap, tablet
spacing, a locked transfer artifact drawn on the wrong side, and an inherited green
button rule. The final product stylesheet consolidates the old overrides.

## Reproduce

After the pinned runtime setup and initial `scripts/build`:

```sh
scripts/start-sandbox
```

Open the private `open.html` path printed by the command. Open Bank, approve; open
Buyer, continue; open Reviewer, observe. Keep participants in separate tabs.
The book is served by that same local server. Ctrl-C closes the demo network.

Focused verification:

```sh
scripts/build-design
scripts/harmonia live-check
node design/0.2/checks/infographic.mjs http://127.0.0.1:DEBUG_PORT
node design/0.2/checks/live.mjs http://127.0.0.1:DEBUG_PORT .artifacts/live-RUN/sessions.json
```

Browser checks navigate one explicitly owned Chrome debugging tab. The live check
consumes a fresh case; restart the sandbox before demonstrating it again. The final
sandbox is left at the pending bank decision. Only one Canton environment is owned
at a time. Its heap is capped at 2 GiB, with a separate 512 MiB cap for the Scala app;
actual resident memory includes non-heap allocations. The older book JVM was closed.

## Scope

Live: the private financing handoff, existing workflow composition, and package APIs.
Recorded: the complete four-person property offer and four-party custody transfer.
The changed live path was exercised end to end; the untouched composition and
package command suites were not rerun in this increment. Their mounted interfaces
continue to use the existing API, and unfinished composition state was checked.

Quotation inclusion does not establish product completion. Publisher trust,
independent application adoption, evaluator feedback, and the full release check
remain separate work in the [0.2 plan](PLAN.md). `main` remains the fourth draft.

The generated [reference](../../design/0.2/infographic/concept.png) came from the
built-in image-generation tool; the [exact prompt](../../design/0.2/infographic/prompt.md)
is preserved beside it. The delivered interface is native HTML/CSS rendered by
Scala.js, not a bitmap or an assertion of pixel identity with the generated concept.

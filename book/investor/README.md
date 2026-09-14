# Canton demonstrations

Five interactive demonstrations: branch/join execution, direct composition,
private continuation, generated integration, and atomic settlement. Eighteen
selected moments come from **48 observed actions in seven preserved Canton runs**.
They are recorded playback, not fresh ledger submissions.

Unzip the [portable package](canton-demo.zip) and open `index.html`. No repository,
installation, server or network is needed. **Export log** saves the observations,
presentation and selected step as JSON. Open `open.html`, choose that JSON, and
the same screen returns. `evidence.html` is the static reading/printing edition.

## Source and milestones

[demos.json](demos.json) contains short captions and recording/action references.
[milestones.json](milestones.json) maps all eight original milestones to relevant
demos, production implementation and remaining requirements. The export includes
original quotations and full milestone sections.

This maps specific evidence to requirements, without certifying acceptance.
Broader live branch/join views, the proposed package-provider journey, release
publication and external adoption remain qualified. M7 and M8 require independent
adopting teams; a synthetic demo cannot prove them.

Each diagram stays mounted while the existing `StepCarousel` changes recorded
states. Use connected stops, arrows, swipe or keyboard. The second settlement lane
is a separate run with a rejecting receiving rule; it does not undo a successful
run. Query parameters `demo`, `path` and `step` reproduce the selection. History
works. On short screens the stage scrolls while the controls remain fixed.

## Build or record

Export existing hash-verified recordings using Node and the already built scene
bundle, then open the portable reader:

```sh
node book/investor/build.mjs
python3 book/investor/package.py
open .artifacts/canton-demo/index.html
```

Outputs: `.artifacts/canton-demo/` and `.artifacts/harmonia-canton-demo.zip`.
`build.mjs --check` requires byte-identical outputs. A saved selection can also be
reproduced from the command line:

```sh
node book/investor/build.mjs --replay exported-log.json --out .artifacts/replayed-demo
```

For the internal team, this command builds, runs the existing checks sequentially,
collects their exact outputs, verifies them through the existing exporter, then
produces a fresh event log, reader and ZIP:

```sh
node book/investor/record.mjs
```

It requires the [pinned toolchain](../setup.md): Java 17, Daml SDK 3.4.11, sbt, Node
and Python 3. `--plan` prints its commands without running them. The harness lease
prevents overlapping ledger environments. Stop an owned live sandbox before
recording; the recorder does not stop existing services.

Docker is not required for browser delivery. A containerized SDK build has not
been implemented or verified here; recording uses the established native
macOS/Linux harness.

Already exported runs can be supplied with repeated `--recordings
/path/to/evidence.json` arguments. Missing records fail instead of silently using
historical evidence. Collection copies only input, expected, actual, diff,
observation and provenance files. Participant configuration and credentials stay
with the internal run.

## The portable event log

The `harmonia-event-log/1` envelope contains:

| Field | Purpose |
| --- | --- |
| `runs` | Starting inputs, ordered requests/observations, goldens, raw evidence and run provenance |
| `scenes` | Selected event references and frozen diagram markup |
| `demos` | Narrative and finite navigation paths |
| `milestones`, `proposal` | Requirement mapping and original source text |
| `presentation` | Shared renderer, styles, images, frozen HTML and evidence document |
| `cursor` | Selected demo, path and step |
| `content_sha256` | Integrity fingerprint excluding the movable cursor |

An event is an observed action, not necessarily one ledger transaction. Raw
participant events and transaction identities remain in the observation artifact.
Each run retains its topology, timestamp, revision, SDK, DAR and evidence hashes.
Arrays preserve order; object keys are sorted. Expectations never generate a
simulated success. The archived presentation keeps one copy of each shared asset.

`recordings.mjs` validates evidence; `diagrams.mjs` defines the views; `build.mjs`
assembles the log; `portable.mjs` packages its reader; `player.js` owns selection;
`player.css` owns layout; `evidence.mjs` renders the static document. `record.mjs`
orchestrates fresh checks and `package.py` creates the archive. Product modules
have no dependency on this folder.

## Verification

```sh
node book/investor/verify.mjs
node book/investor/check.mjs http://127.0.0.1:61322 \
  http://127.0.0.1:56202/.artifacts/canton-demo/
```

The first checks deterministic output/packaging, complete imported runs, and
rejection of missing runs or modified baselines, outcomes, scenes and assets.
The second uses one temporary tab in an existing Chrome debugging session. It
checks all eighteen scenes, fixed controls, accessible selection, history,
reload, swipe and keyboard behavior. It downloads JSON through Export log,
rebuilds it, disconnects networking and compares screenshot bytes. It also opens
the JSON through `open.html`, as a recipient without the repository would.

Verified 2026-09-14: desktop, phone and short-phone checks passed. Both offline
round trips reproduced identical screenshot pixels. Evidence is under
`.artifacts/investor-review/`. Fresh Canton runs were not launched in this pass;
the existing sandbox was preserved. The new recorder's plan and import boundary
were checked; its complete build-and-ledger sequence still needs an end-to-end run.

## Implementation record

- [x] Cite the proposal and map all milestones, including unresolved scope.
- [x] Preserve complete ordered observations and independent goldens.
- [x] Provide a finite carousel, stable diagrams and explicit alternate runs.
- [x] Export/reopen JSON with the same renderer, assets and selected state.
- [x] Package offline replay and static evidence for readers without the repo.
- [x] Verify navigation, integrity, deterministic archives and pixel round trips.
- [x] Add the sequential internal recorder and document its verification limit.

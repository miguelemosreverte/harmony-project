# The source-cited 0.2 edition

This book follows five reader intentions through ten chapter destinations. The two
use cases include interactive playback of four preserved ledger recordings. The
workspace and builder remain explicitly labelled design simulations. The Scala
runtime integration remains in [the product plan](../../docs/0.2/PLAN.md).

From the repository root:

```sh
scripts/build-design
scripts/design-preview
```

Open <http://127.0.0.1:56202/design/0.2/book-overview.html>.
The build creates one isolated Python environment under `.artifacts/book-tools`
and installs the two pinned renderer packages if needed. Viewing the committed
HTML needs no build, JVM, ledger, or network. The recordings are embedded in HTML,
so they also work from `file://` and do not rely on a running API.

| Owner | What to edit |
| --- | --- |
| `chapters/` | Human-readable narratives, responsibilities, and product boundaries |
| `coverage-map.md`, `sources.json` | Original passage destinations and byte fingerprints |
| `recordings/` | Pinned historical evidence, revision, and input/expectation hashes |
| `render.py` | Readable Markdown/HTML and isolated original SVG rendering |
| `pages.py`, `workspace.py` | Book hierarchy and task-oriented prototype HTML |
| `build.py` | Corpus, recording verification, and deterministic artifact assembly |
| `check.py` | Exact quotation, rendering, source drift, recording drift, and link checks |
| `design/0.2/state.js` | Validated URL state and browser history |
| `design/0.2/navigation.js` | Reader routes, appearance, sharing, and disclosures |
| `design/0.2/book.js` | Observation-driven recorded playback |
| `design/0.2/application.js` | Deterministic workspace and builder simulations |
| `design/0.2/style.css` | Shared appearance, reading sizes, responsive and print layouts |

Generated HTML, isolated diagrams, and coverage JSON are committed for direct
review. The build check rejects stale generated output. Every original quotation
unit appears once in the chapter audit panels; readable renderings do not increase
the numerator. Product evidence status is separately authored in
`docs/0.2/product-contract.md`.

The [UX contract](../../docs/0.2/UX.md) defines the reader journeys, screen goals,
URL behavior, accessibility, and evidence boundaries. See the
[design review guide](../../design/0.2/README.md) for browser verification.

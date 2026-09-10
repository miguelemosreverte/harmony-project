# ADR 002: a Scala book backed by recorded evidence

Status: implemented for recorded playback and the first two chapters.

The browser target uses Scala.js 1.22.0 with Scala 3.3.6, scala-js-dom 2.8.1, Cats Effect 3.6.3, and the same Circe version as the JVM. This follows the official [Scala.js build setup](https://www.scala-js.org/doc/project/index.html) and [DOM integration guidance](https://www.scala-js.org/doc/tutorial/basic/index.html). These versions have been compiled and exercised together locally.

The shared source directory contains only the record model and structural comparator used on both targets. It is compiled into each target; it contains no filesystem, server, or ledger dependency. The JVM retains Markdown/YAML parsing, snapshot verification, book export, and the local static server. The browser owns rendering and selection state.

Effects use concrete Cats Effect `IO`. The browser entry point owns a `Dispatcher` for event callbacks and a `Ref` containing immutable selection state. DOM mutations occur within a rendering effect. The JVM server owns its executor and HTTP server through `Resource`; it binds to loopback and restricts requests to the export directory.

The browser receives recorded inputs, expected results, actual results, and provenance in one bundle. The shared comparator produces every displayed difference. No alternate simulation or expected-result-driven execution exists in the browser.

The exporter requires exact input/expectation snapshots and verifies their hashes, the normalized actual file, and raw observations. It escapes raw HTML and sanitizes URLs when rendering authored chapters; scenario text is inserted as text. Source links refer to the worktree included when the bundle is exported. The recording's own revision and dirty-worktree status remain explicit in its provenance. A release must align those source and recording revisions during packaging.

This edition uses native DOM elements and a small stylesheet. Its graph is an observed progression of attempts, including failed attempts; it does not claim to visualize the general workflow graph that later increments will introduce. Recorded perspective selection and live authentication are separate later requirements.

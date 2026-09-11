# The Harmonia book

This nine-chapter book accompanies the working implementation. Chapters link to the actual story inputs, committed expectations, and code used by the regression suite.

1. [Set up the local runtime](setup.md)
2. [A story the ledger can prove](01-first-story.md)
3. [Two ways an application can participate](02-two-integration-paths.md)
4. [A private application and a shared next step](03-participant-views.md)
5. [Progress survives the client](04-progression.md)
6. [A private assessment enables a separate offer](05-financing-and-offer.md)
7. [Four parties, one final transaction](06-atomic-transfer.md)
8. [Generate an adapter from a deliberate mapping](07-generated-bindings.md)

9. [Bring an application and compose a workflow](08-compose-a-workflow.md)

10. [Extend the system without losing the proof](09-extend-with-evidence.md)

[Glossary](glossary.md) · [Troubleshooting](troubleshooting.md) · [Extension guide](extension-guide.md) · [Developer walkthrough](../docs/developer-walkthrough.md)

Use the [recorded playback guide](playback.md) to open the browser edition.

The current edition includes interactive playback for recorded stories, chapter navigation, observed progression, contract details, and complete expected/actual differences. Follow the playback guide to export and open a saved run. Each chapter provides direct access to its matching recordings when they are included in the export.

## Book implementation

`browser/` owns the recorded Scala.js reader, `model/` its recording and chapter types, `export/` the JVM exporter/server, and `site/` the reader assets. This directory is independent of the live product browser. From the repository root, `scripts/harmonia export-book RUN OUTPUT` exports existing evidence; `scripts/harmonia serve-book OUTPUT` opens it without a ledger.

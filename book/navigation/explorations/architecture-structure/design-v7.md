# Version 7 — render the proposed contract map

Recorded: 2026-09-14 UTC. Based on `57ccba7`.

The user asked to see the diagram proposed in the conversation. This is the
browser rendering of that three-part map, authored in SVG with the established
palette and typography. It preserves the distinction between a declared mapping
and permission to execute an action.

The source is the original contract model: [instance and assignment][instance],
[applicability declaration][binding], and [source application][application].
The workflow card summarizes the instance/assignment relationship; it is not an
exact template schema. The declaration is a build artifact, not another runtime
service. Solid arrows describe semantic execution and results, not an RPC trace.
As in the source, detailed binding mechanics and package relationships remain open.

[instance]: https://github.com/miguelemosreverte/harmony-project/blob/57ccba7/docs/proposal/harmonia-architecture.html#L355-L411
[binding]: https://github.com/miguelemosreverte/harmony-project/blob/57ccba7/docs/proposal/harmonia-architecture.html#L426-L433
[application]: https://github.com/miguelemosreverte/harmony-project/blob/57ccba7/docs/proposal/harmonia-architecture.html#L435-L453

This sheet displays the exact contract relationship proposed in the conversation.
Version 6 remains the broader view with UI, build tooling and continuation.
Source: `07-architecture.svg`; explanation: `read-v7.md`; browser page: `v7.html`.

```sh
.artifacts/book-tools/bin/python book/navigation/explorations/architecture-structure/render-review.py 7
open 'http://127.0.0.1:56202/book/navigation/explorations/architecture-structure/v7.html'
```

Browser review: the desktop screenshot at 1440 × 900 was visually inspected.
The three cards, mapping lines and execution/return arrows are legible and do not
overlap. Desktop and mobile fit checks passed, including all 10 component labels
within their cards, no text collisions or clipping, two interaction surfaces,
and no browser exceptions or failed resources. Mobile remains a pan/zoom overview.
Evidence: `.artifacts/architecture-structure-v7-review/` contains desktop/mobile
screenshots, `checks.json`, and `native-geometry.json`.

Opened with the macOS `open` command. User feedback: pending.

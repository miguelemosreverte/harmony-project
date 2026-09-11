# Harmonia 0.2 design review

Run `scripts/design-preview` from the repository root and open
<http://127.0.0.1:56202/design/0.2/book-overview.html>.
The preview serves local files; it starts no JVM or ledger.

- [Book overview](book-overview.html)
- [Interactive chapter](book-chapter.html)
- [Source coverage](coverage.html)
- [Application workspace](application.html)
- [Bring an application](application-builder.html)
- [Concept/render comparison](review.html)

Each principal screen has a generated `.png` concept, an adjacent `.html` mockup,
and a separately captured `.rendered.png`. Two `.mobile.png` screenshots show the
responsive chapter and application. These are working DOM interfaces with labelled
sample state, not screenshots masquerading as interactive pages.

The built-in image generation tool produced the five concepts. Prompts are in
`prompts/`; `provenance.json` records source paths, image fingerprints, dimensions,
and style-reference relationships. All project images are retained locally here.

The [UI design document](../../docs/0.2/ui-design.md) records interaction scope,
runtime integration boundaries, and remaining visual differences. Full product
implementation remains in the [linear 0.2 plan](../../docs/0.2/PLAN.md).

To verify quotations and regenerated book pages:

```sh
python3 book/edition-0.2/build.py
python3 book/edition-0.2/check.py
```

The dependency-free DOM checks live in `checks/browser.js`. Load that script into
the local preview browser, then run `harmoniaDesignChecks.chapter()`,
`.application()`, `.refusal()` (fresh workspace), `.builder()`, or `await .coverage()`
on the corresponding page. `.narrow()` checks the current page and mobile menu.
The recorded browser results and tested viewport sizes are in
[verification](../../docs/0.2/verification.md).

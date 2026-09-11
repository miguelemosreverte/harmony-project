# Harmonia 0.2 — infographic and live handoff

Start with [Alice's story](chapters/03-financing-to-offer.html) or
[choose a reading path](book-overview.html). Arrows, swipe, and scene dots move
through the business journey. **Evidence & other attempts** contains the exact
recorded input, independent expectation, observation, and every deliberate refusal.

The HTML uses the same compiled Scala scene renderer as the real participant UI.
The book's purchase and custody-transfer stories are recorded. The live server
supports the private financing handoff, composition, and package operations.

After initial runtime setup and `scripts/build`, run:

```sh
scripts/start-sandbox
```

Open the printed private `open.html` launcher. Bank approves; Buyer continues;
Reviewer observes. Keep each participant in its own tab. The server also mounts
the book, and a participant can move between book and workspace in the same tab.
Ctrl-C closes the one owned ledger environment.

For recorded exploration, `scripts/design-preview` serves this folder without
starting a ledger. Light, Dark, Paper, text sizes, story steps, and source disclosures
are URL state. A shared live URL uses the recipient's session and current ledger state.
The book also works from local HTML and supports Print / save PDF.

[Visual review](review.html) · [Experience contract](../../docs/0.2/INFOGRAPHIC.md) ·
[Verification](../../docs/0.2/verification-3.md) · [Original quotation coverage](coverage.html).

The `application.html` and `application-builder.html` files are retained historical
mockups, labelled as simulations. They are not the route into the live server.
Generated reference art and its exact prompt are under `infographic/`; actual
screenshots and a PDF are under `infographic/review/`.

Rebuild the scene with `sbt 'scene/fastLinkJS'`, then `scripts/build-design` validates
the committed source quotations and recordings. Browser checks reuse one explicitly
selected, owned Chrome debugging session:

```sh
node design/0.2/checks/infographic.mjs http://127.0.0.1:DEBUG_PORT
node design/0.2/checks/live.mjs http://127.0.0.1:DEBUG_PORT .artifacts/live-RUN/sessions.json
```

The live check consumes a fresh sandbox's financing case. Restart the sandbox
before a new demonstration. Capabilities stay in the private local launcher and
are never written into the verification reports.

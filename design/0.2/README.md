# Harmonia 0.2 — second UX pass

Run `scripts/design-preview` and begin here:

**<http://127.0.0.1:56202/design/0.2/book-overview.html>**

Choose your reason for reading. The sidebar shows that route's next stops; the
complete contents are available under All chapters. Use Read, Try it, Original
sources, and Evidence & limits inside a single canonical chapter.

- [Alice's purchase](chapters/03-financing-to-offer.html): four people, a financing result, and a proposal handoff.
- [Four-party transfer](chapters/04-four-party-transfer.html): staged preparation, settlement, and a failed final leg.
- [Document coverage](coverage.html): every original text unit has a destination; product evidence stays separate.
- [Workspace](application.html): a clearly labelled task simulation with handoff and recovery states.
- [Connect an application](application-builder.html): one integration stage at a time.
- [Design review](review.html): current entry points and preserved first-pass concepts.

The book uses four actual **historical recordings**, with independently committed
expectations and checked fingerprints. It does not submit new ledger commands.
The application screens simulate the proposed interaction. They are not the live
Scala-backed production UI.

Appearance offers Light, Dark, Paper, and three reading sizes. Share copies the
current URL, including selected scenario, step, actor, evidence tab, source panels,
and appearance. Back/Forward and cold navigation restore that state. No local
storage overrides a link. Appearance also offers Print / save PDF.

Read the [UX contract](../../docs/0.2/UX.md) for the explicit screen goals, personas,
color provenance, and navigation rules. `v0.2.0-design.1` preserves the first pass:
its root `.png`, `.rendered.png`, and `.mobile.png` images are historical, with
original generation prompts and `provenance.json`. They are not current screenshots.

To rebuild and check the edition:

```sh
scripts/build-design
```

This installs pinned Markdown rendering dependencies into an isolated Python
environment on first use. Opening the committed HTML requires no installation.

Browser checks use Node's built-in WebSocket client and one existing, owned local
Chrome preview tab. Enable Chrome's local debugging endpoint, open the preview,
then supply that browser's port explicitly:

```sh
node design/0.2/checks/run.mjs http://127.0.0.1:DEBUG_PORT
```

The runner exercises the actual DOM, cold URL replay, browser history, all recorded
actions, refusal and recovery states, source disclosures, responsive layouts,
print, and JavaScript-disabled reading. It writes `docs/0.2/browser-results-2.json`
and captures `review-2/`. It starts no browser, JVM, or ledger. The browser must be
owned by this review because the script navigates its existing preview tab.

# Canton demonstrations

Three short demonstrations for Canton builders and technical investors. Reuse the
existing executions and the approved connected-stop carousel. Each scene answers
one question through a request and its observed consequence.

This is interactive **recorded Canton playback**, not a new ledger submission.
Selecting a stop restores a complete recorded state, including refusals. The URL
must reproduce it without credentials, a running Canton network, or previous clicks.

## Content and interaction

1. **Private approval:** Alice cannot approve the private application; Northbank
   approves; Alice continues. Keep the bank's visibility boundary on screen.
2. **An existing application:** a reviewed adapter compiles; Alice's action is
   refused; Northbank advances the application and workflow together. Distinguish
   the build recording from the separate execution recording.
3. **Atomic settlement:** ready, settled, duplicate refused. A second carousel
   lane shows the separate run whose receiving application rejects settlement;
   withdrawal and receipt both roll back. Name the changed setup explicitly.

The reader changes stops directly, swipes, or uses fixed previous/next arrows.
At a demonstration's end, the next arrow opens the next demonstration. The fourth
reading path stays independent of User, Architecture and Implementation.
Each demonstration has a stable diagram, one short explanation and a result strip.
No role picker, configuration panel, or extra action buttons. Source evidence
lives in a separate readable document available from the header.

## Implementation plan

- [ ] Define `demos.json`: short authored captions and references to existing
  recording/action identities; no copied expected values.
- [ ] Implement `build.mjs`: verify committed input/expectation hashes, complete
  actual-versus-expected comparisons and source citations; produce the HTML,
  replay data and a flat evidence document deterministically.
- [ ] Implement `diagrams.mjs`: three explicit, small HTML diagrams using recorded
  values. Keep labels in context and reuse the project's colors and typography.
- [ ] Implement `player.js` and `player.css`: reuse `StepCarousel`; retain mounted
  diagrams; support exact URLs, history, keyboard and swipe navigation.
- [ ] Verify every selected result, including the alternate settlement run; test
  broken evidence and browser failures; inspect desktop and phone screenshots.
- [ ] Document reproduction, link the reader, commit, push and open it for review.

All new presentation code belongs here under `book/`. Product and ledger modules
remain independent. Reuse the built scene bundle and existing browser; this work
does not need another JVM. Fresh ledger execution remains the existing harness's
responsibility, with its resource lease and independently committed goldens.

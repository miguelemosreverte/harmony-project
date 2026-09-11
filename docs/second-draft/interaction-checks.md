# Second-draft interaction checks

Checked locally on 2026-09-11, using the implemented browser and a disposable authenticated Canton network. The live walkthrough uses the implementation at `9536732`; subsequent reader extraction and focus refinements receive an additional build/reader check before release. The initial book preview uses all 32 preserved first-release recordings and displays their original provenance. Fresh second-draft ledger evidence belongs to the final release gate.

| Interaction | Observed result |
| --- | --- |
| Open the book | Chapter 1 is the starting page, with guided example controls before the technical instructions |
| Browse the inventory | All 32 recordings render, have selectable units, and show matching independent observations |
| Navigate an example and attempt | The address preserves story, step, and originating chapter; reloading restores the selected attempt |
| Open actual Markdown | The inspector shows the retained file, focuses its close control, and leaves the story behind it |
| Navigate from a focused chapter control | Focus moves to the main story content |
| View an execution boundary | The reader says “Phase 2 of 2” and “Verification phase”; it displays the seven raw concurrent-request observations without adding an invented outcome |
| Read at 390 pixels | Chapter, story, and live workspace have no document-level horizontal overflow; graphs/code have local scroll regions |
| Edit through repeated live polling | The same input DOM node retains text, selection range `[7,12]`, and focus; the package file control remains the same node |
| Lose an approval reply and subsequent observations | The client says the submission is unconfirmed and shows the last observation; reconnect finds the actual committed approval |
| Continue as Buyer | Shared progress completes; the Buyer sees no private income/rating data |
| Reorder a proposed plan | Buyer review precedes generated Bank approval; the authored workflow name is retained |
| Duplicate a step name | The editor displays “Step names must be distinct”; the draft remains editable |
| Consent and execute | Buyer accepts the exact plan, completes review, then Bank executes the generated approval; both sources and the process report completion |
| Inspect metadata | LF 2.1 identity is visible, with an explicit lack of executable mapping support |
| Inspect/generate the legacy application | The reviewed adapter and example compile, and the download control delivers a 2,199,604-byte ZIP |
| Observer session | Shared progress is complete; private data, composition authority, and package inputs are absent |
| Refresh package state | The selected source and the owned file control survive redraw |

Representative captures are retained in the session as `second-book-chapter-desktop`, `second-book-mobile-chapter`, `second-book-mobile-phases`, and `second-live-mobile-composition`. Live artifacts are under `.artifacts/live-4890657360903977245/`; their authorization files remain local and are excluded from release packaging. The live owner and its Canton child are stopped after the walkthrough.

The reference checks also exercise direct API authority, wrong actors, stale requests, repeat protection, definite rejection, malformed packages, ordering, and concurrent advancement. The final clean-revision release repeats those checks independently of this browser walkthrough. These are local interaction checks, not a claim of formal accessibility certification or an independent human readability review.

Final refinement: the chapter shell, playback, comparison, and participant evidence now have separate owners under the book package. The candidate build passes 36 Scala tests. Release verification decodes the reader envelope, checks exact inventory membership, and recomputes full expected/actual comparisons. The boundary recording identifies both its integration-test and live-demo DARs in provenance.

The final 390-pixel regression walkthrough used a real successful workflow with a deliberately wrong independent expectation. The ordinary story passed and `wrong-workflow` failed at `$.actions[1].workflow` (`waiting` expected, `complete` observed). The book showed exactly that difference without page overflow (`second-book-mobile-regression`). Source inspection opens the selected operation, and advancing to the last attempt places focus on `step-1` when Next becomes disabled.

The overnight run exposed an SDK log-rotation edge case: `log/canton.log.DATE.gz` was outside the owned artifact directory and made the worktree appear dirty. `CantonSandbox` now runs with its artifact directory as the working directory, keeping SDK-owned rotating logs alongside that run. Existing logs were preserved under `.artifacts/sandbox-logs-before-ownership-fix/`.

# Harmonia, third draft

**Status:** implementation in progress.  
**Branch:** `third-draft`  
**Preserved second draft:** `c97184587c5b68589fd69ddf98e98e404c231a29`  
**Verified second-draft software:** `b1ed52f38553888696fd512c9558a18f395a0ec5`

The second draft established capability ownership, structured LF inspection, a coherent reader, and independent evidence. This third pass makes those boundaries easier to follow inside the server. It preserves the existing ledger behavior, examples, package identities, toolchain, and book experience.

## What the review found

| Reading friction in the second draft | Third-draft change |
| --- | --- |
| Browser models are typed, but the server constructs workspace and package state with JSON objects and merges | Construct the shared models on the server and encode only at the HTTP boundary |
| A feature returns `Option[(ActiveContract, String, Value)]` | Return a named `LedgerExercise` whose fields explain the submitted operation |
| Financing repeatedly searches the same contracts and parses status through JSON; missing text silently becomes empty | Decode a financing observation once, with named application/progress values and explicit failures |
| Composition projection mixes ledger decoding, missing-field defaults, and JSON presentation | Keep ledger payload schemas beside a typed composition projector; reject malformed required observations |
| Package generation chooses behavior by rereading JSON stored as mutable state | Keep inspected metadata, reviewed source, and generated archive as typed state; derive the transport view |
| Book projectors construct JSON even though a typed recording envelope already exists | Produce `StoryPresentation`, `StoryUnit`, and `RecordedStory` directly; retain raw independent results |

These are observed problems in actual entry points, not a reason to move every directory again. The existing platform/feature layout remains useful.

## Principles

1. A reader should find the operation, its inputs, and its result together. Prefer names that state the business or boundary role.
2. Use the same meaningful model across producer and consumer. JSON belongs at an external boundary or in deliberately retained raw evidence.
3. Decode required external data explicitly. Missing disclosure is an optional observation; malformed visible data is a failure. Do not conflate them.
4. Keep transformations pure, and keep `IO` and `Resource` at actual effect and lifetime boundaries.
5. Introduce a shared abstraction only when the current code has repeated operations that it makes easier to read. Avoid placeholder modules and generic service frameworks.
6. Preserve ledger authority, independent expectations, uncertain-result handling, and exact package identity checks.
7. Prove the migration with real observations. Preserve the second-draft preview until the replacement is verified; run heavy tools sequentially within the existing heap limits.

## Ordered implementation

### TD01 — Record the design

- [x] Preserve `second-draft` and create `third-draft` from its clean handoff.
- [x] Review the implementation and record concrete reading friction.
- [x] Document scope, principles, and acceptance before changing implementation.

### TD02 — Make ledger operations and financing observations explicit

- [x] Replace anonymous exercise tuples with a named operation.
- [x] Decode financing observations once and remove empty-string success for missing required ledger fields.
- [x] Add focused malformed-observation and decoding checks; compile both targets.

### TD03 — Prove the first complete slice

- [x] Run actual authenticated financing and failure/recovery checks with unchanged goldens.
- [x] Confirm the browser still shows private approval and shared continuation correctly.
- [x] Make the third actual commit include this proven slice and its evidence.

### TD04 — Use typed models through the workspace

- [x] Construct financing, composition, submission, and history views as shared models.
- [x] Decode composition ledger payloads at one named boundary and remove silent defaults for required data.
- [x] Encode the unchanged HTTP representation at the server boundary; keep the editor and polling lifetimes intact.

### TD05 — Use typed package and book state

- [ ] Store inspected metadata, reviewed-source identity, and generated archive as typed package state.
- [ ] Generate package transport views without reading control decisions from JSON.
- [ ] Make book projectors and export produce the existing typed recording envelope directly.
- [ ] Preserve full raw comparisons and render deliberately missing/wrong observations honestly.

### TD06 — Finish the review path and delivery

- [ ] Remove replaced APIs and update the repository guide and concrete reading traces.
- [ ] Run focused tests and the complete clean-checkout release gate sequentially.
- [ ] Verify all unchanged examples, fresh book recordings, package identities, archive integrity, relocated launchers, and browser interactions.
- [ ] Record exact commits, measured memory/process cleanup, and remaining limitations.
- [ ] Retain one bounded book preview and commit the final handoff.

## Acceptance

The supported capabilities and all 67 preserved example files remain unchanged. The complete release must contain the same 32 example identities and nine chapters, all matching their independent expectations. New checks establish malformed-data behavior and preservation of the existing transport fields. The third draft must remove the old JSON-driven state paths, rather than leave a second implementation alongside them.

Human readability is the purpose of this pass, not something a successful compiler or test suite alone proves. The final reading guide must show what a reviewer now needs to understand for financing, composition, packages, and book export, and identify remaining intentional boundary code.

## Implementation evidence

TD02 passes 40 Scala tests and JVM/browser compilation (`.artifacts/third-02-build.log`). The new checks distinguish absent contracts from malformed visible payloads, reject malformed collections and competing Ledger API value variants, and retain valid omitted protobuf collections. Named exercises replace the financing/composition tuples.

TD03 proves `fc2de14` on the real ledger: authenticated handoff, direct API authority, stale/repeated requests, and reconnect all pass (`.artifacts/third-03-live-check.log`; `.artifacts/live-check-13369180420142803701/live-handoff/`). In the browser, Bank approval changed the private application to approved; Buyer continuation completed shared progress, without exposing the private income details. The owned live runtime (`.artifacts/live-6253572949849521465/`) was stopped afterward; the second-draft book remains at its original URL. This evidence is recorded in the third actual commit.

TD04 constructs a typed workspace throughout the server, with shared codecs at HTTP serialization. Composition has its own command family and named ledger payload schemas; malformed visible workspaces/drafts fail instead of becoming empty views. Ledger history retains command IDs in a shared `LedgerUpdate`. All 44 Scala tests and both targets pass (`.artifacts/third-04-verified.log`), including transport-field preservation. Dedicated composition execution follows this commit and is repeated in the final release.

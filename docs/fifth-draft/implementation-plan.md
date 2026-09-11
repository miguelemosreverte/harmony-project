# Fifth-draft implementation plan

The goal is to make a production operation understandable from its values and its sequence of effects. The [principles](../../FIFTH-DRAFT.md) remain the design constraints. The baseline is fourth-draft `bef8ac5`: 58 production Scala files and 4,252 lines. Implementation proceeds below in commit order; a checked item means it has been completed and verified.

## Ownership and scope

- `product/server/src/main/scala/harmonia/` remains the JVM product.
  - `packages/inspect`: owns external compiler metadata decoding and inspected facts.
  - `packages/resolve`: owns acquisition, pinned identity checks, cached paths, and resolution evidence.
  - `packages/workspace`: owns inspected inputs, generation availability, and project download assembly.
  - `bindings/generate`: owns generated sources, compilation, and generation records.
  - `app/http`: owns HTTP transport, session checks, request bounds, and endpoint dispatch.
  - `ledger/client`: consumes inspected package identities to recognize supported templates.
  - `submission`: owns prepared effects, duplicate/stale requests, and reconciliation.
- `product/api/` and `product/web/` retain public models and browser controls. Change callers only when a clearer shared boundary requires it; preserve wire names and controls.
- `product/ledger/` and `product/packages/` retain contracts, pins, and authored mappings. No business-rule or expectation edits are planned.
- `harness/runner/` retains independent verification. Adapt typed callers; continue to read actual saved artifacts when verifying a file or archive boundary.
- `book/` retains chapters, reader, models, and exporter. Update source inclusion and explanations where the new operation changes a reading trace.
- `docs/fifth-draft/` owns this plan, a concise reading guide, measurements, and acceptance. Root build targets and scripts retain their existing dependency direction and heap limits.

## FD50 — second actual commit: expand the approved plan

- [x] Inspect production call sites and the independent checks that exercise them.
- [x] Record file-level changes, verification gates, and the preserved scope below.
- [x] Keep the fourth-draft branch and its running packaged book available throughout implementation.

The earlier proposal is the first branch commit. This expanded plan is the second. FD51 below must therefore prove working behavior before the third actual commit is made.

## FD51 — third actual commit: typed package acquisition and inspection

- [x] `packages/inspect/InspectDar.scala`
  - Return an `InspectedDar` value containing the main package identity, LF version, and retained dependency metadata.
  - Decode compiler output once here, cross-check the main package identity against structured LF, and retain raw `packages.json` for evidence.
  - Keep archive size, hash, and structured LF checks.
- [x] `packages/workspace/BuilderInput.scala`
  - Move the existing inspection model to its inspection owner; consume that model directly.
  - Keep UI availability derived from the inspected source and compiled project.
- [x] `packages/resolve/ResolvePackages.scala`
  - Return a map of named `ResolvedPackage` values from `run` and a value from `resolve`.
  - Carry the verified pin, actual file path, and inspection together; render the existing JSON evidence only when writing it.
  - Preserve downloaded/local byte checks, repeated local-source verification, LF checks, bounded downloads, and temporary-file cleanup.
- [x] `packages/workspace/PackageBuilder.scala`
  - Read the resolved path directly and receive inspected facts directly; remove both internal JSON decode steps.
  - Preserve input limits, serialization of package operations, cancellation cleanup, and participant export matching.
- [x] `bindings/generate/GenerateBinding.scala`
  - Select the named resolved package and use its path and verified identity without decoding a JSON result.
  - Preserve the generation manifest's established source fields.
- [x] `ledger/client/TemplateCatalog.scala`
  - Use the inspection result's dependency metadata directly, keeping the unique-package-name check.
- [x] `harness/runner/.../packages/verify/CheckPackages.scala`
  - Adapt resolution consumption and retained evidence writing to the typed result.
  - Preserve independent DAR digest comparison and both fresh-network executions.
- [x] Add focused inspection-boundary tests under `product/server/src/test/scala/harmonia/packages/` for valid metadata, malformed required fields, and identity disagreement.
- [x] Format, compile product and tools, run Scala tests, then run `packages-check` and `builder-check` sequentially against the existing expectations.
- [x] Record this working package slice and commit only after those checks pass. State honestly that these preliminary checks precede the final clean release.

## FD52 — fourth actual commit: retain generation facts

- [x] `bindings/generate/GenerateBinding.scala`
  - Extend `GeneratedProject` with the generated member names and final manifest already known to the producer.
  - Construct the manifest in memory, add compiled artifact digests to that value, and write evidence without rereading it to continue the operation.
  - Preserve the ownership-marker read when reusing an existing output directory; this is an external-state check, not a redundant conversion.
- [x] `packages/workspace/ProjectArchive.scala`
  - Build the ZIP member list from the generated project's member names rather than decoding its JSON manifest.
  - Keep member-count, containment, regular-file checks, and deterministic entry timestamps.
- [x] `packages/workspace/PackageBuilder.scala`
  - Archive the returned project and retain its manifest without reading the just-written `generation.json`.
  - Preserve the final browser response shape and repeat-generation behavior.
- [x] `harness/runner/.../verification/CheckPortableProject.scala`
  - Reconstruct the project facts when loading an existing manifest or unpacking an archive. Decode at this actual input boundary.
  - Continue rebuilding from empty outputs and independently comparing artifact hashes and ledger observations.
- [x] Review `bindings/verify/CheckBinding.scala`, `GenerationDeterminism.scala`, and `BindingMutation.scala`.
  - Keep reads whose purpose is to verify saved artifacts and the deliberately modified compiled adapter.
- [x] Add an archive-boundary test for preserved member selection and rejection of an escaping path.
- [x] Run Scala tests and generation, builder, and portability checks sequentially; compare generated source and DAR identities with the preserved baseline.

## FD53 — fifth actual commit: readable HTTP handling

- [x] `app/http/LiveServer.scala`
  - Keep server/executor lifetime, session creation, origin validation, authentication, and endpoint routing visible.
  - Extract bounded request decoding into one cohesive `RequestBody.scala` in the same package; keep the route table short and explicit.
  - Make submission's HTTP 202 response and ordinary HTTP 200 responses explicit while retaining all other statuses and headers.
- [x] `app/http/RequestBody.scala` (new)
  - Own bounded JSON reading, action envelope decoding, and single-key package requests.
  - Return `ActionRequest` or a validated key to callers; reject malformed, excessive, unknown, or missing fields at the boundary.
- [x] `api/.../workspace/ActionRequest.scala` (moved from submission ownership)
  - Share the existing request identity, typed command, and state version with the browser; encode the unchanged flattened HTTP format only at transport/storage boundaries.
  - Decode the envelope once into this shared model, preserving exact-field and typed command validation.
- [x] `web/.../live/LiveApp.scala`
  - Keep unconfirmed retries as `ActionRequest`, compare request IDs directly, and encode only for HTTP or session storage.
  - Decode existing saved requests on browser startup; preserve reconnect and retry behavior.
- [x] `harness/runner/.../live/LiveFailureSuite.scala` and `submission/SubmissionLifetimeSuite.scala`: update the moved request-model import; retain their independent lifecycle checks.
- [x] `submission/Submissions.scala`, `api/.../workspace/WorkspaceCommand.scala`, and `app/workspace/Workspace.scala`
  - Review the complete request-to-effect path; retain the existing command families, stale check, request identity, uncertain outcomes, and Resource ownership.
  - Edit only a demonstrated redundant representation or forwarding step; record the final scope.
- [x] Add focused request-boundary tests for sizes, malformed input, extra actor fields, and existing financing/composition payloads.
- [x] Run the standalone authenticated live gate and builder gate; preserve independent negative checks and compiler isolation.

## FD54 — sixth actual commit: explain and measure the final operations

- [x] Review financing, composition, and package-generation paths from input to ledger or file effect.
  - Record what each named function owns, which facts are established at its boundary, and where the independent expectation lives.
  - Retain the already direct financing selector and typed composition editor unless the completed trace reveals a necessary adjustment.
- [x] Final trace findings: `submission/Submissions.scala`, `ledger/client/SubmitChoice.scala`, and `app/workspace/Workspace.scala`
  - Remove the unread transaction copy from `LiveJob` and `SubmissionResult`; the ledger snapshot and recorded history remain authoritative.
  - Pass confirmed command IDs to reconciliation, which only needs membership to recover an uncertain job.
  - Preserve supervised execution, stale checks, definite rejection classification, and reconciliation behavior; rerun the live gate after this change.
- [x] Remove redundant `json` forwarding methods from `LiveJob`, `FinancingState`, `PlannedStep`, and `Composition`.
  - Use the existing codecs explicitly at the actual `CompositionCommand` wire boundary and in `FinancingStateSuite` and `WorkspaceContractSuite`.
  - Keep every established field and independent expectation.
- [x] `docs/fifth-draft/reading-guide.md` (new): provide direct source links for the three traces and explain intentional external JSON/file decoding.
- [x] `product/README.md`, root `README.md`, `docs/architecture/scala.md`, `docs/progress.md`, and `docs/release/changelog.md`: describe the final operation flow and point to current evidence.
- [x] `book/export/.../ExportBook.scala`: include the fifth-draft design in source export; update affected authored links and specimens if necessary.
- [x] Record comparable production Scala counts, per-file deltas, removed encode/decode or write/read cycles, and test totals. Distinguish added test/documentation code from product changes.
- [x] Check all source/book links and freeze the implementation in a clean commit before the full release.

## FD55 — final acceptance and handoff

- [ ] Run `scripts/harmonia release-check`: clean build, Daml tests, Scala tests, packages, generated bindings, regular stories, live service, composer, builder, boundaries, and portable rebuilding.
- [ ] Preserve all 67 original golden files, 32 matching fresh recordings, nine chapters, and pinned DAR identities. Compare Daml source bytes with draft four.
- [ ] Verify the archive digest and payload, extract into a path with spaces, and test rejection/restoration of a deliberately changed payload file.
- [ ] Exercise relocated `run-product`, `run-live`, and `run-book`; perform real browser handoff, consented composition, package compilation/download, source inspection, and desktop/mobile book checks.
- [ ] Sample Java RSS and process ownership during sequential verification. Keep only the bounded book preview after all owned ledger processes stop.
- [ ] Write `docs/fifth-draft/measurements.md` and `acceptance.md`, update `docs/release/acceptance.md`, complete the plan, and commit the handoff.

Any necessary adjustment is recorded here before its implementation. Reducing line count never permits weakening an independent expectation, removing a runtime boundary, or hiding a failed check.

FD51 proof: all 52 Scala tests pass (`.artifacts/fifth-51-build.log`); package import/retrieval passes on two fresh networks (`fifth-51-packages.log`); the real builder story matches its original expectation with zero differences (`fifth-51-builder.log`). All three resolution records equal the retained fourth-draft JSON (`fifth-51-resolution-parity.json`). These checks were run before committing the implementation; the final release will record a fresh clean revision.

FD52 proof: all 53 Scala tests pass (`.artifacts/fifth-52-build.log`). Deterministic generated sources and DARs, the compiled mutation regression, the unchanged builder golden, and a portable rebuild from empty outputs all pass (`fifth-52-bindings.log`, `fifth-52-builder.log`, `fifth-52-portable.log`). The harness retains independent artifact reads.

FD53 proof: all 57 Scala tests pass, including literal HTTP-format and bounded request-reading cases; the live Scala.js browser compiles (`.artifacts/fifth-53-build.log`). The authenticated live gate preserves authority, stale views, repeats, and reconnect; the builder golden still has zero differences (`fifth-53-live.log`, `fifth-53-builder.log`). The browser now retains a shared `ActionRequest` instead of an untyped JSON request.

FD54 proof: all 57 Scala tests and the linked live browser pass (`.artifacts/fifth-54-build.log`); authenticated ledger handoff, stale/repeat protection, and reconnect pass after removing unused submission copies (`fifth-54-live.log`). All source links, nine exported chapters, and recording links pass (`fifth-54-book.log`). The preliminary book intentionally uses retained fourth-draft recordings; FD55 generates all evidence afresh from the clean implementation commit. Production is 4,235 lines versus 4,252.

# Package input and compiled project proof

The independently authored `evaluations/package-builder/` story passes with zero differences at `.artifacts/builder-check-17447850100362222392/package-builder/` on 2026-09-10. Requests use the actual authenticated HTTP service and a three-participant Canton runtime.

The bank uploads the unchanged legacy DAR and retrieves a second copy exported by its participant administrator. Both match the committed archive digest, package ID, and LF version. It generates and compiles the reviewed typed adapter and example, then downloads their portable ZIP. The verifier hashes the actual downloaded source DAR against the committed pin and checks all four compiled/vendor DAR digests against the generation manifest.

The upstream metadata DAR resolves from its pinned source and is inspectable without an executable action mapping. Attempting to generate that unsupported mapping returns HTTP 400. Buyer credentials receive 403 from the builder. Malformed and oversized archive uploads return 400. Every failed operation leaves the accepted input count unchanged. The story retains exact HTTP status, response, resulting builder state, source/expected/actual provenance, and the downloaded ZIP.

Input bytes are bounded before inspection. ZIP declarations and actual streamed expansion are checked before the compiler reads the archive. A semaphore permits one package operation at a time, and eight accepted-input slots bound retained package data. Generation uses reviewed mappings tied to exact pinned archive identities. Arbitrary inspected packages do not become executable actions.

The live workspace explicitly offers legacy generated approval; the primitive fixture can generate a portable project but needs typed registration and rebuilding to enter that menu. The participant DAR export is an operator setup action, not a business-session administrator proxy.

## Reader walkthrough

The browser used a separate owned network at `.artifacts/live-4011973114253668598/`. Its native file input received the real 363,165-byte legacy DAR. **Inspect selected DAR** accepted it, **Generate and compile project** completed, and **Download compiled project** fetched a 2,199,596-byte ZIP. The same session retrieved the actual participant export and the pinned metadata input. Metadata displayed the unsupported executable-mapping explanation and no generation control. Final observed state contains three accepted inputs and one compiled project.

The package panel fits at 390 pixels with document width 390. Native labelled file/select controls, operation status, input slots, identity details, and compilation state were visually checked. Evidence: `.artifacts/verification/16-builder-state.json` and `16-package-builder-mobile.png`. The test network was stopped afterward; only the 128 MiB-heap book preview remained.

The existing book was updated in place with eight chapters and thirty recordings: the twenty-seven regular ledger stories, both composition stories, and this package story. Its interactive buyer-denial attempt shows HTTP 403 and three occupied slots in both expected and observed panels. Chapter 8 renders the actual setup, package, consent, execution, and verification steps. The browser has no horizontal document overflow at the checked sizes. Capture: `.artifacts/verification/16-package-playback.png`. These recordings retain their original source provenance; the final release gate will produce recordings from one clean source revision.

Scala formatting, JVM compilation, Scala.js linking, and complete book export pass. The package golden was executed before the final result-schema and reader changes; those changes compile and successfully validate/export its retained result. The release suite will rerun all checks together.

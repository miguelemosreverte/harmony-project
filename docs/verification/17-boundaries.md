# Execution boundaries and portable reference verification

Verified locally on 2026-09-10. The independent Markdown baseline in `evaluations/execution-boundaries/` passes with zero differences at `.artifacts/boundaries-8113632047783060608/execution-boundaries/`. Its first phase uses a real Canton sandbox and Daml Script; its second uses three authenticated participants and direct concurrent Ledger API submissions.

## Observed ledger limits

The script completes sixteen source approvals through a sixteen-step core process, then queries sixteen approved application contracts. A seventeen-step published definition is rejected. A fourteen-prerequisite exhaustive join completes after its selected branch's seven actions, while the other branch is skipped. The old process contract is unusable, a repeated recorded request returns the same completed contract, and reusing that request for another step is rejected. A caller that knows an undisclosed source ID cannot start a process using it.

The same script accepts and completes four generated composition actions, queries four approved legacy sources, and rejects a five-action plan. It queries eight stored proposal references and rejects a ninth. These are enforced ledger boundaries, independently of HTTP validation. Template-precondition rejection is recognized by its explicit observed SDK error identifier; unknown SDK/transport errors still abort the script.

In the concurrent phase, two bank-authenticated commands target the same observed process contract with distinct request IDs. Exactly one commits; the other receives `ABORTED`. The final ledger has one approved source, one active process, one completed step, and one transaction with either race command ID. The original source is archived. Both submission responses and actual participant transactions are retained inside the hashed observation artifact.

## Failed reads and projection identity

Twenty-seven Scala tests pass (`.artifacts/test17-final.log`). Controlled participant-adapter responses verify that permission, not-found, and invalid-argument errors during state observation produce **disconnected**, with zero submission calls. A definite permission denial during submission produces **rejected**; an unavailable submission remains **disconnected**. A missing selected contract produces **unavailable**, without claiming a ledger rejection.

The live catalog is loaded from the actual compiled smoke DAR package inventory. It requires exactly one package for each registered module family. A namesake application from another package cannot populate the live application projection. Real live startup and composition in the concurrent phase exercise catalog loading and authenticated streaming.

Stream admission tests retain the 512th protobuf record and reject the 513th without returning partial state. Two 3 MiB records are admitted, while a third exceeds the cumulative 8 MiB payload bound and fails. gRPC calls run under a cancellable context that is closed after success, error, or interruption. The last extraction of this unchanged bound into its focused helper is covered by those tests.

## Rebuild the downloaded project

`portable-check` consumes the actual ZIP downloaded by the package golden. It extracts source/configuration and vendor dependencies into `.artifacts/portable-project-17810139466980039634/`, deliberately omitting archived `.daml` build outputs. Both packages compile again, and their DAR digests match the archived manifest. The unchanged source DAR is checked separately.

The freshly compiled example executes on a new Canton sandbox and matches the independent generated-approval golden with zero differences at `.artifacts/bindings-7295226499799006072/`. The installed SDK and dependency cache were reused; this is an isolated source/build-directory proof, not a fresh operating-system installation. The final release gate separately checks a clean source checkout.

## Regression scope and resources

Existing continuation/issuer/subject/reuse, privacy, atomic rollback, and generated mutation checks remain the linked evidence in the [capability matrix](../capabilities.md). The full release suite will rerun them at one clean revision. `scripts/check` now includes execution boundaries and the portable reference after the existing checks, sequentially.

All owned ledger and script JVMs exited after verification. A final process listing contained only the existing book preview, configured with a 128 MiB maximum heap. No production throughput, durable restart, arbitrary action-cost, or untrusted-publisher proof is claimed; those boundaries are explicit in the capability matrix.

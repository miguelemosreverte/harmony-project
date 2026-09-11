# Authenticated composition foundation

Verified locally on 2026-09-10. `scripts/harmonia composer-check` executes two independently authored Markdown stories through the actual HTTP API, restricted participant credentials, and a three-participant Canton network. Both pass with zero differences at `.artifacts/composer-check-5686992144229421141/`.

- `examples/evaluations/composer-direct/` proposes a financing approval followed by buyer review. Bank acceptance, review before its prerequisite, and review by the wrong actor are rejected. Buyer acceptance creates the sources and core instance atomically; correct actors complete both source actions.
- `examples/evaluations/composer-generated/` reverses the business sequence: buyer review, then bank approval through the generated legacy adapter. The source statuses and actual core completion match the independent expectation.

The final result reads the observed process name/reference and each source contract's state. `observation.json` retains the normalized result, final snapshot, and raw participant events under the provenance hash. Per-action artifacts retain submitted request, observed job, and resulting state. The checker refuses non-final, stale, or disconnected outcomes; they cannot satisfy an expected ledger rejection. Baselines are read before execution and are never rewritten.

All twenty-two Scala tests pass. The existing authenticated handoff and authority checks also pass with the new workspace seed at `.artifacts/live-check-10054034749125271722/`. The four-party transfer passes after the subsequent memory correction. New composer and smoke DARs build successfully.

Recorded book support renders proposal/consent state, interactive attempts, assigned roles, and expected/observed source states. A desktop browser walkthrough of the direct story's early-review rejection showed the prerequisite approval enabled, review waiting, both sources pending, and a matching comparison. The browser editor and DAR-input walkthrough are separate remaining parts of step 16.

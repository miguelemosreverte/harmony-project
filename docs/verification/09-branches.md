# Branch and join proof

Verified locally on 2026-09-10. `scripts/check` passed all builds, twelve Scala tests, Daml smoke checks, eleven malformed-definition publication attempts, and eleven live ledger goldens. The aggregate run is `.artifacts/check-4024739415185362658/`.

The approved route enables financing and buyer review. An unselected closure is rejected. Joining before the decision or before review fails with unchanged application state. Once both application actions complete, the join succeeds. A matching repeated request is `duplicate`; a new join request is rejected. Reusing the decision request with another option is also rejected.

The declined route skips financing and review, rejects its unselected financing action, records closure, and completes the join. The skipped application contracts remain pending. Completed and skipped steps remain separate in the observations.

`DefinitionTests:testDefinitions` requires rejection of eleven invalid definitions, including cycles, missing predecessors, cross-option dependencies, absent options, and incomplete joins. It also publishes the valid reference definition as a positive control.

The browser edition at `.artifacts/book-branches/` was exported from the aggregate run. Its premature-join table shows approved financing, pending review, waiting workflow, and review still enabled. Its successful join shows completed workflow with closure still skipped. Chapter 4 renders both diagrams (nine nodes total), and all sixteen chapter links returned HTTP 200.

The [decision record](../architecture/005-decisions-and-joins.md) states the supported graph subset and staged transaction boundary. This increment implements explicit human selection, not automatic inference from an unverified external result.

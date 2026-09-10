# ADR 005: explicit decisions and exhaustive joins

Status: implemented; live verification is recorded with this increment.

The process model distinguishes application actions, exclusive decisions, and joins. Application actions have interface bindings. Decisions and joins are controlled ledger choices; neither masquerades as an application result. Every step has an assigned role. Decision receipts include the chosen option, so a reused request cannot silently select a different route.

The supported decision shape has one decision, two to four nonempty options, conditional application actions, and exactly one join. Each option must have an action. All conditional actions require the decision and may depend only on ordinary predecessors or predecessors in their own option. The join names exactly all conditional actions. Ordinary steps cannot directly depend on conditional actions; they may depend on the join.

Definitions are ordered acyclic graphs with at most sixteen steps. Unique identifiers and prerequisite validation reject forward references, cycles, missing nodes, repeated prerequisites, and cross-option dependencies. Exhaustive join validation prevents missing branches or dead routes. Nested decisions and multiple joins are rejected in this version. These limits are checked by the published template's `ensure`, before instantiation.

Selection records one option. Other conditional steps become skipped. A step is enabled only when its branch is selected and all prerequisites are completed or skipped. A join therefore waits for every selected prerequisite, while explicitly disregarding only unselected work. Completion accounts for completed and skipped nodes separately. Receipts make matching repeats idempotent and reject a new request for an already completed step.

This model does not cancel or mutate unselected application contracts. It does not infer a decision from browser state or a reported success flag. The branch example uses an authorized human selection followed by independently validated application actions. Earlier branch actions are staged transactions; the merge does not make them one atomic transaction.

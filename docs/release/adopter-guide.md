# First integration with an application team

Begin with one application action and one independently reviewable result. The team that owns the application should identify its choice controller, signatories, observers, consuming behavior, return value, and the business state that proves success. Record what other participants may learn.

1. Read the [capability matrix](../capabilities.md), [compatibility matrix](../compatibility.md), and [extension guide](../../book/extension-guide.md). Confirm that the proposed workflow and application action fit the demonstrated boundary.
2. Choose direct participation for an owned application that can implement `StepAction`. For an unchanged DAR, inspect the exact pinned identity and compare the action with the supported generated mapping shape. Unsupported records, collections, choices, or results require an explicit implementation decision.
3. Write a small Markdown scenario and a separate expected result before integration. Include a valid actor, a wrong actor, and the meaningful failed-action state. Define disclosure and result-consumption expectations where one application enables another.
4. Put the owned Daml code in its application package. Keep common interfaces small and acyclic. Put off-ledger orchestration, parsing, and UI support in the corresponding Scala feature slice; retain pure validation and explicit `IO`/`Resource` boundaries.
5. Build and execute on the pinned local runtime. Inspect actual source contracts and participant events, not just workflow status. For a generated adapter, verify the source DAR remains identical and compile/run the exported project independently.
6. Register a new action in the live typed catalog only after its package identity, controller, input bounds, and commands have executable evidence. A downloaded DAR alone does not authorize arbitrary live execution.
7. Add a chapter experiment or extend an existing one. Run the relevant checks, then the release gate from a clean commit. Record limitations and leave the committed expectation unchanged unless the intended behavior changed and was reviewed.

Before production work, the application team must separately establish its topology, authentication and key management, operational recovery, data retention, package upgrade policy, load requirements, and licensing. The current local multi-participant proof supplies behavior evidence, not those operational decisions.

Use the [feedback template](evaluator-feedback.md) to capture the first experiment. Contacts, demonstrations, announcements, and adoption confirmations remain external activities requiring authorization and actual evidence.

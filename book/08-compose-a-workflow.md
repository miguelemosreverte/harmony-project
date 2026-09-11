# Bring an application and compose a workflow

You can now make the plan yourself. The bank proposes an ordered workflow, the buyer accepts the exact plan, and each participant executes its assigned actions through the existing core. A second tool accepts compiled application packages and shows which integrations are actually supported.

## Start two participant sessions

Build with `scripts/build`, then run:

```sh
scripts/harmonia live
```

Open the bank and buyer links from the printed private `sessions.json` file in separate browser tabs. Each link provisions one fixed session. The bank's private financing handoff and the public composition workspace are separate examples on the same disposable local network.

Only one ledger environment can run in this workspace. Stop it with Ctrl-C before running the verification commands below. The book preview can remain open; see the [memory and process guide](setup.md).

## Bring the application

In the bank session, find **Bring an application package**. Either choose a local `.dar` file and click **Inspect selected DAR**, or select a verified source and click **Retrieve and inspect**.

The participant option reads an actual DAR exported by the local participant administrator during setup. The other options use the [committed input identities](../product/packages/inputs.md): the unchanged legacy financing application, the primitive-action fixture, and the upstream Splice metadata package. External retrieval uses the pinned commit URL and verifies the archive digest, package ID, and Daml-LF version. Party credentials do not become administrator credentials.

Expand **Package identity and origin**. The archive digest identifies the supplied bytes; the package ID identifies the compiled main package. A different ZIP containing the same compiled package has a different archive identity. The builder accepts at most eight inputs, each no larger than 8 MiB compressed, 32 MiB expanded, or 2,048 entries. It checks actual expanded bytes before invoking the compiler.

For legacy financing or the primitive fixture, choose **Generate and compile project**. The resulting ZIP contains a typed library, runnable example, source/configuration, copied DAR dependencies, compiled DARs, and a manifest. The builder verifies the uploaded identity against the reviewed mapping; it never guesses the meaning of arbitrary source fields. [Chapter 7](07-generated-bindings.md) explains the mapping and generated code.

Legacy financing's generated approval is already registered in this edition's action menu. The primitive fixture can produce a compiled standalone project but needs typed registration and a rebuild to join the live menu. Metadata can be inspected; it has no executable action mapping. These distinctions remain visible in the package card.

## Propose, authorize, and run

1. In the bank's **Build a workflow together** panel, choose a workflow name and a unique reference.
2. Keep two actions, or add up to four. Give each action a distinct step name, a meaningful role, and an actor: bank or buyer. A repeated role must always refer to the same party.
3. Choose direct financing approval, generated legacy approval, or review confirmation. Use **Move earlier** and **Move later** to set the order. Inputs survive background ledger refreshes while you edit.
4. Click **Propose workflow**. The proposal is recorded, but its source contracts do not exist yet.
5. In the buyer session, review the complete ordered plan and click **Accept this plan**. Acceptance atomically creates the sources, publishes a core definition, and starts its process. Before acceptance, the bank can cancel the proposal.
6. Use **Execute** in the session assigned to the ready step. Watch the actual source status and core completion change. A later action stays waiting until its prerequisite completes.

These evaluation sources are shared with both parties. A private application should use the signed-result continuation from [Chapter 3](03-participant-views.md), keeping its payload outside this public plan. The composer supports sequential workflows; [Chapter 4](04-progression.md) demonstrates the broader core's explicit branches and joins.

The ledger enforces authority, consent, and order even when a caller bypasses the buttons. Pending and uncertain requests block new submissions until the client reconciles the original request. Input errors remain visible until dismissed or replaced by another submission. The workspace retains at most eight unique proposal references, including cancelled proposals. Reloading recovers committed state; stopping the network ends the evaluation.

## Compare with committed stories

The [direct composition input](../examples/evaluations/composer-direct/input.md) and its [expectation](../examples/evaluations/composer-direct/expected.md) include wrong-party acceptance, early review, and execution by the wrong actor. The [generated composition](../examples/evaluations/composer-generated/input.md) reverses the order and uses the generated adapter. Their interactive recordings show consent, enabled steps, and expected/observed source status after each attempt.

The [package input story](../examples/evaluations/package-builder/input.md) and its [expectation](../examples/evaluations/package-builder/expected.md) check upload, participant export, generated project download, and rejected inputs. Its recording shows whether each operation consumes an input slot. Failed operations leave the accepted inputs unchanged.

```sh
scripts/harmonia composer-check
scripts/harmonia builder-check
```

Try giving both actions the same name: the UI must show a specific diagnostic. Then restore unique names, put buyer review first, and select generated bank approval second. Complete the workflow from the two real participant sessions. In the package panel, inspect metadata and observe why package availability does not imply an executable action.

Read the [composition decision](../docs/architecture/010-composition.md), [on-ledger consent model](../product/ledger/composition/daml/Composer.daml), [Scala input builder](../product/server/src/main/scala/harmonia/packages/workspace/PackageBuilder.scala), and [verification evidence](../docs/verification/16c-builder.md).

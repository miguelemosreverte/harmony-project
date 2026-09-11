# Read the product

Start in [product/](../../product/README.md). Its five directories identify contracts, public values, service operations, browser controls, and pinned package inputs. Read one operation before following infrastructure.

## A financing handoff

| Read | What it answers |
| --- | --- |
| [FinancingState](../../product/api/src/main/scala/harmonia/financing/FinancingState.scala) | What actions and visible state exist? |
| [FinancingObservation](../../product/server/src/main/scala/harmonia/financing/FinancingObservation.scala) | How do visible contracts become those values? |
| [Financing](../../product/server/src/main/scala/harmonia/financing/Financing.scala) | Which ledger choice performs the action? |
| [Workspace](../../product/server/src/main/scala/harmonia/app/workspace/Workspace.scala) | How are participants, current state, and submission coordinated? |
| [FinancingPanel](../../product/web/src/main/scala/harmonia/financing/FinancingPanel.scala) | How does the browser present that state and request an action? |

The ledger application owns approval authority. The service observes and submits; the browser acts on the public response. [LiveServer](../../product/server/src/main/scala/harmonia/app/http/LiveServer.scala) owns HTTP routing and session checks. [Connections](../../product/server/src/main/scala/harmonia/app/Connections.scala) supplies the participant clients. [ServerConfig](../../product/server/src/main/scala/harmonia/app/ServerConfig.scala) is the standalone startup boundary.

## Compose a workflow

[Composition](../../product/api/src/main/scala/harmonia/composition/model/Composition.scala) defines and validates the plan. [CompositionEditor](../../product/web/src/main/scala/harmonia/composition/CompositionEditor.scala) edits that typed value. [ComposerCommands](../../product/server/src/main/scala/harmonia/composition/ledger/ComposerCommands.scala) translates consent and execution commands into ledger choices. [Composer](../../product/ledger/composition/daml/Composer.daml) enforces consent and creates the process.

## Bring an application

[PackageBuilder](../../product/server/src/main/scala/harmonia/packages/workspace/PackageBuilder.scala) coordinates acquisition, inspection, and generation. [BuilderInput](../../product/server/src/main/scala/harmonia/packages/workspace/BuilderInput.scala) names the available facts. [TemplateShapeReader](../../product/server/src/main/scala/harmonia/bindings/inspect/TemplateShapeReader.scala) checks structured LF types; [GenerateBinding](../../product/server/src/main/scala/harmonia/bindings/generate/GenerateBinding.scala) compiles the reviewed mapping into a portable project.

## Follow the evidence separately

The [examples](../../examples/README.md) pair readable inputs with committed expectations. The [harness](../../harness/README.md) executes them. The [book](../../book/README.md) turns the same results into chapters and interactive playback. These are useful review companions with their own directories and compiler targets.

The [repository map](../architecture/repository.md) explains dependency direction. The [principles and ordered plan](../../FOURTH-DRAFT.md) record why these boundaries exist. Previous designs and measurements remain in [history](../history/README.md).

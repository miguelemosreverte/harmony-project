# Harmonia architecture

*The proposed architecture, based on the original documents.*

## 1. The purpose

Harmonia coordinates processes that span independently developed smart-contract applications. It records **what has happened, what may happen next, and who may act**. Application teams can reuse that coordination model across products.

The central decision is to keep workflow state and execution rules **on Canton**, using Daml smart contracts. Each participating application keeps its own contracts, permissions and visibility rules.

## 2. Where the work happens

**On the ledger:** Harmonia Core stores workflow definitions and running workflows. Application contracts provide the actions those workflows use. Canton validates and commits their transactions.

**Outside the ledger:** the Dapp lets people define workflows, inspect progress and submit actions. The UI requests transitions; ledger contracts enforce the rules. A running workflow's recorded state survives the browser closing.

**Before deployment:** application developers or Builder prepare the code that connects an application's actions to Harmonia's workflow model.

## 3. What happens during execution

A **workflow definition** describes the permitted steps and paths. Starting it creates a **workflow instance**: one running process, with its own progress and participants. Roles resolve to concrete ledger identities, called **parties**.

For a step that invokes an application, Harmonia needs two things: the party assigned to act and the contract action to execute. In Daml, that action is called a **choice**.

A **binding declaration** records which application contract template and choice fulfil a particular workflow step kind and role. This gives the shared workflow model a declared connection to the application's actual code.

Execution must satisfy both the workflow's rules and the application's authorization requirements. **Assigning a step does not grant additional permission.** Successful execution records the resulting workflow progress and outputs.

The next step may involve another application or party. Core preserves the process state between those actions.

## 4. How applications join

Daml code is distributed in archives called **DARs**. A **Binding DAR** packages the declarations used to connect application actions to Harmonia.

An application team can supply this integration itself. For supported existing applications, **Builder** generates the binding package and supporting code. Both routes participate in the same Core workflow model.

Builder runs during development. The deployed Daml contracts govern execution.

## 5. How processes compose

A **continuation** carries one workflow's outputs to another workflow definition. This lets separately owned processes connect while retaining their own participants and access rules.

Where the workflow structure and required authorizations permit it, several steps can execute **atomically**: all succeed together or none do. Otherwise, coordination proceeds through separately authorized stages.

The first release supports a bounded set of steps, branching, joins and continuations. The original proposal leaves detailed binding mechanics and package relationships for technical design.

---

Sources: [original proposal](../../../../docs/proposal/harmonia.md), especially the Objective, Core, Dapp and Builder sections; [original architecture](../../../../docs/proposal/harmonia-architecture.html), especially the contract model and open design points.

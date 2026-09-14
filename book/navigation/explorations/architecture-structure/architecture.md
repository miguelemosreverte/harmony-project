# Harmonia architecture

## Start with Canton

Banks need to agree on transactions without exposing every customer's records. [Canton](https://docs.canton.network/overview/understand/what-is-canton) is a blockchain network designed for that combination: shared transactions with selective visibility.

Applications on Canton use **Daml** smart contracts to describe their records, permitted changes and required authorizations. Banks are using this technology: Lloyds reported a [pilot purchase of a government bond using tokenised deposits](https://www.lloydsbankinggroup.com/media/press-releases/2026/lloyds/lloyds-tokenisation.html) on Canton.

## An interface the contracts share

A bank's contract might approve financing; a custodian's might release an asset. Canton already lets applications transact together. Harmonia supplies a reusable way to coordinate their actions into a larger process.

Each participating contract exposes **StepAction**, a small Daml interface. Its **actor** identifies the party required to authorize the call; its **subject** identifies the business reference the action concerns.

The interface's **ExecuteAction** operation runs the application's implementation and returns a contract reference for subsequent use. A workflow can therefore hold references to different application contracts and invoke them through the same interface. Each application keeps its own business rules and permissions.

## Compose the actions on-ledger

Harmonia's workflow engine, **Core**, is itself written as Daml contracts. A workflow records its steps, assigned parties, application-contract references and progress on the ledger.

To advance, Core checks that the step is available, the requester is assigned, and the target action's actor and subject match the workflow. It then invokes ExecuteAction and records completion.

**The application action and that step's progress commit in one transaction.** If either fails, neither change is committed. Later decisions can advance through separate transactions.

![A workflow contract calls financing and custody application contracts through their matching StepAction interfaces.](09-contract-composition.png)

*One calling convention across independently owned application contracts.*

Processes can also compose through results: a receiving process checks the result's issuer, intended recipient, subject and permitted continuation before proceeding. Each process retains its own progress and authority.

## Prepare and request work off-ledger

An application can implement StepAction directly. To connect an existing application, a developer can supply an **adapter**: a separate Daml contract that implements StepAction and calls the selected application operation.

**Builder works off-ledger.** For supported application shapes, it checks a developer's reviewed mapping against the compiled application and generates the adapter project. The adapter is compiled and deployed on-ledger alongside the existing application, whose code stays unchanged.

During use, off-ledger software submits authorized requests and reads confirmed results. It checks ledger commits when a submission's outcome is uncertain. **The on-ledger contracts enforce the rules and record progress.**

---

Project basis: *Development Fund Proposal*, Abstract and §2, checked against the current implementation.

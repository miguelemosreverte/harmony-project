# Harmonia architecture

## 1. What the system adds

Harmonia coordinates a process across applications owned by different organizations. Each application performs its own business actions. Harmonia keeps track of their order, the required participants and the progress between them.

These applications use smart contracts: code that controls changes to their records. The contracts run on Canton, a ledger that validates those changes and stores the results. Their rules are written in Daml.

Harmonia puts its coordination rules on that same ledger. **The ledger enforces both process rules and application rules**, while each application retains control of its data and permissions.

## 2. Three parts at runtime

The **browser** shows the process and accepts requests. It communicates over HTTP with a Scala service.

The **Scala service** reads the relevant contracts, translates requests into supported ledger commands, and submits them through the requesting participant's configured connection. It also tracks requests awaiting confirmation and turns ledger state into responses the browser can display.

The **Daml contracts** make the authoritative decisions. Harmonia Core's contracts store the permitted steps, assignments and progress. Application contracts enforce the business rules for each action. The service needs both sets of rules to permit the requested change.

## 3. Follow one request

Suppose a user asks to advance a process. The service submits that request to Canton. Core checks it against the stored process: the step must be available, the requester must be assigned, and the target action must match the process.

Core then invokes the application action. The application's authorization requirements still apply. For this supported execution path, **the action and the progress update commit in one transaction**. If either fails, neither change is committed.

The service reads the confirmed ledger state to refresh the browser. If a connection drops before the outcome is known, it reconciles the request against ledger commits. A missing response does not establish that the action failed.

## 4. Connecting an application

Core calls a small, shared Daml interface. An application can implement that interface directly. Alternatively, an adapter can translate the call into an existing application's operation. Core therefore needs the shared interface, without importing each application's implementation.

The adapter must connect a workflow action to specific application code. A developer supplies that mapping. Builder checks the reviewed mapping against the compiled application and generates an adapter project for the supported application shape.

The adapter is compiled and deployed before use. Builder performs development work; the deployed adapter performs the translation during execution. The existing application's code stays unchanged.

## 5. Passing work between processes

One process can produce a result needed by another, separately owned process. The receiving process checks who issued that result, whom it was intended for, and what it authorizes before continuing.

This shares a result under explicit access rules. Each process keeps its own participants, progress and authority.

---

Sources: [original proposal](../../../../docs/proposal/harmonia.md) and [product implementation guide](../../../../docs/fourth-draft/reading-guide.md).

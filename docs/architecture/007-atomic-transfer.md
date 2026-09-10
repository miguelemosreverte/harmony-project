# Decision 007: staged consent and bounded atomic transfer

Status: implemented for the local synthetic reference.

## Decision

Separate source custody, destination custody, and the reference coordinator into independent Daml packages. Common `Harmonia.Transfer` terms name four distinct parties, a Settler drawn from those parties, a trade reference, an asset, and an exact positive decimal quantity. The coordinator uses the actual typed custody contracts, and participates in the generic core through `StepAction`.

The source allocation requires seller acceptance. A receiving request is signed by the buyer and prepared by the destination. The buyer's initial trade accumulates the seller, source, and destination as signatories through their controlled choices. Source and destination confirmations fetch typed contracts and check complete equality of terms. A settled trade cannot be created with the buyer's authority alone.

Final settlement calls `Source.Withdraw`, `Destination.Receive`, and creates the settled trade inside one update. `PublishedDefinition.StartAndAdvance` starts and completes its single core step in that same transaction. Failure rolls back all final effects. Successful preparation remains committed after failure.

Both custody choices require the four named parties as controllers. The fully consented trade supplies the necessary authority inside its settlement consequences. A single Settler cannot bypass it by directly withdrawing a lock. The destination consumes a source-and-seller-signed release and checks all terms before receipt. These are synthetic issuer-controlled positions; this is not an implementation of a public token standard or a payment exchange.

## Observations and limits

The feature owns its Scala input model, Markdown parser, runner, and Daml Script modules. The runner reads actual holdings, locks, releases, trades, and workflows after each attempted action. It waits for the relevant transaction to reach the participating nodes before collecting balances. Observation or unexpected transport failures abort the run.

A settlement transaction is counted only when the Settler's event stream contains the archive of the recorded source lock plus creation of a destination holding, a trade, and a workflow instance in the same transaction. The counted identifiers and full stream are retained. Per-step queries independently require settled trade and completed core state, preventing the transaction count from standing in for business-state validation.

One whole position is supported: one source lock, one release, one destination receipt, and one workflow action. The story format permits 1–32 attempts and quantities up to 1000000000000 with at most ten decimal places. Both successful Settler assignments and missing consent, missing lock, missing readiness, rejected final receipt, wrong Settler, repeated settlement, and direct withdrawal are checked against independent goldens.

The destination's `accept` or `reject` receipt rule is an explicit fixture. Preparation authorizes an attempt; a local receipt rule can still reject it. The reference does not implement cancellations, expired locks, partial quantities, fees, a payment leg, or a production custody service. All four trade parties observe trade and custody quantities in this example; it makes no confidential-balance claim.

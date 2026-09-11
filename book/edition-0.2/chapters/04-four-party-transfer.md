# A transfer across four parties

Alice is buying a position from a seller. The source custodian holds it today. The destination custodian will receive it for Alice. The example uses ten synthetic TEST units.

## First, everyone prepares

1. The seller agrees to the trade and locks the source position.
2. The source custodian confirms the locked position and trade terms.
3. The destination custodian prepares and confirms its receiving permission.
4. Alice, the designated settler in this example, requests settlement.

The settler is one of these four parties. No fifth party takes ownership of their responsibilities.

## The final transfer is one transaction

The final action withdraws the locked position, passes a typed release to the destination, creates the destination holding, and records completion. These effects belong to one eligible ledger transaction.

The earlier preparations are separate transactions. Grouping them in one diagram does not make the whole journey atomic.

## Watch the refusal as well as the success

From the opening scene, choose **Follow a recorded outcome**, then **Follow successful settlement**. The displayed quantities come directly from the observed ledger result. Attempts to bypass the coordinator, settle as the wrong party, or settle again are refused.

On the outcome page, choose **Follow a refused final leg**. The destination rejects the final action. Ten units remain locked at the source and zero arrive at the destination. The earlier preparations remain in place; this reference does not implement cancellation or expiry.

This is a bounded transfer example, without a payment leg, fees, splitting, or an arbitrary settlement graph.

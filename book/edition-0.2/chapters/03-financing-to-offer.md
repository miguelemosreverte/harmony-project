# Alice buys a home

Alice wants to make an offer for **Riverside 17**. Northbank has her financing documents. Ben works on the buyer's side of the offer, and Sofia receives it on the seller's side.

## Four people, one understandable handoff

| Person | Responsibility | What they pass forward |
| --- | --- | --- |
| Northbank | Finalize its financing assessment | A result for this buyer and this offer |
| Alice | Prepare an offer and make a proposal | An authorized purchase proposal |
| Ben | Relay the proposal | The proposal ready for receipt |
| Sofia | Receive the relayed proposal | A completed receipt |

Northbank's documents stay in the financing domain. The offer uses the scoped result. It does not receive the private document payload.

## Start with the successful path

Choose **Try it**, then **Next action**. The recording includes deliberate wrong turns: Alice cannot assess her own financing or forge a proposal, and Sofia cannot receive a proposal before Ben relays it.

By the end, Sofia has received the proposal. This example ends at receipt; it does not claim a completed property sale.

## Then change one condition

Choose **Financing refused**. Northbank successfully records a negative assessment. Recording that decision is a committed transaction, but it does not authorize a purchase proposal. The later proposal attempt fails and the offer remains waiting.

The distinction matters: “the request was processed” and “the financing was approved” are different outcomes.

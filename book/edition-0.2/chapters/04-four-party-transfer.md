# Four-party transfer

Follow staged work into one transaction

Buyer, seller, source custodian, and destination custodian each have work to do. One of these four parties fulfills the Settler role. There is no implied fifth operator.

## How to read this chapter

First establish consent, the source lock, and destination readiness. Only after the required conditions hold may the eligible final path commit atomically. Four animated steps do not by themselves prove four ledger transactions or one atomic transaction.

## Current evidence and next work

The existing reference demonstrates a bounded final transfer. The 0.2 application design must expose each party’s work and the evidence of atomic completion, with refusal and rollback cases beside the successful path.

[Existing transfer chapter and golden evidence](../../../book/06-atomic-transfer.md)

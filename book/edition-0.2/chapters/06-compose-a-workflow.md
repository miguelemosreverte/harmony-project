# Do the next task

Nina works at the bank. Her task is to approve a private financing case. The scene shows the bank, the buyer, and an observer of shared progress.

## Make a live handoff

[Open the local sandbox](../sandbox.html). Its private launcher opens each participant in a separate tab.

1. In **Bank**, inspect the private case and approve financing.
2. In **Buyer**, wait for the signed approval, then continue.
3. In **Reviewer**, observe the shared completion.

The scene changes when the server observes the ledger result. Refreshing recovers that participant's current state. The buyer and reviewer never receive the bank's private details.

## Compose a workflow

The **Compose** view lets the bank propose a bounded workflow and the buyer consent before execution. The draft stays in place during polling and when switching workspace tabs. Inspect committed operations in **Evidence**; connect an application under **Applications** in the bank session.

## When a submission is interrupted

An unresolved request remains pending. Reconnecting reconciles it with the server; retrying reuses the same request identity. A lost connection keeps the last observed state visible. It does not turn uncertainty into success.

This sandbox covers the existing financing and composition APIs. The full property offer and custody transfer remain recorded stories in their respective chapters.

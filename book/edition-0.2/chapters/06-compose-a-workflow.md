# Do the next task

Nina works at the bank. Her task is to approve a private financing case. The scene shows the bank, the buyer, and an observer of shared progress.

## Make a live handoff

The chapter's next step offers the local sandbox. Its private launcher opens each participant in a separate tab.

1. In **Bank**, inspect the private case and approve financing.
2. In **Buyer**, wait for the signed approval, then continue.
3. In **Reviewer**, observe the shared completion.

The scene changes when the server observes the ledger result. Refreshing recovers that participant's current state. The buyer and reviewer never receive the bank's private details.

## Compose a workflow

Choose **Choose the next task**, then **Compose a workflow**. Bank answers one question per page and reviews the complete plan before proposing it. Buyer consents before execution. The URL preserves the unfinished plan and current question; polling does not detach the input.

## When a submission is interrupted

An unresolved request remains pending. Reconnecting reconciles it with the server; retrying reuses the same request identity. A lost connection presents a focused recovery page; reconnecting restores the current task. It does not turn uncertainty into success.

This sandbox covers the existing financing and composition APIs. The full property offer and custody transfer remain recorded stories in their respective chapters.

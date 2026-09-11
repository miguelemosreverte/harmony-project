# Try the live handoff

Build the project using the [setup guide](../book/setup.md), then run:

```sh
scripts/build
scripts/harmonia live
```

The command starts three local participants and a loopback web server. It prints
the viewer address and the path to `sessions.json`, an owner-readable local file
containing a different provisioned link for each participant. Open the bank and
buyer links in separate tabs. Open the reviewer link in a third tab to inspect
Olivia's view. These links grant access to the running synthetic demonstration;
keep them in your local session.

1. In the bank tab, inspect the private application and click **Approve financing**.
   Wait for the submission to say **committed**.
2. In the buyer tab, the signed approval enables **Continue shared workflow**.
   Click it and wait for **committed**.
3. All tabs show the shared workflow as complete. Only the bank displays the
   private application payload. Expand visible ledger transactions to inspect
   their identities.
4. If a submission loses its HTTP response, the tab retains its request ID.
   Reconnect to recover a recorded result, or use **Retry unconfirmed request**
   to send the same request safely. New actions stay disabled while it is uncertain.
5. Reload the buyer tab or use **Reconnect / refresh**. The participant query
   recovers the committed state. A closed or unavailable service is shown as
   disconnected; previously displayed state is retained as an older observation.

Stop the command with Ctrl-C when finished. The network is disposable: restarting
creates a new setup and new session links. The [architecture note](architecture/009-live-sessions.md)
explains authentication, permission boundaries, submission states, and the
in-memory lifecycle.

## Repeat the acceptance check

```sh
scripts/harmonia live-check
```

This runs the [authored input](../evaluations/live-handoff/input.md), compares
actual business state with the [committed golden](../evaluations/live-handoff/expected.md),
and checks [identity and reconnect expectations](../evaluations/live-handoff/security-expected.md).
It also attempts to read and act as the bank using the buyer's actual ledger
credentials, sends an actor override directly to HTTP, submits an old view,
retries a request, and reconnects through a fresh HTTP client.

The printed evidence directory contains the business result, field-level diffs,
security result, scoped raw ledger events, and provenance. Open it with
`scripts/book .artifacts/live-check-RUN` to inspect the handoff as a recorded
story. Ordinary checks do not update expected files.

## Compose a shared workflow

The same bank and buyer sessions also contain **Build a workflow together**. The bank chooses a name, unique reference, one to four ordered actions, and an actor/role for each action. Use **Move earlier**, **Move later**, **Add action**, and **Remove action** to arrange the sequence. The form survives background participant refreshes while you edit it.

**Propose workflow** records the plan. In the buyer session, read every proposed action and choose **Accept this plan**. The ledger creates the public source contracts and the core process together. Each party then uses its own **Execute** control when its assigned step is ready. Source status, integration path, and prerequisite/completion state are observed from actual contracts. The bank can cancel an unaccepted proposal.

Only direct financing approval, generated legacy approval, and review confirmation are compiled into this evaluation. The composer supports eight unique proposal references per disposable workspace. Repeated role names must bind to the same party. Invalid input produces a persistent diagnostic; pending or uncertain commands disable new submissions until their outcome is reconciled. Adding a new action package requires a reviewed mapping and rebuild.

These composed sources are visible to both parties. The private financing handoff above remains a separate example with its own disclosure boundary. Stopping the local network ends both evaluations.

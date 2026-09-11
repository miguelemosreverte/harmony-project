# Authenticated live handoff proof

Verified locally on 2026-09-10. The final authored live story passes at
`.artifacts/live-check-1624511490887743420/live-handoff/`. Its setup, action list,
and independent expectations are in `evaluations/live-handoff/`. The business
result uses the existing Markdown result format and compares with zero
differences; the independent security/reconnect comparison also has zero
differences. Raw party-filtered events, HTTP request bodies/results, and source,
input, expected, observed, and DAR provenance are retained.

## Actual authority and state checks

- Three independent participant nodes use restricted JWT users. The bank's
  authenticated query sees its private application and shared progress. The
  buyer and Olivia see shared progress, without the private application payload.
- Direct buyer-token Ledger API requests with the bank's read-as and act-as
  party both return `PERMISSION_DENIED`.
- HTTP without a provisioned session returns 401. An actor override returns 400.
- The buyer's attempt before approval is rejected. Bank approval commits, the
  observer's continuation is rejected, and the buyer's continuation commits.
- An old contract version produces `stale` before submission. Retrying the exact
  bank request returns `committed` and adds zero ledger transactions.
- A fresh HTTP client recovers the completed workflow. Only the bank's complete
  authenticated event stream contains the synthetic private payload.

The first matching run, before changing the input's display names to Northbank,
Alice, and Olivia, is `.artifacts/live-check-17698644450990526220/`. Expectations
were authored independently; no checker writes them.

## Browser walkthrough and injected connection failures

The browser executed bank approval and buyer continuation against the actual
running service at `.artifacts/live-9987274450224758765/`. The buyer view never
showed the private payload; reloading recovered its committed continuation.
Desktop captures are `.artifacts/verification/15-live-bank.png` and
`15-live-buyer.png`.

A fresh service at `.artifacts/live-2386776837720040583/` was used for two browser
fault-injection checks. These replaced browser fetch behavior around real HTTP
requests; they did not substitute ledger results:

1. Send the bank command, discard its accepted response, and temporarily fail
   state reads. A DOM observer captured **Pending** before the request completed.
   The UI retained an unconfirmed request in session storage. Reloading restored
   network access, recovered the actual committed job, and cleared uncertainty.
2. Fail the buyer's POST before sending it. The UI showed **Retry unconfirmed
   request** and disabled new actions. Restoring transport and clicking retry
   submitted the same request ID. The buyer finished with one job, a complete
   workflow, no retained uncertain request, and no private payload disclosure.

A subsequent state-read failure explicitly displayed **Disconnected — showing
the last observed state**, retained the completed observation, and recovered
with the refresh control. At 390 pixels wide there was no horizontal document
overflow. Captures: `.artifacts/verification/15-live-mobile.png` and
`15-live-disconnected-mobile.png`. Browser-check observations are summarized in
`.artifacts/verification/15-browser.json`.

## Regression and limits

All twenty-two Scala tests and the existing private-approval, purchase-approved,
and transfer-approved ledger stories pass after the authenticated network
changes: `.artifacts/check-5403854857099033244/`, with command output in
`.artifacts/check15-regression.log`. The full twenty-seven-story suite passed in
step 14. `scripts/check` now also runs `live-check`; hosted CI has not been run.
Scala formatting, JVM compilation, Scala.js linking, and recorded live-story
book export pass. The earlier complete book remains available separately.

This proves browser/HTTP reconnection within a running in-memory evaluation.
It does not claim durable recovery after terminating that network. Test HMAC
credentials and all APIs stay local to loopback. Session links and bootstrap
material are excluded from exported evidence; see ADR 009 for exact boundaries.

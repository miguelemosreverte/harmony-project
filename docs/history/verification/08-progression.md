# Persisted progression proof

Verified locally on 2026-09-10. `scripts/check` passed the Daml builds and smoke checks, JVM/browser builds, twelve Scala tests, and all nine live ledger goldens. Evidence is retained at `.artifacts/check-10058380565397350314/`.

The uninterrupted and resumed sequences end with equal normalized business observations: financing approved, review confirmed, both steps completed, no enabled steps, and two active application contracts. An independent comparison of the retained raw observations confirmed equality after excluding incidental contract identifiers.

The resumed story uses nine distinct client processes: setup `39068`, then attempts `39079`, `39090`, `39102`, `39113`, `39124`, `39136`, `39149`, and `39160`. Each attempt rediscovers the instance from the ledger. `.artifacts/verification/08-restart.json` records the independent equality and process checks. Waiting and reconnection change no state; a matching request returns `duplicate` without repeating the application action. A premature review and a new request for an already completed step are rejected.

Creation-path checks reject direct forged completion of both a process instance and private shared progress. The process requires publisher authority. Shared progress now requires issuer and owner signatures, obtained through a bank-signed proposal that creates only waiting state. This deliberately extends the earlier private story and closes its owner-only creation path; see [ADR 004](../../architecture/004-persisted-processes.md).

The exported book at `.artifacts/book-progress/` was checked in the browser. The duplicate attempt displays unchanged financing and pending review; the final attempt displays confirmed review and completed workflow. Chapter 4 renders its four-node SVG and all eleven links returned HTTP 200. The browser was rebuilt and the book re-exported after the diagram change.

The proof uses fresh clients against a continuously running ledger. It does not claim ledger storage recovery after a node restart or production publisher-key management.

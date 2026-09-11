# Bounded local JVM lifecycle

Verified on 2026-09-10 on the 24 GiB macOS development machine. Twenty-two old Java processes had accumulated across previews and live demos. Nine obsolete sbt launchers, their nine applications, and two Canton JVMs were shut down. The retained preview was then replaced at the same URL by a direct Java launch. No unrelated process was stopped. The final idle state has one book JVM and no ledger or sbt JVM.

## Real execution and overlap rejection

`scripts/harmonia check examples/stories/transfer-approved` passed with zero differences at `.artifacts/check-3915631785737725915/transfer-approved/`. Its four-party Canton runtime used a 2 GiB maximum heap and the Scala driver used 512 MiB, verified through `jcmd VM.flags` while running. The Daml Script log confirms its 512 MiB `JAVA_TOOL_OPTIONS`. All three transient programs exited after the check.

During that execution, `scripts/harmonia network-smoke` failed with exit code 1 and the explicit workspace-lease diagnostic before launching another Canton. The negative result is retained at `.artifacts/ram-overlap.log`. The transfer remained successful.

The replacement book returned HTTP 200 at `http://127.0.0.1:56007/`; its only process had a 128 MiB maximum heap, confirmed by `jcmd`. A final process listing contained no sbt or Canton JVM. Resident memory for that preview was approximately 94 MiB at the observation. System-wide compressed-memory storage fell from about 6.5 GiB before cleanup to about 0.9 GiB afterward; this is a host observation, not an isolated project benchmark.

Evidence: `.artifacts/verification/ram-cleanup.json`, `ram-limits.json`, `.artifacts/ram-transfer.log`, and `.artifacts/book-current.log`. Bash syntax checks and Scala formatting/compilation pass. The launch wrapper reuses its classpath only while both the source fingerprint and immutable application JAR remain valid.

## Scope

Build/check wrappers now release sbt before ledger work. A file lock shared by both sandbox and multi-participant runtime prevents overlapping environments within this workspace. Cats Effect resources stop owned descendants and wait for forced termination when graceful shutdown times out. This bounds ordinary local development; it does not impose an OS-wide memory limit or promise cleanup after SIGKILL. Heap limits exclude native JVM allocations.

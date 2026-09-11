# Local technical acceptance

The fourth draft is complete on `fourth-draft`. Its [handoff record](../fourth-draft/acceptance.md) identifies the exact software, ledger evidence, archive, relocated launchers, browser checks, and memory measurements.

| Item | Accepted delivery |
| --- | --- |
| Verified software revision | `0014c0d7a3eb8a31f16a2ac4ee81b1ef320d5948` |
| Archive | `.artifacts/release-14660662735469547171/harmonia-0014c0d7a3eb.tar.gz` |
| Archive SHA-256 | `6c504d928974561c350cc69f5e0f2484198ce72ee5eb005dffb6a894fa9811dc` |
| Verification | Eleven clean-checkout gates, 49 Scala tests, four Daml scripts |
| Book and evidence | Nine chapters, 32 fresh passing recordings, 1,338 payload hashes |
| Local book | `http://127.0.0.1:56007/` |

Start with [the product](../../product/README.md) and [reading guide](../fourth-draft/reading-guide.md). Product, book, and harness have visible ownership and compiler boundaries. The service runs with no book or harness classes on its classpath. The [measurements](../fourth-draft/measurements.md) distinguish simplification from relocation: 4,252 product Scala lines, 63 fewer lines in shared response code, and a net increase of 234 total Scala lines.

All 67 original golden files and all 47 Daml source/specimen files remain unchanged. Real browser checks passed for the standalone product, consented generated/direct composition, package compilation/download, and recorded playback. Only the 128 MiB book server remains running; measured RSS was about 91 MiB. The full sequential release peaked at 3.43 GiB sampled aggregate Java RSS, excluding the preview and native tools.

This acceptance documentation follows the verified software snapshot. Prior [third](../history/third-draft/acceptance.md), [second](../history/second-draft/acceptance.md), and [first](../history/first-draft-acceptance.md) records and Git branches remain preserved. Read [packaging](packaging.md), [setup](../../book/setup.md), and the [capability limits](../capabilities.md) for reproduction and scope.

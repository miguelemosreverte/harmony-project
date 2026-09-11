# Local technical acceptance

The fifth draft is complete on `fifth-draft`. Its [handoff record](../fifth-draft/acceptance.md) identifies the exact software, ledger evidence, archive, relocated launchers, browser checks, and memory measurements.

| Item | Accepted delivery |
| --- | --- |
| Verified software revision | `75ff1c0a524ae69b5df29eb93c4a59c0b70a0f74` |
| Archive | `.artifacts/release-6910669127351732605/harmonia-75ff1c0a524a.tar.gz` |
| Archive SHA-256 | `7ef992fa1f5f7d5a07a2183ce58cee9ac01d1934e4cdbe871c6f15c7e9deac98` |
| Verification | Eleven clean-checkout gates, 57 Scala tests, four Daml scripts |
| Book and evidence | Nine chapters, 32 fresh passing recordings, 1,360 payload hashes |
| Local book | `http://127.0.0.1:56007/` |

Start with [the product](../../product/README.md) and [reading guide](../fifth-draft/reading-guide.md). Typed package and generation facts flow directly between operations; browser retries and HTTP share `ActionRequest`. Product, book, and harness retain their compilation and runtime boundaries. The [measurements](../fifth-draft/measurements.md) report 4,235 production Scala lines, a modest reduction of 17, and explain the more substantial change in internal representations.

All 67 original golden files and all 47 Daml source/specimen files remain unchanged. Real browser checks passed for standalone handoff, consented generated/direct composition, package compilation/download, and recorded playback at desktop/mobile widths. Only the 128 MiB book server remains running; sampled RSS was about 93 MiB. The full sequential release peaked at 3.02 GiB sampled aggregate Java RSS, excluding the preview, browser, and native tools.

This acceptance documentation follows the verified software snapshot. Prior [fourth](../fourth-draft/acceptance.md), [third](../history/third-draft/acceptance.md), [second](../history/second-draft/acceptance.md), and [first](../history/first-draft-acceptance.md) records and Git branches remain preserved. Read [packaging](packaging.md), [setup](../../book/setup.md), and the [capability limits](../capabilities.md) for reproduction and scope.

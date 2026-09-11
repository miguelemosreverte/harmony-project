# Fifth-draft measurements

Counts include comments and blank lines. Baseline: fourth-draft `bef8ac5`; fifth-draft implementation frozen after FD54. Source count compares tracked Scala files, with tests separated from production. All 47 Daml files and all examples remain byte-identical to draft four.

## Source size

| Measure | Fourth | Fifth | Change |
| --- | ---: | ---: | ---: |
| Production Scala | 4,252 | 4,235 | -17 |
| Server Scala | 2,899 | 2,857 | -42 |
| Shared API Scala | 393 | 419 | +26 |
| Live browser Scala | 960 | 959 | -1 |
| Scala tests | 989 | 1,149 | +160 |
| Book Scala, including tests | 1,818 | 1,818 | +0 |
| Harness Scala, including tests | 5,244 | 5,250 | +6 |
| All Scala | 11,343 | 11,491 | +148 |
| All Daml | 2,391 | 2,391 | +0 |

Production changes from **58 files / 4,252 lines to 60 files / 4,235 lines**: 17 fewer lines, or **0.4%**. The median falls from 52.5 to 50 lines; the largest file remains the 261-line source generator. There are two additional production files because the shared request and bounded HTTP body reader now have explicit owners. This is a modest size reduction; its main benefit is a more direct flow of values.

Three focused product test files add 159 lines; one additional harness import makes total test growth 160 lines. The test count increases from 49 to **57**. Book source size stays constant; its exporter additionally includes the fifth-draft principles. Harness implementation grows five lines to read archived project facts at a real file boundary.

## Per-file production changes

Paths below are relative to `product/`; only files whose line count changes are listed. Other touched files update typed imports or callers without changing their size.

| File | Fourth | Fifth | Delta |
| --- | ---: | ---: | ---: |
| [api/src/main/scala/harmonia/composition/CompositionCommand.scala](../../product/api/src/main/scala/harmonia/composition/CompositionCommand.scala) | 44 | 45 | +1 |
| [api/src/main/scala/harmonia/composition/model/Composition.scala](../../product/api/src/main/scala/harmonia/composition/model/Composition.scala) | 92 | 89 | -3 |
| [api/src/main/scala/harmonia/financing/FinancingState.scala](../../product/api/src/main/scala/harmonia/financing/FinancingState.scala) | 51 | 49 | -2 |
| [api/src/main/scala/harmonia/workspace/ActionRequest.scala](../../product/api/src/main/scala/harmonia/workspace/ActionRequest.scala) | 0 | 30 | +30 |
| [server/src/main/scala/harmonia/app/http/LiveServer.scala](../../product/server/src/main/scala/harmonia/app/http/LiveServer.scala) | 242 | 173 | -69 |
| [server/src/main/scala/harmonia/app/http/RequestBody.scala](../../product/server/src/main/scala/harmonia/app/http/RequestBody.scala) | 0 | 39 | +39 |
| [server/src/main/scala/harmonia/bindings/generate/GenerateBinding.scala](../../product/server/src/main/scala/harmonia/bindings/generate/GenerateBinding.scala) | 132 | 122 | -10 |
| [server/src/main/scala/harmonia/packages/inspect/InspectDar.scala](../../product/server/src/main/scala/harmonia/packages/inspect/InspectDar.scala) | 38 | 51 | +13 |
| [server/src/main/scala/harmonia/packages/resolve/ResolvePackages.scala](../../product/server/src/main/scala/harmonia/packages/resolve/ResolvePackages.scala) | 132 | 141 | +9 |
| [server/src/main/scala/harmonia/packages/workspace/BuilderInput.scala](../../product/server/src/main/scala/harmonia/packages/workspace/BuilderInput.scala) | 60 | 52 | -8 |
| [server/src/main/scala/harmonia/packages/workspace/PackageBuilder.scala](../../product/server/src/main/scala/harmonia/packages/workspace/PackageBuilder.scala) | 191 | 187 | -4 |
| [server/src/main/scala/harmonia/packages/workspace/ProjectArchive.scala](../../product/server/src/main/scala/harmonia/packages/workspace/ProjectArchive.scala) | 37 | 35 | -2 |
| [server/src/main/scala/harmonia/submission/Submissions.scala](../../product/server/src/main/scala/harmonia/submission/Submissions.scala) | 134 | 124 | -10 |
| [web/src/main/scala/harmonia/live/LiveApp.scala](../../product/web/src/main/scala/harmonia/live/LiveApp.scala) | 154 | 153 | -1 |

Git reports 214 inserted and 231 deleted production Scala lines: **17 net removed**. Those gross counts include moved definitions, new typed values, and formatting, so they are not a measure of functionality removed. No product folder moved in this draft.

## Fewer internal representations

Six immediate recoveries of facts already held by the producer are removed:

- Two JSON resolution reads: generation and package retrieval now use `ResolvedPackage` directly.
- Two JSON inspection decodes: builder and template catalog now consume `InspectedDar`.
- Two write/read cycles: generation retains its initial manifest while adding compiled artifacts, and the builder uses the returned final manifest.

Two further internal field scans disappear: archive assembly uses generated member names, and browser retry reconciliation reads `ActionRequest.id`. Transaction JSON is no longer duplicated in submission jobs; four redundant `json` forwarding methods are removed. External compiler/ledger metadata, saved inputs, and independent artifact verification still decode at their boundaries.

## Subjective reading assessment

These are my judgments against the same principles, not measured developer research.

| Quality | Fourth | Fifth | Reason |
| --- | ---: | ---: | --- |
| Clear ownership | 9/10 | 9/10 | Established product/book/harness boundaries remain |
| Typed internal flow | 8/10 | 9/10 | Package, generation, and remembered requests retain their facts |
| Reading one operation | 8/10 | 9/10 | Direct routes and fewer recoveries through JSON or files |
| Minimalism | 7/10 | 8/10 | Less duplicate state; only 0.4% fewer production lines |
| Evidence quality | 9/10 | 9/10 | Same independent goldens with focused boundary coverage |

A senior developer should still review the reading traces. More draft branches alone would not justify a higher score. Final clean-release, browser, and memory evidence is recorded in acceptance after verification.

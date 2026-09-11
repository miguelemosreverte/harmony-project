# A developer and evaluator walkthrough

Use the [book](../book/README.md) as the reader's entry point and the [repository map](architecture/repository.md) to find a feature. This is a local synthetic evaluation. Publication, licensing selection, and adopter acceptance remain separate decisions.

## Ten-minute recorded walkthrough

Open an exported book with `scripts/harmonia serve-book DIRECTORY`; the packaged evaluation supplies its own book launcher. Playback starts no ledger.

1. Read Chapter 1 and open its recorded example. Select the rejected attempt, then the approved one. Open the independent expectation and actual result.
2. In Chapter 2, compare direct and unchanged-application participation. In Chapter 7, inspect the generated adapter and its parity recording.
3. Use Chapter 3's participant perspectives to see the private application and shared result. Recorded perspectives are saved evidence, not live credentials.
4. In Chapter 5, follow private financing into an independently owned offer. In Chapter 6, inspect the final-leg rejection and unchanged balances.
5. Open Chapter 8's composition and package examples. The first exposes consent/order/source status; the second exposes accepted inputs and unsupported mappings.
6. Open Chapter 9's boundary recording. Compare maximum sizes and the two competing commands. Finish with its extension guide and capability matrix.

Each chapter has direct buttons for matching recordings. The book opens at Chapter 1. Example and attempt selections have stable navigation addresses. Code and evidence open in an inspector; closing it restores focus, and returning to the chapter restores the reading position. The laboratory keeps actor perspectives and complete differences available. Read the [three source traces](second-draft/reading-traces.md) for financing, composition, and package generation.

## Live walkthrough

```sh
scripts/demo live
```

Open the bank and buyer links from the printed private session file in separate tabs. First complete the private handoff: approve as the bank, then continue as the buyer. Confirm that the buyer sees no private income/rating payload.

In the bank composer, name a new workflow, put buyer review first, and generated bank approval second. Propose it. Review and accept the actual draft in the buyer tab. Execute the buyer's review, then the bank's approval, and inspect the completed source states. Try duplicate step names to see input diagnostics; the two committed composition goldens also exercise authorization and ordering failures.

In the bank package panel, inspect a local legacy DAR, generate its portable project, and download it. Retrieve the participant export and the metadata package to compare package identity with executable mapping support. Use **Refresh package inputs** to recover package state after a lost response.

Stop the live owner with Ctrl-C when finished. The book can stay open. The workspace lease prevents a second ledger environment from starting alongside the first.

## Reproduce the complete technical check

```sh
scripts/check
```

The wrapper builds the ordered Daml packages and Scala targets, runs Daml and Scala tests, reproduces pinned packages and generated adapters, runs the regular goldens, and verifies authenticated live behavior, composition, package input, execution bounds, and a portable reference. Stages run sequentially with bounded JVM heaps.

For a focused development iteration, use the matching `scripts/harmonia` command from the [capability matrix](capabilities.md). Checks retain their run paths and fail rather than rewriting expectations. The final bundle manifest identifies the exact checked source revision, artifact hashes, and reproduction commands.

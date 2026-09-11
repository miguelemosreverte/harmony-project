# Executable examples

Every example pairs a readable `input.md` with an independently committed `expected.md`. The runner writes actual results and differences under `.artifacts/`; it never updates the expectation automatically.

- `stories/` contains application, workflow, privacy, purchase, and transfer scenarios run by `scripts/harmonia check`.
- `evaluations/` contains authenticated live, composition, package, and boundary scenarios run by their dedicated checks.

The [typed inventory](../off-ledger/shared/src/main/scala/harmonia/examples/Examples.scala) is the single membership list for discovery, chapter links, and release coverage. It records identity and location, leaving scenario facts in the Markdown. To add an example, write its input and independent expectation, register it in that inventory, and run its owning check. The [move manifest](../docs/second-draft/example-moves.json) accounts for the first-draft contents without changing their bytes.

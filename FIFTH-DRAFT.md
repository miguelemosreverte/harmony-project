# Fifth draft: easy to understand locally

Status: design proposal; implementation has not started. Branch `fifth-draft` starts from verified fourth-draft handoff `bef8ac5`. The fourth-draft branch and packaged book remain preserved.

## Purpose

A senior developer should be able to open an operation, understand its input, decision, effects, and failure behavior, and identify the story that proves it. The code should explain itself through ordinary names and a direct sequence of steps. Documentation should deepen understanding rather than supply missing explanations of control flow.

Keep the established `product/`, `book/`, and `harness/` boundaries. Improve the operations inside them. The 19-line financing choice selector is a useful reference: it names supported actions and makes the resulting ledger choice evident.

## Principles

1. Optimize for what a reader must hold in mind. Keep a related operation together; give a helper a name only when it expresses a useful concept.
2. Carry typed values between internal functions. Encode JSON at HTTP and evidence-writing boundaries. Do not write an artifact and immediately reread it to recover a value already available to its producer.
3. Validate external input once and preserve the facts validation established. Add types where they prevent a concrete mistake or remove repeated checking; avoid wrappers that merely rename a value.
4. Make the important path visible in reading order: authenticate, decode, decide, submit, observe. Keep transport mechanics and resource ownership explicit without repeating them in each operation.
5. Prefer concrete Cats Effect IO, immutable values, exhaustive cases, and scoped Resource ownership. Preserve cancellation, authority, stale-request checks, and uncertain-outcome reconciliation.
6. Reduce implementation through fewer representations and less duplicated work. Report deleted, moved, and added code separately. Do not shorten code by compressing formatting or removing useful names.
7. Preserve the behavior and existing readable golden expectations. Keep the book as a companion to the product and the harness as its independent evidence.

## First candidates

- Package acquisition and inspection: return their existing named facts directly, replacing JSON results that callers immediately decode.
- Binding generation: retain the generated project and manifest facts together so package assembly can use them without reading its own just-written evidence file.
- HTTP handling: make endpoint dispatch easy to scan and give request decoding a clear owner. Preserve the current wire format, limits, session checks, response behavior, and scoped server lifetime.
- Follow the resulting call paths through the feature. Remove forwarding helpers and duplicated representations only when the complete operation becomes easier to read.

These are candidates identified from the current implementation. A small, already clear operation should remain small. This draft does not need a new general framework or a new directory hierarchy.

## Evidence and review

Baseline: 58 production Scala files, 4,252 lines including comments and blanks; largest file 261 lines. Book, harness, tests, Daml, assets, and configuration are measured separately. Seek a real net reduction in production Scala while preserving useful names and explicit behavior. Do not present moved lines or shorter formatting as implementation removed.

For financing, composition, and package generation, provide a concrete reading trace showing where input becomes a validated value, which function makes the decision, where effects occur, and which expectation proves the behavior. Count redundant encode/decode and write/read cycles removed. Explain any remaining unchecked assumptions at the named boundary that justifies them.

Retain all 67 original golden files, 32 recording identities, nine chapters, supported HTTP behavior, Daml business rules, and pinned package identities. Verify product compilation and runtime isolation. Run the complete release sequentially, test relocated launchers and real browser interactions, and measure memory without claiming that heap caps equal resident memory.

## Proposed implementation order

- [x] FD50: first actual commit records this proposal and preserves the verified fourth draft.
- [ ] FD51: second actual commit simplifies typed package acquisition and inspection, verified by existing package and malformed-input checks.
- [ ] FD52: third actual commit carries generation facts through compilation and archive creation and proves a working end-to-end package operation against its original expectations.
- [ ] FD53: simplify HTTP request handling while preserving its authenticated contract and failure behavior.
- [ ] FD54: review the three feature traces and remove demonstrated indirection or repetition; document the remaining necessary boundaries.
- [ ] FD55: run the clean release and browser proofs; record production-only deltas, memory, and a concise reviewer handoff.

The brief is a proposal for the next implementation pass. A human readability assessment remains necessary even when every check passes.

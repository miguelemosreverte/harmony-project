# The source-cited 0.2 edition

This is the design-stage edition of the book. It accompanies the existing working
book without replacing its Scala reader or pretending its simulations are ledger
observations. The eventual runtime integration is in [the 0.2 plan](../../docs/0.2/PLAN.md).

Edit a chapter's narrative in `chapters/`. Assign original passages in the readable
[coverage map](coverage-map.md). The source originals remain under `docs/proposal/`.
`sources.json` pins their exact bytes. `build.py` derives quotations and preview
pages; `check.py` independently checks the rendered quotations and exercises
deliberate omissions, duplication, text alteration, and source drift.

From the repository root:

```sh
python3 book/edition-0.2/build.py
python3 book/edition-0.2/check.py
scripts/design-preview
```

The preview contains eight product chapters and two named context appendices.
The coverage page reports document units, unique words, and source fingerprints.
Source coverage targets 100% of the defined textual denominator. Product claim
status is separately authored in `docs/0.2/product-contract.md`; no quote count
changes that status. Full editorial quality still needs human review.

Generated HTML and coverage JSON are committed to keep the prototype directly
openable and reviewable. The check requires them to match a fresh deterministic
build. No JVM, ledger, third-party Python package, or network is needed.

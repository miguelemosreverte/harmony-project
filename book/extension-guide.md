# Add an application integration

Start with a copy of a small story, a concrete source action, and an independently written expected result. Preserve the source's own authority and disclosure rules.

## An application you own

1. Add a cohesive Daml package under `on-ledger/`, depending on the small `interfaces` package. Follow [Financing](../product/ledger/applications/financing/daml/Financing.daml) or [Review](../product/ledger/applications/review/daml/Review.daml).
2. Implement the `StepAction` view with the actual actor and subject. Its operation exercises the application's real choice and returns the correct replacement action contract.
3. Add the package to the ordered build only after its dependencies. Add a typed story slice if its input/result shape needs a new model; extend an existing slice only when its meaning fits.
4. Add an approved path, wrong actor, invalid source state, and repeated/stale action. Query actual source and workflow state after each attempt.
5. Link the story and relevant source from the matching chapter. Verify that a meaningful wrong expectation is visible in the comparator and recording.

## An unchanged package

Inspect and pin the exact archive bytes and main package identity in a reviewed package manifest. A separate source manifest can be checked with `scripts/harmonia resolve-packages path/to/inputs.md`. The current generator uses the repository's committed input catalog; add the new pin there when integrating it.

Copy a mapping from [the mapping directory](../product/packages/mappings/financing.md). Name the actual package alias, module, template, choice, actor/readers, subject, observed status, and typed example values. The compiler supplies types; avoid copying those declarations into YAML.

```sh
scripts/harmonia generate-bindings product/packages/mappings/your-application.md .artifacts/your-application
scripts/harmonia bindings-check product/packages/mappings/your-application.md product/packages/mappings/your-expected.md .artifacts/your-application
```

The second command compiles and executes the generated example. It compares with the supplied independent expectation. See [Chapter 7](07-generated-bindings.md) for supported shapes and deliberate mutation testing. Do not change the frozen legacy source to make a new mapping pass; it is a regression reference.

## Register an action in the live composer

A compiled project alone does not extend the live menu. Add the typed constructor/dispatch in [Composer](../product/ledger/composition/daml/Composer.daml), its allowed plan vocabulary in [Composer.Model](../product/ledger/composition/daml/Composer/Model.daml), and the matching Scala validation/editor option. Extend the exact package catalog and source projection for the new package. Keep any package-specific decoding in the feature that owns it.

Create an authenticated golden that proposes, obtains partner consent, and executes the action. Include wrong-party and invalid-order attempts. Rebuild and start a fresh local evaluation so the running catalog and uploaded DAR identify the same code. Dynamic installation of an arbitrary uploaded action is outside this edition.

For private data, implement a minimal signed-result continuation instead of copying private source fields into the shared plan. Add wrong issuer, subject, consumer, continuation, and reuse cases where applicable.

## Review the change

Keep the source input, expectation, implementation, chapter, and evidence together in the review. Explain any intentional baseline change. Run focused checks first, then `scripts/check` for release acceptance. Treat failed observation as failed verification. Record the checked revision and package identities; external adoption and publication require separate evidence.

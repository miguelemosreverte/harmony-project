# ADR 006: independent financing and offer continuations

Status: implemented; verification is recorded with this increment.

`PropertyFinancing` and `PropertyOffer` are independent application packages. They depend on the common action and result contracts, not each other or the core engine. The script binds their direct interface implementations to four versioned domain definitions. Financing is owned by the bank; proposal and relay use the buyer-agent domain; receipt uses the seller-agent domain.

A buyer-signed submission and bank review create the pending financing application. Both buyer and bank sign application state, while documents remain outside agent views. The fixture's bank decision is explicit setup data; finalization requires the bank and publishes an issuer-signed result. The result identifies its consumer, application subject, decision, and permitted continuation. It contains no document payload. A bank can issue its own statements, so the expected issuer is a trust anchor, not proof of independent underwriting quality.

The buyer-agent signs an invitation that creates only a waiting offer when accepted by the buyer. Offer and proposal state require buyer and agent signatures. The checked proposal choice validates the bank, buyer, application, approval, and exact offer reference, consumes the result, creates the proposal, and updates the source offer. The core advances in the same transaction. A malformed or spent result rolls the whole attempted proposal transition back.

Offer preparation and core instance creation are two staged transactions by the buyer. A failure starting that prepared workflow is a runner failure with retained evidence, never disguised as a fully rolled-back business rejection. Relay and receipt use the core's bounded `StartAndAdvance` choice: checked instantiation and the application action occur in one transaction. Each agent supplies its own authority. The whole financing-to-offer process is intentionally not one atomic transaction.

Result references bind the exact application and offer reference agreed by the parties. The reference is not a global uniqueness registry. Multiple offers may be prepared under that reference, but a particular result is consumed once. The reuse story preserves the first proposal and rejects the second attempt. Consumers must trust the expected bank and intended application/continuation identities, rather than arbitrary lookalike results.

Four participant nodes host the four business parties. Full party-filtered streams and contract queries verify that the bank/buyer observe the private financing data while the two agents do not, and that the bank does not observe offer contracts. This local test topology shares one JVM, synchronizer, and operator administration boundary. The recorded viewer bundles synthetic operator evidence; it does not enforce live authentication by hiding UI fields.

`VerifiedResult.continuation` also strengthens the earlier private-progress example. Its issuer uses a `shared-progress:` reference, and continuation checks require that exact value. The existing private golden must continue to pass after this interface extension.

Application choices retain their own authorization context: the acting parties and signatories of the exercised contract authorize its consequences. The interface does not replace those rules. See the [Daml ledger integrity model](https://archived.docs.digitalasset.com/overview/3.4/explanations/ledger-model/ledger-integrity.html).

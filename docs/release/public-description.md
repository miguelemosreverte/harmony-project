# Public release description — unpublished draft

Harmonia lets independently owned Daml applications participate in a shared workflow while their own choices retain control over business actions. An application can implement the common interface directly, or use a generated typed adapter for a supported existing DAR.

The local evaluation edition demonstrates two complete workflows: private financing approval that enables an independently owned property offer, and a four-party transfer whose final transaction either completes both custody legs or rolls them back. Smaller examples explain progression, branches, joins, consent, retries, and application boundaries.

The deliverable pairs working software with an executable book. Each story has a readable Markdown input, a committed expected result, and observations from actual execution. The browser lets readers step through attempts, compare results, inspect participant views, and follow the source. Live sessions offer a bounded workflow editor and DAR builder with explicit partner consent.

The implementation uses Daml on ledger and organized Scala feature slices off ledger, with Cats Effect `IO` and `Resource` for effects and lifecycle management. The local release records its exact source revision, pinned dependencies, verification logs, and artifact hashes.

The edition is a reference implementation for evaluation. Its capability matrix describes supported workflow shapes, adapter mappings, payload limits, privacy boundaries, and local topology. It does not establish production suitability or third-party adoption.

**Publication status:** this description is a draft. Select the public repository and project license, review upstream redistribution conditions, and obtain authorization for publication and announcements before using it externally. No external acceptance or adoption is claimed.

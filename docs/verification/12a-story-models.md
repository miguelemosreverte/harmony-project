# Reference input model separation

Verified locally on 2026-09-10. `scripts/check` passed the complete build, sixteen Scala tests, Daml checks, package reproduction checks, and all nineteen live stories. Workflow evidence: `.artifacts/check-4652622842057408608/`.

Financing and purchase now own their Scala input models and Daml Script inputs. The runner uses a small `Story` contract for identity, actions, workflow selection, and serialization. Custody can add its own model without adding optional fields to financing. Parsing and execution remain within their respective feature slices.

No file under `stories/` changed in this refactor. The committed expectations independently verify preserved behavior.

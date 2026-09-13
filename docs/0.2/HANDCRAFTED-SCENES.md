# Entry and story illustrations

The participant-link form was an implementation detail presented as a product
requirement. The repeated portrait pairs also failed the requested art direction.
This pass corrects both. The approved upper infographic is not being redesigned.

- [x] Remove pasted links from the home screen.
  - [x] Give the local sandbox two ordinary entry choices: Alice and Northbank.
  - [x] Provision the selected sandbox role internally; keep command authorization.
  - [x] Keep sandbox entry explicitly disabled for configured non-demo services.
  - [x] Preserve the selected workspace and role in shareable URLs.
- [x] Give every recorded workflow an individually composed story illustration.
  - [x] Write an explicit setting, action and composition for each of the 32 stories.
  - [x] Generate and inspect each asset against the approved visual reference.
  - [x] Use situations and environments rather than repeated portrait cutouts.
  - [x] Keep artwork fixed within a story. Only the observed dialogue changes by step.
  - [x] Keep illustrations descriptive: no baked-in success badges or false outcomes.
  - [x] Connect live workspaces and authored explanations to their relevant scenes.
- [x] Verify the delivered experience.
  - [x] Check anonymous entry, role isolation, expired access and deep links.
  - [x] Review every workflow on desktop and mobile, including short and long lines.
  - [x] Check that the upper infographic has not moved or changed.
  - [x] Publish the corrected build, screenshots and generation provenance.

The [review](HANDCRAFTED-REVIEW.md) records the resulting experience, visual
decisions, validation, and remaining limits.

## Ownership

`product/scene` owns a neutral illustration identifier and retained scene rendering.
Product features choose illustrations for their own workflows. The book owns the
recording-to-illustration mapping and authored chapter choices. Artwork conveys
the situation, while HTML dialogue describes actual observations; neither artwork
nor expected output proves an action occurred.

`LiveServer` exposes self-service entry only when explicitly started as a local
sandbox. `SessionEntry` presents two character choices without a token input.
Existing bearer credentials remain an internal transport detail. Selecting a
character does not submit a ledger command.

Exact prompts and asset hashes are kept beside the design study. The old portrait
assets remain as historical reference, but are no longer the support-scene UI.

## Reconnecting a long-running sandbox

`scripts/demo attach EXISTING_SANDBOX_DIRECTORY OUTPUT_DIRECTORY` opens a new
small service against existing participants. It does not start Canton or reset
contracts. The harness retains normal local signing credentials privately and
renews five-minute Ledger API tokens for each request. Previously exported token
files are snapshots and expire; they are not a durable reconnect mechanism.
The product service remains independent of this demonstration provisioning code.

# Entry and story illustrations

The participant-link form was an implementation detail presented as a product
requirement. The repeated portrait pairs also failed the requested art direction.
This pass corrects both. The approved upper infographic is not being redesigned.

- [ ] Remove pasted links from the home screen.
  - [ ] Give the local sandbox two ordinary entry choices: Alice and Northbank.
  - [ ] Provision the selected sandbox role internally; keep command authorization.
  - [ ] Keep sandbox entry explicitly disabled for configured non-demo services.
  - [ ] Preserve the selected workspace and role in shareable URLs.
- [ ] Give every recorded workflow an individually composed story illustration.
  - [ ] Write an explicit setting, action and composition for each of the 32 stories.
  - [ ] Generate and inspect each asset against the approved visual reference.
  - [ ] Use situations and environments rather than repeated portrait cutouts.
  - [ ] Keep artwork fixed within a story. Only the observed dialogue changes by step.
  - [ ] Keep illustrations descriptive: no baked-in success badges or false outcomes.
  - [ ] Connect live workspaces and authored explanations to their relevant scenes.
- [ ] Verify the delivered experience.
  - [ ] Check anonymous entry, role isolation, expired access and deep links.
  - [ ] Review every workflow on desktop and mobile, including short and long lines.
  - [ ] Check that the upper infographic has not moved or changed.
  - [ ] Publish the corrected build, screenshots and generation provenance.

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

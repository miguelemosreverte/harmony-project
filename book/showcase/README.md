# The workflow as a document

Export what the recorded reader shows into JSON so a reviewer can inspect the
story without clicking through it. A workflow document should answer: what does
this demonstrate, what happened at each step, what did the reader see, and where
does the evidence come from?

The first comparison is `financing-approved` versus `purchase-approved`. Both
depict a bank signing an approval, but the purchase continues into a property
proposal. Their use of the same image must be visible in the documents.

## Implementation plan

- [ ] Capture the existing reader's structured projections in an isolated tab.
  - [ ] Read the selected book's recordings, including supplied run overrides.
  - [ ] Use the compiled Scala projectors that already supply the UI.
  - [ ] Check the displayed illustration and dialogue against each projection.
- [ ] Export one readable document per workflow and one shared catalog.
  - [ ] Preserve story purpose, ordered actions, observed values and navigation.
  - [ ] Keep a stable diagram once; describe each step's varying fields separately.
  - [ ] Reference images once by identity, path and hash; list every use.
  - [ ] Link recording provenance, independent expectations and original sources.
  - [ ] Record renderer dependencies needed to reproduce the presentation.
- [ ] Verify the complete export and publish the two comparison links.
  - [ ] Reconstruct every diagram and compare it with the existing projector.
  - [ ] Check all image, source, evidence and navigation references.
  - [ ] Repeat the export and require identical JSON.
  - [ ] Commit and push the documents and exporter.

## Boundaries

This belongs to the book. It adds no product controls, ledger commands or new
execution model. The current approved illustration and infographic stay intact.
An illustration is explanatory artwork, not execution evidence. Reusing an image
does not mean two stories share a ledger transaction.

JSON references the existing image files rather than embedding their bytes or
copying them. Original source quotations retain their existing source identity;
an association with a chapter does not certify that its requirements are complete.

# A finite book, with two possible actions

The approved infographic is the ceiling for interaction complexity. A screen must
make its next step obvious. Two interaction areas are not permission to collect
controls inside panels. No selector bars, inspector tabs, hidden control drawers,
clickable progress dots, or automatic navigation loops belong in these journeys.

## Screen contracts

| Screen | Reader's question | What appears | First action | Second action | End condition |
| --- | --- | --- | --- | --- | --- |
| Welcome | Why am I here? | Two plain intentions | Understand | Verify | A second, specific choice |
| Understand | What should I read? | Product or implementation | Follow the product | Explore the code | One of four reader paths |
| Verify | What am I checking? | Architecture or original requirements | Review the diagrams | Follow the original documents | One of four reader paths |
| Product | What does this provide? | Narrated applications, people, and a concrete handoff | Begin the story | Read the mechanism | A bounded chapter route |
| Workflow scene | What happens now, and why? | Approved infographic, one observed moment, one caption | Previous | Next | The last scene continues to the next chapter |
| Developer | What does this file do? | File tree, actual colored source, purpose and passive slice diagram | Select a file in the tree | Return to the reading paths | Reader chooses when to leave |
| Reviewer | Does the implementation match the design? | One diagram, explanation, source identities and evidence | Previous | Next | A finite sequence of reviewed relationships |
| Original author | Where did these words go? | Original passage on the left; reused explanation on the right | Previous passage | Next passage | Quotation coverage and implementation limits |
| Chapter | What should I understand? | Readable prose, static citations, passive diagrams | Previous chapter | Next chapter | A stated conclusion |
| Sandbox entry | What can I try? | Bank-to-buyer handoff and honest live/recorded status | Enter the provisioned product | Return to the product story | The separate product opens |
| Live financing | What can my participant do? | Observed infographic and current responsibility | Eligible ledger action | Recovery only when needed | Observed completion |
| Live composition | What am I proposing? | One question or one reviewable plan at a time | Answer/select the current field | Continue or submit | Consent, execution, completion |
| Package integration | What does this application expose? | One input or one observed package result at a time | Supply the current input | Continue | Compiled project or explicit unsupported boundary |

The file tree is the sole composite selection control: folders and files form one
natural navigation structure. Graphs, line numbers, quotation titles, progress
indicators, badges, and references are explanatory content, not extra controls.
Browser Back/Forward and native scrolling continue to work. Query parameters
identify the current page, file, passage, or observed moment. Sharing the address
reproduces it. Previous/Next never wraps silently to the start.

## Ownership

- Product HTTP and browser code know nothing about book pages, chapters, or routes.
- Product module documentation explains responsibilities independently of the book.
- The book owns source extraction, chapter maps, quotations, and presentation.
- A development launcher starts the book and product as separate services. A book
  can link outward to the provisioned product; the product does not mount the book.
- Ledger authority, golden expectations, provenance, and product scope stay explicit.

## Work and review

- [x] Commit 1: document the page contracts and finite reader routes.
- [x] Commit 2: remove product/book runtime and navigation coupling.
  - [x] Replace book-specific product documentation with module documentation.
  - [x] Separate the book host and product host in development orchestration.
- [x] Commit 3: implement all four reader paths and prove the small interaction budget.
  - [x] Replace the shared control drawer and six audience presets with binary entrances.
  - [x] Simplify source, reviewer, original-author, workflow and chapter pages.
  - [x] Preserve actual source, all 32 recordings, exact quotations and useful URLs.
- [x] Commit 4: simplify live entry, financing, composition and package journeys.
  - [x] Keep each live step within two interactions without hiding a dashboard.
  - [x] Exercise real commands and recovery against one disposable network.
- [ ] Commit 5: navigate the complete routes, capture screenshots and write page notes.
  - [ ] Inspect desktop and mobile: purpose, caption, next action, destination, spacing.
  - [ ] Count interactions, including links, inputs, disclosures and embedded controls.
  - [ ] Check cold URLs, Back/Forward, keyboard, static reading and PDF output.
  - [ ] Commit and push the verified delivery with an accessible entry point.

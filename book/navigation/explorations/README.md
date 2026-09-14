# Image design records

Each complete presentation gets a Markdown record beside its versioned images.
The purpose is to make the message, visual decisions and review history easy to
inspect. For architecture, the unit of review is the whole architecture story.
Regions within it may answer specific questions; never require the reader to
approve isolated fragments before they can see how the whole works.

1. **Establish the message.** Name the reader's question and the intended
   understanding. Cite short, exact passages from the original model. Keep source
   facts distinct from visual interpretations and illustrative examples.
2. **Refine before generating.** Use three to five useful passes from meaning to
   visual hierarchy to the exact prompt. Record the decision and its consequence.
   Identify whether these are passes in one session or separate work sessions.
3. **Generate and critique.** Usually make two or three image iterations, each
   addressing an observed weakness. Save the exact prompt, input references,
   output filename, tool name, UTC times and commit. Preserve prior versions.
4. **Present the whole candidate in one browser page.** Make the complete story
   visible together, with pan and zoom for detail. Explain what works, what remains
   uncertain and which candidate is proposed. Assistant inspection is separate
   from user approval. Every round preserves this complete context.
5. **Append the user's review.** Record the actual words, image and commit reviewed,
   date recorded and resulting decisions. Update the book after that review.

The main measure is how readily a reader forms a correct mental model. Extra
words, extra variants and successful rendering checks do not establish that.

Current example: [Complete architecture](architecture-whole/design.md).
Scope correction: [Application authority](application-authority/design.md).
Earlier exploration: [Architecture overview and execution](architecture-v2/README.md).

Latest review: [Architecture version 7](architecture-structure/v7.html), the
three-part contract map proposed in the conversation, with [formatted prose](architecture-structure/read-v7.md)
and a [design record](architecture-structure/design-v7.md).
[Version 6](architecture-structure/v6.html) retains the broader diagram with UI,
build tooling and continuation.
The [version 5 design record](architecture-structure/design-v5.md) contains the
underlying architectural citations.
The [version 4 review](architecture-structure/design-v4.md) records the rejection
of repeated generic component maps and the switch away from image generation.
Earlier pages remain available for comparison.

Direction: [Architecture structure](architecture-structure/design.md).
The complete architectural presentation follows the original component and
contract views. Concrete financing and transfer walkthroughs belong to the user
branch. The design record develops the content in sections separated by horizontal
rules, ending with the exact generation prompt and the user's subsequent review.

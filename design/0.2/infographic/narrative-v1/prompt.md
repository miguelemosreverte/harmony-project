# Desktop narrative study

Generated with the built-in image generation tool. The generated lower conversation
is rendered beneath the original desktop screenshot in `desktop.html`.
The upper infographic and navigation in that preview come from `desktop-reference.png`,
not from the generated reinterpretation. Production rendering is unchanged.

The reference was captured at 1280 × 1000 from the purchase approval step of
`v0.2.0-reader.6`, with the unapproved state badge hidden only in the capture tab.
`review.json` verifies that the rendered preview changes pixels only within the
previously blank lower conversation area. This is a desktop still for review.

## Exact generation prompt

Use case: precise-object-edit / ui-mockup.
Asset: one high-fidelity DESKTOP web UI proposal, a still frame for a future character animation.

Input image 1 is the EDIT TARGET: an actual 1280 x 1000 Harmonia desktop screenshot. Preserve its entire existing infographic, header, footer navigation, fonts, images, arrows, colors and geometry exactly. Do not redraw, resize, reinterpret or add labels to the existing upper infographic. No state badges inside it.

CHANGE ONLY the currently blank white area beneath the two large infographic panels, approximately x=160..1120, y=670..850. Leave the white breathing space and navigation below y=878 intact. Keep the original full desktop framing and aspect ratio.

In that lower blank area compose a small, elegant, airy narrative scene: a short exchange between Northbank and Alice, styled to match the existing softly shaded blue-and-white illustrations. On the left use a small Northbank bank illustration matching the bank already present above, with its N medallion. Its restrained speech bubble says exactly:
"Your financing is approved."
On the right show Alice, recognizably the same dark-haired character and blue clothing as the existing Alice portrait, using a small laptop. A minimal blue check on the laptop screen suggests the application; no readable screen text. Her restrained speech bubble says exactly:
"I'll send my proposal."

The lower scene should feel like a quiet illustrated conversation, not a second workflow diagram or a dashboard. Make it modest in scale, about 150px high, centered across the available white space. Keep a clear gap below the approved infographic. White or very pale blue speech bubbles, subtle tails that clearly indicate the speaker, almost no shadow, generous margins. Match the existing Arial/Helvetica-like typography; dialogue must be comfortably legible at about 19-20px at the source screenshot size, sentence case, normal weight, dark navy. Each utterance should use at most two short lines. Keep this suitable for later faithful HTML/CSS implementation.

No extra headings, captions, labels, paragraphs, buttons, playback controls, panels, decorative arrows, explanatory legend, logo changes, new colors, or extra characters. In particular preserve all upper labels and portraits exactly and add NOTHING inside the approved infographic. Final output: the complete desktop screenshot with ONLY this separate lower narrative scene added.

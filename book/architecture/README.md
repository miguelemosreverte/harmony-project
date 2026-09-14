# Architecture reader

The three illustrated architecture passages use the same `StepCarousel` renderer
as the product stories. This folder owns the presentation only. The reviewed
[Markdown](../navigation/explorations/architecture-structure/architecture.md) and
illustrations remain the single content source; their prompts and provenance stay
beside them.

Open `index.html?step=0`. The zero-based `step` parameter preserves the selected
passage. Previous/next, the connected stops, horizontal swipes, and arrow keys
navigate within this architecture path. Home/End select the first/last passage.
The endpoints stop, without wrapping or leading into another audience's path.

Images and slides stay mounted. Shared grid rows keep the stage geometry stable;
only the selected passage is visible and available to assistive technology.
The header's Read link opens the complete source chapter. Printing, disabling
JavaScript, or an unavailable scene bundle retains all three passages.

The standard desktop and 390×844 phone layouts fit the viewport. On short screens
the passage scrolls within its stage while the dock stays fixed; the picture
keeps a useful minimum size instead of shrinking its labels to fit.

The four paths are User, Investor / Canton ecosystem, Architecture, and
Implementation. The investor path remains the next content/design discussion;
this change does not certify completion of all four paths.

Build with the existing Python environment:

```sh
.artifacts/book-tools/bin/python book/architecture/render.py
```

The carousel uses the existing product scene bundle at
`product/scene/target/scala-3.3.6/harmonia-scene-fastopt/main.js`, together with the
same shared scene and reader styles. No new Scala compilation is needed when that
bundle has already been built. Product code has no dependency on this reader.

## Browser review

Use an existing Chrome debugging endpoint and the source server:

```sh
node book/architecture/check.mjs \
  http://127.0.0.1:61322 \
  http://127.0.0.1:56202/book/architecture/
```

The check opens and closes its own temporary tab. It covers all three slides at
desktop, phone and short-phone sizes; shared carousel selection; arrow keys and
swipes; history and reload; invalid addresses; endpoint behavior; retained DOM
nodes; image loading; fixed stage and controls; and print/no-JavaScript reading.
On the short viewport, it also checks that the entire figure and final explanation
can be brought into view while the dock stays fixed.

Verified 2026-09-14: desktop and 390×844 phone layouts require no scrolling. Stage
and control positions did not change between slides. All three passages printed
to a three-page PDF. The images and visible layouts were inspected in screenshots;
no browser errors were reported. Evidence, including the PDF, is under
`.artifacts/architecture-carousel-review/`. These are presentation checks, not a
fresh Canton execution test.

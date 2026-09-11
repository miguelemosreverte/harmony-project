# Match the approved reference

The user has selected `design/0.2/infographic/concept.png` as the visual specification,
including its desktop and mobile compositions. Visual similarity is now an acceptance
condition, alongside the working behavior. The preceding UI is superseded.

- [x] Use the reference's actual bank, house, and character illustrations as CSS sprites.
  Keep the original pixels unchanged; all labels, documents, connectors, navigation,
  panels, and controls remain responsive HTML/CSS/SVG.
- [x] Match the compact Harmonia / appearance / Evidence header. Move reading routes,
  background, scenarios, and technical evidence into an accessible secondary drawer.
- [x] Desktop: two application panels, the illustrated horizontal handoff, one caption,
  and four numbered, named steps. Mobile: the reference's vertical sequence and a
  compact caption/navigation card. Native browser chrome is outside the app.
- [x] Preserve all recorded actions and URL selection. Four purchase beats summarize
  the business journey; the evidence drawer still exposes every observation.
- [x] Use the same shell and scene in the live financing view. Keep session authority,
  real commands, pending/error states, and the other workspace tasks available.
- [x] Compare screenshots at the reference content sizes, inspect intermediate widths,
  check overlays and keyboard use, and verify the existing live server connection.

The original generated image and prompt remain the provenance source. No new concept
will replace the approved reference. The screenshot is not used as a page background;
only its individual illustrations are reused. Text, state, layout, and interaction stay native.

## Implementation and evidence

The shared `product/scene` package owns the illustrations, native document cards,
connectors, and responsive geometry. `surface.css` owns the compact header and
accessible drawer chrome. The book owns its four purchase beats, reading routes,
and recorded evidence; the live workspace owns authenticated commands and participant
state. No ledger expectations or original source documents changed.

The live server now serves the shared illustration assets and surface stylesheet.
It also avoids sending a second error response when a browser cancels an image after
headers have already been written. Only one disposable Canton environment is run at a time.

| Verification | Result |
| --- | --- |
| Scene and live Scala.js compilation | Passed |
| Service tests, including static-file confinement | 4 passed |
| Source coverage and recording integrity | 19 passed; 736/736 units, 8,110/8,110 words |
| [Book browser checks](reference-browser.json) | 109 passed; no browser errors |
| [Final visual and responsive checks](reference-visual.json) | 43 passed; no browser errors |
| [Live server checks](reference-live.json) | 23 passed; bank approval and buyer continuation committed |
| [Fresh sandbox checks](reference-startup.json) | 10 passed; approval untouched, assets and exported book served |

[Compare the original and browser captures](../../design/0.2/infographic/reference-review/compare.html).
The desktop capture is 1280 × 1024 with an 1118 × 868 application frame. The mobile
reference content is checked at 304 × 816, with additional phone, tablet, and desktop
widths checked between 304 and 1280 CSS pixels. The device frame and operating-system
chrome belong to the reference presentation, not the web application.

Visual review found and corrected mobile portrait clipping, a connector crossing the
bank badge, narrow custody receipt overlap, crowded tablet step labels, and dark-theme
illustration edges. Evidence opens through a native modal dialog, supports Escape and
focus return, and is restored by URL. Four purchase beats retain every detailed action
in the evidence selector, including rejected attempts. File opening without a server,
printing, and readable content without JavaScript remain covered.

The comparison is the visual acceptance artifact. It does not assert a zero-pixel-difference
score: browser-rendered text, SVG marks, and CSS shadows replace their raster counterparts
in the generated image. The original illustration image is byte-for-byte unchanged.

To repeat the browser checks, open a local preview in the owned Chromium browser, then
run the three scripts in `design/0.2/checks/`: `infographic.mjs`, `reference.mjs`, and
`live.mjs`. Each takes its debugging endpoint first; `live.mjs` also takes the private
sessions file from a fresh local sandbox. Reports contain no session capabilities.

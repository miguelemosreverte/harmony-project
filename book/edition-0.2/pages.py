"""The book's information hierarchy and static, printable page structure."""
from html import escape
import json
from pathlib import Path
from urllib.parse import urlencode

from render import markdown, html_fragment, diagram

CHAPTERS = {
    "01-product": "What Harmonia provides", "02-roles-and-trust": "Who can act, and why",
    "03-financing-to-offer": "Alice buys a home", "04-four-party-transfer": "A transfer across four parties",
    "05-bring-an-application": "Connect your application", "06-compose-a-workflow": "Do the next task",
    "07-evidence-and-boundaries": "What the evidence proves", "08-release-and-adoption": "Readiness and adoption",
    "09-proposal-context": "Proposal context", "10-context-and-references": "Context and references"
}
JOURNEYS = {
    "reviewer": {"label":"Review the diagrams", "description":"Trace manifest, contracts, service, and evidence; challenge the boundaries.", "steps":["reviewer","code","author"]},
    "explorer": {"label": "Understand the product", "description": "See what it provides, then follow a concrete example.", "steps": ["01-product", "03-financing-to-offer", "04-four-party-transfer"]},
    "author": {"label": "Review the original proposal", "description": "Find your requirements, their explanation, and the supporting evidence.", "steps": ["author", "reviewer", "coverage"]},
    "developer": {"label": "Explore the implementation", "description": "Understand the boundary, connect an application, and inspect a real result.", "steps": ["code", "reviewer", "03-financing-to-offer"]},
    "investor": {"label": "Assess the opportunity", "description": "Understand the user benefit, see a working example, and assess readiness.", "steps": ["workflows", "reviewer", "08-release-and-adoption"]},
    "operator": {"label": "Use a workflow", "description": "Understand your role, try a task, and see the next handoff.", "steps": ["06-compose-a-workflow", "sandbox", "03-financing-to-offer"]}
}


def json_script(value):
    return json.dumps(value, ensure_ascii=False).replace("<", "\\u003c").replace("&", "\\u0026")


def controls():
    return '''<div class="reference-tools"><button id="appearance-toggle" class="reference-appearance" aria-label="Appearance" aria-expanded="false" aria-controls="appearance-panel"><svg viewBox="0 0 32 32" aria-hidden="true"><circle cx="16" cy="16" r="6.5"/><path d="M16 1v5m0 20v5M1 16h5m20 0h5M5.4 5.4l3.5 3.5m14.2 14.2 3.5 3.5M5.4 26.6l3.5-3.5M23.1 8.9l3.5-3.5"/></svg></button><button id="open-evidence" class="reference-evidence" aria-haspopup="dialog">Evidence</button></div><aside id="appearance-panel" class="appearance-panel" hidden aria-label="Appearance"><fieldset><legend>Color</legend><button data-theme="light">☀ Light</button><button data-theme="dark">☾ Dark</button><button data-theme="paper">Paper</button></fieldset><fieldset><legend>Text size</legend><button data-text="compact">Compact</button><button data-text="standard">Standard</button><button data-text="large">Large</button></fieldset><button id="print-page" class="text-button">Print / save PDF</button><button id="close-appearance" class="text-button">Done</button></aside>'''


def shell(title, body, slug="", prefix="", kind="chapter", stories=None):
    section = "Workspace" if kind in {"workspace", "builder"} else "The book"
    config = {"slug": slug, "kind": kind, "base": prefix or "./", "journeys": JOURNEYS, "chapters": CHAPTERS, "stories": list(stories or {})}
    scripts = '<script src="application.js" defer></script>' if kind in {"workspace", "builder"} else ""
    if kind in {"workspace", "builder"}:
        body = '<p class="prototype-notice">Historical design simulation. <a href="sandbox.html">Open the live sandbox →</a></p>' + body
    links = ''.join(f'<a href="{prefix}chapters/{key}.html" data-chapter="{key}">{label}</a>' for key, label in CHAPTERS.items())
    rail = f'''<aside class="rail"><a class="brand" href="{prefix}book-overview.html">Harmonia<span>THE FIELD GUIDE</span></a><button id="menu-toggle" class="mobile-menu" aria-expanded="false" aria-controls="route-navigation">Reading path</button><nav id="route-navigation" aria-label="Reading path"><p class="nav-label" id="route-label">Your reading path</p><div id="route-links"></div><a class="change-path" href="{prefix}book-overview.html">Change reading path</a><details id="all-chapters" data-disclosure><summary>All chapters</summary>{links}<a href="{prefix}code.html">Source browser</a><a href="{prefix}reviewer.html">Diagram review</a><a href="{prefix}author.html">Originals beside implementation</a><a href="{prefix}workflows.html">Workflow presentations</a><a href="{prefix}coverage.html">Original document coverage</a></details></nav></aside>'''
    return f'''<!doctype html>
<html lang="en" data-theme="light" data-text="standard"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>{escape(title)} · Harmonia</title><link rel="icon" type="image/svg+xml" href="{prefix}../../product/web/site/favicon.svg"><link rel="stylesheet" href="{prefix}style.css"><link rel="stylesheet" href="{prefix}reference.css"><link rel="stylesheet" href="{prefix}reader/reader.css"><link rel="stylesheet" href="{prefix}../../product/scene/site/scene.css"><link rel="stylesheet" href="{prefix}../../product/scene/site/surface.css"><script id="view-config" type="application/json">{json_script(config)}</script><script src="{prefix}recordings.js" defer></script><script src="{prefix}run-recordings.js" defer></script><script src="{prefix}context.js" defer></script><script src="{prefix}../../product/scene/target/scala-3.3.6/harmonia-scene-fastopt/main.js" defer></script><script src="{prefix}reader/catalog.js" defer></script><script src="{prefix}state.js" defer></script><script src="{prefix}navigation.js" defer></script><script src="{prefix}book.js" defer></script><script src="{prefix}reader/common.js" defer></script><script src="{prefix}reader/code.js" defer></script><script src="{prefix}reader/reviewer.js" defer></script><script src="{prefix}reader/author.js" defer></script><script src="{prefix}reader/presentation.js" defer></script>{scripts}</head>
<body class="{kind}{" story-layout" if stories else ""}"><a class="skip-link" href="#main">Skip to content</a><div class="reference-shell"><header class="reference-header"><a class="reference-brand" href="{prefix}book-overview.html">Harmonia</a>{controls()}</header><noscript><p class="noscript">The chapters and original documents remain readable without JavaScript. Interactive scenes, source browsing, and companion panes require it.</p></noscript>{body}<dialog id="reader-drawer" class="reader-drawer"><div class="drawer-heading"><h2>Evidence &amp; reading</h2><button id="close-evidence" aria-label="Close evidence">×</button></div><div class="drawer-sharing"><button id="share-view">Share this view</button><div id="share-status" role="status" class="share-status"></div><label id="share-fallback" class="share-fallback" hidden>Copy this URL<input id="share-url" readonly></label></div><div id="drawer-body"></div>{rail}</dialog></div></body></html>\n'''


def welcome():
    entrances = [
        ('workflows.html','Follow a workflow','See the handoff, the Daml mechanism, and what is live or recorded.','For users and technical investors'),
        ('code.html','Explore the codebase','Open real source files, read their annotations, and follow a vertical slice.','For developers'),
        ('reviewer.html','Review the diagrams','Trace manifests, contracts, service, and evidence. Challenge the boundaries.','For technical reviewers'),
        ('author.html','Read beside the implementation','Keep your original document on the left and its evidence on the right.','For the original author')]
    choices=''.join(f'<a class="workflow-story" href="{url}"><span class="story-symbol">{i+1}</span><span class="evidence-kind">{who}</span><h2>{title}</h2><p>{detail}</p><span class="story-open">Start here →</span></a>' for i,(url,title,detail,who) in enumerate(entrances))
    body=f'''<main id="main" class="reader-content"><div class="reader-intro"><p class="eyebrow">Independent applications. Shared progress.</p><h1>Make the handoff clear.</h1><p>Harmonia coordinates Daml applications while each application keeps its authority. What would you like to understand?</p></div><div class="workflow-stories">{choices}</div><p class="reader-footnote"><a href="chapters/01-product.html">Read the book from chapter one →</a> · <a href="sandbox.html">Open the live sandbox →</a> · <a href="coverage.html">Check quotation coverage →</a></p></main>'''
    return shell('Start here',body,slug='welcome',kind='welcome')


def passage(p, corpus):
    _, text, units = corpus[p.source]
    excerpt = '\n'.join(text.splitlines()[p.start-1:p.end])
    rich = markdown(excerpt, source=True) if p.source == "proposal" else html_fragment(excerpt)
    if p.source == "architecture" and p.start <= 88 <= p.end:
        rich += diagram("component-map", "Original component map: off-ledger and on-ledger")
    if p.source == "architecture" and p.start <= 318 <= p.end:
        rich += diagram("contract-model", "Original illustrative contract model")
    if p.source == "proposal" and "```plantuml" in excerpt:
        rich = '<p class="note">The original contains a diagram program. Its supplied architecture illustration is available in <a href="../sources/architecture.html">the original diagram view</a>.</p>' + rich
    exact = '\n'.join(f'<span class="source-unit" data-unit="{u.id}">{escape(u.text)}</span>' for u in units if p.start <= u.line <= p.end)
    key = f"{p.source}-L{p.start}"
    return f'''<details class="source-section" id="{key}" data-disclosure><summary>{escape(p.title)}<small>{p.source.title()} · lines {p.start}–{p.end}</small></summary><div class="rich-source">{rich}</div><p class="citation"><a href="../sources/{p.source}.html?view=source#L{p.start}">Inspect original location ↗</a></p><details class="exact-source" id="exact-{key}" data-disclosure><summary>Exact source text for quotation audit</summary><pre>{exact}</pre></details></details>'''


def story_panel(stories):
    scenarios = ''.join(f'<option value="{key}">{"Financing approved" if key == "purchase-approved" else "Financing refused" if key == "purchase-rejected" else "Transfer completes" if key == "transfer-approved" else "Final transfer rolls back"}</option>' for key in stories)
    first = next(iter(stories.values()))
    static = ""
    for key, story in stories.items():
        rows = ''.join(f'<tr><td>{i}</td><td>{escape(u["actor"])}</td><td>{escape(u["action"])}</td><td>{escape(u["outcome_label"])}</td><td>{escape(u["observed_state"])}</td></tr>' for i, u in enumerate(story["presentation"]["units"],1))
        static += f'<div class="static-steps" data-recording="{key}"><h3>Recorded actions: {escape(story["title"])}</h3><table><thead><tr><th>Step</th><th>Actor</th><th>Action</th><th>Outcome</th><th>Observed state</th></tr></thead><tbody>{rows}</tbody></table></div>'
    return f'''<section class="mode-panel infographic-panel" data-mode="try" id="chapter-try"><div class="scene-toolbar"><span class="recording-label">Recorded ledger story</span><label class="scenario-choice">Outcome<select id="story-select">{scenarios}</select></label></div><div id="story-carousel" class="story-carousel" role="region" aria-roledescription="carousel" aria-label="Story scenes" tabindex="0"><div id="story-scene" aria-live="polite" aria-atomic="true"></div><nav class="carousel-controls" aria-label="Scene navigation"><button id="previous-step" aria-label="Previous scene">‹</button><ol id="scene-dots" aria-label="Scenes"></ol><span id="recorded-step"></span><button id="next-step" class="primary" aria-label="Next scene">›</button><button id="reset-story" class="text-button" hidden>Replay ↻</button></nav></div><p id="story-complete" class="completion-note" hidden></p><a class="live-sandbox-link button primary" href="../sandbox.html">Try a live financing handoff →</a><details id="step-inspector" class="inspector" data-disclosure><summary>Evidence &amp; other attempts</summary><div class="inspector-controls"><label>Every recorded action<select id="evidence-action"></select></label><label>Follow a person<select id="story-actor"><option value="all">Everyone</option></select></label></div><p id="recorded-action"></p><p id="scene-detail"></p><p><span id="recorded-outcome" class="badge"></span></p><div id="recorded-state" class="observed-summary"></div><div class="inspector-tabs" role="tablist" aria-label="Recorded evidence"><button data-tab="input" role="tab" aria-controls="inspector-panel">Input</button><button data-tab="expected" role="tab" aria-controls="inspector-panel">Expected</button><button data-tab="observed" role="tab" aria-controls="inspector-panel">Observed</button><button data-tab="provenance" role="tab" aria-controls="inspector-panel">Provenance</button></div><div id="inspector-panel" role="tabpanel" tabindex="0"><p id="inspector-caption"></p><pre id="recorded-json"></pre></div></details>{static}</section>'''



def chapter(slug, text, passages, corpus, stories):
    title = CHAPTERS[slug]
    modes = [("read", "Background" if stories else "Read")]
    if stories:
        modes.insert(0, ("try", "Follow the story"))
    modes += [("sources", "Original sources"), ("evidence", "Evidence & limits")]
    tabs = ''.join(f'<button data-view="{key}" aria-controls="chapter-{key}" aria-current="{'page' if key == 'read' else 'false'}">{label}</button>' for key,label in modes)
    authored = '\n'.join(text.splitlines()[1:])
    sources = ''.join(passage(p, corpus) for p in passages if p.chapter == slug)
    cta = '<button class="primary" data-view="try" data-interactive>Try this example →</button>' if stories else '<a class="button primary route-next" href="03-financing-to-offer.html">Continue to Alice’s story →</a>'
    evidence = '<p>The carousel replays recorded ledger observations. Input, expected and observed data are attached to every action; provenance identifies the run. Playback submits no commands. Open the live sandbox to make a new financing handoff.</p>' if stories else '<p>This chapter explains the product and its boundaries. Source quotations establish what was proposed; they do not establish completed implementation or adoption.</p>'
    body = f'''<main id="main" class="content chapter-content"><p class="eyebrow">Chapter {slug[:2]} · <span id="reader-context">Your reading path</span></p><h1>{title}</h1><nav class="mode-tabs" aria-label="Chapter view" data-interactive>{tabs}</nav><article class="mode-panel prose" data-mode="read" id="chapter-read"><div data-chapter-diagram="{slug}" class="chapter-diagram"></div>{markdown(authored)}<div class="chapter-action">{cta}</div></article>{story_panel(stories) if stories else ''}<section class="mode-panel" data-mode="sources" id="chapter-sources"><h2>The original words, in context.</h2><p>Read the source as a document. Exact original text is available inside each passage for auditing.</p>{sources}</section><section class="mode-panel prose" data-mode="evidence" id="chapter-evidence"><h2>What supports this chapter?</h2>{evidence}<p><a href="../coverage.html">Open the coverage and product evidence map →</a></p><p><a href="../../../book/{'05-financing-and-offer.md' if slug.startswith('03') else '06-atomic-transfer.md' if slug.startswith('04') else 'extension-guide.md'}">Read the existing implementation guide</a></p><details id="run-locally" data-disclosure><summary>Run the real ledger example locally</summary><p>The recorded sandbox is available immediately. A new run needs the pinned local Daml/Canton tools and one owned ledger environment.</p><pre>scripts/harmonia check examples/stories/{next(iter(stories)) if stories else 'purchase-approved'}</pre><p><a href="../../../book/setup.md">Open the runtime setup guide</a></p></details></section><footer class="chapter-footer"><a href="../book-overview.html">Change reading path</a><a class="route-next" href="04-four-party-transfer.html">Next: a transfer across four parties →</a></footer></main>'''
    return shell(title, body, slug, "../", stories=stories)


def source_page(name, text, pin):
    readable = markdown(text, source=True) if name == "proposal" else html_fragment(text) + diagram("component-map", "Original component map") + diagram("contract-model", "Original contract model")
    lines = '\n'.join(f'<span class="source-line" id="L{n}"><a href="#L{n}">{n}</a>{escape(line)}</span>' for n,line in enumerate(text.splitlines(),1))
    body = f'''<main id="main" class="content"><p class="eyebrow">Original document</p><h1>{name.title()}</h1><p><a href="../coverage.html">← Back to document coverage</a></p><nav class="mode-tabs" data-interactive><button data-view="read">Read the document</button><button data-view="source">Exact source</button></nav><article class="mode-panel rich-source" data-mode="read">{readable}</article><section class="mode-panel" data-mode="source"><p class="note">SHA-256: {pin['sha256']}</p><pre class="source-view">{lines}</pre></section><p><a download href="../../../{pin['path']}">Download the intact original file</a></p></main>'''
    return shell(f"Original {name}",body,slug=name,prefix="../",kind="source")


def coverage(report, claims):
    rows = ''.join(f'<tr><td><a href="sources/{d["id"]}.html">{d["id"].title()}</a></td><td>{d["quoted_units"]} / {d["units"]}</td><td>{d["quoted_words"]:,} / {d["words"]:,}</td></tr>' for d in report['documents'])
    destinations = ''.join(f'<a class="destination" href="chapters/{c["id"]}.html?view=sources"><span>{c["id"][:2]} · {CHAPTERS[c["id"]]}</span><small>{c["quoted_units"]} original text units →</small></a>' for c in report['chapters'])
    claim_table = ''.join('<tr>'+''.join(f'<td>{escape(cell)}</td>' for cell in row)+'</tr>' for row in claims)
    body = f'''<main id="main" class="content"><p class="eyebrow">For the proposal's reviewer</p><h1>Find your requirement.</h1><p class="lead">Every original passage has a chapter destination. Choose a topic to read the source beside its explanation.</p><div class="coverage-summary"><strong>100% of the defined text is quoted.</strong><p>{sum(d["units"] for d in report["documents"]):,} source units · {sum(d["words"] for d in report["documents"]):,} source words · two original documents.<br>Quotation inclusion is measured separately from working product capability.</p></div><div class="destinations">{destinations}</div><details id="coverage-method" data-disclosure><summary>How quotation coverage is counted</summary><table><thead><tr><th>Original</th><th>Quoted units</th><th>Quoted words</th></tr></thead><tbody>{rows}</tbody></table><p>All nonblank Markdown lines and visible HTML body text nodes are included once. HTML markup, CSS, scripts, and comments are excluded. Missing original diagram attachments and linked external works remain outside the corpus.</p><p><a href="coverage.json">Inspect the report</a> · <a href="../../book/edition-0.2/coverage-map.md">Read the source map</a></p></details><details id="product-evidence" data-disclosure><summary>Which product claims still need evidence?</summary><p>These are fourth-draft baseline assessments. Quoting a requirement does not complete it.</p><div class="table-scroll"><table><thead><tr><th>ID</th><th>Claim</th><th>Source</th><th>Baseline</th><th>Next evidence</th></tr></thead><tbody>{claim_table}</tbody></table></div></details><footer class="chapter-footer"><a href="book-overview.html">Change reading path</a><a class="route-next" href="chapters/07-evidence-and-boundaries.html">Next: what the evidence proves →</a></footer></main>'''
    return shell("Document coverage",body,slug="coverage",kind="coverage")


def review():
    shots = [("purchase", "The approved desktop composition", "chapters/03-financing-to-offer.html?step=2"),
             ("mobile", "The approved mobile composition", "chapters/03-financing-to-offer.html?step=2"),
             ("transfer-dark", "A final settlement rolls back", "chapters/04-four-party-transfer.html?story=transfer-final-leg-rejected&step=6&theme=dark"),
             ("live-bank", "The bank's real financing task", "sandbox.html"),
             ("live-buyer-mobile", "The buyer's real next step", "sandbox.html"),
             ("live-complete", "Shared completion on the ledger", "sandbox.html"),
             ("source", "Original words in context", "chapters/01-product.html?view=sources&open=proposal-L160")]
    gallery = ''.join(f'<figure><a data-exact-view href="{url}">{label} →</a><a href="infographic/reference-review/{name}.png"><img src="infographic/reference-review/{name}.png" alt="Actual browser capture: {label}" loading="lazy"></a></figure>' for name,label,url in shots)
    body = f'''<main id="main" class="content"><p class="eyebrow">Infographic and live integration</p><h1>Follow the handoff.</h1><p class="lead">One scene, one caption, one next step. The same HTML scene renderer powers recorded stories and the live financing workspace.</p><p><a href="chapters/03-financing-to-offer.html">Follow Alice’s story →</a> · <a href="sandbox.html">Make a live handoff →</a></p><p><a href="infographic/reference-review/compare.html">Reference beside implementation</a> · <a href="../../docs/0.2/REFERENCE-FIDELITY.md">Visual contract and verification</a> · <a href="infographic/reference-review/transfer.pdf">Print example</a></p><div class="review-gallery">{gallery}</div><details id="visual-reference" data-disclosure><summary>Generated visual reference and design history</summary><p>This is the approved visual specification. The browser captures above show the responsive implementation with the same illustration pixels.</p><a href="infographic/concept.png"><img src="infographic/concept.png" alt="Generated desktop and mobile infographic reference" loading="lazy"></a><p><a href="infographic/prompt.md">Exact image-generation prompt</a> · <a href="../../docs/0.2/verification-2.md">Previous UX pass</a></p></details></main>'''
    return shell("Follow the handoff",body,slug="review",kind="review")


def sandbox():
    return shell("Try a live handoff", '''<main id="main" class="content"><p class="eyebrow">Live sandbox</p><h1 id="sandbox-title">Make the handoff yourself.</h1><p class="lead" id="sandbox-lead">First, the bank approves a private financing case. Then the buyer uses that approval to continue.</p><div id="live-entry"><p>Start the local sandbox, then open a participant from the private launcher.</p><pre>scripts/start-sandbox</pre><p>The launcher opens the bank, buyer, and observer in separate tabs. Each tab keeps its own session.</p><a href="../../book/setup.md">Runtime setup →</a></div><p class="note">The live API supports this financing handoff, workflow composition, and application packages. The four-person property offer and custody transfer are recorded examples.</p><a href="chapters/03-financing-to-offer.html">Back to Alice’s story →</a></main>''', slug="sandbox", kind="sandbox")

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
    "explorer": {"label": "Understand the product", "description": "See what it provides, then follow a concrete example.", "steps": ["01-product", "03-financing-to-offer", "04-four-party-transfer"]},
    "author": {"label": "Review the original proposal", "description": "Find your requirements, their explanation, and the supporting evidence.", "steps": ["coverage", "07-evidence-and-boundaries", "08-release-and-adoption"]},
    "developer": {"label": "Explore the implementation", "description": "Understand the boundary, connect an application, and inspect a real result.", "steps": ["01-product", "05-bring-an-application", "03-financing-to-offer", "07-evidence-and-boundaries"]},
    "investor": {"label": "Assess the opportunity", "description": "Understand the user benefit, see a working example, and assess readiness.", "steps": ["01-product", "03-financing-to-offer", "08-release-and-adoption"]},
    "operator": {"label": "Use a workflow", "description": "Understand your role, try a task, and see the next handoff.", "steps": ["06-compose-a-workflow", "application", "03-financing-to-offer"]}
}


def json_script(value):
    return json.dumps(value, ensure_ascii=False).replace("<", "\\u003c").replace("&", "\\u0026")


def controls():
    return '''<div class="page-tools"><button id="appearance-toggle" aria-expanded="false" aria-controls="appearance-panel">◐ <span>Appearance</span></button><button id="share-view">Share this view</button></div>
<aside id="appearance-panel" class="appearance-panel" hidden aria-label="Appearance"><fieldset><legend>Color</legend><button data-theme="light">☀ Light</button><button data-theme="dark">☾ Dark</button><button data-theme="paper">Paper</button></fieldset><fieldset><legend>Text size</legend><button data-text="compact">Compact</button><button data-text="standard">Standard</button><button data-text="large">Large</button></fieldset><button id="print-page" class="text-button">Print / save PDF</button><button id="close-appearance" class="text-button">Done</button></aside><div id="share-status" role="status" class="share-status"></div><label id="share-fallback" class="share-fallback" hidden>Copy this URL<input id="share-url" readonly></label>'''


def shell(title, body, slug="", prefix="", kind="chapter", stories=None):
    section = "Workspace" if kind in {"workspace", "builder"} else "The book"
    config = {"slug": slug, "kind": kind, "base": prefix or "./", "journeys": JOURNEYS, "chapters": CHAPTERS, "stories": stories or {}}
    scripts = '<script src="application.js" defer></script>' if kind in {"workspace", "builder"} else ""
    links = ''.join(f'<a href="{prefix}chapters/{key}.html" data-chapter="{key}">{label}</a>' for key, label in CHAPTERS.items())
    rail = f'''<aside class="rail"><a class="brand" href="{prefix}book-overview.html">Harmonia<span>THE FIELD GUIDE</span></a><button id="menu-toggle" class="mobile-menu" aria-expanded="false" aria-controls="route-navigation">Reading path</button><nav id="route-navigation" aria-label="Reading path"><p class="nav-label" id="route-label">Your reading path</p><div id="route-links"></div><a class="change-path" href="{prefix}book-overview.html">Change reading path</a><details id="all-chapters" data-disclosure><summary>All chapters</summary>{links}<a href="{prefix}coverage.html">Original document coverage</a></details></nav></aside>'''
    return f'''<!doctype html>
<html lang="en" data-theme="light" data-text="standard"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>{escape(title)} · Harmonia</title><link rel="stylesheet" href="{prefix}style.css"><script id="view-config" type="application/json">{json_script(config)}</script><script src="{prefix}state.js" defer></script><script src="{prefix}navigation.js" defer></script><script src="{prefix}book.js" defer></script>{scripts}</head>
<body class="{kind}"><a class="skip-link" href="#main">Skip to content</a>{rail}<header class="topbar"><span class="breadcrumb">{section} / {escape(title)}</span>{controls()}</header><noscript><p class="noscript">You can read this edition without JavaScript. Interactive exploration requires JavaScript; the recorded actions and source text remain available below.</p></noscript>{body}</body></html>\n'''


def welcome():
    choices = ''.join(f'<label class="audience-choice"><input type="radio" name="audience" value="{key}" {"checked" if key == "explorer" else ""}><span><strong>{value["label"]}</strong><small>{value["description"]}</small></span></label>' for key, value in JOURNEYS.items())
    return shell("Start here", f'''<main id="main" class="content welcome-content"><p class="eyebrow">Independent applications. Shared progress.</p><h1>Shared workflows.<br>Clear responsibilities.</h1><p class="lead">Harmonia coordinates work across independent applications. Each application keeps its authority and private information.</p><section class="welcome-choice"><h2>What brings you here?</h2><p>Choose a starting point. You can change it at any time.</p><fieldset class="audience-options"><legend class="visually-hidden">Your reason for reading</legend>{choices}</fieldset><a id="start-route" class="button primary" href="chapters/01-product.html?audience=explorer">Start with the product →</a><div class="route-preview"><span class="eyebrow">Your reading path</span><ol id="welcome-route"><li>Understand the product</li><li>Follow Alice's purchase</li><li>Explore a transfer</li></ol></div></section></main>''', slug="welcome", kind="welcome")


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
    return f'''<section class="mode-panel" data-mode="try" id="chapter-try"><div class="section-intro"><h2>Try the story.</h2><p class="note">Preserved ledger recording · no new transactions. Choose Next to follow the first action.</p></div><div class="sandbox-selectors" data-interactive><label>Scenario<select id="story-select">{scenarios}</select></label></div><div class="playback-controls" data-interactive><button id="previous-step">← Previous</button><button id="next-step" class="primary">Next action →</button><button id="reset-story" class="text-button">Start again</button></div><div class="sandbox-stage" aria-live="polite"><div class="step-heading"><span id="recorded-step" class="eyebrow">Before the first action</span><span class="badge" id="recorded-outcome">Recorded setup</span></div><h3 id="recorded-action">The applications are ready.</h3><p id="recorded-explanation">Choose Next to inspect the first attempted action and its observed outcome.</p><div id="recorded-state" class="observed-summary"></div></div><p id="story-complete" class="completion-note" hidden></p><details id="step-inspector" class="inspector" data-disclosure><summary>Inspect the input and the evidence</summary><label>Highlight a person<select id="story-actor"><option value="all">Everyone</option></select></label><p class="note">This is the evaluator’s complete synthetic recording. Highlighting a person is a reading aid, not a change of ledger permissions.</p><div class="inspector-tabs" role="tablist" aria-label="Recorded evidence"><button data-tab="input" role="tab" aria-controls="inspector-panel">Input</button><button data-tab="expected" role="tab" aria-controls="inspector-panel">Expected</button><button data-tab="observed" role="tab" aria-controls="inspector-panel">Observed</button><button data-tab="provenance" role="tab" aria-controls="inspector-panel">Provenance</button></div><div id="inspector-panel" role="tabpanel" tabindex="0"><div id="inspector-caption"></div><pre id="recorded-json"></pre></div></details>{static}<button class="text-button" data-view="read" data-interactive>← Back to the story</button></section>'''


def chapter(slug, text, passages, corpus, stories):
    title = CHAPTERS[slug]
    modes = [("read", "Read the story" if stories else "Read")]
    if stories:
        modes.append(("try", "Try it"))
    modes += [("sources", "Original sources"), ("evidence", "Evidence & limits")]
    tabs = ''.join(f'<button data-view="{key}" aria-controls="chapter-{key}" aria-current="{'page' if key == 'read' else 'false'}">{label}</button>' for key,label in modes)
    authored = '\n'.join(text.splitlines()[1:])
    sources = ''.join(passage(p, corpus) for p in passages if p.chapter == slug)
    cta = '<button class="primary" data-view="try" data-interactive>Try this example →</button>' if stories else '<a class="button primary route-next" href="03-financing-to-offer.html">Continue to Alice’s story →</a>'
    evidence = '<p>The sandbox uses preserved fourth-draft ledger recordings. Inputs and independently committed expectations still match their recorded SHA-256 fingerprints. No fresh 0.2 ledger run is claimed.</p>' if stories else '<p>This chapter explains the product and its boundaries. Source quotations establish what was proposed; they do not establish completed implementation or adoption.</p>'
    body = f'''<main id="main" class="content chapter-content"><p class="eyebrow">Chapter {slug[:2]} · <span id="reader-context">Your reading path</span></p><h1>{title}</h1><nav class="mode-tabs" aria-label="Chapter view" data-interactive>{tabs}</nav><article class="mode-panel prose" data-mode="read" id="chapter-read">{markdown(authored)}<div class="chapter-action">{cta}</div></article>{story_panel(stories) if stories else ''}<section class="mode-panel" data-mode="sources" id="chapter-sources"><h2>The original words, in context.</h2><p>Read the source as a document. Exact original text is available inside each passage for auditing.</p>{sources}</section><section class="mode-panel prose" data-mode="evidence" id="chapter-evidence"><h2>What supports this chapter?</h2>{evidence}<p><a href="../coverage.html">Open the coverage and product evidence map →</a></p><p><a href="../../../book/{'05-financing-and-offer.md' if slug.startswith('03') else '06-atomic-transfer.md' if slug.startswith('04') else 'extension-guide.md'}">Read the existing implementation guide</a></p><details id="run-locally" data-disclosure><summary>Run the real ledger example locally</summary><p>The recorded sandbox is available immediately. A new run needs the pinned local Daml/Canton tools and one owned ledger environment.</p><pre>scripts/harmonia check examples/stories/{next(iter(stories)) if stories else 'purchase-approved'}</pre><p><a href="../../../book/setup.md">Open the runtime setup guide</a></p></details></section><footer class="chapter-footer"><a href="../book-overview.html">Change reading path</a><a class="route-next" href="04-four-party-transfer.html">Next: a transfer across four parties →</a></footer></main>'''
    return shell(title, body, slug, "../", stories=stories)


def source_page(name, text, pin):
    readable = markdown(text, source=True) if name == "proposal" else html_fragment(text) + diagram("component-map", "Original component map") + diagram("contract-model", "Original contract model")
    lines = '\n'.join(f'<span class="source-line" id="L{n}"><a href="#L{n}">{n}</a>{escape(line)}</span>' for n,line in enumerate(text.splitlines(),1))
    body = f'''<main id="main" class="content"><p class="eyebrow">Original document</p><h1>{name.title()}</h1><p><a href="../coverage.html">← Back to document coverage</a></p><nav class="mode-tabs" data-interactive><button data-view="read">Read the document</button><button data-view="source">Exact source</button></nav><article class="mode-panel rich-source" data-mode="read">{readable}</article><section class="mode-panel" data-mode="source"><p class="note">SHA-256: {pin['sha256']}</p><pre class="source-view">{lines}</pre></section><p><a href="../../../{pin['path']}">Open the intact original file ↗</a></p></main>'''
    return shell(f"Original {name}",body,slug=name,prefix="../",kind="source")


def coverage(report, claims):
    rows = ''.join(f'<tr><td><a href="sources/{d["id"]}.html">{d["id"].title()}</a></td><td>{d["quoted_units"]} / {d["units"]}</td><td>{d["quoted_words"]:,} / {d["words"]:,}</td></tr>' for d in report['documents'])
    destinations = ''.join(f'<a class="destination" href="chapters/{c["id"]}.html?view=sources"><span>{c["id"][:2]} · {CHAPTERS[c["id"]]}</span><small>{c["quoted_units"]} original text units →</small></a>' for c in report['chapters'])
    claim_table = ''.join('<tr>'+''.join(f'<td>{escape(cell)}</td>' for cell in row)+'</tr>' for row in claims)
    body = f'''<main id="main" class="content"><p class="eyebrow">For the proposal's reviewer</p><h1>Find your requirement.</h1><p class="lead">Every original passage has a chapter destination. Choose a topic to read the source beside its explanation.</p><div class="coverage-summary"><strong>100% of the defined text is quoted.</strong><p>{sum(d["units"] for d in report["documents"]):,} source units · {sum(d["words"] for d in report["documents"]):,} source words · two original documents.<br>Quotation inclusion is measured separately from working product capability.</p></div><div class="destinations">{destinations}</div><details id="coverage-method" data-disclosure><summary>How quotation coverage is counted</summary><table><thead><tr><th>Original</th><th>Quoted units</th><th>Quoted words</th></tr></thead><tbody>{rows}</tbody></table><p>All nonblank Markdown lines and visible HTML body text nodes are included once. HTML markup, CSS, scripts, and comments are excluded. Missing original diagram attachments and linked external works remain outside the corpus.</p><p><a href="coverage.json">Inspect the report</a> · <a href="../../book/edition-0.2/coverage-map.md">Read the source map</a></p></details><details id="product-evidence" data-disclosure><summary>Which product claims still need evidence?</summary><p>These are fourth-draft baseline assessments. Quoting a requirement does not complete it.</p><div class="table-scroll"><table><thead><tr><th>ID</th><th>Claim</th><th>Source</th><th>Baseline</th><th>Next evidence</th></tr></thead><tbody>{claim_table}</tbody></table></div></details><footer class="chapter-footer"><a href="book-overview.html">Change reading path</a><a class="route-next" href="chapters/07-evidence-and-boundaries.html">Next: what the evidence proves →</a></footer></main>'''
    return shell("Document coverage",body,slug="coverage",kind="coverage")


def review():
    screens = [("book-overview", "Choose a reading path"), ("book-chapter", "Follow a use case"), ("application", "Do the current task"), ("application-builder", "Connect an application"), ("coverage", "Find a requirement")]
    links = ''.join(f'<a class="destination" href="{name}.html">{label} →</a>' for name,label in screens)
    historical = ''.join(f'<figure><figcaption>{label} · first-pass generated concept</figcaption><a href="{name}.png"><img src="{name}.png" alt="First design concept: {label}" loading="lazy"></a></figure>' for name,label in screens)
    shots = [("welcome", "Choose a reading path", "book-overview.html"),
             ("purchase", "Follow a recorded action", "chapters/03-financing-to-offer.html?view=try&step=2"),
             ("source", "Read the original passage", "chapters/01-product.html?audience=author&view=sources&open=proposal-L160"),
             ("workspace", "Act and hand off", "application.html?audience=operator"),
             ("builder", "Review an integration mapping", "application-builder.html?phase=3"),
             ("coverage", "Find a requirement", "coverage.html?audience=author"),
             ("mobile", "Read on a narrow screen", "chapters/03-financing-to-offer.html?view=try&step=2&text=large"),
             ("dark", "Inspect a rolled-back final action", "chapters/04-four-party-transfer.html?view=try&story=transfer-final-leg-rejected&step=6&theme=dark"),
             ("paper", "Read Alice's story in Paper", "chapters/03-financing-to-offer.html?theme=paper")]
    gallery = ''.join(f'<figure><a data-exact-view href="{url}">{title} →</a><a href="review-2/{name}.png"><img src="review-2/{name}.png" alt="Actual browser capture: {escape(title)}"></a></figure>' for name,title,url in shots)
    body = f'''<main id="main" class="content"><p class="eyebrow">Design review · second UX pass</p><h1>A clear path through the product.</h1><p class="lead">The second pass begins with a reader's purpose, one task at a time, and source material that reads like a document.</p><p><a href="../../docs/0.2/UX.md">Read the explicit UX contract</a>. The current HTML is the design reference. The first image concepts are retained as historical artifacts below.</p><h2>Explore the current screens</h2><div class="destinations">{links}</div><h2>Captured browser views</h2><p>Each title opens the captured state. Each image opens its full-size screenshot. <a href="review-2/transfer-rollback.pdf">Open the verified print example (PDF)</a>.</p><div class="review-gallery">{gallery}</div><details id="first-concepts" data-disclosure><summary>First-pass concepts · preserved design history</summary><p>These images belong to v0.2.0-design.1. Their olive palette and layout are superseded by this UX pass. They are not screenshots of the current interface.</p><div class="review-gallery">{historical}</div><p><a href="provenance.json">Original image provenance</a></p></details></main>'''
    return shell("Second UX pass", body, slug="review", kind="review")

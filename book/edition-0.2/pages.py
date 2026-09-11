"""Finite, authored reading routes. Page navigation contains at most two actions."""
from html import escape
import json
from render import markdown, html_fragment, diagram, passive

CHAPTERS = {
    "01-product": "What Harmonia provides", "02-roles-and-trust": "Who can act, and why",
    "03-financing-to-offer": "Alice buys a home", "04-four-party-transfer": "A transfer across four parties",
    "05-bring-an-application": "Connect your application", "06-compose-a-workflow": "Do the next task",
    "07-evidence-and-boundaries": "What the evidence proves", "08-release-and-adoption": "Readiness and adoption",
    "09-proposal-context": "Proposal context", "10-context-and-references": "Context and references"
}
JOURNEYS = {key:{'label':label,'steps':[]} for key,label in [('developer','Explore the code'),('reviewer','Review the diagrams'),('author','Follow the original documents'),('public','Understand the product')]}


def json_script(value):
    return json.dumps(value,ensure_ascii=False).replace('<','\\u003c').replace('&','\\u0026')


def paging(previous, previous_label, following, following_label):
    return f'<nav class="quiet-paging" aria-label="Continue reading"><a id="page-previous" href="{previous}">{previous_label}</a><a id="page-next" href="{following}">{following_label}</a></nav>'


def shell(title, body, slug='', prefix='', kind='chapter', stories=None):
    for mode in ["try","sources","source","evidence"]:
        body=body.replace(f'data-mode="{mode}"',f'data-mode="{mode}" hidden')
    config=dict(slug=slug,kind=kind,base=prefix or './',journeys=JOURNEYS,chapters=CHAPTERS,stories=list(stories or {}))
    scripts=['recordings.js','run-recordings.js','context.js','../../book/browser/target/scala-3.3.6/harmonia-reader-fastopt/main.js','reader/catalog.js','state.js','reader/common.js','navigation.js','book.js','reader/code.js','reader/reviewer.js','reader/author.js','reader/presentation.js']
    includes=''.join(f'<script defer src="{prefix}{name}"></script>' for name in scripts)
    return f'''<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>{escape(title)} · Harmonia</title><link rel="icon" href="{prefix}../../product/web/site/favicon.svg"><link rel="stylesheet" href="{prefix}../../product/scene/site/scene.css"><link rel="stylesheet" href="{prefix}../../product/scene/site/surface.css"><link rel="stylesheet" href="{prefix}quiet.css"><link rel="stylesheet" href="{prefix}../../book/site/print.css"><script id="view-config" type="application/json">{json_script(config)}</script>{includes}</head><body class="{kind}"><div class="reference-shell"><header class="reference-header"><span class="reference-brand">Harmonia</span><span class="quiet-location">The field guide</span></header>{body}<noscript><p class="quiet-note">The written chapters and quotations remain readable. The source tree and recorded scenes need JavaScript.</p></noscript></div></body></html>'''


def welcome():
    return shell('Your reading path','''<main id="main" class="quiet-content"><p class="eyebrow">Independent applications. Shared progress.</p><h1>What brings you here?</h1><p class="lead">Harmonia coordinates work between Daml applications. Each application keeps its own rules and authority.</p><div class="quiet-choices"><a href="understand.html"><h2>I want to understand it.</h2><p>Follow the product, or explore its code.</p><span>Begin →</span></a><a href="verify.html"><h2>I want to verify it.</h2><p>Review the implementation, or trace the original proposal.</p><span>Begin →</span></a></div></main>''',slug='welcome',kind='welcome')


def entrance(verify=False):
    choices= [('reviewer.html','Review the architecture','Follow manifests, contracts and execution. See the evidence and the limits.'),('author.html','Follow my original documents','Read each original passage beside what implements or explains it.')] if verify else [('workflows.html','Show me what the product does','Follow people and applications through an observed handoff.'),('code.html','Show me the implementation','Read actual source, its purpose and the vertical slice it belongs to.')]
    cards=''.join(f'<a href="{url}"><h2>{title}</h2><p>{detail}</p><span>Continue →</span></a>' for url,title,detail in choices)
    return shell('Choose your reading path',f'<main id="main" class="quiet-content"><p class="eyebrow">{"Verify" if verify else "Understand"}</p><h1>{"What are you checking?" if verify else "Where would you like to begin?"}</h1><div class="quiet-choices">{cards}</div></main>',kind='entrance')


def passage(p,corpus):
    _,text,units=corpus[p.source]
    excerpt='\n'.join(text.splitlines()[p.start-1:p.end])
    rich=markdown(excerpt,source=True) if p.source=='proposal' else html_fragment(excerpt)
    if p.source=='architecture' and p.start<=88<=p.end:rich+=diagram('component-map','Original component map')
    if p.source=='architecture' and p.start<=318<=p.end:rich+=diagram('contract-model','Original contract model')
    exact='\n'.join(f'<span class="source-unit" data-unit="{u.id}">{escape(u.text)}</span>' for u in units if p.start<=u.line<=p.end)
    return f'<section class="source-section" id="{p.source}-L{p.start}"><h2>{escape(p.title)}</h2><p class="citation">Original {p.source} · lines {p.start}–{p.end}</p><div class="rich-source">{passive(rich)}</div><pre class="exact-source" hidden>{exact}</pre></section>'


def story_panel(stories):
    first=next(iter(stories.values()))
    return f'''<section id="chapter-try" class="mode-panel" data-mode="try"><p class="eyebrow">Recorded ledger story · <span id="recorded-step">Start</span></p><div id="story-carousel" class="story-carousel"><div id="story-scene" aria-live="polite"></div></div><p id="scene-detail" class="quiet-note"></p><nav class="quiet-paging" aria-label="Story scenes"><button id="previous-step">← Previous</button><button id="next-step">Next →</button></nav><section class="scene-explanation"><h2 id="mechanism-title">What makes this possible?</h2><p id="mechanism-detail">The applications retain their authority. The scene follows observed contracts.</p><p class="citation" id="recording-provenance"></p></section></section>'''


def chapter(slug,text,passages,corpus,stories):
    ids=list(CHAPTERS);index=ids.index(slug)
    previous=f'{ids[index-1]}.html' if index else '../workflows.html'
    following=f'{ids[index+1]}.html' if index+1<len(ids) else '../book-overview.html'
    following={'05-bring-an-application':'../application-builder.html','06-compose-a-workflow':'../application.html','07-evidence-and-boundaries':'../recording-choice.html'}.get(slug,following)
    next_label={'05-bring-an-application':'Try an application integration →','06-compose-a-workflow':'Try the shared workflow →','07-evidence-and-boundaries':'Choose the evidence to follow →'}.get(slug,'Next chapter →' if index+1<len(ids) else 'Return to the reading paths →')
    authored=passive(markdown('\n'.join(text.splitlines()[1:])))
    sources=''.join(passage(p,corpus) for p in passages if p.chapter==slug)
    controls=paging(previous,'← Previous chapter',following,next_label)
    body=f'''<main id="main" class="quiet-content"><div class="chapter-heading"><p class="eyebrow">Chapter {slug[:2]}</p><h1>{CHAPTERS[slug]}</h1></div><article id="chapter-read" class="mode-panel prose" data-mode="read">{controls}{authored}<div data-chapter-diagram="{slug}"></div></article>{story_panel(stories) if stories else ''}<section id="chapter-sources" class="mode-panel prose" data-mode="sources"><h1>The original words</h1>{sources}{controls.replace('id="page-','id="source-page-')}</section><section class="mode-panel prose" data-mode="evidence"><h1>What this chapter establishes</h1><p>Original quotations establish the proposed requirements. Recorded observations establish the behavior of the referenced run. Neither establishes external adoption.</p>{controls.replace('id="page-','id="evidence-page-')}</section></main>'''
    return shell(CHAPTERS[slug],body,slug,'../',stories=stories)


def source_page(name,text,pin):
    rich=markdown(text,source=True) if name=='proposal' else html_fragment(text)+diagram('component-map','Original component map')+diagram('contract-model','Original contract model')
    lines='\n'.join(f'<span class="source-line" id="L{n}"><span>{n}</span>{escape(line)}</span>' for n,line in enumerate(text.splitlines(),1))
    body=f'<main id="main" class="quiet-content prose"><p class="eyebrow">Original document</p><h1>{name.title()}</h1><article class="rich-source mode-panel" data-mode="read">{passive(rich)}</article><section class="mode-panel" data-mode="source"><p class="citation">SHA-256 {pin["sha256"]}</p><pre class="source-view">{lines}</pre></section>{paging("../author.html?source="+name,"Read beside the implementation →","../coverage.html","Quotation coverage →")}</main>'
    return shell('Original '+name,body,slug=name,prefix='../',kind='source')


def coverage(report,claims):
    rows=''.join(f'<tr><td>{d["id"].title()}</td><td>{d["quoted_units"]} / {d["units"]}</td><td>{d["quoted_words"]:,} / {d["words"]:,}</td></tr>' for d in report['documents'])
    claims=''.join(f'<article class="coverage-claim"><h3>{escape(row[0])} · {escape(row[1])}</h3><p><strong>Assessment:</strong> {escape(row[3])}</p><p><strong>Next evidence:</strong> {escape(row[4])}</p><p class="citation">Original {escape(row[2])}</p></article>' for row in claims)
    body=f'''<main id="main" class="quiet-content prose"><p class="eyebrow">The end of the document review</p><h1>736 original units preserved.</h1>{paging('author.html','← Return to the original text','book-overview.html','Finish this reading path →')}<p class="lead">Every defined source unit has a chapter destination. Quotation coverage is 100%; implementation and adoption remain separate questions.</p><table><thead><tr><th>Original</th><th>Quoted units</th><th>Quoted words</th></tr></thead><tbody>{rows}</tbody></table><h2>How this is counted</h2><p>Nonblank Markdown source lines and visible HTML body text nodes are counted once. The source files are pinned by SHA-256. Rendering a second copy does not increase coverage.</p><h2>What the product has demonstrated</h2><div class="coverage-claims">{claims}</div></main>'''
    return shell('Quotation coverage',body,kind='coverage')


def review():
    return shell('The visual standard','''<main id="main" class="quiet-content"><h1>The handoff is the interface.</h1><p class="lead">People, applications, one observed change, and one obvious next step.</p><img class="reference-image" src="infographic/concept.png" alt="Approved desktop and mobile infographic">'''+paging('chapters/03-financing-to-offer.html','Follow Alice →','book-overview.html','Choose a reading path →')+'</main>',kind='review')


def sandbox():
    return shell('Make a live handoff','''<main id="main" class="quiet-content"><p class="eyebrow">A separate, live application</p><h1>Make the handoff yourself.</h1><p class="lead">Open Bank and approve the private financing case. Then open Buyer and use that approval to continue.</p><div id="live-entry"><p>Start the local sandbox and open its private participant launcher.</p><pre>scripts/start-sandbox</pre></div><p class="quiet-note">The product has its own address and participant sessions. A shared URL identifies a task; it does not grant access. Purchase and custody transfer remain recorded examples.</p><nav class="quiet-paging"><a id="sandbox-enter" href="laboratory.html?story=live-handoff">Follow the recorded handoff →</a><a href="chapters/03-financing-to-offer.html">Return to Alice’s story →</a></nav></main>''',kind='sandbox')


def outcome(purchase=True):
    title='What did the bank decide?' if purchase else 'Does the final settlement complete?'
    chapter='03-financing-to-offer' if purchase else '04-four-party-transfer'
    accepted='purchase-approved' if purchase else 'transfer-approved'
    refused='purchase-rejected' if purchase else 'transfer-final-leg-rejected'
    step=2 if purchase else 1
    choices=[(accepted,'Follow the approval.' if purchase else 'Follow successful settlement.','The observed result allows the handoff to continue.'),(refused,'Follow the refusal.' if purchase else 'Follow a refused final leg.','See the refused action and the state that remains.')]
    cards=''.join(f'<a href="chapters/{chapter}.html?story={story}&step={step}"><h2>{label}</h2><p>{detail}</p><span>Follow this recording →</span></a>' for story,label,detail in choices)
    return shell(title,f'<main id="main" class="quiet-content"><p class="eyebrow">Two preserved outcomes</p><h1>{title}</h1><p class="lead">Choose the recorded result you want to understand. These are observed runs, not decisions submitted to a live ledger.</p><div class="quiet-choices">{cards}</div></main>',kind='outcome')


def recording_choice():
    return shell('Follow the evidence','<main id="main" class="quiet-content"><p class="eyebrow">Evidence review</p><h1>What would you like to inspect?</h1><p class="lead">Each recording keeps its committed expectation beside its observed result. The readiness chapter explains what local evidence leaves open.</p><div class="quiet-choices"><a href="laboratory.html"><h2>Walk through the recordings.</h2><p>All 32 examples, one observation at a time.</p><span>Begin the recorded review →</span></a><a href="chapters/08-release-and-adoption.html"><h2>Continue to readiness.</h2><p>Understand the remaining integration and adoption evidence.</p><span>Continue reading →</span></a></div></main>',kind='evidence-choice')

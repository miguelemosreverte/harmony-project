"""Reader-specific screens reuse the same source catalog, workflow, and citation views."""
from html import escape
import pages
from render import markdown, html_fragment


def toolbar(title, description):
    return f'<div class="reader-intro"><p class="eyebrow">The field guide</p><h1>{title}</h1><p>{description}</p></div>'


def code():
    body=toolbar('Explore the actual code.','Choose a file. Read its purpose, its source, and the slice it belongs to.')
    body+='''<div class="source-workbench"><aside class="source-tree-pane"><details id="tree-disclosure" open><summary>Repository files</summary><label class="reader-search">Find a file<input id="file-search" type="search" placeholder="Financing, .daml, Engine…" autocomplete="off"></label><select id="source-scope" aria-label="Source area"><option value="all">All source areas</option><option value="product">Production</option><option value="harness">Verification harness</option><option value="book">Book</option><option value="examples">Golden stories</option><option value="scripts">Build and run</option></select><nav id="file-tree" aria-label="Source files"></nav></details></aside><section class="source-reading"><header class="source-heading"><h2 id="source-name"></h2><p id="source-location"></p><nav class="reader-tabs" aria-label="File view"><button data-code-tab="code">Code</button><button data-code-tab="diagram">Slice diagram</button></nav></header><div id="source-code" class="colored-source" tabindex="0" aria-label="Source code"></div><div id="source-diagram" hidden></div></section><aside id="source-notes" class="source-notes" aria-label="File explanation"></aside></div><p id="source-counts" class="reader-footnote"></p>'''
    return pages.shell('Explore the code',f'<main id="main" class="reader-content">{body}</main>',slug='code',kind='atlas-code')


def reviewer():
    body=toolbar('Review the implementation, end to end.','Follow a diagram into its manifest, contract, service, and evidence. Each relationship is explicit and reviewable.')
    body+='''<div class="reader-bar"><label>Review path<select id="review-slice"></select></label><span id="review-status" class="evidence-kind"></span></div><section id="review-diagram" class="reader-stage"></section><div class="review-notes"><article id="review-node" class="reader-note"></article><article id="review-evidence" class="reader-note"></article></div><details class="reader-note"><summary>Compare with the original architecture drawings</summary><p>The originals are preserved intact. The reading maps above explain selected implementation paths; they are not replacements for the original architecture.</p><div class="original-diagrams"><a href="assets/component-map.svg"><img src="assets/component-map.svg" alt="Original component diagram" loading="lazy"></a><a href="assets/contract-model.svg"><img src="assets/contract-model.svg" alt="Original contract diagram" loading="lazy"></a></div></details>'''
    return pages.shell('Review the diagrams',f'<main id="main" class="reader-content">{body}</main>',slug='reviewer',kind='atlas-review')


def author(passages,corpus):
    items=[]
    for p in passages:
        excerpt='\n'.join(corpus[p.source][1].splitlines()[p.start-1:p.end])
        rich=(markdown(excerpt,source=True) if p.source=='proposal' else html_fragment(excerpt)).replace('href="../sources/', 'href="sources/')
        if p.source=='architecture' and p.start<=88<=p.end:rich+='<img class="author-original" src="assets/component-map.svg" alt="Original component diagram">'
        if p.source=='architecture' and p.start<=318<=p.end:rich+='<img class="author-original" src="assets/contract-model.svg" alt="Original contract model">'
        key=f'{p.source}-{p.start}'
        items.append(f'<section class="author-passage" id="passage-{key}" data-passage="{key}" data-document="{p.source}"><header><button data-select-passage="{key}">{escape(p.title)}</button><a href="sources/{p.source}.html?view=source#L{p.start}">Original lines {p.start}–{p.end} ↗</a></header><div class="rich-source">{rich}</div></section>')
    body=toolbar('Your documents, beside the implementation.','Select a passage on the left. The right side follows it into a workflow, diagram, code, or chapter.')
    body+='''<div class="reader-bar"><label>Original document<select id="author-document"><option value="proposal">Original proposal · Markdown</option><option value="architecture">Original architecture · HTML</option></select></label><span class="evidence-kind">736 / 736 quotation units preserved</span><a href="coverage.html">Coverage method →</a></div>'''
    body+=f'''<div class="author-workbench"><article id="author-document-pane" tabindex="0" aria-label="Original document">{''.join(items)}</article><section class="author-companion"><header><h2 id="companion-title"></h2><nav class="reader-tabs" aria-label="Companion view"><button data-companion="diagram">Diagram</button><button data-companion="workflow">Workflow</button><button data-companion="code">Code</button><button data-companion="chapter">Explanation</button></nav><p id="companion-boundary"></p><a id="companion-open" target="_blank" rel="noopener">Open this view in its own tab ↗</a></header><iframe id="companion-frame" title="Evidence for the selected original passage"></iframe></section></div>'''
    return pages.shell('Read the original beside the product',f'<main id="main" class="reader-content">{body}</main>',slug='author',kind='atlas-author')


def workflows(recordings):
    cards=[('Alice makes an offer','Northbank’s decision lets Alice prepare her proposal. Ben and Sofia carry it into the property application.','Recorded ledger workflow','chapters/03-financing-to-offer.html?step=0','financing'),('A private financing handoff','Act as the bank, then the buyer. The live server submits real commands to the disposable ledger.','Live sandbox','sandbox.html','financing'),('A transfer across four parties','Two custodians prepare their side before one eligible settlement transaction. Explore the refusal as well.','Recorded ledger workflow','chapters/04-four-party-transfer.html?step=0','transfer'),('A plan both parties accept','The bank proposes supported actions. The buyer accepts, then the assigned parties execute them.','Live bounded composer','sandbox.html?task=composer','composition')]
    tiles=''.join(f'<a class="workflow-story" href="{url}"><span class="story-symbol">{i+1}</span><span class="evidence-kind">{kind}</span><h2>{title}</h2><p>{description}</p><span class="story-open">Follow this workflow →</span></a>' for i,(title,description,kind,url,slice) in enumerate(cards))
    body=toolbar('Follow the handoff.','Independent applications keep their authority. Harmonia coordinates the work between them.')+f'<div class="workflow-stories">{tiles}</div>'
    body+='''<section class="reader-note"><h2>What makes the diagram executable?</h2><div id="mechanism-diagram" data-slice-diagram="process"></div><p>Daml templates define the contracts and their stakeholders. Choice controllers define who may act. DARs carry compiled Daml-LF packages to Canton participants. The Scala service submits a supported choice through the Ledger API; it observes the resulting contracts before updating the browser.</p><p>A result’s issuer, consumer, subject, and continuation matter. Seeing an approval is not enough to use it in an unrelated workflow. Multi-party preparation can span transactions; only the final eligible transfer is atomic in the transfer example.</p><p><a href="reviewer.html?slice=process">Review the enforcing contracts →</a> · <a href="chapters/08-release-and-adoption.html">See release and adoption boundaries →</a></p></section><section class="reader-note"><h2>Present the product</h2><p>Use the four-scene purchase story as a clean slideshow. Advance with the arrows, keyboard, or touch. Play starts a timed presentation; it does not submit transactions.</p><a class="button primary" href="chapters/03-financing-to-offer.html?present=1&audience=investor">Open the presentation →</a></section>'''
    families = {
        'Financing': ('A bank decision', 'Northbank approves or refuses Alice’s financing under the application’s own rules.'),
        'Workflow': ('A direct application handoff', 'A typed Daml interface lets the core request an application choice and observe its result.'),
        'Adapter': ('A legacy application joins', 'A wrapper preserves the original financing authority while exposing the shared interface.'),
        'Private': ('Share progress, keep documents private', 'Alice uses a scoped result. The workflow sees progress without receiving the bank’s private application.'),
        'Progression': ('Choose a route, then join', 'Northbank chooses a branch. The join waits for the selected obligations; skipped work is never shown as completed.'),
        'Purchase': ('Alice makes a property offer', 'Ben and Sofia pass a proposal that is backed by Alice’s financing result. Explore the refusal and proof-reuse cases.'),
        'Transfer': ('Four parties settle one trade', 'Two custodians prepare independently. The final Daml transaction either completes both legs or rolls back.'),
        'Generated': ('Use a generated binding', 'A reviewed mapping becomes an adapter that is tested against the same application behavior.'),
        'Composition': ('Agree on the plan', 'The bank proposes actions. The buyer consents before application sources and shared execution are created.'),
        'Packages': ('Inspect before integrating', 'Inspect a DAR, review a bounded mapping, and distinguish a generated project from a compiled and registered package.'),
        'Boundaries': ('Challenge the limits', 'Read the ledger and race observations that establish bounded behavior. These are verification phases, not user actions.')
    }
    body += '<section class="workflow-library"><h2>Explore every recorded workflow</h2><p>32 committed examples, grouped by the question they answer. Each opens a carousel of observed results, with its original provenance and golden comparison.</p>'
    for kind,(title,description) in families.items():
        items = [(key,story) for key,story in recordings.items() if story['presentation']['kind']==kind]
        links = ''.join(f'<li><a href="laboratory.html?story={key}">{escape(story["title"])}</a></li>' for key,story in items)
        body += f'<details class="reader-note"><summary>{title} · {len(items)} examples</summary><p>{description}</p><ul>{links}</ul></details>'
    body += '</section>'
    return pages.shell('Workflow demonstrations',f'<main id="main" class="reader-content">{body}</main>',slug='workflows',kind='atlas-workflows')

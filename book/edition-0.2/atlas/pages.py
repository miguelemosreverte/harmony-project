"""Four reader intentions, each with its own small and explicit reading route."""
from html import escape
import pages
from render import markdown, html_fragment, passive


def code():
    body='''<main id="main" class="source-workbench"><aside class="source-tree-pane"><p class="eyebrow">Developer · choose a file</p><nav id="file-tree" data-file-tree aria-label="Repository files"></nav></aside><section class="source-reading"><header><p class="eyebrow">Actual source from this checkout</p><h1 id="source-name"></h1><p id="source-location" class="citation"></p></header><div id="source-notes"></div><div id="source-code" class="colored-source" aria-label="Source code"></div><section class="source-context"><h2>This file in its vertical slice</h2><div id="source-diagram"></div></section><p id="source-counts" class="quiet-note"></p><a class="quiet-return" href="book-overview.html">Return to the reading paths →</a></section></main>'''
    return pages.shell('Explore the actual code',body,slug='code',kind='atlas-code')


def reviewer():
    body='''<main id="main" class="quiet-content"><p class="eyebrow">Reviewer · <span id="review-position"></span></p><h1 id="review-title">Review the implementation.</h1><p class="lead" id="review-question"></p><p id="review-status" class="citation"></p><section id="review-diagram" class="reader-stage"></section><article id="review-evidence" class="reader-note"></article>'''+pages.paging('verify.html','← Previous','coverage.html','Next →')+'</main>'
    nav=pages.paging('verify.html','← Previous','coverage.html','Next →')
    body=body.replace(nav,'').replace('<section id="review-diagram"',nav+'<section id="review-diagram"')
    return pages.shell('Review the diagrams',body,slug='reviewer',kind='atlas-review')


def author(passages,corpus):
    items=[]
    for p in passages:
        excerpt='\n'.join(corpus[p.source][1].splitlines()[p.start-1:p.end])
        rich=markdown(excerpt,source=True) if p.source=='proposal' else html_fragment(excerpt)
        key=f'{p.source}-{p.start}'
        items.append(f'<section class="author-passage" id="passage-{key}" data-passage="{key}"><p class="citation">Original {p.source} · lines {p.start}–{p.end}</p><h2>{escape(p.title)}</h2><div class="rich-source">{passive(rich)}</div></section>')
    body='''<main id="main" class="author-content"><header class="author-heading"><p class="eyebrow">Original author · <span id="author-position"></span></p><h1>The original words, beside the work.</h1><p class="citation">736 / 736 quotation units preserved. Quotation inclusion is separate from implementation.</p></header><div class="author-workbench"><article id="author-document-pane" aria-label="Original documents">'''+''.join(items)+'''</article><section class="author-companion"><h2 id="companion-title"></h2><p id="companion-boundary"></p><div id="companion-frame" aria-label="Explanation of the selected original passage"></div></section></div>'''+pages.paging('verify.html','← Previous passage','coverage.html','Next passage →')+'</main>'
    nav=pages.paging('verify.html','← Previous passage','coverage.html','Next passage →')
    body=body.replace(nav,'').replace('<div class="author-workbench">',nav+'<div class="author-workbench">')
    return pages.shell('Follow the original documents',body,slug='author',kind='atlas-author')


def workflows(recordings):
    body='''<main id="main" class="quiet-content"><p class="eyebrow">For users and technical investors</p><h1>Applications keep their rules.<br>Work moves between them.</h1><p class="lead">Alice needs financing before making a property offer. Her bank decides in its own application. A scoped result lets the next application continue.</p><div class="quiet-choices"><a href="chapters/03-financing-to-offer.html"><h2>Follow Alice’s purchase.</h2><p>See the people, applications and handoffs in a recorded ledger execution.</p><span>Begin the story →</span></a><a href="chapters/01-product.html"><h2>Understand the mechanism.</h2><p>Daml contracts, Canton participants, and the boundary of the implementation.</p><span>Read chapter one →</span></a></div></main>'''
    return pages.shell('What the product provides',body,slug='workflows',kind='atlas-workflows')

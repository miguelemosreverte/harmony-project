"""Historical mock addresses lead into the current narrated product boundary."""
from pages import shell,paging


def application():
    return shell('The financing handoff','<main id="main" class="quiet-content"><p class="eyebrow">The product workflow</p><h1>Approve privately. Continue together.</h1><p class="lead">The working product has its own participant session. The book supplies a recorded explanation of the same handoff.</p>'+choices('sandbox.html?task=composer','Try the shared workflow','Submit a plan, consent, then execute.','chapters/07-evidence-and-boundaries.html','Follow the evidence','Compare recorded outcomes with their expectations.')+'</main>',kind='historical-entry')


def builder():
    return shell('Bring an application','<main id="main" class="quiet-content"><p class="eyebrow">Application integration</p><h1>Inspect before connecting.</h1><p class="lead">A DAR identifies compiled Daml packages. A reviewed mapping determines whether a supported action can be adapted. Compilation is distinct from registration.</p>'+choices('sandbox.html?task=packages','Try the package workspace','Inspect a DAR and compile an adapter.','chapters/06-compose-a-workflow.html','Continue the story','See how participants agree on their next tasks.')+'</main>',kind='historical-entry')


def choices(first, title, detail, second, other_title, other_detail):
    return f'<div class="quiet-choices"><a id="page-previous" href="{first}"><h2>{title}</h2><p>{detail}</p><span>Explore →</span></a><a id="page-next" href="{second}"><h2>{other_title}</h2><p>{other_detail}</p><span>Continue →</span></a></div>'

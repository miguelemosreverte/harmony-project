// Load in the preview browser, then await harmoniaDesignChecks.<page>().
// Checks use the actual DOM and native controls. They submit no ledger commands.
window.harmoniaDesignChecks = (() => {
  function assert(condition, message) { if (!condition) throw Error(message); }
  function click(selector) { const node=document.querySelector(selector); assert(node,`Missing ${selector}`); node.click(); }
  function select(selector,value) { const node=document.querySelector(selector); node.value=value; node.dispatchEvent(new Event('change',{bubbles:true})); }
  function layout() {
    assert(document.documentElement.scrollWidth <= innerWidth, 'Page overflows viewport');
    assert(document.querySelector('h1'), 'Page has no primary heading');
    assert(document.querySelector('a[href="#main"]'), 'Missing keyboard skip link');
    return {width:innerWidth,height:innerHeight,overflow:false};
  }
  function chapter() {
    const next=document.getElementById('next-step');
    click('#next-step'); assert(document.getElementById('step-count').textContent==='Step 3 of 4','Next step did not advance');
    click('#next-step'); assert(next.disabled,'Completed story still allows progression');
    click('#reset-story'); assert(!next.disabled && document.getElementById('step-count').textContent==='Step 1 of 4','Reset failed');
    click('[data-role="Seller"]'); assert(document.getElementById('visibility-explanation').textContent.includes('Private financing documents remain outside'),'Seller privacy explanation missing');
    click('#tab-input'); assert(!document.getElementById('panel-input').hidden,'Input tab did not open');
    const input=document.getElementById('tab-input'); input.focus(); input.dispatchEvent(new KeyboardEvent('keydown',{key:'ArrowRight',bubbles:true}));
    assert(document.activeElement.id==='tab-expected' && !document.getElementById('panel-expected').hidden,'Keyboard tab navigation failed');
    click('#tab-source'); assert(document.querySelector('#panel-source a').getAttribute('href').includes('03-financing'),'Source destination missing');
    return {checks:7,...layout(),simulation:true};
  }
  function application() {
    const advance=document.getElementById('advance-workflow');
    select('#actor','Buyer'); assert(advance.disabled,'Wrong illustrated actor can advance');
    select('#actor','Bank');
    for(const mode of ['pending','refused','stale','disconnected']) {
      select('#simulation-state',mode); assert(advance.disabled,`${mode} still enables commands`);
      assert(document.getElementById('workflow-status').textContent==='Awaiting bank',`${mode} advanced progress`);
    }
    select('#simulation-state','ready'); click('#advance-workflow');
    assert(document.getElementById('workflow-status').textContent==='Awaiting buyer' && advance.disabled,'Bank action did not hand off to buyer');
    select('#actor','Buyer'); click('#advance-workflow'); assert(document.getElementById('workflow-status').textContent==='Awaiting seller','Buyer did not create offer');
    select('#actor','Seller'); click('#advance-workflow'); assert(advance.disabled && document.getElementById('workflow-status').textContent==='Complete · sample','Final completion incorrect');
    click('[data-view="history"]'); assert(!document.getElementById('view-history').hidden && document.querySelectorAll('#workflow-history li').length===4,'History does not follow actions');
    return {checks:9,...layout(),simulation:true};
  }
  function refusal() {
    click('#reject-workflow');
    assert(document.getElementById('workflow-status').textContent==='Financing rejected','Rejection missing');
    assert(document.getElementById('advance-workflow').disabled,'Rejected workflow can advance');
    assert(!document.querySelector('[data-progress="3"]').classList.contains('complete'),'Rejection created an offer');
    return {checks:3,...layout(),simulation:true};
  }
  function builder() {
    select('#sample-package','unsupported'); click('#inspect-sample');
    assert(document.getElementById('builder-result').textContent.includes('Unsupported sample shape'),'Unsupported shape is not explained');
    select('#sample-package','legacy'); click('#inspect-sample');
    assert(document.getElementById('builder-result').textContent.includes('not a fresh DAR inspection'),'Inspection wrongly claims live evidence');
    click('#inspect-sample'); assert(document.getElementById('builder-result').textContent.includes('Mapping preview'),'Mapping is not shown');
    click('#inspect-sample'); assert(document.getElementById('inspect-sample').disabled,'Final design state still advances');
    assert(document.getElementById('builder-result').textContent.includes('performs none of those operations'),'Build simulation claims compilation');
    return {checks:5,...layout(),simulation:true};
  }
  async function coverage() {
    const original=await (await fetch('../../docs/proposal/harmonia-architecture.html')).text();
    const document=new DOMParser().parseFromString(original,'text/html');
    const walker=document.createTreeWalker(document.body,NodeFilter.SHOW_TEXT);
    const units=[]; let node;
    while((node=walker.nextNode())) if(node.textContent.trim() && !node.parentElement.closest('style,script,template')) units.push(node.textContent.trim());
    const report=await (await fetch('coverage.json')).json();
    const architecture=report.documents.find(d=>d.id==='architecture');
    assert(units.length===architecture.units, 'Browser HTML text count differs from report');
    const words=units.flatMap(t=>t.match(/[\p{L}\p{N}_]+(?:[’'-][\p{L}\p{N}_]+)*/gu)||[]).length;
    assert(words===architecture.words,'Browser HTML word count differs from report');
    const quoted=[];
    for(const chapter of report.chapters) {
      const html=await (await fetch(`chapters/${chapter.id}.html`)).text();
      const page=new DOMParser().parseFromString(html,'text/html');
      quoted.push(...[...page.querySelectorAll('[data-unit^="architecture-"]')].map(n=>({id:n.dataset.unit,text:n.textContent})));
    }
    quoted.sort((a,b)=>a.id.localeCompare(b.id));
    assert(JSON.stringify(quoted.map(q=>q.text))===JSON.stringify(units),'Browser parsed source does not equal chapter quotations');
    return {checks:3,...layout(),htmlUnits:units.length,htmlWords:words};
  }
  function narrow() {
    const menu=document.querySelector('.mobile-menu');
    if(innerWidth<=900) { click('.mobile-menu'); assert(menu.getAttribute('aria-expanded')==='true','Mobile menu did not open'); click('.mobile-menu'); assert(menu.getAttribute('aria-expanded')==='false','Mobile menu did not close'); }
    return {checks:2,...layout()};
  }
  return {chapter,application,refusal,builder,coverage,narrow,layout};
})();

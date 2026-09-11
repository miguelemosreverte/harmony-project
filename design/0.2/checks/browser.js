// Run inside the preview browser. Assertions exercise rendered behavior and independent evidence.
window.harmoniaDesignChecks = (() => {
  const assert=(condition,message)=>{if(!condition)throw Error(message);};
  const $=selector=>document.querySelector(selector);
  const click=selector=>{assert($(selector),`Missing ${selector}`);$(selector).click();};
  const select=(selector,value)=>{$(selector).value=value;$(selector).dispatchEvent(new Event('change',{bubbles:true}));};
  const settle=()=>new Promise(resolve=>setTimeout(resolve,40));
  const state=()=>window.HarmoniaView.state;
  function layout() {
    assert(document.documentElement.scrollWidth<=innerWidth,`Page overflows ${innerWidth}px: ${document.documentElement.scrollWidth}`);
    assert(document.querySelectorAll('h1').length===1,'Expected one primary heading');
    assert($('a[href="#main"]'),'Missing keyboard skip link');
    for(const image of document.images) assert(image.complete&&image.naturalWidth>0,`Broken image: ${image.src}`);
    return {width:innerWidth,height:innerHeight,overflow:false};
  }
  function snapshot() {
    return {state:state(), heading:$('h1').textContent, visibleModes:[...document.querySelectorAll('.mode-panel')].filter(n=>!n.hidden).map(n=>n.dataset.mode),
      selected:[...document.querySelectorAll('select')].map(n=>[n.id,n.value]), disclosures:[...document.querySelectorAll('[data-disclosure][open]')].map(n=>n.id).sort(),
      outcome:$('#recorded-outcome')?.textContent, action:$('#recorded-action')?.textContent, facts:$('#recorded-state')?.textContent, evidence:$('#recorded-json')?.textContent,
      task:$('#action-title')?.textContent, history:$('#workflow-history')?.textContent, builder:$('#builder-result')?.textContent,
      route:$('#route-links')?.textContent};
  }
  async function common() {
    click('#appearance-toggle');click('[data-theme="dark"]');click('[data-text="large"]');
    assert(location.search.includes('theme=dark')&&location.search.includes('text=large'),'Appearance is absent from URL');
    assert(document.documentElement.dataset.theme==='dark'&&document.documentElement.dataset.text==='large','Appearance is not applied');
    click('#close-appearance');assert(document.activeElement.id==='appearance-toggle','Appearance did not return focus');
    const before=location.href;window.HarmoniaView.update({theme:'paper'});
    await new Promise(resolve=>{addEventListener('popstate',resolve,{once:true});history.back();});await settle();
    assert(location.href===before&&state().theme==='dark','Back did not restore appearance');
    await new Promise(resolve=>{addEventListener('popstate',resolve,{once:true});history.forward();});await settle();
    assert(state().theme==='paper','Forward did not restore appearance');
    return {checks:5,...layout()};
  }
  function welcome() {
    const expected={explorer:'01-product',author:'coverage',developer:'01-product',investor:'01-product',operator:'06-compose-a-workflow'};
    for(const [audience,target] of Object.entries(expected)) {
      click(`[name=audience][value=${audience}]`);
      assert($('#start-route').href.includes(target),`Wrong first stop for ${audience}`);
      assert(state().audience===audience,'Audience was not serialized');
      assert($('#route-links').children.length===window.HarmoniaView.config.journeys[audience].steps.length,'Wrong path length');
    }
    return {checks:15,...layout()};
  }
  async function chapter() {
    click('[data-view="try"]');
    let attempts=0,refusals=0;
    for(const [key,story] of Object.entries(window.HarmoniaView.config.stories)) {
      select('#story-select',key);
      assert(state().step===0&&$('#previous-step').disabled,'Scenario did not start at setup');
      for(let index=0;index<story.presentation.units.length;index++) {
        const expected=story.presentation.units[index];click('#next-step');attempts++;
        assert(state().step===index+1&&location.search.includes(`step=${index+1}`),'Next action did not update URL');
        assert($('#recorded-json').textContent===JSON.stringify(expected.actual,null,2),'Displayed observation differs from recorded result');
        assert($('#recorded-outcome').textContent===(expected.actual.outcome==='committed'?'Transaction committed':'Action refused'),'Outcome label is wrong');
        if(expected.actual.outcome==='rejected')refusals++;
      }
      assert($('#next-step').disabled&&!$('#story-complete').hidden,'End of story has no completion state');
      click('#previous-step');assert(!$('#next-step').disabled,'Previous action does not restore controls');
      click('#reset-story');assert(state().step===0,'Reset failed');
    }
    click('#step-inspector summary');await settle();click('[data-tab="input"]');
    $('[data-tab="input"]').focus();$('[data-tab="input"]').dispatchEvent(new KeyboardEvent('keydown',{key:'ArrowRight',bubbles:true}));
    assert(state().tab==='expected'&&document.activeElement.id==='evidence-expected','Evidence tabs do not support arrows');
    return {attempts,refusals,...layout()};
  }
  function application() {
    const advance=$('#advance-workflow');
    select('#actor','Buyer');assert(advance.disabled,'Wrong sample actor can advance');select('#actor','Bank');
    for(const status of ['pending','refused','stale','disconnected']) {
      select('#simulation-state',status);assert(advance.disabled&&state().step===2,`${status} allows progression`);assert($('#action-notice').textContent.length>20,'No recovery explanation');
    }
    select('#simulation-state','ready');click('#advance-workflow');
    assert(state().step===3&&advance.disabled&&!$('#handoff-workflow').hidden,'Bank action lacks handoff');
    click('#handoff-workflow');assert(state().actor==='Buyer'&&!advance.disabled,'Handoff does not enable buyer task');
    click('#advance-workflow');click('#handoff-workflow');click('#advance-workflow');
    assert(state().step===5&&advance.hidden,'Completion not shown');
    click('[data-view="history"]');assert($('#workflow-history').children.length===4,'History does not reflect the URL state');
    click('#reset-workflow');click('#reject-workflow');
    assert(state().state==='rejected'&&state().step===2&&advance.disabled,'Refusal did not stop the sample');
    return {checks:10,...layout()};
  }
  function builder() {
    select('#sample-package','unsupported');click('#inspect-sample');
    assert(state().phase===2&&$('#inspect-sample').disabled&&$('#builder-title').textContent.includes('unsupported'),'Unsupported shape can proceed');
    select('#sample-package','legacy');
    for(let phase=2;phase<=4;phase++){click('#inspect-sample');assert(state().phase===phase,'Builder phase absent from URL');}
    assert($('#inspect-sample').disabled&&!$('#builder-complete').hidden,'No final integration guide');
    click('#builder-back');assert(state().phase===3&&!$('#inspect-sample').disabled,'Builder cannot go back');
    return {checks:6,...layout()};
  }
  async function sources() {
    window.HarmoniaView.update({view:'sources'});
    let count=0;
    for(const details of document.querySelectorAll('.source-section')) {
      details.open=true; await settle();count++;
      assert(state().open.split(',').includes(details.id),'Source disclosure not serialized');
      assert(details.querySelector('.rich-source'),'Missing readable source');
      layout(); details.open=false;await settle();
    }
    return {passages:count,...layout()};
  }
  return {layout,snapshot,common,welcome,chapter,application,builder,sources};
})();

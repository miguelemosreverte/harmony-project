// A shared URL is the complete durable view state. No hidden storage or server session.
(() => {
  'use strict';
  const config = JSON.parse(document.getElementById('view-config').textContent);
  config.stories = Object.fromEntries((config.stories || []).map(key => [key, (window.HarmoniaRunRecordings || {})[key] || window.HarmoniaRecordings[key]]));
  const atlas=window.HarmoniaAtlas;
  const firstStory = Object.keys(config.stories || {})[0] || '';
  const defaults = {audience:'explorer', theme:'light', text:'standard', panel:'closed', nav:'closed', open:'', view:firstStory ? 'try' : 'read', story:firstStory, step:0, actor:'all', tab:'observed', state:'ready', package:'legacy', phase:1,embed:0,present:0,autoplay:0,file:'product/server/src/main/scala/harmonia/financing/FinancingObservation.scala',line:1,slice:'financing',node:'',source:'proposal',passage:'proposal-1',companion:'diagram',q:'',scope:'all',codeTab:'code',detail:'',task:'financing'};
  if (config.kind === 'workspace') Object.assign(defaults, {view:'overview', step:2, actor:'Bank'});
  const allowedViews = config.kind === 'workspace' ? ['overview','applications','history'] : config.kind === 'source' ? ['read','source'] : ['read','sources','evidence', ...(firstStory ? ['try'] : [])];
  const common = ['audience','theme','text','panel','nav','open','embed'];
  const fields = [...common,...(config.kind==='sandbox'?['task']:[]), ...(config.kind.startsWith('atlas-')?['file','line','slice','node','source','passage','companion','q','scope','codeTab','detail']:[]), ...(firstStory?['present','autoplay']:[]), ...(config.kind === 'workspace' ? ['view','state','step','actor'] : config.kind === 'builder' ? ['package','phase'] : config.kind === 'chapter' || config.kind === 'source' ? ['view', ...(firstStory ? ['story','step','actor','tab'] : [])] : [])];
  const choose = (value, options, fallback) => options.includes(value) ? value : fallback;
  const integer = (value, min, max, fallback) => /^\d+$/.test(String(value)) ? Math.max(min, Math.min(max, Number(value))) : fallback;
  function normalize(input) {
    const s = {...defaults};
    s.audience = choose(input.audience, Object.keys(config.journeys), defaults.audience);
    s.theme = choose(input.theme, ['light','dark','paper'], defaults.theme);
    s.text = choose(input.text, ['compact','standard','large'], defaults.text);
    s.panel = choose(input.panel, ['closed','appearance','evidence'], 'closed');
    s.nav = choose(input.nav, ['closed','open'], 'closed');
    const ids = new Set([...document.querySelectorAll('[data-disclosure][id]')].map(node => node.id));
    const opened = new Set(String(input.open || '').split(',').filter(id => ids.has(id)));
    for (const id of [...opened]) {
      let parent = document.getElementById(id).parentElement?.closest('[data-disclosure]');
      while (parent) {opened.add(parent.id); parent=parent.parentElement?.closest('[data-disclosure]');}
    }
    s.open = [...opened].sort().join(',');
    s.view = choose(input.view, allowedViews, defaults.view);
    s.story = choose(input.story, Object.keys(config.stories || {}), firstStory);
    const story = config.stories?.[s.story];
    s.step = integer(input.step, config.kind === 'workspace' ? 2 : 0, config.kind === 'workspace' ? 5 : story?.presentation.units.length || 0, defaults.step);
    s.actor = choose(input.actor, config.kind === 'workspace' ? ['Bank','Buyer','Seller'] : ['all', ...new Set(story?.presentation.units.map(u => u.actor) || [])], defaults.actor);
    s.tab = choose(input.tab, ['input','expected','observed','provenance'], defaults.tab);
    s.state = choose(input.state, ['ready','pending','refused','stale','disconnected','rejected'], defaults.state);
    if (s.state === 'rejected') s.step = 2;
    s.package = choose(input.package, ['legacy','unsupported'], defaults.package);
    s.phase = integer(input.phase, 1, s.package === 'unsupported' ? 2 : 4, 1);
    s.task=choose(input.task,['financing','composer','packages'],'financing');
    s.embed=integer(input.embed,0,1,0);s.present=integer(input.present,0,1,0);s.autoplay=integer(input.autoplay,0,1,0);
    s.file=choose(input.file,Object.keys(atlas.files),defaults.file);
    s.line=integer(input.line,1,atlas.files[s.file]?.lines||1,1);
    s.slice=choose(input.slice,Object.keys(atlas.slices),'financing');
    s.node=choose(input.node,atlas.slices[s.slice].nodes.map(n=>n.id),'');
    s.source=choose(input.source,['proposal','architecture'],'proposal');
    const passages=atlas.passages.filter(p=>p.source===s.source);
    s.passage=choose(input.passage,passages.map(p=>p.id),passages[0].id);
    s.companion=choose(input.companion,['diagram','workflow','code','chapter'],'diagram');
    s.detail='';
    if(input.detail&&String(input.detail).length<2400){try{const base=new URL(config.base,location.href),u=new URL(input.detail,base),path=u.pathname.slice(base.pathname.length);if(u.origin===base.origin&&u.pathname.startsWith(base.pathname)&&['code.html','reviewer.html',...Object.keys(config.chapters).map(c=>'chapters/'+c+'.html')].includes(path))s.detail=path+u.search;}catch{}}
    s.codeTab=choose(input.codeTab,['code','diagram'],'code');
    s.q=String(input.q||'').slice(0,80);s.scope=choose(input.scope,['all','product','harness','book','examples','scripts'],'all');
    return s;
  }
  let state;
  const listeners = new Set();
  function urlFor(s) {
    const url = new URL(location.href);
    url.search = '';
    for (const key of fields) if (s[key] !== defaults[key]) url.searchParams.set(key, s[key]);
    return url;
  }
  function read() {
    // The first occurrence wins for a repeated parameter.
    const input = {};
    for (const [key,value] of new URLSearchParams(location.search)) if (!(key in input)) input[key] = value;
    let hash = location.hash.slice(1);
    try {hash = decodeURIComponent(hash);} catch {history.replaceState(null, '', location.pathname + location.search);}
    const anchor = document.getElementById(hash);
    if (anchor && !input.view) {
      if (config.kind === 'source' && /^L[0-9]+$/.test(hash)) input.view = 'source';
      else if (anchor.closest('.source-section')) input.view = 'sources';
    }
    if (anchor?.matches('[data-disclosure]') && !input.open) input.open = anchor.id;
    state = normalize(input);
    const url = urlFor(state);
    if (url.href !== location.href) history.replaceState(null, '', url);
    for (const listener of listeners) listener(state);
  }
  function update(patch, {replace = false} = {}) {
    state = normalize({...state, ...patch});
    const url = urlFor(state);
    url.hash = ''; // An explicit view change supersedes a source-location anchor.
    if (url.href !== location.href) history[replace ? 'replaceState' : 'pushState'](null, '', url);
    for (const listener of listeners) listener(state);
  }
  window.HarmoniaView = {config, get state() {return {...state};}, update,
    subscribe(listener) {listeners.add(listener); listener(state);},
    href(path) {
      const url = new URL(path, new URL(config.base, location.href));
      for (const key of ['audience','theme','text']) if (!url.searchParams.has(key) && state[key] !== defaults[key]) url.searchParams.set(key, state[key]);
      return url.href;
    }
  };
  addEventListener('popstate', read);
  addEventListener('hashchange', read);
  read();
  document.documentElement.classList.add('js');
})();

// The address owns the selected page, source, passage and recorded moment.
(() => {
  const config=JSON.parse(document.getElementById('view-config').textContent),atlas=HarmoniaAtlas;
  config.stories=Object.fromEntries((config.stories||[]).map(id=>[id,(window.HarmoniaRunRecordings||{})[id]||HarmoniaRecordings[id]]));
  const defaults={theme:'light',text:'standard',embed:0,present:0,view:Object.keys(config.stories).length?'try':'read',story:Object.keys(config.stories)[0]||'',step:0,actor:'all',file:'product/server/src/main/scala/harmonia/financing/FinancingObservation.scala',line:1,slice:'financing',relationship:'reading',node:'',source:'proposal',passage:atlas.passages[0].id,task:'financing',companion:'',detail:''};
  const choose=(v,values,fallback)=>values.includes(v)?v:fallback;
  const number=(v,min,max,fallback)=>/^\d+$/.test(String(v))?Math.max(min,Math.min(max,Number(v))):fallback;
  const normalize=input=>{
    const s={...defaults};
    s.theme=choose(input.theme,['light','dark','paper'],'light');s.text=choose(input.text,['compact','standard','large'],'standard');
    s.embed=number(input.embed,0,1,0);s.present=number(input.present,0,1,0);
    s.view=choose(input.view,['read','try','sources','source','evidence'],defaults.view);
    s.story=choose(input.story,Object.keys(config.stories),defaults.story);
    s.step=number(input.step,0,config.stories[s.story]?.presentation.units.length||0,0);
    s.file=choose(input.file,Object.keys(atlas.files),defaults.file);s.line=number(input.line,1,atlas.files[s.file].lines,1);
    s.slice=choose(input.slice,Object.keys(atlas.slices),'financing');
    s.relationship=choose(input.relationship,['reading',...Object.keys(atlas.relationships).filter(k=>atlas.relationships[k][s.slice])],'reading');
    const graph=s.relationship==='reading'?atlas.slices[s.slice]:atlas.relationships[s.relationship][s.slice];s.node=choose(input.node,graph.nodes.map(n=>n.id),'');
    s.source=choose(input.source,['proposal','architecture'],'proposal');
    s.passage=choose(input.passage,atlas.passages.map(p=>p.id),atlas.passages.find(p=>p.source===s.source).id);s.source=atlas.passages.find(p=>p.id===s.passage).source;
    s.task=choose(input.task,['financing','composer','packages'],'financing');
    return s;
  };
  let state;const listeners=new Set();
  const urlFor=s=>{const u=new URL(location.href);u.search='';for(const [key,value] of Object.entries(s))if(value!==defaults[key])u.searchParams.set(key,value);return u;};
  function read(){const values=Object.fromEntries(new URLSearchParams(location.search));if(config.kind==='source'&&/^#L\d+$/.test(location.hash))values.view='source';state=normalize(values);const url=urlFor(state);if(url.href!==location.href)history.replaceState(null,'',url);for(const listener of listeners)listener(state);}
  window.HarmoniaView={config,get state(){return {...state};},subscribe(listener){listeners.add(listener);listener(state);},update(patch,{replace=false}={}){state=normalize({...state,...patch});const url=urlFor(state);url.hash='';if(url.href!==location.href)history[replace?'replaceState':'pushState'](null,'',url);for(const listener of listeners)listener(state);},href(path){const u=new URL(path,new URL(config.base,location.href));for(const key of ['theme','text'])if(state[key]!==defaults[key]&&!u.searchParams.has(key))u.searchParams.set(key,state[key]);return u.href;}};
  addEventListener('popstate',read);addEventListener('hashchange',read);read();
})();

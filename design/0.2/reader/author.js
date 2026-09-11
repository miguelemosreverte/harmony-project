(() => {
  const pane=document.getElementById('author-document-pane');if(!pane)return;
  const view=HarmoniaView,atlas=HarmoniaAtlas,{query}=HarmoniaReader;
  const frame=document.getElementById('companion-frame');let previous='';
  const reveal=()=>{const section=document.getElementById('passage-'+view.state.passage);pane.scrollTop+=section.getBoundingClientRect().top-pane.getBoundingClientRect().top-12;};
  document.fonts.ready.then(reveal);
  view.subscribe(s=>{
    const passage=atlas.passages.find(p=>p.id===s.passage),slice=atlas.slices[passage.slice];
    document.getElementById('author-document').value=s.source;
    for(const section of pane.querySelectorAll('[data-passage]')){section.hidden=section.dataset.document!==s.source;section.classList.toggle('selected-passage',section.dataset.passage===s.passage);}
    if(previous!==s.passage){reveal();previous=s.passage;}
    const mode=!slice?'chapter':s.companion==='workflow'&&!['financing','transfer'].includes(slice.id)?'diagram':s.companion;
    for(const b of document.querySelectorAll('[data-companion]')){b.hidden=(!slice&&b.dataset.companion!=='chapter')||(b.dataset.companion==='workflow'&&!['financing','transfer'].includes(slice?.id));b.setAttribute('aria-pressed',String(b.dataset.companion===mode));}
    let url;
    if(mode==='diagram')url=query('reviewer.html',{slice:slice.id,embed:1});
    else if(mode==='code')url=query('code.html',{file:slice.nodes[0].file,embed:1});
    else if(mode==='workflow')url=query(`chapters/${slice.chapter}.html`,{step:0,embed:1});
    else url=query(`chapters/${passage.chapter}.html`,{view:'read',embed:1});
    if(s.detail)url=new URL(s.detail,new URL(view.config.base,location.href)).href;
    const embedded=new URL(url);embedded.searchParams.set('embed','1');for(const [key,fallback] of [['theme','light'],['text','standard'],['audience','explorer']]){if(s[key]===fallback)embedded.searchParams.delete(key);else embedded.searchParams.set(key,s[key]);}url=embedded.href;
    let current='';try{current=frame.contentWindow.location.href;}catch{}
    if(current!==url)frame.src=url;
    document.getElementById('companion-title').textContent=passage.title;
    document.getElementById('companion-boundary').textContent=slice?slice.gap:'This passage is preserved as original context. Its inclusion does not establish implementation, publication, or adoption.';
    const external=new URL(url);external.searchParams.delete('embed');document.getElementById('companion-open').href=external.href;
  });
  document.getElementById('author-document').addEventListener('change',e=>view.update({source:e.target.value,passage:'',detail:''}));
  for(const b of document.querySelectorAll('[data-select-passage]'))b.addEventListener('click',()=>view.update({passage:b.dataset.selectPassage,detail:''}));
  for(const section of pane.querySelectorAll('[data-passage]'))section.addEventListener('click',e=>{if(!e.target.closest('a,button,summary'))view.update({passage:section.dataset.passage,detail:''},{replace:true});});
  for(const b of document.querySelectorAll('[data-companion]'))b.addEventListener('click',()=>view.update({companion:b.dataset.companion,detail:''}));
  // Interactions in a reused view become part of the parent's shareable URL.
  frame.addEventListener('load',()=>{try{const child=frame.contentWindow.HarmoniaView;if(child)child.subscribe(()=>{const base=new URL(view.config.base,location.href),u=new URL(frame.contentWindow.location.href),detail=u.pathname.slice(base.pathname.length)+u.search+u.hash;if(detail!==view.state.detail)view.update({detail},{replace:true});});}catch{}});
})();

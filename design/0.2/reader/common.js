// Small shared reader primitives; source and workflow semantics stay in their data owners.
HarmoniaView.mounts.push(() => {
  const view=window.HarmoniaView, atlas=window.HarmoniaAtlas;
  const node=(tag,text='',css='')=>{const n=document.createElement(tag);n.textContent=text;n.className=css;return n;};
  const link=(text,path)=>{const a=node('a',text);a.href=view.href(path);return a;};
  const query=(page,values)=>{const u=new URL(view.href(page));for(const [key,value] of Object.entries(values))if(value!==undefined&&value!=='')u.searchParams.set(key,value);return u.href;};
  function diagram(root,key,selected='') {
    const slice=atlas.slices[key];if(!root||!slice)return;
    diagramValue(root,slice,selected);
  }
  function diagramValue(root,slice,selected="") {
    renderHarmoniaDiagram(root,JSON.stringify({title:slice.title,caption:slice.relationship,conversation:slice.conversation,nodes:slice.nodes.map(n=>({id:n.id,label:n.label,detail:n.detail,actor:n.actor,state:n.id===selected?'current':'pending'})),edges:slice.edges.map(([from,to])=>({from,to,state:to===selected?'current':'pending'}))}));
  }
  function render(root,kind,json){
    if(root.dataset.renderer!==kind){root.replaceChildren(node('div'));root.dataset.renderer=kind;pruneHarmoniaScenes();pruneHarmoniaDiagrams();}
    (kind==='scene'?renderHarmoniaScene:renderHarmoniaDiagram)(root.firstElementChild,json);
  }
  function chapterDiagram(root,chapter){
    if(atlas.chapterDiagrams[chapter])render(root,'diagram',JSON.stringify(atlas.chapterDiagrams[chapter]));
    else if(atlas.chapterSlices[chapter])diagram(root,atlas.chapterSlices[chapter]);
  }
  function citation(slice){const [source,start,end]=slice.citation;return link(`Original ${source} · lines ${start}–${end}`,`sources/${source}.html?view=source#L${start}`);}
  function carousel(nav,paths,selected,select){
    nav.classList.add('has-carousel');
    let root=nav.querySelector('.step-carousel');
    if(!root){root=node('div');nav.insertBefore(root,nav.lastElementChild);}
    renderHarmoniaCarousel(root,JSON.stringify({paths,selected}),select);
  }
  const loading=new Map();
  async function source(path){
    if(window.HarmoniaSourceFiles?.[path])return window.HarmoniaSourceFiles[path];
    if(!loading.has(path))loading.set(path,new Promise((resolve,reject)=>{
      const script=document.createElement('script'),file=atlas.files[path];
      script.src=query(`reader/files/${file.id}.js`,{v:file.sha256.slice(0,12)});
      script.onload=()=>resolve(window.HarmoniaSourceFiles[path]);script.onerror=()=>{loading.delete(path);script.remove();reject(Error('The exported source file could not be loaded. Rebuild this edition and try again.'));};document.head.append(script);
    }));
    return loading.get(path);
  }
  // Object keys have one order at every depth. Array order is part of the observation.
  const ordered=value=>Array.isArray(value)?value.map(ordered):value&&typeof value==='object'?Object.fromEntries(Object.keys(value).sort().map(key=>[key,ordered(value[key])])):value;
  const json=value=>JSON.stringify(ordered(value),null,2);
  window.HarmoniaReader={node,link,query,diagram,diagramValue,citation,source,json,render,chapterDiagram,carousel};
  view.subscribe(s=>{document.documentElement.dataset.embed=String(s.embed);document.documentElement.dataset.presentation=String(s.present);});
  for(const root of document.querySelectorAll('[data-slice-diagram],[data-chapter-diagram]')) {
    const slice=root.dataset.sliceDiagram||atlas.chapterSlices[root.dataset.chapterDiagram];
    if(root.dataset.chapterDiagram)chapterDiagram(root,root.dataset.chapterDiagram);
    else if(slice)diagram(root,slice);
  }
});

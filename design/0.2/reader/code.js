(() => {
  const tree=document.getElementById('file-tree');if(!tree)return;
  const view=HarmoniaView,atlas=HarmoniaAtlas,{node,link,query,diagram,source,citation}=HarmoniaReader;
  const code=document.getElementById('source-code'),map=document.getElementById('source-diagram'),notes=document.getElementById('source-notes');
  let loaded='',treeKey='',request=0;
  function fileTree(s) {
    const key=[s.q,s.scope].join('|');
    if(key!==treeKey||!tree.children.length) {
      treeKey=key;tree.replaceChildren();
      const files=Object.values(atlas.files).filter(f=>(s.scope==='all'||f.path.startsWith(s.scope+'/'))&&f.path.toLowerCase().includes(s.q.toLowerCase()));
      const branch=()=>({folders:new Map(),files:[]}),root=branch();
      for(const file of files) {
        let current=root;
        for(const part of file.path.split('/').slice(0,-1)){if(!current.folders.has(part))current.folders.set(part,branch());current=current.folders.get(part);}
        current.files.push(file);
      }
      function append(parent,folder,prefix='') {
        for(const [name,child] of folder.folders) {
          let label=name,descendant=child;
          while(!descendant.files.length&&descendant.folders.size===1){const [part,next]=[...descendant.folders][0];label+='/'+part;descendant=next;}
          const path=prefix+label,d=node('details'),title=node('summary',label);d.dataset.folder=path;d.open=!!s.q;d.append(title);parent.append(d);append(d,descendant,path+'/');
        }
        for(const file of folder.files){
          const a=link(file.path.split('/').at(-1),query('code.html',{file:file.path,line:1,scope:s.scope,q:s.q}));a.dataset.file=file.path;a.title=file.path;a.addEventListener('click',e=>{if(e.metaKey||e.ctrlKey)return;e.preventDefault();view.update({file:file.path,line:1,codeTab:'code'});});parent.append(a);
        }
      }
      append(tree,root);
      if(!files.length)tree.append(node('p','No source paths match this search.'));
    }
    for(const a of tree.querySelectorAll('[data-file]')) {
      const selected=a.dataset.file===s.file;a.setAttribute('aria-current',selected?'page':'false');
      if(selected){let d=a.closest('details');while(d){d.open=true;d=d.parentElement.closest('details');}}
    }
    const active=tree.querySelector('[aria-current=page]'),pane=tree;
    if(active){const a=active.getBoundingClientRect(),r=pane.getBoundingClientRect();if(a.top<r.top||a.bottom>r.bottom)pane.scrollTop+=a.top-r.top-pane.clientHeight/2;}
  }
  function describe(file,slice) {
    notes.replaceChildren(node('p',file.owner,'eyebrow'),node('h2',file.annotation.role||file.path.split('/').at(-1)),node('p',file.annotation.summary||'This file belongs to the '+file.owner.toLowerCase()+' area. It has no file-specific @book annotation yet.'));
    const stats=node('p',`${file.lines.toLocaleString()} lines · ${file.language}`,'reader-footnote');notes.append(stats);
    const download=link('Open the original file ↗','../../'+file.path);download.download=file.path.split('/').at(-1);notes.append(download);
    if(file.annotation.line)notes.append(link('Jump to its documentation',query('code.html',{file:file.path,line:file.annotation.line})));
    if(slice)notes.append(node('h3','This vertical slice'),node('p',slice.question),link('Review the slice diagram →',query('reviewer.html',{slice:slice.id})),citation(slice),node('h3','Boundary to remember'),node('p',slice.gap));
    const hash=node('details');hash.append(node('summary','Source fingerprint'),node('code',file.sha256));notes.append(hash);
  }
  view.subscribe(async s=>{
    fileTree(s);document.getElementById('file-search').value=s.q;document.getElementById('source-scope').value=s.scope;
    const file=atlas.files[s.file],slice=atlas.slices[file.annotation.slice||file.slices?.[0]];
    document.getElementById('source-name').textContent=s.file.split('/').at(-1);
    document.getElementById('source-location').textContent=s.file;
    document.getElementById('source-counts').textContent=`This checkout: ${atlas.counts.files.toLocaleString()} source files · ${atlas.counts.lines.toLocaleString()} lines · ${atlas.counts.annotated} file annotations. Generated files and private runtime state are excluded.`;
    for(const b of document.querySelectorAll('[data-code-tab]')){b.hidden=b.dataset.codeTab==='diagram'&&!slice;b.setAttribute('aria-pressed',String(b.dataset.codeTab===s.codeTab));}
    code.hidden=s.codeTab==='diagram'&&!!slice;map.hidden=!code.hidden;
    describe(file,slice);
    if(!map.hidden)diagram(map,slice.id,slice.nodes.find(n=>n.file===s.file)?.id);
    if(loaded!==s.file){
      const ticket=++request;loaded='';code.replaceChildren(node('p','Loading this source file…'));
      try {
        const data=await source(s.file);if(ticket!==request||view.state.file!==s.file)return;
        const fragment=document.createDocumentFragment();
        data.lines.forEach((html,i)=>{const row=node('div','','code-line');row.id='code-L'+(i+1);row.dataset.line=i+1;const a=link(String(i+1),query('code.html',{file:s.file,line:i+1}));a.setAttribute('aria-label',`Line ${i+1}`);a.addEventListener('click',e=>{if(e.metaKey||e.ctrlKey)return;e.preventDefault();view.update({line:i+1});});const content=node('code');content.innerHTML=html||' ';row.append(a,content);fragment.append(row);});
        code.replaceChildren(fragment);loaded=s.file;
      }catch(error){code.replaceChildren(node('p',error.message));return;}
    }
    for(const row of code.children)row.classList.toggle('selected-line',Number(row.dataset.line)===view.state.line);
    const selected=document.getElementById('code-L'+view.state.line);
    if(selected&&!code.hidden)code.scrollTop+=selected.getBoundingClientRect().top-code.getBoundingClientRect().top-80;
  });
  document.getElementById('file-search').addEventListener('input',e=>view.update({q:e.target.value},{replace:true}));
  document.getElementById('source-scope').addEventListener('change',e=>view.update({scope:e.target.value}));
  for(const button of document.querySelectorAll('[data-code-tab]'))button.addEventListener('click',()=>view.update({codeTab:button.dataset.codeTab}));
  map.addEventListener('harmonia-select',e=>{const file=atlas.files[view.state.file],slice=atlas.slices[file.annotation.slice||file.slices?.[0]],target=slice.nodes.find(n=>n.id===e.detail);if(target)view.update({file:target.file,line:1,codeTab:'code'});});
})();

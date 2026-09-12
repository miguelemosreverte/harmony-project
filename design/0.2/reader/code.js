HarmoniaView.mounts.push(() => {
  const tree=document.getElementById('file-tree');if(!tree)return;
  const view=HarmoniaView,atlas=HarmoniaAtlas,{node,diagram,source}=HarmoniaReader;
  const code=document.getElementById('source-code'),map=document.getElementById('source-diagram'),notes=document.getElementById('source-notes');
  let loaded='',request=0;
  if(view.state.embed){tree.parentElement.remove();document.querySelector('.quiet-return').remove();}
  else {
    const branch=()=>({folders:new Map(),files:[]}),root=branch();
    for(const file of Object.values(atlas.files)){
      let current=root;for(const part of file.path.split('/').slice(0,-1)){if(!current.folders.has(part))current.folders.set(part,branch());current=current.folders.get(part);}current.files.push(file);
    }
    function append(parent,folder,prefix=''){
      for(const [name,child] of folder.folders){let label=name,descendant=child;while(!descendant.files.length&&descendant.folders.size===1){const [part,next]=[...descendant.folders][0];label+='/'+part;descendant=next;}const d=node('details'),title=node('summary',label);d.dataset.folder=prefix+label;d.append(title);parent.append(d);append(d,descendant,prefix+label+'/');}
      for(const file of folder.files){const a=node('a',file.path.split('/').at(-1));a.href=view.href('code.html?file='+encodeURIComponent(file.path));a.dataset.file=file.path;a.title=file.path;a.onclick=e=>{if(e.metaKey||e.ctrlKey)return;e.preventDefault();view.update({file:file.path,line:1});};parent.append(a);}
    }
    append(tree,root);
  }
  view.subscribe(async s=>{
    const file=atlas.files[s.file],context=atlas.contexts[file.context],slice=atlas.slices[file.annotation.slice||file.slices?.[0]||context.slice];
    for(const a of tree.querySelectorAll('[data-file]')){const active=a.dataset.file===s.file;a.setAttribute('aria-current',active?'page':'false');if(active){let d=a.closest('details');while(d){d.open=true;d=d.parentElement.closest('details');}}}
    const active=tree.querySelector('[aria-current=page]');if(active){const a=active.getBoundingClientRect(),r=tree.getBoundingClientRect();if(a.top<r.top||a.bottom>r.bottom)tree.scrollTop+=a.top-r.top-tree.clientHeight/2;}
    document.getElementById('source-name').textContent=s.file.split('/').at(-1);document.getElementById('source-location').textContent=s.file;
    notes.replaceChildren(node('p',file.annotation.summary||context.summary,'source-purpose'),node('p',`${context.title} · ${file.lines} lines · ${file.language}`,'citation'));
    if(slice){diagram(map,slice.id,slice.nodes.find(n=>n.file===s.file)?.id);}
    else map.replaceChildren(node('p',context.summary));
    document.getElementById('source-counts').textContent=`${atlas.counts.files} source files · ${atlas.counts.lines.toLocaleString()} lines. Exact source is exported from this checkout. SHA-256 ${file.sha256}`;
    if(loaded!==s.file){const ticket=++request;loaded='';code.setAttribute('aria-busy','true');try{const data=await source(s.file);if(ticket!==request||view.signal.aborted||!code.isConnected)return;const fragment=document.createDocumentFragment();data.lines.forEach((html,i)=>{const row=node('div','','code-line');row.id='code-L'+(i+1);row.dataset.line=i+1;const number=node('span',String(i+1),'line-number'),content=node('code');content.innerHTML=html||' ';row.append(number,content);fragment.append(row);});code.replaceChildren(fragment);loaded=s.file;code.removeAttribute('aria-busy');}catch(error){code.replaceChildren(node('p',error.message));return;}}
    for(const row of code.children)row.classList.toggle('selected-line',Number(row.dataset.line)===s.line);
    code.scrollTop=0;if(s.line>1){const selected=document.getElementById('code-L'+s.line);code.scrollTop=selected.offsetTop-code.offsetTop-50;}
  });
});

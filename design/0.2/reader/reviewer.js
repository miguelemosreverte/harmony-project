(() => {
  const root=document.getElementById('review-diagram');if(!root)return;
  const view=HarmoniaView,atlas=HarmoniaAtlas,{node,link,query,diagram,citation}=HarmoniaReader;
  const choice=document.getElementById('review-slice');
  for(const slice of Object.values(atlas.slices)){const option=node('option',slice.title);option.value=slice.id;choice.append(option);}
  choice.addEventListener('change',()=>view.update({slice:choice.value,node:''}));
  root.addEventListener('harmonia-select',e=>view.update({node:e.detail}));
  view.subscribe(s=>{
    const slice=atlas.slices[s.slice],selected=slice.nodes.find(n=>n.id===s.node)||slice.nodes[0];
    choice.value=s.slice;diagram(root,slice.id,selected.id);document.getElementById('review-status').textContent=slice.status;
    const detail=document.getElementById('review-node');detail.replaceChildren(node('p',selected.actor,'eyebrow'),node('h2',selected.label),node('p',selected.detail),link(selected.file,query('code.html',{file:selected.file})),citation(slice));
    const evidence=document.getElementById('review-evidence');evidence.replaceChildren(node('h2','What to challenge'),node('p',slice.gap),link('Inspect the supporting evidence ↗','../../'+slice.evidence),link('Read the use-case explanation →',`chapters/${slice.chapter}.html?view=read`),link('Compare the original passage →',query('author.html',{source:slice.citation[0],passage:atlas.passages.find(p=>p.source===slice.citation[0]&&p.start<=slice.citation[1]&&p.end>=slice.citation[1])?.id})));
  });
})();

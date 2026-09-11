(() => {
  const root=document.getElementById('review-diagram');if(!root)return;
  const view=HarmoniaView,atlas=HarmoniaAtlas,{node,link,query,diagramValue,citation}=HarmoniaReader;
  const choice=document.getElementById('review-slice'),relationship=document.getElementById('review-relationship');
  relationship.addEventListener('change',()=>view.update({relationship:relationship.value,node:''}));
  for(const slice of Object.values(atlas.slices)){const option=node('option',slice.title);option.value=slice.id;choice.append(option);}
  choice.addEventListener('change',()=>view.update({slice:choice.value,node:''}));
  root.addEventListener('harmonia-select',e=>view.update({node:e.detail}));
  view.subscribe(s=>{
    const slice=atlas.slices[s.slice],graph=s.relationship==='reading'?slice:atlas.relationships[s.relationship][s.slice],selected=graph.nodes.find(n=>n.id===s.node)||graph.nodes.find(n=>!graph.edges.some(([,to])=>to===n.id))||graph.nodes[0];
    relationship.replaceChildren();for(const [key,label] of [['reading','Suggested reading order'],['dependencies','Daml manifest dependencies'],['execution','Execution handoffs']]){if(key==='reading'||atlas.relationships[key][s.slice]){const option=node('option',label);option.value=key;option.selected=s.relationship===key;relationship.append(option);}}
    choice.value=s.slice;diagramValue(root,graph,selected.id);document.getElementById('review-status').textContent=graph.status;
    const detail=document.getElementById('review-node');detail.replaceChildren(node('p',selected.actor,'eyebrow'),node('h2',selected.label),node('p',selected.detail),link(selected.file,query('code.html',{file:selected.file})),citation(slice));
    for(const reference of selected.related||[])detail.append(link(reference.file,query('code.html',{file:reference.file})));
    const evidenceFile=graph.evidence||slice.evidence;
    const support=link('Inspect the supporting evidence ↗',atlas.files[evidenceFile]?query('code.html',{file:evidenceFile}):'../../'+evidenceFile);if(!atlas.files[evidenceFile]){support.target='_blank';support.rel='noopener';}
    const evidence=document.getElementById('review-evidence');evidence.replaceChildren(node('h2','What to challenge'),node('p',slice.gap),support,link('Read the use-case explanation →',`chapters/${slice.chapter}.html?view=read`),link('Compare the original passage →',query('author.html',{source:slice.citation[0],passage:atlas.passages.find(p=>p.source===slice.citation[0]&&p.start<=slice.citation[1]&&p.end>=slice.citation[1])?.id})));
  });
})();

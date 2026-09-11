(() => {
  const root=document.getElementById('review-diagram');if(!root)return;
  const view=HarmoniaView,atlas=HarmoniaAtlas,{node,diagramValue}=HarmoniaReader;
  const trail=Object.values(atlas.slices).flatMap(slice=>['reading','dependencies','execution'].filter(r=>r==='reading'||atlas.relationships[r][slice.id]).map(relationship=>({slice:slice.id,relationship})));
  const address=entry=>view.href('reviewer.html?slice='+entry.slice+'&relationship='+entry.relationship);
  view.subscribe(s=>{
    const slice=atlas.slices[s.slice],graph=s.relationship==='reading'?slice:atlas.relationships[s.relationship][s.slice],index=trail.findIndex(e=>e.slice===s.slice&&e.relationship===s.relationship);
    document.getElementById('review-position').textContent=`${index+1} of ${trail.length}`;
    document.getElementById('review-title').textContent=slice.title;
    document.getElementById('review-question').textContent=slice.question;
    document.getElementById('review-status').textContent=graph.status;
    diagramValue(root,graph,s.node);
    const detail=document.getElementById('review-node');detail.replaceChildren(node('h2',({reading:'Read this path',dependencies:'What the manifests declare',execution:'What executes'})[s.relationship]));
    for(const item of graph.nodes){detail.append(node('h3',item.label),node('p',item.detail),node('p',item.file,'citation'));}
    const evidence=document.getElementById('review-evidence');evidence.replaceChildren(node('h2','What to challenge'),node('p',slice.gap),node('p','Evidence: '+(graph.evidence||slice.evidence),'citation'),node('p',`Original ${slice.citation[0]} · lines ${slice.citation[1]}–${slice.citation[2]}`,'citation'));
    const previous=document.getElementById('page-previous'),next=document.getElementById('page-next');previous.href=index?address(trail[index-1]):view.href('verify.html');previous.textContent=index?'← Previous relationship':'← Choose a reading path';next.href=index+1<trail.length?address(trail[index+1]):view.href('coverage.html');next.textContent=index+1<trail.length?'Next relationship →':'Finish with the evidence boundary →';
  });
})();

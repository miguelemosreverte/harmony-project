HarmoniaView.mounts.push(() => {
  const pane=document.getElementById('author-document-pane');if(!pane)return;
  const view=HarmoniaView,atlas=HarmoniaAtlas,frame=document.getElementById('companion-frame');
  let selected='',scrolling=false;
  const address=p=>view.href('author.html?passage='+p.id+'&source='+p.source);
  view.subscribe(s=>{
    const index=atlas.passages.findIndex(p=>p.id===s.passage),passage=atlas.passages[index],slice=atlas.slices[passage.slice];
    document.getElementById('author-position').textContent=`Passage ${index+1} of ${atlas.passages.length}`;
    for(const section of pane.children)section.classList.toggle('selected-passage',section.dataset.passage===s.passage);
    if(selected!==s.passage&&!scrolling){const section=document.getElementById('passage-'+s.passage);pane.scrollTop+=section.getBoundingClientRect().top-pane.getBoundingClientRect().top-16;}selected=s.passage;
    const kind=passage.chapter==='03-financing-to-offer'?'purchase-approved':passage.chapter==='04-four-party-transfer'?'transfer-approved':null;
    if(kind)HarmoniaReader.render(frame,'scene',projectHarmoniaRecording(JSON.stringify(HarmoniaRecordings[kind]),2,'all'));
    else if(HarmoniaAtlas.chapterDiagrams[passage.chapter])HarmoniaReader.chapterDiagram(frame,passage.chapter);
    else if(slice)HarmoniaReader.render(frame,'diagram',JSON.stringify({title:slice.title,caption:slice.relationship,nodes:slice.nodes.map(n=>({...n,state:'pending'})),edges:slice.edges.map(([from,to])=>({from,to,state:'pending'}))}));
    document.getElementById('companion-title').textContent=passage.title;
    document.getElementById('companion-boundary').textContent=slice?slice.gap:'This original context is preserved. Inclusion does not establish implementation or adoption.';
    const previous=document.getElementById('page-previous'),next=document.getElementById('page-next');previous.href=index?address(atlas.passages[index-1]):view.href('verify.html');previous.textContent=index?'← Previous passage':'← Choose a reading path';next.href=index+1<atlas.passages.length?address(atlas.passages[index+1]):view.href('coverage.html');next.textContent=index+1<atlas.passages.length?'Next passage →':'See the quotation coverage →';
    for(const a of [previous,next])a.onclick=e=>{const p=new URL(a.href).searchParams.get('passage');if(p&&!e.metaKey&&!e.ctrlKey){e.preventDefault();view.update({passage:p});}};
  });
  let pending;
  pane.addEventListener('scroll',()=>{clearTimeout(pending);pending=setTimeout(()=>{const y=pane.getBoundingClientRect().top+24;const current=[...pane.children].reduce((best,n)=>Math.abs(n.getBoundingClientRect().top-y)<Math.abs(best.getBoundingClientRect().top-y)?n:best);if(current.dataset.passage!==selected){scrolling=true;view.update({passage:current.dataset.passage},{replace:true});scrolling=false;}},160);},{passive:true,signal:view.signal});
  view.signal.addEventListener('abort',()=>clearTimeout(pending),{once:true});
});

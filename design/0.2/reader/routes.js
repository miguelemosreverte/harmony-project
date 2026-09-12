// Decisions about the next chapter belong in its dock, beside a persistent explanation.
HarmoniaView.mounts.push(() => {
  const view=HarmoniaView,{node,chapterDiagram}=HarmoniaReader;
  const kind=view.config.kind,main=document.querySelector('main');
  const choices=main.querySelector('.quiet-choices');
  if(choices&&!['welcome','entrance'].includes(kind)){
    const chapter=kind==='atlas-workflows'?'01-product':kind==='historical-entry'?(location.pathname.endsWith('application-builder.html')?'05-bring-an-application':'06-compose-a-workflow'):kind==='evidence-choice'?'07-evidence-and-boundaries':({financing:'02-roles-and-trust',composer:'06-compose-a-workflow',packages:'05-bring-an-application'}[view.state.task]||'01-product');
    const stage=node('div','','reader-stage');main.insertBefore(stage,choices);chapterDiagram(stage,chapter);
    const dock=node('nav','','route-dock');dock.setAttribute('aria-label','Choose the next part');
    const label=node('span','Continue with','carousel-path-label');dock.append(label);
    choices.querySelectorAll('a').forEach(a=>{const title=a.querySelector('h2').textContent;a.textContent=title+' →';a.className='route-option';dock.append(a);});
    choices.replaceWith(dock);
  }
  if(view.config.kind==='chapter'&&!Object.keys(view.config.stories).length){
    const chapterIds=Object.keys(view.config.chapters),index=chapterIds.indexOf(view.config.slug);
    const paths=[{id:'chapters',label:'Chapters',steps:chapterIds.map((id,i)=>({id,label:view.config.chapters[id],state:i<index?'complete':i===index?'current':'pending'}))}];
    for(const nav of main.querySelectorAll('.quiet-paging'))HarmoniaReader.carousel(nav,paths,view.config.slug,id=>view.go(view.href('chapters/'+id+'.html')));
  }
});

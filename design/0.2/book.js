// A finite sequence of observed scenes. No selectors, disclosures or clickable progress dots.
HarmoniaView.mounts.push(() => {
  const view=HarmoniaView,{node}=HarmoniaReader;
  const enter=document.getElementById('sandbox-enter');
  if(enter){const task=view.state.task,copy={financing:['Make the handoff yourself.','Open Bank and approve the private financing case. Then open Buyer and use that approval to continue.','live-handoff','workflows.html'],composer:['Agree on a workflow.','Bank proposes one plan. Buyer consents, then each participant executes its assigned action.','composer-direct','chapters/07-evidence-and-boundaries.html'],packages:['Inspect a real application.','Open Bank and choose Bring an application. Inspect the DAR, review the supported mapping, then compile the project.','package-builder','chapters/06-compose-a-workflow.html']}[task];document.querySelector('main h1').textContent=copy[0];document.querySelector('main .lead').textContent=copy[1];enter.href=view.href('laboratory.html?story='+copy[2]);const back=enter.nextElementSibling;back.href=view.href(copy[3]);back.textContent='Continue reading →';}
  if(enter&&window.HarmoniaLiveRoot){const u=new URL(HarmoniaLiveRoot);u.searchParams.set('view',view.state.task);enter.href=u.href;enter.textContent='Enter the live workspace →';}
  const scene=document.getElementById('story-scene'),laboratory=document.getElementById('laboratory-stage');
  if(!scene&&!laboratory)return;
  const previous=document.getElementById('previous-step'),next=document.getElementById('next-step');
  const move=(url)=>view.go(view.href(url));
  let back,forward;
  view.subscribe(s=>{
    const story=view.config.stories[s.story],units=story.presentation.units;
    if(scene){
      const purchase=story.presentation.kind==='Purchase';
      const sequence=purchase?(s.story==='purchase-approved'?[0,2,5,8]:[0,...units.flatMap((u,i)=>u.actual.outcome==='committed'||u.id==='rejected-financing'?[i+1]:[])]):[0,...units.flatMap((u,i)=>u.actual.outcome==='committed'||u.id==='settle'?[i+1]:[])];
      if(!sequence.includes(units.length))sequence.push(units.length);
      const index=Math.max(0,sequence.findLastIndex(n=>n<=s.step));
      const frame=JSON.parse(projectHarmoniaRecording(JSON.stringify(story),s.step,'all'));
      renderHarmoniaScene(scene,JSON.stringify(frame));
      document.getElementById('recorded-step').textContent=`${index+1} of ${sequence.length}`;

      const decision=purchase?'purchase-outcome.html':'transfer-outcome.html';
      back=()=>index>1?view.update({step:sequence[index-1]}):move(index===0?'workflows.html':decision);
      forward=()=>s.step===0?move(decision):index+1<sequence.length?view.update({step:sequence[index+1]}):move(purchase?'chapters/04-four-party-transfer.html':'chapters/05-bring-an-application.html');
      previous.textContent=index===0?'← Product overview':index===1?'← Recorded outcomes':'← Previous scene';
      next.textContent=s.step===0?'Follow a recorded outcome →':index+1<sequence.length?'Next scene →':'Continue the book →';
    } else {
      const keys=Object.keys(view.config.stories),storyIndex=keys.indexOf(s.story),step=Math.min(s.step,units.length-1),unit=units[step];
      document.getElementById('laboratory-title').textContent=story.title;
      document.getElementById('laboratory-position').textContent=`Example ${storyIndex+1} of ${keys.length} · observation ${step+1} of ${units.length}`;
      if(['Purchase','Transfer'].includes(story.presentation.kind))HarmoniaReader.render(laboratory,'scene',projectHarmoniaRecording(JSON.stringify(story),step+1,'all'));
      else HarmoniaReader.render(laboratory,'diagram',projectHarmoniaDiagram(JSON.stringify(story),step));
      document.getElementById('laboratory-action').textContent=`${unit.actor}: ${unit.action.replaceAll('-',' ')} · observed ${unit.actual.outcome||unit.outcome_label}.`;
      const detail=document.getElementById('laboratory-observation');detail.replaceChildren(node('p',story.differences?.length?'This recording differs from its expectation.':'Recorded result matches the committed expectation.','citation'));
      const columns=node('div','','observed-comparison');for(const [title,value] of [['Committed expectation',unit.expected],['Recorded observation',unit.actual]]){const column=node('section');column.append(node('h3',title),node('pre',HarmoniaReader.json(value)));columns.append(column);}detail.append(columns);
      back=()=>step?view.update({step:step-1}):storyIndex?view.update({story:keys[storyIndex-1],step:view.config.stories[keys[storyIndex-1]].presentation.units.length-1}):move('workflows.html');
      forward=()=>step+1<units.length?view.update({step:step+1}):storyIndex+1<keys.length?view.update({story:keys[storyIndex+1],step:0}):move('coverage.html');
      previous.textContent=step?'← Previous observation':storyIndex?'← Previous example':'← Product overview';next.textContent=step+1<units.length?'Next observation →':storyIndex+1<keys.length?'Next example →':'Finish the recorded review →';
    }
    document.getElementById('recording-provenance').textContent=`Recorded on Canton · ${story.provenance.revision.slice(0,12)} · playback only.`;
  });
  if(view.state.embed)return;
  previous.onclick=()=>back();next.onclick=()=>forward();
});

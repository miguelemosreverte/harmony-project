// A retained stage with directly addressable observations and explicit outcome paths.
HarmoniaView.mounts.push(() => {
  const view=HarmoniaView,{node,carousel}=HarmoniaReader;
  const enter=document.getElementById('sandbox-enter');
  if(enter){const task=view.state.task,copy={financing:['Make the handoff yourself.','Open Bank and approve the private financing case. Then open Buyer and use that approval to continue.','live-handoff','workflows.html'],composer:['Agree on a workflow.','Bank proposes one plan. Buyer consents, then each participant executes its assigned action.','composer-direct','chapters/07-evidence-and-boundaries.html'],packages:['Inspect a real application.','Open Bank and choose Bring an application. Inspect the DAR, review the supported mapping, then compile the project.','package-builder','chapters/06-compose-a-workflow.html']}[task];document.querySelector('main h1').textContent=copy[0];document.querySelector('main .lead').textContent=copy[1];enter.href=view.href('laboratory.html?story='+copy[2]);const back=enter.nextElementSibling;back.href=view.href(copy[3]);back.querySelector('h2').textContent='Continue reading';}
  if(enter&&window.HarmoniaLiveRoot){const u=new URL(HarmoniaLiveRoot);u.searchParams.set('view',view.state.task);enter.href=u.href;enter.querySelector('h2').textContent='Enter the live workspace';enter.querySelector('p').textContent='Use your provisioned participant session.';}
  const scene=document.getElementById('story-scene'),laboratory=document.getElementById('laboratory-stage');
  if(!scene&&!laboratory)return;
  const previous=document.getElementById('previous-step'),next=document.getElementById('next-step');
  const move=url=>view.go(view.href(url));
  const labels={'bank-assessment':'Bank decides','make-proposal':'Alice proposes','buyer-agent-relays':'Ben relays','seller-agent-receives':'Sofia receives','rejected-financing':'Offer refused','seller-agrees':'Agree trade','seller-locks':'Lock assets','source-confirms':'Source ready','destination-prepares':'Permit receipt','destination-confirms':'Destination ready','settle':'Settle'};
  const featured={
    'purchase-approved':{stops:[0,2,5,7,8],sharedPrefix:0,outcome:'complete'},
    'purchase-rejected':{stops:[0,1,3],sharedPrefix:1,outcome:'refused'},
    'transfer-approved':{stops:[0,1,2,3,4,5,8],sharedPrefix:0,outcome:'complete'},
    'transfer-final-leg-rejected':{stops:[0,1,2,3,4,5,6],sharedPrefix:6,outcome:'refused'}
  };
  let back,forward;
  view.subscribe(s=>{
    const story=view.config.stories[s.story],units=story.presentation.units;
    if(scene){
      const purchase=story.presentation.kind==='Purchase';
      const sequences=Object.fromEntries(Object.keys(view.config.stories).map(key=>[key,[...new Set([...featured[key].stops,...(key===s.story?[s.step]:[])])].sort((a,b)=>a-b)]));
      const sequence=sequences[s.story],index=sequence.indexOf(s.step);
      const frame=JSON.parse(projectHarmoniaRecording(JSON.stringify(story),s.step,'all'));
      renderHarmoniaScene(scene,JSON.stringify(frame));
      const paths=Object.entries(sequences).map(([key,stops])=>({id:key,label:featured[key].outcome==='complete'?'Approval path':'Refusal path',sharedPrefix:featured[key].sharedPrefix,outcome:featured[key].outcome,steps:stops.map((step,i)=>{
        const unit=view.config.stories[key].presentation.units[step-1];
        return {id:key+':'+step,label:step===0?'Begin':labels[unit.id]||unit.id.replaceAll('-',' '),state:unit?.actual.outcome==='rejected'||unit?.actual.application==='rejected'?'refused':key===s.story&&i<index?'complete':key===s.story&&i===index?'current':'pending'};
      })}));
      carousel(previous.parentElement,paths,s.story+':'+s.step,id=>{const [story,step]=id.split(':');view.update({story,step:Number(step)});});
      back=()=>index?view.update({step:sequence[index-1]}):move('workflows.html');
      forward=()=>index+1<sequence.length?view.update({step:sequence[index+1]}):move(purchase?'chapters/04-four-party-transfer.html':'chapters/05-bring-an-application.html');
      previous.textContent=index?'← Previous scene':'← Product overview';next.textContent=index+1<sequence.length?'Next scene →':'Continue the book →';
    } else {
      const keys=Object.keys(view.config.stories),storyIndex=keys.indexOf(s.story),step=Math.min(s.step,units.length-1),unit=units[step];
      document.getElementById('laboratory-title').textContent=story.title;
      document.getElementById('laboratory-position').textContent=`Example ${storyIndex+1} of ${keys.length} · observation ${step+1} of ${units.length}`;
      if(['Purchase','Transfer'].includes(story.presentation.kind))HarmoniaReader.render(laboratory,'scene',projectHarmoniaRecording(JSON.stringify(story),step+1,'all'));
      else HarmoniaReader.render(laboratory,'diagram',projectHarmoniaDiagram(JSON.stringify(story),step));
      document.getElementById('laboratory-action').hidden=true;
      carousel(previous.parentElement,[{id:s.story,label:'Observations',steps:units.map((u,i)=>({id:String(i),label:u.id.replaceAll('-',' '),state:u.actual.outcome==='rejected'?'refused':i<step?'complete':i===step?'current':'pending'}))}],String(step),id=>view.update({step:Number(id)}));
      const detail=document.getElementById('laboratory-observation');detail.replaceChildren(node('p',story.differences?.length?'This recording differs from its expectation.':'Recorded result matches the committed expectation.','citation'));
      const columns=node('div','','observed-comparison');for(const [title,value] of [['Committed expectation',unit.expected],['Recorded observation',unit.actual]]){const column=node('section');column.append(node('h3',title),node('pre',HarmoniaReader.json(value)));columns.append(column);}detail.append(columns);
      back=()=>step?view.update({step:step-1}):storyIndex?view.update({story:keys[storyIndex-1],step:view.config.stories[keys[storyIndex-1]].presentation.units.length-1}):move('workflows.html');
      forward=()=>step+1<units.length?view.update({step:step+1}):storyIndex+1<keys.length?view.update({story:keys[storyIndex+1],step:0}):move('coverage.html');
      previous.textContent=step?'← Previous observation':storyIndex?'← Previous example':'← Product overview';next.textContent=step+1<units.length?'Next observation →':storyIndex+1<keys.length?'Next example →':'Finish the recorded review →';
    }
    const provenance=document.getElementById('recording-provenance');
    if(provenance)provenance.textContent=`Recorded on Canton · ${story.provenance.revision.slice(0,12)} · playback only.`;
  });
  if(view.state.embed)return;
  previous.onclick=()=>back();next.onclick=()=>forward();
});

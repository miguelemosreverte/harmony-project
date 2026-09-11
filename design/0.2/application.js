// Deterministic task simulations. A URL can reproduce every illustrated state.
(() => {
  'use strict';
  const view=window.HarmoniaView, node=id=>document.getElementById(id);
  if (view.config.kind==='workspace') {
    const tasks={
      2:{actor:'Bank',title:'Review financing',description:'Review the assessment in the financing application, then record the sample decision.',action:'Approve financing',next:'After approval, the buyer can create an offer.',event:'Bank approved financing.'},
      3:{actor:'Buyer',title:'Create the offer',description:'The sample financing is approved. Use that result to create the offer in the property application.',action:'Create offer',next:'After the offer is created, the seller can review it.',event:'Buyer created the offer.'},
      4:{actor:'Seller',title:'Review the offer',description:'Review the sample offer in the property application. Private financing documents are not part of this handoff.',action:'Accept offer',next:'Acceptance completes this simplified sample.',event:'Seller accepted the offer.'}
    };
    const messages={ready:'',pending:'Submission pending. Wait for an observed result before submitting again.',refused:'The source application refused the action. Progress has not advanced.',stale:'The observed state changed. A fresh observation is required before retrying.',disconnected:'Connection unavailable. Actions stay disabled until state is observed again.',rejected:'Financing was refused. No offer was created.'};
    view.subscribe(s=>{
      const task=tasks[Math.min(s.step,4)], finished=s.step===5||s.state==='rejected', canAct=!finished&&s.state==='ready'&&s.actor===task.actor;
      node('actor').value=s.actor; node('simulation-state').value=s.state;
      node('action-context').textContent=finished?'Sample outcome':s.actor===task.actor?'Your next task':`Waiting for the ${task.actor.toLowerCase()}`;
      node('action-title').textContent=s.state==='rejected'?'Financing refused':finished?'The sample offer was accepted':task.title;
      node('action-description').textContent=finished?'You have reached the end of this sample. Start again to compare another decision, or explore the recorded ledger story below.':task.description;
      node('action-notice').textContent=messages[s.state];
      node('advance-workflow').disabled=!canAct;
      node('advance-workflow').hidden=finished;
      node('advance-workflow').textContent=task.action;
      node('reject-workflow').hidden=s.step!==2||finished;
      node('reject-workflow').disabled=!canAct;
      node('handoff-workflow').hidden=finished||s.actor===task.actor;
      node('handoff-workflow').textContent=`View as ${task.actor.toLowerCase()} →`;
      node('next-owner').textContent=finished?'No commands were submitted to the ledger.':task.next;
      document.querySelectorAll('[data-progress]').forEach(li=>li.classList.toggle('active',Number(li.dataset.progress)===s.step));
      const events=['Buyer submitted evidence.'];
      for(let index=2;index<s.step;index++) events.push(tasks[index].event);
      if(s.state==='rejected') events.push('Bank refused financing. No offer was created.');
      node('workflow-history').replaceChildren(...events.map(text=>{const li=document.createElement('li');li.textContent=text;return li;}));
    });
    node('actor').addEventListener('change',event=>view.update({actor:event.target.value}));
    node('simulation-state').addEventListener('change',event=>view.update({state:event.target.value}));
    node('advance-workflow').addEventListener('click',()=>{if(!node('advance-workflow').disabled)view.update({step:view.state.step+1});});
    node('reject-workflow').addEventListener('click',()=>{if(!node('reject-workflow').disabled)view.update({state:'rejected'});});
    node('handoff-workflow').addEventListener('click',()=>view.update({actor:tasks[Math.min(view.state.step,4)].actor}));
    node('reset-workflow').addEventListener('click',()=>view.update({step:2,state:'ready',actor:'Bank',view:'overview'}));
  }
  if(view.config.kind==='builder') {
    const phases={1:['Start with one application','Explore a documented sample archive and its approval action.','Inspect the sample →'],2:['Inspect the eligible action','The sample approval is a consuming choice that returns its source template. Actor and subject fields are available. These are documented fixture properties, not a fresh archive inspection.','Review the mapping →'],3:['Give the mapping business meaning','The bank is the actor. The application subject identifies the buyer. The approval choice returns the replacement source contract. The compiler must check the actual signatures.','Review build & export →'],4:['The next step needs a real build','Compile the adapter, run its independently committed golden, then export a portable project. Inspecting or exporting a package does not install it in the live catalog.','Sample complete']};
    view.subscribe(s=>{
      const unsupported=s.package==='unsupported'&&s.phase===2;
      node('sample-package').value=s.package;
      node('builder-stage').textContent=unsupported?'Unsupported sample':`Step ${s.phase} of 4`;
      node('builder-title').textContent=unsupported?'This argument shape is unsupported':phases[s.phase][0];
      node('builder-result').textContent=unsupported?'Nested choice arguments are outside the current generator. Choose the supported financing sample to continue. No adapter has been compiled or installed.':phases[s.phase][1];
      node('inspect-sample').textContent=unsupported?'Choose another sample':phases[s.phase][2];
      node('inspect-sample').disabled=unsupported||s.phase===4;
      node('builder-back').disabled=s.phase===1;
      node('builder-complete').hidden=s.phase!==4;
      document.querySelectorAll('[data-phase]').forEach(li=>li.classList.toggle('active',Number(li.dataset.phase)===s.phase));
    });
    node('sample-package').addEventListener('change',event=>view.update({package:event.target.value,phase:1}));
    node('inspect-sample').addEventListener('click',()=>{if(!node('inspect-sample').disabled)view.update({phase:view.state.phase+1});});
    node('builder-back').addEventListener('click',()=>view.update({phase:view.state.phase-1}));
  }
})();

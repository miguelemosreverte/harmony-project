// Read-only playback of pinned ledger evidence. The browser never invents an outcome.
(() => {
  'use strict';
  const view = window.HarmoniaView;
  if (!document.getElementById('story-select')) return;
  const node = id => document.getElementById(id);
  const names = {'assess-financing':'Finalize the financing assessment', 'forge-proposal':'Attempt to create a proposal directly', 'open-offer':'Prepare the offer', 'make-proposal':'Make the purchase proposal', 'receive-proposal':'Receive the proposal', 'relay-proposal':'Relay the proposal', 'agree-trade':'Agree to the trade', 'lock-position':'Lock the source position', 'confirm-source':'Confirm the source position', 'prepare-destination':'Prepare the receiving permission', 'confirm-destination':'Confirm destination readiness', 'withdraw-directly':'Attempt to bypass the coordinator', settle:'Request settlement'};
  const reasons = {'not-visible':'The required private contract is not visible to this actor.', unauthorized:'This actor does not have the required authority.', 'application-rejected':'The source application refused this action under its rules.', 'destination-rejected':'The destination refused receipt. The final transaction rolled back; earlier preparations remain.'};
  function fact(label, value) {
    const pair=document.createElement('div'), term=document.createElement('dt'), detail=document.createElement('dd');
    term.textContent=label; detail.textContent=String(value); pair.append(term,detail); return pair;
  }
  function observed(story, unit) {
    const result=document.createElement('dl');
    result.className='state-facts';
    if (!unit) {result.append(fact('Starting point',story.presentation.start_detail)); return result;}
    const actual=unit.actual;
    if (actual.source) {
      result.append(fact('Source · available',`${actual.source.available} TEST`),fact('Source · locked',`${actual.source.locked} TEST`),fact('Destination · received',`${actual.destination} TEST`));
      // Quantities are exact text from the recording. Bar lengths are only a visual aid.
      for (const [label, quantity] of [['Available',actual.source.available],['Locked',actual.source.locked],['Received',actual.destination]]) {
        const row=document.createElement('div'); row.className='quantity-row';
        const caption=document.createElement('span'); caption.textContent=label;
        const meter=document.createElement('meter'); meter.min=0; meter.max=10; meter.value=Number(quantity); meter.setAttribute('aria-label',`${label}: ${quantity} TEST`);
        row.append(caption,meter); result.append(row);
      }
    } else {
      result.append(fact('Financing decision',actual.application),fact('Offer',actual.offer),fact('Proposal',actual.proposal || 'none'));
    }
    result.append(fact('Workflow',actual.workflow));
    return result;
  }
  view.subscribe(s => {
    const story=view.config.stories[s.story], units=story.presentation.units, unit=units[s.step-1];
    node('story-select').value=s.story;
    document.querySelectorAll('[data-recording]').forEach(table=>{table.hidden=table.dataset.recording!==s.story;});
    const actors=['all',...new Set(units.map(u=>u.actor))];
    node('story-actor').replaceChildren(...actors.map(actor=>{const option=document.createElement('option');option.value=actor;option.textContent=actor==='all'?'Everyone':actor;return option;}));
    node('story-actor').value=s.actor;
    node('recorded-step').textContent=s.step ? `Action ${s.step} of ${units.length} · ${unit.actor}` : `Before action 1 of ${units.length}`;
    node('recorded-action').textContent=unit ? names[unit.action] || unit.action : story.presentation.start_detail;
    node('recorded-outcome').textContent=unit ? unit.actual.outcome === 'committed' ? 'Transaction committed' : 'Action refused' : 'Recorded setup';
    node('recorded-outcome').dataset.outcome=unit?.actual.outcome || 'setup';
    let explanation=unit ? unit.actual.outcome==='rejected' ? reasons[unit.actual.reason] || `The attempt was refused (${unit.actual.reason}). Observed state: ${unit.observed_state}.` : `${unit.actor}'s action was recorded. ${unit.action==='assess-financing' ? `The financing decision is ${unit.actual.application}.` : `The observed state is ${unit.observed_state}.`}` : 'The scenario begins after its declared setup. Next shows the first attempted action, including any refusal.';
    if (s.actor !== 'all') explanation += unit?.actor === s.actor ? ` This is ${s.actor}'s action.` : ` You are following ${s.actor}; this ${unit ? 'action belongs to '+unit.actor : 'is the shared starting point'}.`;
    node('recorded-explanation').textContent=explanation;
    node('recorded-state').replaceChildren(observed(story,unit));
    node('previous-step').disabled=s.step===0;
    node('next-step').disabled=s.step===units.length;
    node('next-step').textContent=s.step===units.length?'Recording complete':'Next action →';
    node('story-complete').hidden=s.step!==units.length;
    node('story-complete').textContent='You have reached the end of this recording. Compare the other scenario, return to the story, or continue along your reading path below.';
    const data = s.tab==='provenance' ? story.provenance : s.tab==='input' ? (unit ? story.input.actions[s.step-1] : story.input.setup) : unit ? unit[s.tab==='observed'?'actual':'expected'] : story[s.tab==='observed'?'actual':'expected'];
    node('recorded-json').textContent=JSON.stringify(data,null,2);
    node('inspector-caption').textContent={input:'Declared story input. These actions were supplied to the recorded run.',expected:'Independently committed golden expectation.',observed:'Observed result from the preserved ledger recording.',provenance:'Historical run metadata. This is not a fresh submission.'}[s.tab];
    document.querySelectorAll('[data-tab]').forEach(button=>{button.id=`evidence-${button.dataset.tab}`;button.tabIndex=button.dataset.tab===s.tab?0:-1;button.setAttribute('aria-selected',String(button.dataset.tab===s.tab));node('inspector-panel').setAttribute('aria-labelledby',`evidence-${s.tab}`);button.classList.toggle('active',button.dataset.tab===s.tab);});
  });
  document.querySelector('.inspector-tabs').addEventListener('keydown', event => {
    const tabs=[...document.querySelectorAll('[data-tab]')];
    const index=tabs.indexOf(document.activeElement);
    if(index<0||!['ArrowLeft','ArrowRight','Home','End'].includes(event.key))return;
    event.preventDefault();
    const next=event.key==='Home'?0:event.key==='End'?tabs.length-1:(index+(event.key==='ArrowRight'?1:-1)+tabs.length)%tabs.length;
    tabs[next].click();tabs[next].focus();
  });
  node('story-select').addEventListener('change',event=>view.update({story:event.target.value,step:0,actor:'all'}));
  node('story-actor').addEventListener('change',event=>view.update({actor:event.target.value}));
  node('previous-step').addEventListener('click',()=>view.update({step:view.state.step-1}));
  node('next-step').addEventListener('click',()=>view.update({step:view.state.step+1}));
  node('reset-story').addEventListener('click',()=>view.update({step:0}));
  document.querySelectorAll('[data-tab]').forEach(button=>button.addEventListener('click',()=>view.update({tab:button.dataset.tab})));
})();

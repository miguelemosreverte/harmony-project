// Project recorded observations into a shared, typed Scala/HTML scene.
(() => {
  'use strict';
  const view = window.HarmoniaView;
  const node = id => document.getElementById(id);
  if (window.HarmoniaLiveRoot) {
    document.querySelectorAll('.live-sandbox-link').forEach(a => {a.href=view.href('sandbox.html');});
    if (node('live-entry')) {
      const a=document.createElement('a'); a.href=window.HarmoniaLiveRoot; a.className='button primary'; a.textContent='Open your participant workspace →';
      const p=document.createElement('p'); p.textContent='Use the private launcher to open your bank or buyer session first. Keep each participant in its own tab.';
      node('live-entry').replaceChildren(a,p);
    }
  }
  if (window.HarmoniaLaboratory && node('chapter-evidence')) {
    const a=document.createElement('a');a.href=view.href(window.HarmoniaLaboratory);a.textContent='Open all recorded experiments →';node('chapter-evidence').append(a);
  }
  if (!node('story-select')) return;
  const names = {'assess-financing':'Assess financing','forge-proposal':'Attempt a direct proposal','open-offer':'Prepare the offer','make-proposal':'Make the proposal','receive-proposal':'Receive the proposal','relay-proposal':'Relay the proposal','agree-trade':'Agree the trade','lock-position':'Lock the position','confirm-source':'Confirm the source','prepare-destination':'Prepare the destination','confirm-destination':'Confirm readiness','withdraw-directly':'Attempt a direct withdrawal',settle:'Settle the trade'};
  const reasons = {'not-visible':'The required private contract is not visible to this actor.',unauthorized:'This actor does not have the required authority.','application-rejected':'The application refused this attempt under its rules.','destination-rejected':'The destination refused receipt. The final transaction rolled back; the earlier source lock remains.'};
  const mainAttempt = u => u.actual.outcome==='committed' || ['rejected-financing','settle'].includes(u.id);
  const purchaseBeats=['Documents','Approval','Proposal','Offer'];
  let sequence=[];
  const beatIndex=step=>{
    const exact=sequence.indexOf(step), next=sequence.findIndex(candidate=>candidate>step);
    return exact>=0?exact:next>=0?next:sequence.length-1;
  };
  function frame(story,unit,actor) {
    const transfer=!!story.input.setup.trade, setup=story.input.setup, a=unit?.actual;
    const refused=a?.outcome==='rejected';
    const person=(id,role,icon='person')=>({id,name:id,role,icon});
    let people,title,caption,phase=0,artifact='',amounts=[];
    if (transfer) {
      const t=setup.trade;
      people=[person(t.seller,'Owns the position'),person(t.source,'Source custodian','bank'),person(t.buyer,'Receives the asset'),person(t.destination,'Destination custodian','bank')];
      const available=a?.source.available ?? t.quantity, locked=a?.source.locked ?? '0', received=a?.destination ?? '0';
      phase=a?.trade==='settled'?4:a?.trade==='ready'?3:a?.source.locked!=='0'&&a?2:a?1:0;
      artifact=`${phase===4?'Received':Number(locked)>0?'Locked':'Available'} · ${phase===4?received:Number(locked)>0?locked:available} ${t.asset}`;
      amounts=[['Available at source',available],['Locked at source',locked],['Received at destination',received]].map(([label,value])=>({label,value:`${value} ${t.asset}`,fraction:Number(value)/Number(t.quantity)}));
      [title,caption]=!unit ? ['One trade. Four responsibilities.',`${t.seller} is transferring ${t.quantity} ${t.asset} to ${t.buyer}. The two custodians must prepare before settlement.`] : ({
        'agree-trade':['The seller agrees.',`${t.seller} accepts the trade. No assets have moved.`],
        'lock-position':['The position is locked.',`${t.quantity} ${t.asset} is reserved at the source. It is no longer available for another transfer.`],
        'confirm-source':['The source is ready.',`${t.source} confirms that the locked position can be withdrawn during settlement.`],
        'prepare-destination':['The receiving side prepares.',`${t.destination} creates the permission needed to receive the asset.`],
        'confirm-destination':['Both sides are ready.',`The destination confirms readiness. ${t.settler} can now request settlement.`],
        settle:['The asset arrives.',`One final transaction withdraws the locked position and records receipt of ${received} ${t.asset}.`]
      }[unit.action] || ['An additional attempt.',unit.observed_state]);
    } else {
      const b=setup.application,o=setup.offer;
      people=[person(b.bank,'Financing','bank'),person(b.buyer,'Buyer'),person(o.buyer_agent,'Buyer’s agent'),person(o.seller_agent,'Seller’s agent')];
      phase=a?.proposal==='received'?4:a?.proposal==='relayed'?3:a?.proposal==='draft'?2:a?.evidence_available?1:0;
      artifact=phase>=2?'Purchase proposal':phase===1?`Financing · ${a.application}`:'';
      [title,caption]=!unit ? [`${b.buyer} wants to make an offer.`,`Her financing application is with ${b.bank}. The property agents will need a verified result to move the offer forward.`] : ({
        'assess-financing':a?.application==='approved' ? [`${b.bank} approves. ${b.buyer} can make her proposal.`,`A verified result is available to ${b.buyer}. Her financing documents stay within the financing application.`] : [`${b.bank} declines the financing.`,`The result records a refusal. It cannot authorize a purchase proposal.`],
        'open-offer':[`${b.buyer} prepares the offer.`,`The property application is ready to check her financing result.`],
        'make-proposal':[`${b.buyer} makes her proposal.`,`The property application accepts the approved financing result and creates one proposal.`],
        'relay-proposal':[`${o.buyer_agent} relays the proposal.`,`The buyer’s agent passes the proposal to the seller’s agent.`],
        'receive-proposal':[`${o.seller_agent} receives the proposal.`,`The financing-to-offer workflow is complete. The property agents receive the proposal, without the private financing documents.`]
      }[unit.action] || ['An additional attempt.',unit.observed_state]);
    }
    if (refused) {
      title=unit.id==='rejected-financing'?'The offer cannot proceed.':unit.actual.reason==='destination-rejected'?'Settlement rolls back.':'This attempt is refused.';
      caption=unit.id==='rejected-financing'?'The financing decision is rejected, so the property application creates no proposal.':reasons[unit.actual.reason] || unit.observed_state;
    }
    return {kind:transfer?'transfer':'purchase',title,caption,people,phase,focus:actor==='all'?(unit?.actor || people[0].id):actor,artifact,refused,amounts};
  }
  view.subscribe(s => {
    const story=view.config.stories[s.story], units=story.presentation.units, unit=units[s.step-1];
    const purchase=!story.input.setup.trade;
    sequence=purchase && s.story==='purchase-approved' ? [0,2,5,8] : [0,...units.flatMap((u,i)=>mainAttempt(u)?[i+1]:[])];
    const current=beatIndex(s.step);
    const labels=purchase && s.story==='purchase-approved'?purchaseBeats:sequence.map((step,i)=>step===0?'Start':names[units[step-1].action]||`Step ${i}`);
    node('story-select').value=s.story;
    const currentFrame=frame(story,unit,s.actor);
    renderHarmoniaScene(node('story-scene'),JSON.stringify(currentFrame));
    node('story-scene').dataset.follow=s.actor;
    node('scene-detail').textContent=currentFrame.caption;
    node('recorded-step').textContent=`${current+1} / ${sequence.length}`;
    node('scene-dots').replaceChildren(...sequence.map((step,i)=>{
      const li=document.createElement('li'),b=document.createElement('button'),label=document.createElement('span');
      b.textContent=String(i+1);b.setAttribute('aria-label',`Scene ${i+1}: ${labels[i]}`);b.setAttribute('aria-current',i===current?'step':'false');
      b.addEventListener('click',()=>view.update({step}));label.textContent=labels[i];li.classList.toggle('active',i===current);li.append(b,label);return li;
    }));
    node('previous-step').hidden=false;node('next-step').hidden=false;node('reset-story').hidden=true;
    node('story-complete').hidden=true;
    const option=(value,label)=>{const o=document.createElement('option');o.value=String(value);o.textContent=label;return o;};
    node('story-actor').replaceChildren(...['all',...new Set(units.map(u=>u.actor))].map(actor=>option(actor,actor==='all'?'Current actor':actor)));node('story-actor').value=s.actor;
    node('evidence-action').replaceChildren(option(0,'Setup'),...units.map((u,i)=>option(i+1,`${i+1}. ${u.actor}: ${names[u.action]||u.action} · ${u.actual.outcome}`)));node('evidence-action').value=String(s.step);
    node('recorded-action').textContent=unit?`Recorded action ${s.step}: ${unit.actor} · ${names[unit.action]||unit.action}`:story.presentation.start_detail;
    node('recorded-outcome').textContent=unit?unit.actual.outcome:'Setup';
    node('recorded-outcome').dataset.outcome=unit?.actual.outcome||'setup';
    node('recorded-state').textContent=unit?.observed_state||'';
    document.querySelectorAll('[data-recording]').forEach(table=>{table.hidden=table.dataset.recording!==s.story;});
    const data=s.tab==='provenance'?story.provenance:s.tab==='input'?(unit?story.input.actions[s.step-1]:story.input.setup):unit?unit[s.tab==='observed'?'actual':'expected']:story[s.tab==='observed'?'actual':'expected'];
    node('recorded-json').textContent=JSON.stringify(data,null,2);
    node('inspector-caption').textContent={input:'Input supplied to the recorded run.',expected:'Independently committed golden expectation.',observed:'Actual ledger result from the preserved recording.',provenance:'Run identity and artifact fingerprints. Playback submits no new transactions.'}[s.tab];
    document.querySelectorAll('[data-tab]').forEach(b=>{b.id=`evidence-${b.dataset.tab}`;b.tabIndex=b.dataset.tab===s.tab?0:-1;b.setAttribute('aria-selected',String(b.dataset.tab===s.tab));b.classList.toggle('active',b.dataset.tab===s.tab);});node('inspector-panel').setAttribute('aria-labelledby',`evidence-${s.tab}`);
  });
  const move=delta=>view.update({step:sequence[(beatIndex(view.state.step)+delta+sequence.length)%sequence.length]});
  node('story-select').addEventListener('change',e=>view.update({story:e.target.value,step:0,actor:'all'}));
  node('story-actor').addEventListener('change',e=>view.update({actor:e.target.value}));
  node('evidence-action').addEventListener('change',e=>view.update({step:Number(e.target.value)}));
  node('previous-step').addEventListener('click',()=>move(-1));node('next-step').addEventListener('click',()=>move(1));node('reset-story').addEventListener('click',()=>view.update({step:0}));
  node('story-carousel').addEventListener('keydown',e=>{if(e.key==='ArrowRight'||e.key==='ArrowLeft'){e.preventDefault();move(e.key==='ArrowRight'?1:-1);}});
  let touch;
  node('story-carousel').addEventListener('touchstart',e=>{touch={x:e.touches[0].clientX,y:e.touches[0].clientY};},{passive:true});
  node('story-carousel').addEventListener('touchend',e=>{if(!touch)return;const dx=e.changedTouches[0].clientX-touch.x,dy=e.changedTouches[0].clientY-touch.y;if(Math.abs(dx)>55&&Math.abs(dx)>Math.abs(dy)*1.5)move(dx<0?1:-1);touch=null;},{passive:true});
  document.querySelectorAll('[data-tab]').forEach(b=>b.addEventListener('click',()=>view.update({tab:b.dataset.tab})));
  document.querySelector('.inspector-tabs').addEventListener('keydown',e=>{
    const tabs=[...document.querySelectorAll('[data-tab]')],i=tabs.indexOf(document.activeElement);if(i<0||!['ArrowLeft','ArrowRight','Home','End'].includes(e.key))return;e.preventDefault();const n=e.key==='Home'?0:e.key==='End'?tabs.length-1:(i+(e.key==='ArrowRight'?1:-1)+tabs.length)%tabs.length;tabs[n].click();tabs[n].focus();
  });
})();

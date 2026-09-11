// Sample state for the application concept. This file never calls the product API.
document.querySelector('.mobile-menu')?.addEventListener('click', event => {
  const open = document.getElementById('app-nav').classList.toggle('open');
  event.currentTarget.setAttribute('aria-expanded', String(open));
});

if (document.getElementById('advance-workflow')) {
  let step = 2;
  let rejected = false;
  const actor = document.getElementById('actor');
  const mode = document.getElementById('simulation-state');
  const advance = document.getElementById('advance-workflow');
  const reject = document.getElementById('reject-workflow');
  const history = ['Buyer submitted evidence — illustrative starting state.'];
  const actions = {
    2: {owner:'Bank', title:'Review financing', action:'Approve financing', source:'Financing', next:'Buyer', description:'Confirm the assessment in your application before sharing an approval.', outcome:'Bank approved financing'},
    3: {owner:'Buyer', title:'Create the offer', action:'Create offer', source:'Property offers', next:'Seller', description:'Use the signed approval to create an offer in the separately owned application.', outcome:'Buyer created the offer'},
    4: {owner:'Seller', title:'Review the offer', action:'Accept offer', source:'Property offers', next:'Complete', description:'Review the offer without receiving the buyer’s private financing documents.', outcome:'Seller accepted the offer'}
  };
  const set = (id, value) => { document.getElementById(id).textContent = value; };
  function render() {
    const action = actions[Math.min(step,4)];
    const finished = step === 5 || rejected;
    const canAct = actor.value === action.owner && mode.value === 'ready' && !finished;
    advance.disabled = !canAct;
    reject.disabled = !canAct || step !== 2;
    set('workflow-status', rejected ? 'Financing rejected' : step === 5 ? 'Complete · sample' : `Awaiting ${action.owner.toLowerCase()}`);
    set('action-owner', action.owner);
    set('action-context', finished ? 'This sample workflow has ended' : actor.value === action.owner ? 'The next action belongs to you' : `Waiting for ${action.owner.toLowerCase()}`);
    set('action-title', rejected ? 'Financing was rejected' : step === 5 ? 'The offer was accepted' : action.title);
    set('action-description', finished ? 'Reset the prototype to explore another path. No ledger commands were submitted.' : action.description);
    set('source-application', action.source); set('source-authority', action.owner); set('next-owner', action.next);
    advance.textContent = finished ? 'Workflow ended' : action.action;
    const messages = {ready:'Changing the illustrated actor is a design aid, not authentication.',pending:'Submission pending. Wait for an observed result; do not enable another command.',refused:'The source application refused the action. Progress has not advanced.',stale:'The observed state changed. Refresh the workflow before retrying.',disconnected:'Connection unavailable. Actions are disabled until state is observed again.'};
    set('action-notice', messages[mode.value]);
    for (const node of document.querySelectorAll('[data-progress]')) {
      const index = Number(node.dataset.progress);
      node.classList.toggle('current', index === step && !finished);
      node.classList.toggle('complete', index < step);
      node.querySelector('.progress-dot').textContent = index < step ? '✓' : String(index).padStart(2,'0');
    }
    const list = document.getElementById('workflow-history'); list.replaceChildren();
    for (const entry of history) { const li=document.createElement('li'); li.textContent=entry; list.append(li); }
    if (history.length > 1) {
      const list = document.getElementById('recent-activity'); list.replaceChildren();
      for (const entry of history.slice(-2)) { const li=document.createElement('li'); li.textContent=entry; list.append(li); }
    }
  }
  advance.addEventListener('click', () => { if (advance.disabled) return; history.push(`${actions[step].outcome} — simulation.`); step++; render(); });
  reject.addEventListener('click', () => { if (reject.disabled) return; rejected=true; history.push('Bank rejected financing; no offer was created — simulation.'); render(); });
  actor.addEventListener('change', render); mode.addEventListener('change', render);
  document.getElementById('reset-workflow').addEventListener('click', () => { window.location.assign('application.html'); });
  function showView(name) {
    if (!['overview','applications','history'].includes(name)) return;
    for (const panel of document.querySelectorAll('.app-panel')) panel.hidden = panel.id !== `view-${name}`;
    for (const control of document.querySelectorAll('[data-view]')) {
      const active = control.dataset.view === name;
      control.classList.toggle('active',active);
      if(active) control.setAttribute('aria-current','page'); else control.removeAttribute('aria-current');
    }
  }
  for (const control of document.querySelectorAll('[data-view]')) control.addEventListener('click', event => { event.preventDefault(); showView(control.dataset.view); });
  showView(location.hash.slice(1) || 'overview');
}

if (document.getElementById('inspect-sample')) {
  const select = document.getElementById('sample-package');
  const result = document.getElementById('builder-result');
  const next = document.getElementById('inspect-sample');
  let phase = 1;
  function resetBuilder() { phase=1; result.hidden=true; next.textContent='Inspect sample →'; next.disabled=false; updateStepper(); }
  function updateStepper() { for(const node of document.querySelectorAll('[data-phase]')) node.classList.toggle('active',Number(node.dataset.phase)===phase); }
  select.addEventListener('change', resetBuilder);
  next.addEventListener('click', () => {
    result.hidden=false;
    if(select.value==='unsupported') { result.textContent='Unsupported sample shape: nested choice arguments are outside the current generator. No project has been compiled or installed.'; return; }
    phase=Math.min(4,phase+1); updateStepper();
    const messages={
      2:'Sample inspection: a consuming approval choice returns its source template. Actor and subject fields are available. These are documented fixture properties, not a fresh DAR inspection.',
      3:'Mapping preview: the bank is the actor; the application subject identifies the buyer; the approval choice returns the replacement source contract. The compiler must check the actual signatures.',
      4:'Build and export boundary: the real builder must compile the adapter, run the independent golden, and export a portable project. This design prototype performs none of those operations. Inspecting or exporting a package does not install it into the live catalog.'
    };
    result.textContent=messages[phase]; next.textContent={2:'Review the mapping →',3:'Preview build & export →',4:'Design journey complete'}[phase];next.disabled=phase===4;
  });
}

// Three authored views of recorded values. No business rules execute in this reader.
export const escape = value => String(value).replace(/[&<>"']/g, c => ({
  '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;'
})[c]);

const drawings = {
  bank: '<path d="m4 12 20-9 20 9M7 15h34M10 19v18m9-18v18m10-18v18m9-18v18M5 41h38"/>',
  contract: '<path d="M13 5h17l8 8v30H13zM30 5v9h8M19 22h13m-13 7h13m-13 7h8"/>',
  workflow: '<rect x="4" y="6" width="15" height="12" rx="3"/><rect x="29" y="30" width="15" height="12" rx="3"/><path d="M12 18v18h17m-5-5 5 5-5 5"/>',
  adapter: '<rect x="5" y="6" width="16" height="25" rx="3"/><rect x="27" y="17" width="16" height="25" rx="3"/><path d="M11 13h4m-4 7h4m18 4h4m-4 7h4M21 18h6"/>',
  vault: '<rect x="5" y="7" width="38" height="34" rx="5"/><circle cx="25" cy="24" r="9"/><path d="M25 19v10m-5-5h10M10 15v5m0 10v5"/>'
};
const icon = kind => `<svg class="demo-icon" viewBox="0 0 48 48" aria-hidden="true">${drawings[kind]}</svg>`;
const card = (owner, title, value, detail, kind, state = 'pending') => `
  <section class="contract-card" data-state="${escape(state)}">
    ${icon(kind)}<div class="contract-copy"><p class="contract-owner">${escape(owner)}</p>
    <h3>${escape(title)}</h3><strong class="contract-value">${escape(value)}</strong>
    <p class="contract-detail">${escape(detail)}</p></div>
  </section>`;
const connection = (state, label) => `<div class="demo-connection" data-state="${escape(state)}" aria-label="${escape(label)}"><i></i></div>`;
const label = value => ({pending:'Pending', approved:'Approved', confirmed:'Confirmed', waiting:'Waiting', complete:'Complete', ready:'Ready', settled:'Settled'})[value] ?? value;

function coordinated(frame, joining) {
  const a = frame.observed;
  const approval = joining ? a.application : a.sources.approval;
  const review = joining ? a.review : a.sources.review;
  return card('Northbank · financing application', 'Financing', label(approval), joining ? 'Approval branch selected' : 'First step · StepAction', 'bank', approval === 'approved' ? 'complete' : 'pending')
    + connection(approval === 'approved' ? 'complete' : 'pending', 'Application progress is recorded in the workflow')
    + card(joining ? 'Harmonia · requires both actions' : 'Harmonia · consented plan', joining ? 'Join' : 'Workflow', label(a.workflow), `Next: ${a.enabled.join(', ') || 'none'}`, 'workflow', a.workflow === 'complete' ? 'complete' : a.outcome === 'rejected' ? 'refused' : 'pending')
    + connection(review === 'confirmed' ? 'complete' : 'pending', 'The independent review contributes to the same workflow')
    + card('Alice · review application', 'Review', label(review), joining ? 'Required before the join' : 'Second step · StepAction', 'contract', review === 'confirmed' ? 'complete' : 'pending');
}

function privacy(frame) {
  const a = frame.observed;
  const approved = a.application === 'approved', complete = a.workflow === 'complete';
  return card('Northbank · bank only', 'Private application', label(a.application), `Visible to ${a.visible_to.join(', ')}`, 'bank', approved ? 'complete' : 'pending')
    + connection(approved ? 'complete' : 'pending', 'Approval authorizes a scoped result')
    + card('Shared handoff', 'Approval for Alice', complete ? 'Used' : approved ? 'Available' : 'Not issued', complete ? 'Consumed by the continuation' : 'Private bank papers stay at Northbank', 'contract', approved ? 'complete' : 'pending')
    + connection(complete ? 'complete' : 'pending', 'Alice uses the approval to continue')
    + card('Alice · her action', 'Next workflow step', label(a.workflow), complete ? 'Continuation recorded' : 'Alice has not continued', 'workflow', complete ? 'complete' : 'pending');
}

function adapter(frame) {
  const a = frame.observed, build = frame.recording === 'package-builder';
  const approved = a.application === 'approved';
  return card('Existing application', 'Original Daml package', 'Unchanged', 'LegacyFinancing · Approve', 'contract')
    + connection(build ? 'complete' : approved ? 'complete' : 'pending', 'A typed adapter calls the existing choice')
    + card(build ? 'Build time · reviewed mapping' : 'Execution · Northbank only', 'Generated adapter', build ? (a.compiled ? 'Compiled' : 'Not compiled') : 'StepAction', build ? 'Source DAR preserved' : 'Calls the typed Approve choice', 'adapter', build || approved ? 'complete' : 'pending')
    + connection(approved ? 'complete' : 'pending', 'The application action and workflow step commit together')
    + card('On ledger · application + workflow', 'Coordinated result', build ? 'Not executed here' : `${label(a.application)} · ${label(a.workflow)}`, build ? 'Compilation is a separate demonstration' : approved ? 'Both changes committed' : 'Neither contract advanced', 'workflow', approved ? 'complete' : 'pending');
}

function settlement(frame) {
  const a = frame.observed, accepting = frame.setup.destination_receipts === 'accept';
  const settled = a.trade === 'settled';
  const rollback = a.reason === 'destination-rejected';
  const state = settled ? 'complete' : rollback ? 'refused' : 'pending';
  return card('Seller · source custodian', 'Locked at source', `${a.source.locked} TEST`, `${a.source.available} TEST available`, 'vault', settled ? 'complete' : 'pending')
    + connection(state, 'Withdraw in the settlement transaction')
    + card('Alice · settler', 'Daml settlement', rollback ? 'Rolled back' : label(a.trade), `Workflow: ${label(a.workflow)}`, 'workflow', state)
    + connection(state, 'Receive in the same settlement transaction')
    + card('Buyer · destination custodian', 'Received at destination', `${a.destination} TEST`, `Receiving rule: ${accepting ? 'accept' : 'reject'}`, 'vault', rollback ? 'refused' : settled ? 'complete' : 'pending');
}

export function diagram(demo, frame) {
  const views = {privacy, adapter, settlement, core: f => coordinated(f, true), composition: f => coordinated(f, false)};
  const body = views[demo.id](frame);
  const a = frame.observed;
  const outcome = a.outcome ?? (a.http === 200 ? 'compiled' : 'rejected');
  const action = frame.input.action;
  const actionName = {
    'approve-financing':'Approve application', 'publish-approval':'Continue workflow',
    generate:'Generate adapter', 'confirm-destination':'Confirm readiness', settle:'Settle',
    'choose-approve':'Choose approval', 'complete-join':'Complete join', 'confirm-review':'Confirm review',
    accept:'Accept plan', advance: frame.input.step === 'approval' ? 'Approve financing' : 'Confirm review'
  }[action] ?? action.split('-').join(' ');
  const actor = ({bank:'Northbank',buyer:'Alice'})[frame.input.actor] ?? frame.input.actor;
  return `<div class="demo-diagram" data-kind="${escape(demo.id)}" data-outcome="${escape(outcome)}">
    <div class="ledger-environment"><img src="../navigation/assets/canton.svg" width="106" height="26" alt="Canton"><span>${escape(demo.environment)}</span><img src="../navigation/assets/daml.png" width="67" height="16" alt="Daml"></div>
    <div class="contract-flow">${body}</div>
    <div class="demo-receipt" data-outcome="${escape(outcome)}"><span>${escape(actor)}</span><strong>${escape(actionName)}</strong><span class="receipt-outcome">${escape(outcome)}</span></div>
  </div>`;
}

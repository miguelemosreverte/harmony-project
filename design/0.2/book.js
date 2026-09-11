// Interactions for the design prototype. No service or ledger calls.
document.querySelector('.mobile-menu')?.addEventListener('click', event => {
  const nav = document.querySelector('.rail nav');
  const open = nav.classList.toggle('open');
  event.currentTarget.setAttribute('aria-expanded', String(open));
});

function revealCitation() {
  const target = document.getElementById(decodeURIComponent(location.hash.slice(1)));
  if (target?.tagName === 'DETAILS') target.open = true;
}
revealCitation();
window.addEventListener('hashchange', revealCitation);

const tabs = [...document.querySelectorAll('[role="tab"]')];
function selectTab(tab) {
  for (const other of tabs) {
    const selected = other === tab;
    other.setAttribute('aria-selected', String(selected));
    other.tabIndex = selected ? 0 : -1;
    document.getElementById(other.getAttribute('aria-controls')).hidden = !selected;
  }
}
for (const tab of tabs) {
  tab.addEventListener('click', () => selectTab(tab));
  tab.addEventListener('keydown', event => {
    const index = tabs.indexOf(tab);
    const next = event.key === 'ArrowRight' ? (index + 1) % tabs.length
      : event.key === 'ArrowLeft' ? (index + tabs.length - 1) % tabs.length
      : event.key === 'Home' ? 0 : event.key === 'End' ? tabs.length - 1 : -1;
    if (next >= 0) { event.preventDefault(); selectTab(tabs[next]); tabs[next].focus(); }
  });
}

if (document.getElementById('story-flow')) {
  let step = 2;
  const states = ['Submitted', 'Approved', 'Created', 'Reviewed'];
  const next = document.getElementById('next-step');
  function render() {
    for (const node of document.querySelectorAll('[data-step]')) {
      const index = Number(node.dataset.step);
      node.classList.toggle('current', index === step);
      node.querySelector('small').textContent = index <= step ? states[index - 1] : 'Waiting';
    }
    document.getElementById('step-count').textContent = `Step ${step} of 4`;
    next.disabled = step === 4;
    next.textContent = step === 4 ? 'Story complete' : 'Next step →';
  }
  next.addEventListener('click', () => { step = Math.min(4, step + 1); render(); });
  document.getElementById('reset-story').addEventListener('click', () => { step = 1; render(); });
  for (const button of document.querySelectorAll('[data-role]')) {
    button.addEventListener('click', () => {
      for (const other of document.querySelectorAll('[data-role]')) other.setAttribute('aria-pressed', String(other === button));
      document.getElementById('visibility-explanation').textContent = {
        Buyer: 'The buyer sees their own financing application and carries its signed approval into the offer.',
        Bank: 'A signed approval. The seller does not receive the buyer’s private financing documents.',
        Seller: 'The seller sees the offer and the required approval result. Private financing documents remain outside this view.'
      }[button.dataset.role];
    });
  }
}

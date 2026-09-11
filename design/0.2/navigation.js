(() => {
  'use strict';
  const view = window.HarmoniaView;
  const {config} = view;
  const byId = id => document.getElementById(id);
  const destination = slug => slug === 'coverage' || slug === 'application' ? `${slug}.html` : `chapters/${slug}.html`;
  const label = slug => config.chapters[slug] || {coverage:'Find the original requirements', application:'Try the workspace'}[slug];
  const setPressed = (selector, key, selected) => document.querySelectorAll(selector).forEach(button => {
    button.setAttribute('aria-pressed', String(button.dataset[key] === selected));
  });
  function link(slug, index) {
    const anchor = document.createElement('a');
    anchor.href = view.href(destination(slug));
    anchor.textContent = `${index + 1}. ${label(slug)}`;
    if (slug === config.slug) anchor.setAttribute('aria-current','page');
    return anchor;
  }
  view.subscribe(s => {
    document.documentElement.dataset.theme = s.theme;
    document.documentElement.dataset.text = s.text;
    if (byId('appearance-panel')) byId('appearance-panel').hidden = s.panel !== 'appearance';
    byId('appearance-toggle')?.setAttribute('aria-expanded', String(s.panel === 'appearance'));
    setPressed('button[data-theme]', 'theme', s.theme);
    setPressed('button[data-text]', 'text', s.text);
    byId('route-navigation')?.classList.toggle('open', s.nav === 'open');
    byId('menu-toggle')?.setAttribute('aria-expanded', String(s.nav === 'open'));
    const journey = config.journeys[s.audience];
    if (byId('route-label')) byId('route-label').textContent = journey.label;
    if (byId('reader-context')) byId('reader-context').textContent = journey.label;
    byId('route-links')?.replaceChildren(...journey.steps.map(link));
    document.querySelectorAll('[name=audience]').forEach(radio => { radio.checked = radio.value === s.audience; });
    if (byId('welcome-route')) byId('welcome-route').replaceChildren(...journey.steps.map(slug => {const li=document.createElement('li'); li.textContent=label(slug); return li;}));
    if (byId('start-route')) { byId('start-route').href=view.href(destination(journey.steps[0])); byId('start-route').textContent=`Start: ${label(journey.steps[0])} →`; }
    const position = journey.steps.indexOf(config.slug);
    const next = position < 0 ? journey.steps[0] : journey.steps[position + 1];
    document.querySelectorAll('.route-next').forEach(anchor => {
      anchor.href = view.href(next ? destination(next) : 'book-overview.html');
      anchor.textContent = next ? `Next: ${label(next)} →` : 'Reading path complete · choose another →';
    });
    document.querySelectorAll('.mode-panel').forEach(panel => { panel.hidden = panel.dataset.mode !== s.view; });
    document.querySelectorAll('[data-view]').forEach(button => {
      button.classList.toggle('active', button.dataset.view === s.view);
      if (button.closest('.mode-tabs')) button.setAttribute('aria-current', button.dataset.view === s.view ? 'page' : 'false');
    });
    // Rebuild link preferences on every render, including links opened in a new tab.
    const base = new URL(config.base, location.href);
    document.querySelectorAll('a[href]').forEach(anchor => {
      if (anchor.hasAttribute('data-exact-view') || anchor.getAttribute('href').startsWith('#')) return;
      const url = new URL(anchor.href);
      if (url.origin !== base.origin || !url.pathname.startsWith(base.pathname) || !url.pathname.endsWith('.html')) return;
      for (const key of ['audience','theme','text']) {
        if (s[key] === {audience:'explorer',theme:'light',text:'standard'}[key]) url.searchParams.delete(key);
        else url.searchParams.set(key,s[key]);
      }
      anchor.href=url.href;
    });
    const open = new Set(s.open.split(','));
    document.querySelectorAll('[data-disclosure]').forEach(details => {details.open = open.has(details.id);});
  });
  byId('print-page')?.addEventListener('click', () => window.print());
  byId('appearance-toggle')?.addEventListener('click', () => view.update({panel:view.state.panel === 'closed' ? 'appearance' : 'closed'}));
  byId('close-appearance')?.addEventListener('click', () => {view.update({panel:'closed'}); byId('appearance-toggle').focus();});
  byId('menu-toggle')?.addEventListener('click', () => view.update({nav:view.state.nav === 'closed' ? 'open' : 'closed'}));
  document.querySelectorAll('button[data-theme]').forEach(button => button.addEventListener('click', () => view.update({theme:button.dataset.theme})));
  document.querySelectorAll('button[data-text]').forEach(button => button.addEventListener('click', () => view.update({text:button.dataset.text})));
  document.querySelectorAll('[name=audience]').forEach(radio => radio.addEventListener('change', () => view.update({audience:radio.value})));
  document.querySelectorAll('[data-view]').forEach(button => button.addEventListener('click', event => {event.preventDefault(); view.update({view:button.dataset.view});}));
  document.querySelectorAll('[data-disclosure]').forEach(details => details.addEventListener('toggle', () => {
    if (document.documentElement.classList.contains('printing')) return;
    // Closing a parent also closes its nested audit disclosures.
    if (!details.open) details.querySelectorAll('[data-disclosure]').forEach(child => {child.open=false;});
    const open = [...document.querySelectorAll('[data-disclosure][open]')].map(node => node.id).sort().join(',');
    if (view.state.open !== open) view.update({open});
  }));
  byId('share-view')?.addEventListener('click', async () => {
    try { await navigator.clipboard.writeText(location.href); byId('share-status').textContent='Link copied. It includes this view and your reading preferences.'; }
    catch {byId('share-fallback').hidden=false; byId('share-url').value=location.href; byId('share-url').select(); byId('share-status').textContent='Copy the link below.';}
  });
  addEventListener('beforeprint', () => {
    document.documentElement.classList.add('printing');
    document.querySelectorAll('.source-section').forEach(section => {section.open=true;});
  });
  addEventListener('afterprint', () => {
    const open=new Set(view.state.open.split(','));
    document.querySelectorAll('[data-disclosure]').forEach(section => {section.open=open.has(section.id);});
    setTimeout(() => document.documentElement.classList.remove('printing'), 0);
  });
  document.addEventListener('keydown', event => {if (event.key === 'Escape' && view.state.panel === 'appearance') {view.update({panel:'closed'}); byId('appearance-toggle')?.focus();}});
})();

// Website navigation is outside the recorded presentation and its portable export.
(() => {
  const script=document.currentScript;
  const brand=document.querySelector('.reference-brand');
  if(!brand || brand.matches('a'))return;
  const style=document.createElement('link');style.rel='stylesheet';style.href=new URL('return.css',script.src);document.head.append(style);
  const home=new URL('../index.html',script.src);
  const route=location.pathname;
  const branch=script.dataset.branch || (route.endsWith('/code.html')?'implementation':route.includes('/architecture/')?'architecture':route.includes('/investor/')?'investor':'user');
  home.searchParams.set('branch',branch);
  const link=document.createElement('a');
  link.className=brand.className;link.href=home.href;
  link.textContent='‹ Harmonia';link.title='Choose a reading path';
  link.setAttribute('aria-label','Back to the four reading paths');
  link.dataset.home=branch;
  brand.replaceWith(link);
  for(const existing of document.querySelectorAll('a.quiet-return[href$="book-overview.html"]'))existing.href=home.href;
})();

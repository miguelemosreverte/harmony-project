// Preserve the prose position as well as the diagram camera in shared URLs.
const reading = document.querySelector('#reading');

function restoreReading() {
  const position = Number(new URL(location.href).searchParams.get('read') || 0);
  reading.scrollTop = Number.isFinite(position) ? Math.max(0, position) : 0;
}

reading.addEventListener('scroll', () => {
  const url = new URL(location.href);
  const position = Math.round(reading.scrollTop);
  if (position) url.searchParams.set('read', position);
  else url.searchParams.delete('read');
  history.replaceState(null, '', url);
}, {passive: true});

window.addEventListener('popstate', restoreReading);
restoreReading();

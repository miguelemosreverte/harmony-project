/* The book owns the passages; the existing shared renderer owns the connected stops. */
(async () => {
  if (typeof renderHarmoniaCarousel !== 'function') return;
  const root = document.getElementById('architecture-slides');
  const slides = [...root.querySelectorAll('.architecture-slide')];
  const previous = document.getElementById('slide-previous');
  const next = document.getElementById('slide-next');
  const progress = document.getElementById('architecture-progress');
  const announcement = document.getElementById('slide-announcement');
  const titles = slides.map(slide => slide.querySelector('h2').textContent);
  const clamp = value => Math.max(0, Math.min(slides.length - 1, value));
  let selected = 0;

  // Decode every illustration before enabling slide changes, avoiding an empty frame.
  await Promise.all(slides.map(slide => slide.querySelector('img').decode().catch(() => {})));

  function read() {
    const url = new URL(location.href);
    const value = url.searchParams.get('step');
    selected = /^\d+$/.test(value ?? '') ? clamp(Number(value)) : 0;
    url.searchParams.set('step', selected);
    if (url.href !== location.href) history.replaceState(null, '', url);
    for (const [index, slide] of slides.entries()) {
      const current = index === selected;
      slide.dataset.current = String(current);
      slide.inert = !current;
      slide.setAttribute('aria-hidden', String(!current));
      slide.setAttribute('aria-label', `${index + 1} of ${slides.length}: ${titles[index]}`);
    }
    previous.disabled = selected === 0;
    next.disabled = selected === slides.length - 1;
    previous.title = selected ? titles[selected - 1] : 'Beginning of architecture';
    next.title = selected < slides.length - 1 ? titles[selected + 1] : 'End of architecture';
    renderHarmoniaCarousel(progress, JSON.stringify({
      selected: String(selected),
      paths: [{
        id: 'architecture', label: 'Architecture',
        steps: titles.map((label, index) => ({
          id: String(index), label,
          state: index < selected ? 'complete' : index === selected ? 'current' : 'pending'
        }))
      }]
    }), id => select(Number(id)));
    document.title = titles[selected] + ' · Harmonia architecture';
    announcement.textContent = `Slide ${selected + 1} of ${slides.length}: ${titles[selected]}`;
  }

  function select(index) {
    const target = clamp(index);
    if (target === selected) return;
    const url = new URL(location.href);
    url.searchParams.set('step', target);
    history.pushState(null, '', url);
    read();
    root.scrollTop = 0;
  }

  previous.addEventListener('click', () => select(selected - 1));
  next.addEventListener('click', () => select(selected + 1));
  addEventListener('popstate', read);
  addEventListener('keydown', event => {
    if (event.defaultPrevented || event.altKey || event.ctrlKey || event.metaKey ||
        event.target.closest('input,textarea,select,.step-carousel')) return;
    const destination = {
      ArrowLeft: selected - 1, ArrowRight: selected + 1,
      Home: 0, End: slides.length - 1
    }[event.key];
    if (destination !== undefined) { event.preventDefault(); select(destination); }
  });

  let touch;
  root.addEventListener('touchstart', event => {
    touch = event.touches.length === 1
      ? {x: event.touches[0].clientX, y: event.touches[0].clientY} : null;
  }, {passive: true});
  root.addEventListener('touchcancel', () => { touch = null; }, {passive: true});
  root.addEventListener('touchend', event => {
    if (!touch) return;
    const dx = event.changedTouches[0].clientX - touch.x;
    const dy = event.changedTouches[0].clientY - touch.y;
    touch = null;
    if (Math.abs(dx) > 55 && Math.abs(dx) > Math.abs(dy) * 1.5)
      select(selected + (dx < 0 ? 1 : -1));
  }, {passive: true});

  read();
  document.documentElement.dataset.slides = 'true';
  document.querySelector('.quiet-paging').hidden = false;
  history.scrollRestoration = 'manual';

  // Print the complete textbook, including the passages inactive on screen.
  addEventListener('beforeprint', () => slides.forEach(slide => {
    slide.inert = false;
    slide.removeAttribute('aria-hidden');
  }));
  addEventListener('afterprint', read);
})();

import assert from 'node:assert/strict';

// Own one temporary tab. Existing reader tabs and the live ledger are untouched.
export async function capture(endpoint, entry) {
  for (const value of [endpoint, entry]) {
    assert(['127.0.0.1', 'localhost'].includes(new URL(value).hostname), 'Use a local reader');
  }
  assert(new URL(entry).pathname.endsWith('/laboratory.html'), 'Use the recorded laboratory');
  const tab = await (await fetch(new URL('/json/new?about:blank', endpoint), {method: 'PUT'})).json();
  const socket = new WebSocket(tab.webSocketDebuggerUrl);
  const pending = new Map();
  let sequence = 0;
  await new Promise((resolve, reject) => { socket.onopen = resolve; socket.onerror = reject; });
  socket.onmessage = event => {
    const message = JSON.parse(event.data);
    if (!message.id) return;
    const waiter = pending.get(message.id);
    if (!waiter) return;
    clearTimeout(waiter.timer);
    pending.delete(message.id);
    message.error ? waiter.reject(Error(message.error.message)) : waiter.resolve(message.result);
  };
  const call = (method, params = {}) => new Promise((resolve, reject) => {
    const id = ++sequence;
    const timer = setTimeout(() => { pending.delete(id); reject(Error(`Timed out: ${method}`)); }, 30000);
    pending.set(id, {resolve, reject, timer});
    socket.send(JSON.stringify({id, method, params}));
  });
  const evaluate = async expression => {
    const result = await call('Runtime.evaluate', {expression, awaitPromise: true, returnByValue: true});
    assert(!result.exceptionDetails, result.exceptionDetails?.exception?.description);
    return result.result.value;
  };
  try {
    await call('Page.enable');
    await call('Page.navigate', {url: entry});
    let ready = false;
    for (let attempt = 0; attempt < 100; attempt++) {
      ready = await evaluate('!!window.HarmoniaView?.config?.stories && !!document.querySelector(".support-art")');
      if (ready) break;
      await new Promise(resolve => setTimeout(resolve, 100));
    }
    assert(ready, 'The recorded reader did not load');
    const context = await evaluate(`({
      order: Object.keys(HarmoniaView.config.stories),
      passages: HarmoniaAtlas.passages,
      chapters: HarmoniaView.config.chapters
    })`);
    const stories = [];
    for (const id of context.order) {
      const story = await evaluate(`HarmoniaView.config.stories[${JSON.stringify(id)}]`);
      const moments = [];
      for (let index = 0; index < story.presentation.units.length; index++) {
        const moment = await evaluate(`(async () => {
          HarmoniaView.update({story: ${JSON.stringify(id)}, step: ${index}});
          const story = HarmoniaView.config.stories[HarmoniaView.state.story];
          const scene = ['Purchase', 'Transfer'].includes(story.presentation.kind);
          const frame = JSON.parse(scene
            ? projectHarmoniaRecording(JSON.stringify(story), ${index + 1}, 'all')
            : projectHarmoniaDiagram(JSON.stringify(story), ${index}));
          const stage = document.querySelector('#laboratory-stage');
          const art = stage.querySelector('.support-art');
          const url = getComputedStyle(art).backgroundImage.match(/url\\("?([^"\\)]+)"?\\)/)?.[1];
          if (!url) throw Error('Missing artwork');
          const image = new Image(); image.src = url; await image.decode();
          const dialogue = [...stage.querySelectorAll('.support-speaker')].map(speaker => ({
            name: speaker.querySelector('.support-name').textContent,
            text: speaker.querySelector('.support-speech').textContent
          }));
          const nav = [...document.querySelectorAll('.carousel-step')].map(step => ({
            id: step.dataset.stop, state: step.dataset.state
          }));
          return {renderer: scene ? 'scene' : 'diagram', frame, dialogue, nav,
            image: {id: art.dataset.illustration, description: art.getAttribute('aria-label'),
              url, width: image.naturalWidth, height: image.naturalHeight}};
        })()`);
        assert.equal(moment.image.id, moment.frame.conversation.illustration, `${id}: image differs`);
        assert.deepEqual(moment.dialogue, ['first', 'second'].map(key => {
          const {name, text} = moment.frame.conversation[key]; return {name, text};
        }), `${id}: dialogue differs`);
        moments.push(moment);
      }
      stories.push({story, moments});
    }
    const resources = await evaluate(`performance.getEntriesByType('resource').map(entry => entry.name)`);
    return {...context, stories, resources};
  } finally {
    for (const waiter of pending.values()) clearTimeout(waiter.timer);
    socket.close();
    await fetch(new URL('/json/close/' + tab.id, endpoint));
  }
}

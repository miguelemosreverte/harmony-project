// Uses its own temporary Chrome tab. No new browser, build or ledger process.
// node book/navigation/check.mjs <local-CDP-url> <sheet-url> [review-directory]
import assert from 'node:assert/strict';
import fs from 'node:fs/promises';
import path from 'node:path';

const [endpoint, address, output = '.artifacts/navigation-sheet-review'] = process.argv.slice(2);
for (const url of [endpoint, address]) assert(['localhost', '127.0.0.1'].includes(new URL(url).hostname));
await fs.mkdir(output, {recursive: true});
const target = await (await fetch(new URL('/json/new?about:blank', endpoint), {method: 'PUT'})).json();
const socket = new WebSocket(target.webSocketDebuggerUrl);
await new Promise((resolve, reject) => {socket.onopen = resolve; socket.onerror = reject;});
let sequence = 0;
const pending = new Map(), errors = [], checks = [];
socket.onmessage = event => {
  const message = JSON.parse(event.data);
  if (message.id) {
    const task = pending.get(message.id); pending.delete(message.id);
    message.error ? task.reject(Error(message.error.message)) : task.resolve(message.result);
  } else if (message.method === 'Runtime.exceptionThrown') errors.push(message.params.exceptionDetails);
};
const call = (method, params = {}) => new Promise((resolve, reject) => {
  const id = ++sequence; pending.set(id, {resolve, reject}); socket.send(JSON.stringify({id, method, params}));
});
const evaluate = async expression => {
  const result = await call('Runtime.evaluate', {expression, returnByValue: true, awaitPromise: true});
  assert(!result.exceptionDetails, JSON.stringify(result.exceptionDetails));
  return result.result.value;
};
const until = async expression => {
  for (let i = 0; i < 100; i++) {
    if (await evaluate(expression)) return;
    await new Promise(resolve => setTimeout(resolve, 50));
  }
  throw Error('Did not settle: ' + expression);
};
const navigate = async url => {
  await call('Page.navigate', {url});
  await until(`location.href === ${JSON.stringify(url)} && document.documentElement.dataset.sheet === 'interactive' && [...document.images].every(i => i.complete && i.naturalWidth > 0)`);
};
const inspect = () => evaluate(`(() => {
  const paper = document.querySelector('#paper'), viewport = document.querySelector('#viewport');
  const m = new DOMMatrix(getComputedStyle(paper).transform), v = viewport.getBoundingClientRect();
  const p = paper.getBoundingClientRect();
  return {url: location.href, x: (v.width / 2 - m.e) / m.a, y: (v.height / 2 - m.f) / m.a, z: m.a,
    width: innerWidth, height: innerHeight, scrollWidth: document.documentElement.scrollWidth, scrollHeight: document.documentElement.scrollHeight,
    viewport: {x:v.x,y:v.y,width:v.width,height:v.height}, paper: {x:p.x,y:p.y,width:p.width,height:p.height},
    controls: document.querySelectorAll('a,button,input,select,details').length,
    regions: document.querySelectorAll('.region').length, pages: document.querySelectorAll('.page').length,
    images: document.images.length, dragging: viewport.dataset.dragging === 'true'};
})()`);
const close = (a, b, tolerance = 0.1) => assert(Math.abs(a - b) < tolerance, `${a} != ${b}`);
const screenshot = async name => {
  const shot = await call('Page.captureScreenshot', {format: 'png'});
  await fs.writeFile(path.join(output, name + '.png'), Buffer.from(shot.data, 'base64'));
};
const key = key => call('Input.dispatchKeyEvent', {type: 'keyDown', key});
const worldAt = (state, x, y) => ({
  x: state.x + (x - state.viewport.x - state.viewport.width / 2) / state.z,
  y: state.y + (y - state.viewport.y - state.viewport.height / 2) / state.z
});

try {
  await call('Page.enable'); await call('Runtime.enable');
  await call('Emulation.setDeviceMetricsOverride', {width: 1440, height: 1000, deviceScaleFactor: 1, mobile: false});
  await navigate(address);
  const initial = await inspect();
  assert.equal(initial.controls, 0); assert.equal(initial.regions, 4); assert.equal(initial.pages, 12);
  const audiences = await evaluate(`({
    investorDemos:document.querySelectorAll('[data-branch=investor] .demo').length,
    architectureDiagrams:document.querySelectorAll('[data-branch=architecture] svg').length,
    technicalScreenshots:document.querySelectorAll('.region:not([data-branch=user]) img').length,
    productionFiles:[...document.querySelectorAll('[data-production-source]')].map(n=>n.dataset.productionSource)
  })`);
  assert.equal(audiences.investorDemos,3); assert.equal(audiences.architectureDiagrams,6);
  assert.equal(audiences.technicalScreenshots,0); assert.equal(audiences.productionFiles.length,6);
  assert(audiences.productionFiles.every(file=>file.startsWith('product/') && !file.includes('/src/test/')));
  assert.equal(initial.scrollHeight, 1000); assert.equal(initial.scrollWidth, 1440);
  assert(initial.paper.x >= initial.viewport.x && initial.paper.y >= initial.viewport.y);
  assert(initial.paper.x + initial.paper.width <= initial.viewport.x + initial.viewport.width + 1);
  assert(initial.paper.y + initial.paper.height <= initial.viewport.y + initial.viewport.height + 1);
  const textOverflow = await evaluate(`[...document.querySelectorAll('.page .excerpt-line, .page p')].flatMap(element => {
    const range = document.createRange(); range.selectNodeContents(element);
    const bounds = element.closest('.page').getBoundingClientRect();
    return [...range.getClientRects()].filter(r => r.right > bounds.right + 1 || r.left < bounds.left - 1).map(() => element.textContent);
  })`);
  assert.deepEqual(textOverflow, [], 'Text must remain inside its page');
  const diagramOverflow = await evaluate(`[...document.querySelectorAll('.diagram-node')].flatMap(node => {
    const box=node.querySelector('rect').getBoundingClientRect();
    return [...node.querySelectorAll('text')].filter(text=>{const r=text.getBoundingClientRect();return r.left<box.left-1||r.right>box.right+1||r.top<box.top||r.bottom>box.bottom+1}).map(text=>text.textContent);
  })`);
  assert.deepEqual(diagramOverflow, [], 'Diagram labels must fit their nodes');
  checks.push({check:'Canton demos, six architecture diagrams and production-only files',...audiences});
  checks.push({check: 'All regions fit on entry', ...initial});
  await screenshot('whole-sheet');
  await evaluate('window.originalPaper = document.querySelector("#paper")');

  // Zoom around the pointer, then drag and scroll the same mounted document.
  const pointer = {x: 650, y: 450};
  const anchor = worldAt(initial, pointer.x, pointer.y);
  await call('Input.dispatchMouseEvent', {type: 'mouseWheel', ...pointer, deltaX: 0, deltaY: -160, modifiers: 2});
  await until(`new DOMMatrix(getComputedStyle(document.querySelector('#paper')).transform).a > ${initial.z*1.2}`);
  const zoomed = await inspect(), nextAnchor = worldAt(zoomed, pointer.x, pointer.y);
  assert(zoomed.z > initial.z); close(anchor.x, nextAnchor.x); close(anchor.y, nextAnchor.y);
  await call('Input.dispatchMouseEvent', {type: 'mousePressed', x: 700, y: 500, button: 'left', clickCount: 1});
  assert((await inspect()).dragging);
  await call('Input.dispatchMouseEvent', {type: 'mouseMoved', x: 820, y: 580, button: 'left', buttons: 1});
  await call('Input.dispatchMouseEvent', {type: 'mouseReleased', x: 820, y: 580, button: 'left'});
  const dragged = await inspect();
  close((zoomed.x - dragged.x) * zoomed.z, 120); close((zoomed.y - dragged.y) * zoomed.z, 80);
  assert(!dragged.dragging);
  await call('Input.dispatchMouseEvent', {type: 'mouseWheel', x: 700, y: 500, deltaX: 50, deltaY: 70});
  await until(`Math.abs(new DOMMatrix(getComputedStyle(document.querySelector('#paper')).transform).f - ${dragged.paper.y - dragged.viewport.y}) > 10`);
  const scrolled = await inspect();
  close((scrolled.x - dragged.x) * dragged.z, 50); close((scrolled.y - dragged.y) * dragged.z, 70);
  assert(await evaluate('window.originalPaper === document.querySelector("#paper")'));
  checks.push({check: 'Pointer zoom, drag and wheel pan retain the document', ...scrolled});

  // The shared URL survives reload; Back and Forward restore camera states.
  await until('new URL(location.href).searchParams.has("z")');
  await new Promise(resolve => setTimeout(resolve, 200));
  const shared = await inspect();
  await navigate(shared.url);
  const restored = await inspect();
  for (const field of ['x', 'y', 'z']) close(shared[field], restored[field], field === 'z' ? 0.00002 : 0.1);
  await evaluate(`history.pushState(null, '', location.pathname); dispatchEvent(new PopStateEvent('popstate')); history.back()`);
  await until(`location.href === ${JSON.stringify(shared.url)}`);
  for (const field of ['x', 'y', 'z']) close(shared[field], (await inspect())[field], field === 'z' ? 0.00002 : 0.1);
  await evaluate('history.forward()');
  await until('location.search === ""');
  close((await inspect()).z, initial.z);
  checks.push({check: 'URL reload and browser history restore the camera'});

  for(let i=0;i<10 && (await inspect()).paper.width <= initial.width+200;i++) await key('+');
  const enlarged = await inspect(); assert(enlarged.z > initial.z);
  await key('ArrowRight'); assert((await inspect()).x > enlarged.x);
  await key('Home'); close((await inspect()).z, initial.z);
  checks.push({check: 'Keyboard zoom, pan and fit'});

  // Inspect each region as a reader would by centering the camera on it.
  const regions = await evaluate(`[...document.querySelectorAll('.region')].map(r => {
    const p = document.querySelector('#paper').getBoundingClientRect(), b = r.getBoundingClientRect();
    const z = new DOMMatrix(getComputedStyle(document.querySelector('#paper')).transform).a;
    return {id:r.dataset.branch,x:(b.x-p.x+b.width/2)/z,y:(b.y-p.y+b.height/2)/z,width:b.width/z,height:b.height/z};
  })`);
  for (const region of regions) {
    const url = new URL(address);
    for (const [key, value] of Object.entries({x: region.x, y: region.y, z: Math.min(1360 / region.width, 850 / region.height)})) url.searchParams.set(key, value);
    await navigate(url.href); await screenshot(region.id);
  }
  for (const [name, selector] of [['investor-detail','[data-figure="participants-demo"]'], ['architecture-detail','[data-figure="deployment"]']]) {
    const focus = await evaluate(`(() => {
      const p=document.querySelector('#paper').getBoundingClientRect(), r=document.querySelector(${JSON.stringify(selector)}).getBoundingClientRect();
      const z=new DOMMatrix(getComputedStyle(document.querySelector('#paper')).transform).a;
      return {x:(r.x-p.x+r.width/2)/z,y:(r.y-p.y+r.height/2)/z,z:Math.min(2,850/(r.height/z))};
    })()`);
    const url=new URL(address);
    for(const [key,value] of Object.entries(focus)) url.searchParams.set(key,value);
    await navigate(url.href); await screenshot(name);
  }

  // A real CDP two-finger gesture exercises the pointer/pinch handlers.
  await call('Emulation.setDeviceMetricsOverride', {width: 390, height: 844, deviceScaleFactor: 1, mobile: true});
  await call('Emulation.setTouchEmulationEnabled', {enabled: true, maxTouchPoints: 2});
  await navigate(address);
  const phone = await inspect();
  assert.equal(phone.scrollWidth, 390); assert.equal(phone.scrollHeight, 844);
  await screenshot('phone-whole');
  await call('Input.dispatchTouchEvent', {type: 'touchStart', touchPoints: [{x: 155, y: 400, id: 1}, {x: 235, y: 400, id: 2}]});
  await call('Input.dispatchTouchEvent', {type: 'touchMove', touchPoints: [{x: 75, y: 400, id: 1}, {x: 315, y: 400, id: 2}]});
  await call('Input.dispatchTouchEvent', {type: 'touchEnd', touchPoints: []});
  const pinched = await inspect(); close(pinched.z / phone.z, 3, 0.01); assert(!pinched.dragging);
  checks.push({check: 'Phone pinch gesture', before: phone, after: pinched});
  const detail = new URL(address); detail.search = '?x=1400&y=900&z=0.9';
  await navigate(detail.href); await screenshot('phone-detail');

  const malformed = new URL(address); malformed.search = '?x=NaN&y=1e999&z=-5';
  await navigate(malformed.href); close((await inspect()).z, phone.z);
  checks.push({check: 'Invalid camera values recover to the whole sheet'});

  // Printing unfolds the actual DOM, independent of the current camera.
  await call('Emulation.setEmulatedMedia', {media: 'print'});
  const printed = await evaluate(`({transform:getComputedStyle(document.querySelector('#paper')).transform,visible:[...document.querySelectorAll('.page')].filter(p=>p.getClientRects().length).length})`);
  assert.equal(printed.transform, 'none'); assert.equal(printed.visible, 12);
  checks.push({check: 'Print includes all pages without a camera transform'});
  assert.deepEqual(errors, []);
  await fs.writeFile(path.join(output, 'checks.json'), JSON.stringify({checks, errors}, null, 2) + '\n');
  console.log(JSON.stringify({checks: checks.length, screenshots: 9, errors: errors.length, output}));
} finally {
  socket.close(); await fetch(new URL('/json/close/' + target.id, endpoint));
}

// The camera moves; the document and its content never remount or change layout.
const viewport = document.querySelector('#viewport');
const paper = document.querySelector('#paper');
const zoomLabel = document.querySelector('#zoom');
document.documentElement.dataset.sheet = 'interactive';

let camera = {x: 0, y: 0, z: 1};
let fitting = true;
let saveTimer;
const pointers = new Map();
let pinch;

function fitScale() {
  return Math.min(1, (viewport.clientWidth - 32) / paper.offsetWidth,
    (viewport.clientHeight - 32) / paper.offsetHeight);
}

function paint() {
  const min = fitScale();
  camera.z = Math.max(min, Math.min(3, camera.z));
  for (const [axis, length, available] of [
    ['x', paper.offsetWidth, viewport.clientWidth],
    ['y', paper.offsetHeight, viewport.clientHeight]
  ]) {
    const half = available / (2 * camera.z);
    camera[axis] = length * camera.z <= available - 32 ? length / 2
      : Math.max(half - 48 / camera.z, Math.min(length - half + 48 / camera.z, camera[axis]));
  }
  const x = viewport.clientWidth / 2 - camera.x * camera.z;
  const y = viewport.clientHeight / 2 - camera.y * camera.z;
  paper.style.transform = `translate(${x}px, ${y}px) scale(${camera.z})`;
  zoomLabel.textContent = `${Math.round(camera.z * 100)}%`;
}

function save() {
  clearTimeout(saveTimer);
  saveTimer = setTimeout(() => {
    const url = new URL(location.href);
    for (const key of ['x', 'y', 'z']) url.searchParams.set(key, camera[key].toFixed(key === 'z' ? 5 : 2));
    history.replaceState(null, '', url);
  }, 150);
}

function fit() {
  fitting = true;
  camera = {x: paper.offsetWidth / 2, y: paper.offsetHeight / 2, z: fitScale()};
  paint();
}

function restore() {
  clearTimeout(saveTimer);
  const params = new URL(location.href).searchParams;
  const values = ['x', 'y', 'z'].map(key => params.get(key));
  if (values.some(value => value === null || value.trim() === '' || !Number.isFinite(Number(value))) || Number(values[2]) <= 0) {
    fit();
    return;
  }
  fitting = false;
  camera = {x: Number(values[0]), y: Number(values[1]), z: Number(values[2])};
  paint();
}

function point(clientX, clientY) {
  const bounds = viewport.getBoundingClientRect();
  return {x: clientX - bounds.left - bounds.width / 2, y: clientY - bounds.top - bounds.height / 2};
}

function zoomAt(z, anchor) {
  const next = Math.max(fitScale(), Math.min(3, z));
  camera.x += anchor.x / camera.z - anchor.x / next;
  camera.y += anchor.y / camera.z - anchor.y / next;
  camera.z = next;
  fitting = false;
  paint();
  save();
}

viewport.addEventListener('wheel', event => {
  event.preventDefault();
  const unit = event.deltaMode === 1 ? 16 : event.deltaMode === 2 ? viewport.clientHeight : 1;
  if (event.ctrlKey || event.metaKey) {
    zoomAt(camera.z * Math.exp(-event.deltaY * unit * 0.008), point(event.clientX, event.clientY));
  } else {
    const dx = event.shiftKey && !event.deltaX ? event.deltaY : event.deltaX;
    const dy = event.shiftKey && !event.deltaX ? 0 : event.deltaY;
    camera.x += dx * unit / camera.z;
    camera.y += dy * unit / camera.z;
    fitting = false;
    paint();
    save();
  }
}, {passive: false});

function beginPinch() {
  if (pointers.size !== 2) { pinch = undefined; return; }
  const [a, b] = [...pointers.values()];
  const middle = {x: (a.x + b.x) / 2, y: (a.y + b.y) / 2};
  pinch = {distance: Math.max(1, Math.hypot(a.x - b.x, a.y - b.y)), z: camera.z,
    world: {x: camera.x + middle.x / camera.z, y: camera.y + middle.y / camera.z}};
}

viewport.addEventListener('pointerdown', event => {
  if (event.pointerType === 'mouse' && event.button !== 0) return;
  viewport.focus({preventScroll: true});
  viewport.setPointerCapture(event.pointerId);
  pointers.set(event.pointerId, point(event.clientX, event.clientY));
  viewport.dataset.dragging = 'true';
  beginPinch();
});

viewport.addEventListener('pointermove', event => {
  if (!pointers.has(event.pointerId)) return;
  const before = pointers.get(event.pointerId), after = point(event.clientX, event.clientY);
  pointers.set(event.pointerId, after);
  if (pinch && pointers.size === 2) {
    const [a, b] = [...pointers.values()];
    camera.z = Math.max(fitScale(), Math.min(3, pinch.z * Math.hypot(a.x - b.x, a.y - b.y) / pinch.distance));
    camera.x = pinch.world.x - (a.x + b.x) / (2 * camera.z);
    camera.y = pinch.world.y - (a.y + b.y) / (2 * camera.z);
  } else if (pointers.size === 1) {
    camera.x -= (after.x - before.x) / camera.z;
    camera.y -= (after.y - before.y) / camera.z;
  }
  fitting = false;
  paint();
  save();
});

function release(event) {
  if (!pointers.delete(event.pointerId)) return;
  viewport.dataset.dragging = String(pointers.size > 0);
  beginPinch();
  save();
}
for (const name of ['pointerup', 'pointercancel', 'lostpointercapture']) viewport.addEventListener(name, release);
window.addEventListener('blur', () => {pointers.clear(); pinch = undefined; viewport.dataset.dragging = 'false';});

viewport.addEventListener('keydown', event => {
  if (event.ctrlKey || event.metaKey || event.altKey) return;
  const directions = {ArrowLeft: [-1, 0], ArrowRight: [1, 0], ArrowUp: [0, -1], ArrowDown: [0, 1]};
  if (directions[event.key]) {
    const [dx, dy] = directions[event.key];
    camera.x += dx * 100 / camera.z;
    camera.y += dy * 100 / camera.z;
    fitting = false;
    paint(); save();
  } else if (event.key === '+' || event.key === '=') zoomAt(camera.z * 1.25, {x: 0, y: 0});
  else if (event.key === '-') zoomAt(camera.z / 1.25, {x: 0, y: 0});
  else if (event.key === 'Home' || event.key === '0') {fit(); save();}
  else return;
  event.preventDefault();
});

window.addEventListener('popstate', restore);
window.addEventListener('resize', () => {if (fitting) fit(); else paint();});
restore();
viewport.focus({preventScroll: true});

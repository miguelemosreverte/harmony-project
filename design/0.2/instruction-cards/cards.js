import {drawing} from './drawings.js';

const name = document.body.dataset.card;
const response = await fetch('cards.json');
if (!response.ok) throw Error('The instruction card could not be loaded');
const card = (await response.json())[name];
if (!card) throw Error('Unknown instruction card');
document.title = `${card.label} · Harmonia instruction card`;
document.querySelector('.branch').textContent = card.label;
document.querySelector('.context').textContent = card.context;
document.querySelector('h1').textContent = card.title;
const panels = document.querySelector('.panels');
const rail = document.querySelector('.rail');
const status = document.querySelector('#status');
for (const [index, step] of card.steps.entries()) {
  const panel = document.createElement('section');
  panel.className = 'panel'; panel.id = `picture-${index}`;
  panel.setAttribute('aria-labelledby', `heading-${index}`);
  panel.innerHTML = `<div class="panel-heading"><span class="number">${index+1}</span><h2 id="heading-${index}"></h2></div><div class="picture"></div><p class="caption"></p>`;
  panel.querySelector('h2').textContent = step.title;
  panel.querySelector('.picture').innerHTML = drawing(step.drawing, step.caption);
  panel.querySelector('.caption').textContent = step.caption;
  panels.append(panel);
  const stop = document.createElement('button');
  stop.type = 'button'; stop.className = 'stop'; stop.setAttribute('role', 'tab');
  stop.setAttribute('aria-label', `${index+1}. ${step.title}`);
  stop.setAttribute('aria-controls', panel.id);
  stop.dataset.end = String(index === card.steps.length-1);
  stop.innerHTML = `<span aria-hidden="true">${index === card.steps.length-1 ? '✓' : ''}</span>`;
  stop.onclick = () => select(index);
  stop.onkeydown = event => {
    const target = ({ArrowRight:index+1,ArrowDown:index+1,ArrowLeft:index-1,ArrowUp:index-1,Home:0,End:card.steps.length-1})[event.key];
    if (target === undefined) return;
    event.preventDefault(); select(target); rail.children[selected].focus();
  };
  rail.append(stop);
}
let selected = 0;
function read() {
  const url = new URL(location.href);
  const value = url.searchParams.get('step');
  selected = /^\d+$/.test(value || '') ? Math.min(Number(value),card.steps.length-1) : 0;
  const normalized = new URL(location.href); normalized.search = `?step=${selected}`; normalized.hash = '';
  if (normalized.href !== location.href) history.replaceState(null,'',normalized);
  [...panels.children].forEach((panel,index) => panel.setAttribute('aria-current',String(index === selected)));
  [...rail.children].forEach((stop,index) => {
    stop.setAttribute('aria-selected',String(index === selected));
    stop.dataset.passed = String(index < selected); stop.tabIndex = index === selected ? 0 : -1;
  });
  status.textContent = selected === card.steps.length-1 ? card.finish : card.steps[selected].caption;
}
function select(index) {
  const next = Math.max(0,Math.min(index,card.steps.length-1));
  if (next === selected) return;
  const url = new URL(location.href); url.search = `?step=${next}`;
  history.pushState(null,'',url); read();
}
window.addEventListener('popstate',read);
let touch;
panels.addEventListener('touchstart',event => {const point=event.touches[0];touch={x:point.clientX,y:point.clientY};},{passive:true});
panels.addEventListener('touchend',event => {
  if (!touch) return;
  const point=event.changedTouches[0], dx=point.clientX-touch.x,dy=point.clientY-touch.y;
  if (Math.abs(dx)>45 && Math.abs(dx)>Math.abs(dy)*1.5) select(selected+(dx<0?1:-1));
  touch=null;
},{passive:true});
read();

// Final visual review at the reference dimensions, after responsive refinements.
import fs from 'node:fs/promises';
import assert from 'node:assert/strict';
import {browser} from './cdp.mjs';
const b=await browser(process.argv[2]);
const base=process.argv[3]||'http://127.0.0.1:56202/design/0.2/';
const output='design/0.2/infographic/reference-review';
const checks=[];const check=(name,value)=>{assert(value,name);checks.push(name);};
const go=async page=>{await b.navigate(new URL(page,base).href);await b.until('!!window.HarmoniaView');await b.wait(850);};
try {
  for(const width of [304,390,700,701,768,950,1280]) {
    await b.viewport(width,width===304?816:1024);
    await go('chapters/03-financing-to-offer.html?step=2');
    check(`The approval caption survives at ${width}`,await b.evaluate(`document.querySelector('.scene-title').textContent==='Northbank approves. Alice can make her proposal.'`));
    check(`Panels follow the intended orientation at ${width}`,await b.evaluate(`(()=>{const a=document.querySelector('.scene-private').getBoundingClientRect(),b=document.querySelector('.scene-shared').getBoundingClientRect();return innerWidth<=700?b.top>a.bottom:b.left>a.right;})()`));
    check(`Illustration sprites load at ${width}`,await b.evaluate(`new Promise(resolve=>{const i=new Image();i.onload=()=>resolve(i.naturalWidth===1536&&i.naturalHeight===1024);i.onerror=()=>resolve(false);i.src=getComputedStyle(document.querySelector('.scene-illustration')).backgroundImage.slice(5,-2);})`));
    check(`The scene fits at ${width}`,await b.evaluate('document.documentElement.scrollWidth<=innerWidth+1'));
    if(width===1280||width===304)await b.screenshot(`${output}/${width===1280?'purchase':'mobile'}.png`);
    if(width>700)check(`Four beat labels remain separated at ${width}`,await b.evaluate(`(()=>{const labels=[...document.querySelectorAll('#scene-dots li>span')].map(n=>n.getBoundingClientRect());return labels.slice(1).every((r,i)=>r.left>=labels[i].right);})()`));
    await go('chapters/04-four-party-transfer.html?story=transfer-final-leg-rejected&step=6&theme=dark');
    check(`The custody scene fits at ${width}`,await b.evaluate('document.documentElement.scrollWidth<=innerWidth+1'));
    if(width===1280)await b.screenshot(`${output}/transfer-dark.png`);
  }
  await b.viewport(390,1000);await go('chapters/03-financing-to-offer.html?step=2&panel=evidence&open=step-inspector&tab=observed&actor=Alice');
  check('Deep-linked mobile evidence is readable in the modal',await b.evaluate(`document.querySelector('dialog').open && document.querySelector('#step-inspector').open && JSON.parse(document.querySelector('#recorded-json').textContent).application==='approved'`));
  check('Following a person visibly identifies that person',await b.evaluate(`document.querySelector('#story-scene').dataset.follow==='Alice' && getComputedStyle(document.querySelector('.scene-focus strong')).textDecorationLine==='underline'`));
  await b.screenshot(`${output}/evidence-mobile.png`);
  await b.viewport(1280,1024);await b.navigate(new URL('infographic/reference-review/compare.html',base).href);await b.wait(850);
  check('The comparison renders all four reference and browser images',await b.evaluate(`document.images.length===4 && [...document.images].every(i=>i.complete&&i.naturalWidth>0)`));
  check('No browser errors during final visual review',b.errors.length===0);
  await fs.writeFile('docs/0.2/reference-visual.json',JSON.stringify({checks,errors:b.errors},null,2)+'\n');
  console.log(JSON.stringify({checks:checks.length,errors:b.errors}));
} finally {b.close();}

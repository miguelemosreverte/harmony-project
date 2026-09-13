import fs from 'node:fs/promises';
import assert from 'node:assert/strict';
import {browser} from './cdp.mjs';
const b=await browser(process.argv[2]);const origin=process.argv[3];const checks=[];
const check=(n,v)=>{assert(v,n);checks.push(n)};
const support='[...document.querySelectorAll(".workflow-support")].find(n=>n.getClientRects().length)';
async function fit(label){
 await b.until(`!!(${support})?.querySelector('.support-art[data-illustration]')`);
 await b.evaluate(`${support}.scrollIntoView({block:'center'})`);
 check(label+': no overflow',await b.evaluate('document.documentElement.scrollWidth<=innerWidth+1'));
 check(label+': no portrait pair',await b.evaluate('!document.querySelector(".support-portrait")'));
 check(label+': action labels are visible',await b.evaluate(`[...document.querySelectorAll('button')].filter(n=>n.getClientRects().length&&!n.disabled).every(n=>{const s=getComputedStyle(n);return s.color!==s.backgroundColor})`));
 check(label+': artwork decodes',await b.evaluate(`new Promise(resolve=>{const i=new Image();i.onload=()=>resolve(i.naturalWidth>0);i.onerror=()=>resolve(false);i.src=getComputedStyle((${support}).querySelector('.support-art')).backgroundImage.slice(5,-2)})`));
 check(label+': support above the navigation',await b.evaluate(`(()=>{const r=(${support}).getBoundingClientRect();const dock=[...document.querySelectorAll('.workflow-dock')].find(n=>n.getClientRects().length);return r.top>=0&&r.bottom<= (dock?dock.getBoundingClientRect().top:innerHeight)-2})()`));
}
try{
 await b.cdp('Page.addScriptToEvaluateOnNewDocument',{source:`window.commands=[];const f=window.fetch;window.fetch=(p,o)=>{if(o?.method==='POST'&&!String(p).includes('/api/sandbox/entry/'))commands.push(String(p));return f(p,o)}`});
 for(const width of [1280,390]){
  await b.viewport(width,width===1280?1000:844);
  for(const view of ['financing','composer','packages']){
   await b.navigate(origin+'?actor=bank&view='+view);await b.until('document.querySelector("#live-connection")?.textContent==="Connected"');
   await fit(width+' '+view);check(view+': no ledger commands',await b.evaluate('commands.length===0'));
   await b.screenshot(`design/0.2/handcrafted-review/${width}-live-${view}.png`);
  }
 }
 const report=JSON.parse(await fs.readFile('docs/0.2/handcrafted-scenes.json','utf8'));
 // Recapture the two casting corrections and the explanations that reuse them.
 for(const shot of report.shots.filter(s=>/-(workflow-approved|workflow-rejected|02-roles-and-trust|reviewer)$/.test(s.name))){
  await b.viewport(shot.width,shot.width===1280?1000:844);await b.navigate(shot.url);await fit(shot.name);await b.screenshot(`design/0.2/handcrafted-review/${shot.name}.png`);
 }
 check('No browser errors',b.errors.length===0);
 await fs.writeFile('docs/0.2/handcrafted-delivery.json',JSON.stringify({checks,errors:b.errors,scope:'Live workspaces and final casting corrections; no ledger mutations.'},null,2)+'\n');
 console.log(JSON.stringify({checks:checks.length}));
}finally{b.close()}

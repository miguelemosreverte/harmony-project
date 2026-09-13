// A summary stays in the scene; the rail supplies position and accessible names.
import fs from 'node:fs/promises';
import assert from 'node:assert/strict';
import {browser} from './cdp.mjs';
const b=await browser(process.argv[2]),base=process.argv[3]||'http://127.0.0.1:56202/design/0.2/';
const checks=[];const check=(name,value)=>{assert(value,name);checks.push(name);};
const click=id=>b.evaluate(`document.querySelector('[data-stop="${id}"]').click()`);
async function load(path){await b.navigate(new URL(path,base).href);await b.until('!!document.querySelector(".carousel-graph")');}
try{
 await b.cdp('Network.enable');await b.cdp('Network.setCacheDisabled',{cacheDisabled:true});
 for(const width of [1280,390,320]){
  await b.viewport(width,width===1280?1000:844);
  for(const [chapter,count] of [['03-financing-to-offer',7],['04-four-party-transfer',8]]){
   await load('chapters/'+chapter+'.html');
   check(width+' '+chapter+': shared stops rendered once',await b.evaluate('document.querySelectorAll(".carousel-step").length')===count);
   check(width+' '+chapter+': rail has symbols, accessible names and 44px targets',await b.evaluate('[...document.querySelectorAll(".carousel-step")].every(n=>/^[✓×]?$/.test(n.textContent)&&n.getAttribute("aria-label")&&n.offsetWidth>=44&&n.offsetHeight>=44)'));
   check(width+' '+chapter+': no repeated sentence or provenance footer',await b.evaluate('!document.getElementById("recorded-step")&&!document.getElementById("recording-provenance")&&document.querySelector(".scene-observation").hidden&&document.querySelector(".scene-caption").hidden'));
   const ids=await b.evaluate('[...document.querySelectorAll(".carousel-step")].map(n=>n.dataset.stop)');
   for(const id of ids){
    await click(id);await b.wait(40);
    check(width+' '+id+': short badge stays inside scene and clear of labels',await b.evaluate(`(()=>{
     const badge=document.querySelector('.scene-badge:not([hidden])'),r=badge.getBoundingClientRect(),scene=document.querySelector('.scene-map').getBoundingClientRect();
     const obstacles=[...document.querySelectorAll('.person-icon,.scene-person strong,.scene-zone-label,.document-sheet,.artifact-label,.scene-documents')].filter(n=>n.getClientRects().length);
     return badge.textContent.split(' ').length<=3&&r.left>=scene.left&&r.right<=scene.right&&r.top>=scene.top&&r.bottom<=scene.bottom&&obstacles.every(n=>{const q=n.getBoundingClientRect();return r.right<=q.left||r.left>=q.right||r.bottom<=q.top||r.top>=q.bottom});
    })()`));
   }
   check(width+' '+chapter+': no page overflow',await b.evaluate('document.documentElement.scrollWidth<=innerWidth+1'));
  }
  await load('chapters/03-financing-to-offer.html?step=2');
  await b.cdp('Input.dispatchMouseEvent',{type:'mouseMoved',x:2,y:2});
  await b.screenshot(`design/0.2/carousel-review/summary-${width}.png`);
 }
 await b.viewport(1280,1000);await load('chapters/04-four-party-transfer.html?story=transfer-final-leg-rejected&step=6');
 await click('transfer-approved:3');
 check('A shared stop retains the chosen outcome in its URL',await b.evaluate('HarmoniaView.state.story==="transfer-final-leg-rejected"&&HarmoniaView.state.step===3'));
 const url=await b.evaluate('location.href');await b.navigate(url);await b.until('!!document.querySelector("[aria-current=step]")');
 check('Cold shared address restores the same branch and canonical node',await b.evaluate('HarmoniaView.state.story==="transfer-final-leg-rejected"&&document.querySelector("[aria-current=step]").dataset.stop==="transfer-approved:3"'));
 await b.evaluate('document.querySelector("[aria-current=step]").focus()');
 await b.cdp('Input.dispatchKeyEvent',{type:'keyDown',key:'End',code:'End'});await b.cdp('Input.dispatchKeyEvent',{type:'keyUp',key:'End',code:'End'});
 check('Keyboard End follows the selected branch',await b.evaluate('HarmoniaView.state.story==="transfer-final-leg-rejected"&&HarmoniaView.state.step===6'));
 await b.evaluate('document.querySelector("[data-stop=\\"transfer-approved:8\\"]").focus()');
 await b.cdp('Input.dispatchKeyEvent',{type:'keyDown',key:'Home',code:'Home'});await b.cdp('Input.dispatchKeyEvent',{type:'keyUp',key:'Home',code:'Home'});
 // Shared nodes keep the selected branch until an unshared alternative is selected.
 check('Keyboard Home reaches the shared start',await b.evaluate('HarmoniaView.state.step===0'));
 await load('chapters/03-financing-to-offer.html?view=evidence');
 check('The evidence view preserves exact recorded revisions',await b.evaluate('document.querySelector("[data-mode=evidence]").textContent.includes(HarmoniaView.config.stories["purchase-approved"].provenance.revision)'));
 check('No browser errors',b.errors.length===0);
 await fs.writeFile('docs/0.2/compact-graph.json',JSON.stringify({checks,errors:b.errors},null,2)+'\n');
 console.log({checks:checks.length});
}finally{b.close()}

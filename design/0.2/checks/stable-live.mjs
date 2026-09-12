// Inspection checks never submit ledger commands. Capabilities stay in local memory.
import fs from 'node:fs/promises';import assert from 'node:assert/strict';import {browser} from './cdp.mjs';
const b=await browser(process.argv[2]),sessions=JSON.parse(await fs.readFile(process.argv[3],'utf8'));
const checks=[];const check=(name,value)=>{assert(value,name);checks.push(name);};
const visible=()=>b.evaluate("[...document.querySelectorAll('.workflow-dock')].filter(n=>n.getClientRects().length).length");
const click=selector=>b.evaluate(`document.querySelector(${JSON.stringify(selector)}).click()`);
async function actor(role,query){const u=new URL(sessions[role]);u.search=query;await b.navigate(u.href);await b.until('!!document.querySelector("#live-identity")&&document.querySelector("#live-connection")?.textContent.includes("Connected")');}
async function state(){return b.evaluate("fetch('/api/state',{headers:{Authorization:'Bearer '+sessionStorage.getItem('harmonia-live')}}).then(r=>r.json())");}
try{
 await b.cdp('Network.enable');await b.cdp('Network.setCacheDisabled',{cacheDisabled:true});
 for(const width of [1280,390]){
  await b.viewport(width,1000);await actor('bank','view=financing');await b.until('!!document.querySelector("#financing .carousel-step")');
  const initial=await state();await b.evaluate('window.retained=document.querySelector("#financing .harmonia-scene")');
  for(const id of ['workflow','application','approval']){await click(`#financing [data-stop="${id}"]`);check(`${width} financing ${id}: retained scene`,await b.evaluate('retained===document.querySelector("#financing .harmonia-scene")'));check(`${width} financing ${id}: URL inspection`,await b.evaluate(`new URL(location.href).searchParams.get('inspect')===${JSON.stringify(id)}`));}
  check(`${width}: inspection submits no command`,JSON.stringify(initial)===JSON.stringify(await state()));
  check(`${width}: only the visible workflow owns a dock`,await visible()===1);
  await b.screenshot(`design/0.2/carousel-review/live-financing-${width}.png`);
  await actor('bank','view=composer&new=1');await b.until('!!document.querySelector("#composition-name")');
  await b.evaluate('window.retained=document.querySelector(".editor-stage .workflow-diagram");window.cards=[...retained.querySelectorAll(".workflow-node")]');
  for(const id of ['Reference','Actor(0)','Integration(0)','Review','Name']){
   await click(`#composition-editor [data-stop="${id}"]`);
   check(`${width} draft ${id}: retained infographic`,await b.evaluate('retained===document.querySelector(".editor-stage .workflow-diagram")&&cards.every((n,i)=>n===retained.querySelectorAll(".workflow-node")[i])'));
   check(`${width} draft ${id}: selected question restored in URL`,await b.evaluate(`new URL(location.href).searchParams.get('question')===${JSON.stringify(id)}`));
  }
  await click('#composition-editor [data-stop="Actor(0)"]');check(`${width}: actor alternatives live in the rail`,await b.evaluate('document.querySelectorAll("#composition-editor .carousel-path[data-path=choices] button").length===2&&!document.querySelector("#composition-first")'));
  await b.screenshot(`design/0.2/carousel-review/live-editor-${width}.png`);
  const url=await b.evaluate('location.href');await b.navigate(url);await b.until(`document.querySelector('#composition-editor [aria-current=step]')?.dataset.stop==='Actor(0)'`);check(`${width}: shared draft address restores its question`,true);
  await actor('bank','view=packages');await b.until('!!document.querySelector("#package-builder .carousel-step")');
  await b.evaluate('window.retained=document.querySelector(".package-stage .workflow-diagram")');
  for(const id of ['mapping','compile','register','input']){await click(`#package-builder [data-stop="${id}"]`);check(`${width} package ${id}: retained diagram`,await b.evaluate('retained===document.querySelector(".package-stage .workflow-diagram")'));}
  check(`${width}: one package dock`,await visible()===1);await b.screenshot(`design/0.2/carousel-review/live-packages-${width}.png`);
  check(`${width}: page fits horizontally`,await b.evaluate('document.documentElement.scrollWidth<=innerWidth+1'));
 }
 await actor('buyer','view=financing&inspect=application');await b.until('!!document.querySelector("#financing .carousel-step")');check('Buyer inspection cannot disclose private case details',!(await state()).private_details);
 await actor('reviewer','view=financing&inspect=application');await b.until('!!document.querySelector("#financing .carousel-step")');const observer=await state();check('Observer inspection grants no commands or private details',!observer.private_details&&observer.eligible.length===0);
 check('No browser errors',b.errors.length===0);await fs.writeFile('docs/0.2/stable-live.json',JSON.stringify({scope:'Current Scala service; read-only inspection and unsubmitted draft navigation',checks,errors:b.errors},null,2)+'\n');console.log(JSON.stringify({checks:checks.length}));
}finally{b.close()}

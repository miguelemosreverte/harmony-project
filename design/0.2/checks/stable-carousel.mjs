// The current carousel contract supersedes the old two-arrow-only interaction checks.
import fs from 'node:fs/promises';
import assert from 'node:assert/strict';
import {browser} from './cdp.mjs';
const b=await browser(process.argv[2]),base=process.argv[3]||'http://127.0.0.1:56202/design/0.2/';
const checks=[],frames=[],directory='design/0.2/carousel-review';
await fs.mkdir(directory,{recursive:true});
await fs.mkdir('/tmp/harmonia-carousel-pixels',{recursive:true});
const check=(name,pass)=>{assert(pass,name);checks.push(name);};
const visible="n=>n.getClientRects().length&&getComputedStyle(n).visibility!=='hidden'";
const landmarks=()=>b.evaluate(`(()=>{const root=document.querySelector('#story-scene')||document.querySelector('#laboratory-stage');return [...root.querySelectorAll('.scene-map,.scene-person,.scene-document,.workflow-map,.workflow-node')].filter(${visible}).map((n,i)=>({key:n.dataset.person||n.dataset.node||n.className,index:i,x:n.getBoundingClientRect().x,y:n.getBoundingClientRect().y,width:n.getBoundingClientRect().width,height:n.getBoundingClientRect().height}));})()`);
const controls=()=>b.evaluate("[...document.querySelector('.has-carousel').querySelectorAll(':scope>a,:scope>button')].map(n=>[n.getBoundingClientRect().x,n.getBoundingClientRect().y,n.getBoundingClientRect().width,n.getBoundingClientRect().height])");
const click=selector=>b.evaluate(`document.querySelector(${JSON.stringify(selector)}).click()`);
async function load(page){await b.navigate(new URL(page,base).href);await b.until('!!document.querySelector(".carousel-step")');await b.wait(80);}
async function capture(id,root){const box=await b.evaluate(`(()=>{const r=document.querySelector(${JSON.stringify(root)}).getBoundingClientRect();return {x:r.x+scrollX,y:r.y+scrollY,width:r.width,height:r.height,scale:1}})()`);const shot=await b.cdp('Page.captureScreenshot',{format:'png',captureBeyondViewport:true,clip:box});await fs.writeFile('/tmp/harmonia-carousel-pixels/'+id+'.png',Buffer.from(shot.data,'base64'));return box;}
try{
 await b.cdp('Network.enable');await b.cdp('Network.setCacheDisabled',{cacheDisabled:true});
 for(const width of [1280,390]){
  await b.viewport(width,1000);
  for(const [chapter,prefix] of [['03-financing-to-offer','purchase'],['04-four-party-transfer','transfer']]){
   await load('chapters/'+chapter+'.html');
   await b.evaluate('window.keptScene=document.querySelector(".harmonia-scene");window.keptPeople=[...document.querySelectorAll(".scene-person")];window.emptyFrames=[];window.sampling=true;function sample(){if(!sampling)return;emptyFrames.push(!document.querySelector(".scene-map")||document.querySelectorAll(".scene-person").length!==4);requestAnimationFrame(sample)}requestAnimationFrame(sample)');
   const initial=await landmarks(),edges=await controls();
   const stops=await b.evaluate('[...document.querySelectorAll(".carousel-step")].map(n=>n.dataset.stop)');
   for(const [i,id] of stops.entries()){
    await click(`[data-stop="${id}"]`);await b.wait(70);
    check(`${width} ${id}: stage and portraits retained`,await b.evaluate('keptScene===document.querySelector(".harmonia-scene")&&keptPeople.every((n,i)=>n===document.querySelectorAll(".scene-person")[i])'));
    const current=await landmarks();
    const drift=Math.max(...current.map((n,j)=>Math.max(Math.abs(n.x-initial[j].x),Math.abs(n.y-initial[j].y))));
    check(`${width} ${id}: landmarks within 50px`,drift<=50);
    check(`${width} ${id}: fixed arrow targets`,JSON.stringify(edges)===JSON.stringify(await controls()));
    check(`${width} ${id}: selected stop visible in its track`,await b.evaluate('(()=>{const n=document.querySelector("[aria-current=step]"),r=n.getBoundingClientRect(),t=n.closest(".carousel-track").getBoundingClientRect();return r.left>=t.left-1&&r.right<=t.right+1})()'));
    const file=`${width}-${prefix}-${i}`;const clip=await capture(file,'#story-scene .harmonia-scene');frames.push({file,group:width+'-'+prefix,id,drift,clip});
    if(id.endsWith(':7')&&prefix==='purchase')check('Ben relay turns blue',await b.evaluate('document.querySelector("[data-connection=relay]")?.getAttribute("data-status")==="complete"||[...document.querySelectorAll(".measured-connections [data-status=complete]")].length>=6'));
   }
   await b.evaluate('sampling=false');check(`${width} ${prefix}: no empty animation frame`,await b.evaluate('emptyFrames.every(v=>!v)'));
   await b.screenshot(`${directory}/${prefix}-${width}.png`);
   const shared=await b.evaluate('location.href');const selected=await b.evaluate('document.querySelector("[aria-current=step]").dataset.stop');
   await b.navigate(shared);await b.until('!!document.querySelector("[aria-current=step]")');check(`${width} ${prefix}: cold branch address`,selected===await b.evaluate('document.querySelector("[aria-current=step]").dataset.stop'));
   await click('.carousel-step');await b.evaluate('history.back()');await b.until(`document.querySelector('[aria-current=step]')?.dataset.stop===${JSON.stringify(selected)}`);check(`${width} ${prefix}: Back restores selected branch`,true);
  }
 }
 await b.viewport(1280,1000);await load('laboratory.html');
 const stories=await b.evaluate('Object.keys(HarmoniaView.config.stories)');let observations=0;
 for(const story of stories){
  await b.evaluate(`HarmoniaView.update({story:${JSON.stringify(story)},step:0})`);
  const steps=await b.evaluate('[...document.querySelectorAll(".carousel-step")].map(n=>n.dataset.stop)');
  let previous='';
  for(const id of steps){await click(`[data-stop="${id}"]`);const observation=await b.evaluate('document.querySelector("#laboratory-stage .scene-observation").textContent');check(story+'/'+id+': identifies a different observed attempt',observation!==previous);previous=observation;observations++;check(story+'/'+id+': diagram is present',await b.evaluate('!!document.querySelector("#laboratory-stage .scene-map,#laboratory-stage .workflow-map")'));}
 }
 check('Every recorded observation remains directly accessible',observations===130);
 for(const page of ['reviewer.html','author.html','chapters/01-product.html']){await load(page);const count=await b.evaluate('document.querySelectorAll(".carousel-step").length');check(page+': direct reading stops',count>1);await b.screenshot(directory+'/'+page.replace('.html','').replaceAll('/','-')+'.png');}
 for(const page of ['purchase-outcome.html','transfer-outcome.html']){await load(page);check(page+': legacy address keeps the infographic',await b.evaluate('!!document.querySelector(".harmonia-scene")&&!document.querySelector(".quiet-choices")'));}
 for(const page of ['workflows.html','application.html','application-builder.html','sandbox.html','recording-choice.html']){await b.navigate(new URL(page,base).href);await b.until('!!document.querySelector(".route-dock")');check(page+': decision stays beside an infographic',await b.evaluate('!!document.querySelector(".workflow-diagram")&&!document.querySelector(".quiet-choices")'));}
 await load('chapters/03-financing-to-offer.html?step=5');
 const selector='[data-stop="purchase-approved:7"]';const rect=await b.evaluate(`document.querySelector(${JSON.stringify(selector)}).getBoundingClientRect().toJSON()`);
 const before=await b.evaluate(`getComputedStyle(document.querySelector(${JSON.stringify(selector)})).boxShadow`);
 await b.cdp('Input.dispatchMouseEvent',{type:'mouseMoved',x:rect.x+rect.width/2,y:rect.y+rect.height/2});await b.wait(150);
 check('A clickable step visibly responds to the mouse',before!==await b.evaluate(`getComputedStyle(document.querySelector(${JSON.stringify(selector)})).boxShadow`));
 check('Hover does not move its target',JSON.stringify(rect)===JSON.stringify(await b.evaluate(`document.querySelector(${JSON.stringify(selector)}).getBoundingClientRect().toJSON()`)));
 check('No browser errors',b.errors.length===0);
 await fs.writeFile('docs/0.2/stable-carousel.json',JSON.stringify({base,checks,observations,frames,errors:b.errors},null,2)+'\n');console.log(JSON.stringify({checks:checks.length,observations,frames:frames.length}));
}finally{b.close()}

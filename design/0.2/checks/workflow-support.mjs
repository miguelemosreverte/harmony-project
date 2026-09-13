// Every recorded moment is rendered; the gallery retains one full view per workflow.
import fs from 'node:fs/promises';
import assert from 'node:assert/strict';
import {browser} from './cdp.mjs';
const b=await browser(process.argv[2]),base=process.argv[3]||'http://127.0.0.1:56202/design/0.2/';
const directory='design/0.2/support-review';await fs.mkdir(directory,{recursive:true});
const checks=[],shots=[],moments=[],images=new Set();
const check=(name,condition)=>{assert(condition,name);checks.push(name);};
const visible='[...document.querySelectorAll(".workflow-support")].filter(n=>n.getClientRects().length)';
async function inspect(label){
 const value=await b.evaluate(`(()=>{const roots=${visible};return roots.map(n=>({text:[...n.querySelectorAll('.support-speech')].map(p=>p.textContent),rect:n.getBoundingClientRect().toJSON(),portraits:[...n.querySelectorAll('.support-portrait')].map(p=>({image:getComputedStyle(p).backgroundImage,rect:p.getBoundingClientRect().toJSON()})),overflow:[...n.querySelectorAll('.support-speech')].some(p=>p.scrollHeight>p.clientHeight+1),controls:n.querySelectorAll('a,button,input,summary,select').length,previous:n.previousElementSibling?.getBoundingClientRect().toJSON()}))})()`);
 check(label+': one separate conversation',value.length===1);const n=value[0];
 check(label+': two short readable lines',n.text.length===2&&n.text.every(t=>t.length>0&&t.length<=110)&&!n.overflow);
 check(label+': below the infographic with no controls',n.previous&&n.rect.top>=n.previous.bottom-1&&n.controls===0);
 check(label+': horizontal fit',await b.evaluate('document.documentElement.scrollWidth<=innerWidth+1'));
 for(const p of n.portraits){check(label+': portrait supplied',p.image!=='none');images.add(p.image.slice(5,-2));}
 return n;
}
async function screenshot(name,label,path,selector){
 await b.evaluate(`${visible}[0].scrollIntoView({block:'center'})`);await b.wait(60);
 const viewport=await b.evaluate('({width:innerWidth,height:innerHeight})');
 check(name+': entire conversation visible above navigation',await b.evaluate(`(()=>{const r=${visible}[0].getBoundingClientRect();const nav=[...document.querySelectorAll('.quiet-paging')].find(n=>n.getClientRects().length);const limit=nav?nav.getBoundingClientRect().bottom-126:innerHeight;return r.top>=0&&r.bottom<=limit+1})()`));
 await b.screenshot(directory+'/'+name+'.png');
 const clip=viewport;
 shots.push({name,label,url:new URL(path,base).href,width:clip.width,height:clip.height});
}
try{
 await b.cdp('Network.enable');await b.cdp('Network.setCacheDisabled',{cacheDisabled:false});
 // Decode each consumed asset before measuring retained scene changes.
 await b.navigate(new URL('laboratory.html',base).href);await b.until('!!window.HarmoniaRecordings');
 const imageBase=await b.evaluate('new URL("../../../product/scene/site/assets/support-v1/",location.href).href');
 for(const id of ['bank','alice','ben','sofia','developer','reviewer'])check('Asset decodes: '+id,await b.evaluate(`new Promise(r=>{const i=new Image();i.onload=()=>r(i.naturalWidth===1536);i.onerror=()=>r(false);i.src=${JSON.stringify(imageBase+id+'.png')}})`));
 const stories=await b.evaluate('Object.keys(HarmoniaView.config.stories)');
 for(const width of [1280,390,320]){
  await b.viewport(width,width===1280?1000:844);await b.navigate(new URL('laboratory.html',base).href);await b.until('!!document.querySelector("#laboratory-stage .carousel-step")||!!document.querySelector(".carousel-step")');
  for(const story of stories){
   await b.evaluate(`HarmoniaView.update({story:${JSON.stringify(story)},step:0})`);await b.wait(30);await b.evaluate('window.scrollTo(0,0)');
   const count=await b.evaluate(`HarmoniaRecordings[${JSON.stringify(story)}].presentation.units.length`);
   await b.evaluate(`window.keptSupport=${visible}[0]`);let first;
   for(let step=0;step<count;step++){
    await b.evaluate(`HarmoniaView.update({step:${step}})`);await b.wait(20);
    const n=await inspect(width+' '+story+':'+step);first??=n;
    check(width+' '+story+':'+step+': retained speakers',await b.evaluate(`keptSupport===${visible}[0]`));
    const drift=Math.max(...n.portraits.map((p,i)=>Math.abs(p.rect.y-first.portraits[i].rect.y)));
    check(width+' '+story+':'+step+': stable portrait positions',drift<=50);
    moments.push({width,story,step,lines:n.text,drift});
   }
   if(width!==320)await screenshot(width+'-'+story,story,'laboratory.html?story='+story+'&step='+(count-1),'#laboratory-stage');
  }
  if(width===320)continue;
  const chapters=await b.evaluate('Object.keys(HarmoniaAtlas.chapterDiagrams)');
  for(const chapter of chapters){const path='chapters/'+chapter+'.html';await b.navigate(new URL(path,base).href);await b.until(`${visible}.length===1`);await inspect(width+' '+chapter);await screenshot(width+'-'+chapter,chapter,path,'.quiet-content');}
  for(const [name,path,selector] of [
   ['purchase','chapters/03-financing-to-offer.html?step=2','#story-scene'],
   ['transfer','chapters/04-four-party-transfer.html?step=8','#story-scene'],
   ['reviewer','reviewer.html?slice=process&relationship=execution','#review-diagram'],
   ['developer','code.html?file=product/web/src/main/scala/harmonia/composition/ComposerView.scala','#source-diagram'],
   ['author','author.html?passage=architecture-88','#companion-frame'],
   ['author-contracts','author.html?passage=architecture-318','#companion-frame']
  ]){await b.navigate(new URL(path,base).href);await b.until(`${visible}.length===1`);await inspect(width+' '+name);await screenshot(width+'-'+name,name,path,selector);}
 }
 check('Every recording and observation checked at three widths',moments.length===390);
 check('Every generated portrait is used',images.size===6);
 check('No browser errors',b.errors.length===0);
 await fs.writeFile('docs/0.2/workflow-support.json',JSON.stringify({scope:'Recorded and authored workflow presentation; no ledger commands',checks:checks.length,moments,shots,errors:b.errors},null,2)+'\n');
 await fs.writeFile(directory+'/index.html','<!doctype html><meta charset="utf-8"><title>Workflow support review</title><style>body{font:16px system-ui;background:#edf1f6;color:#123;margin:24px}main{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:20px}figure{margin:0;padding:12px;background:white;border-radius:14px}img{width:100%;height:440px;object-fit:contain;object-position:top}a{color:#1359bf}figcaption{padding:8px 0}</style><h1>Illustrated workflow review</h1><p>Actual browser captures. Each title opens its reproducible workflow.</p><main>'+shots.map(s=>`<figure><a href="${s.url}">${s.name}</a><a href="${s.name}.png"><img src="${s.name}.png"></a></figure>`).join('')+'</main>');
 console.log(JSON.stringify({checks:checks.length,moments:moments.length,screenshots:shots.length}));
}finally{b.close()}

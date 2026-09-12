import fs from 'node:fs/promises';
import assert from 'node:assert/strict';
import {browser} from './cdp.mjs';
const b=await browser(process.argv[2]),sessions=JSON.parse(await fs.readFile(process.argv[3],'utf8'));
const checks=[];const check=(name,value)=>{assert(value,name);checks.push(name);};
const buttonRect=`(()=>{const r=document.querySelector('#composition-next').getBoundingClientRect();return [r.x,r.y,r.width,r.height]})()`;
try{
 await b.cdp('Network.enable');await b.cdp('Network.setCacheDisabled',{cacheDisabled:true});
 for(const width of [1280,390]){
  await b.viewport(width,1000);const u=new URL(sessions.bank);u.search='view=financing';await b.navigate(u.href);
  await b.until(`document.querySelector('#live-connection')?.textContent.includes('Connected')`);
  await b.evaluate(`window.liveDocument=document;window.liveHeader=document.querySelector('.reference-header');document.querySelector('.live-destination>a').click()`);
  await b.until(`new URLSearchParams(location.search).get('view')==='workspace'`);
  check(`Task navigation retains the document at ${width}`,await b.evaluate('document===liveDocument&&liveHeader===document.querySelector(".reference-header")'));
  await b.evaluate(`document.querySelector('.live-destination>a').click()`);
  await b.until(`new URLSearchParams(location.search).get('view')==='composer'`);
  await b.until(`!!document.querySelector('#composition-observations>a')`);
  // The earlier live walkthrough has completed a plan. These checks submit no ledger commands.
  await b.screenshot(`design/0.2/quiet-review/continuous-composition-${width}.png`);
  await b.evaluate(`document.querySelector('#composition-observations>a').click()`);
  await b.until(`!!document.querySelector('#composition-name')&&!document.querySelector('#composition-editor').parentElement.hidden`);
  check(`New plan opens without reloading at ${width}`,await b.evaluate('document===liveDocument'));
  const rect=await b.evaluate(buttonRect);
  await b.evaluate(`window.liveNext=document.querySelector('#composition-next');window.liveInput=document.querySelector('#composition-name')`);await b.wait(2200);
  check(`Polling keeps the input and next control at ${width}`,await b.evaluate(`liveNext===document.querySelector('#composition-next')&&liveInput===document.querySelector('#composition-name')`));
  await b.evaluate(`document.querySelector('#composition-next').click()`);await b.until(`!!document.querySelector('#composition-reference')`);
  check(`The next question keeps its control position at ${width}`,JSON.stringify(rect)===JSON.stringify(await b.evaluate(buttonRect)));
  await b.evaluate('history.back()');await b.until(`!!document.querySelector('#composition-name')`);
  check(`History restores the draft without a reload at ${width}`,await b.evaluate('document===liveDocument'));
  await b.screenshot(`design/0.2/quiet-review/continuous-question-${width}.png`);
 }
 if(b.errors.length)console.error(b.errors);check('No browser errors',b.errors.length===0);
 await fs.writeFile('docs/0.2/continuous-live.json',JSON.stringify({checks,errors:b.errors,scope:'Navigation and unsubmitted drafts; no ledger mutations'},null,2)+'\n');console.log(JSON.stringify({checks:checks.length}));
}finally{b.close();}

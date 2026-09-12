import fs from 'node:fs/promises';
import assert from 'node:assert/strict';
import {browser} from './cdp.mjs';

const b=await browser(process.argv[2]);
const base=process.argv[3]||'http://127.0.0.1:56202/design/0.2/';
const checks=[],samples=[];
const check=(name,value)=>{assert(value,name);checks.push(name);};
const rects=`[...document.querySelectorAll('.quiet-paging')].filter(n=>n.getClientRects().length).flatMap(n=>[...n.children].map(a=>{const r=a.getBoundingClientRect();return [r.x,r.y,r.width,r.height]}))`;
const inspect=async(width,page)=>{
 await b.viewport(width,1000);await b.navigate(new URL(page,base).href);
 await b.until('!!HarmoniaView.state');
 await b.evaluate(`window.readerDocument=document;window.readerHeader=document.querySelector('header');window.readerFrames=[];window.readerProbe=true;(function sample(){if(!readerProbe)return;readerFrames.push({body:!!document.querySelector('main')?.innerText.trim(),controls:${rects},nodes:document.querySelectorAll('.workflow-node,.scene-person').length});requestAnimationFrame(sample)})();`);
};
try{
 await b.cdp('Network.enable');await b.cdp('Network.setCacheDisabled',{cacheDisabled:true});
 for(const width of [1280,390]){
  await inspect(width,'chapters/03-financing-to-offer.html?step=2');
  const before=await b.evaluate(rects);
  await b.evaluate(`window.readerScene=document.querySelector('.harmonia-scene');window.readerArrow=document.querySelector('[data-edge=relay]');document.querySelector('#next-step').click()`);
  await b.until('HarmoniaView.state.step===5');
  check(`Purchase retains scene and arrow at ${width}`,await b.evaluate(`readerScene===document.querySelector('.harmonia-scene')&&readerArrow===document.querySelector('[data-edge=relay]')`));
  check(`Purchase controls stay exactly in place at ${width}`,JSON.stringify(before)===JSON.stringify(await b.evaluate(rects)));
  await b.evaluate(`document.querySelector('#next-step').click()`);await b.wait(80);
  check(`Proposal-to-Ben arrow completes at ${width}`,await b.evaluate(`document.querySelector('[data-edge=relay]').dataset.status==='complete'`));
  await b.evaluate(`document.querySelector('#next-step').click()`);
  await b.until(`location.pathname.endsWith('/04-four-party-transfer.html')`);
  check(`Chapter turn retains document and header at ${width}`,await b.evaluate('document===readerDocument&&document.querySelector("header")===readerHeader'));
  check(`Chapter turn retains control positions at ${width}`,JSON.stringify(before)===JSON.stringify(await b.evaluate(rects)));
  await b.evaluate('history.back()');await b.until(`location.pathname.endsWith('/03-financing-to-offer.html')&&HarmoniaView.state.step===8`);
  check(`Back restores the exact frame without a reload at ${width}`,await b.evaluate('document===readerDocument'));
  await b.wait(150);samples.push({width,frames:await b.evaluate('readerProbe=false;readerFrames')});
  await b.screenshot(`design/0.2/quiet-review/continuous-purchase-${width}.png`);

  await inspect(width,'reviewer.html');
  await b.evaluate(`window.readerMap=document.querySelector('.workflow-map');document.querySelector('#page-next').click()`);
  await b.until(`HarmoniaView.state.relationship==='dependencies'`);
  check(`Reviewer turns in the same map at ${width}`,await b.evaluate(`readerMap===document.querySelector('.workflow-map')&&document===readerDocument`));
  await b.evaluate('readerProbe=false');
  check(`Reviewer has no copied source drawings at ${width}`,await b.evaluate(`document.querySelectorAll('img[src*="assets/component-map"],img[src*="assets/contract-model"]').length===0`));

  await inspect(width,'author.html?passage=proposal-53');
  check(`Author uses a shared renderer without embedded pages at ${width}`,await b.evaluate(`!document.querySelector('iframe')&&!!document.querySelector('#companion-frame .workflow-node,#companion-frame .scene-person')`));
  for(let n=0;n<3;n++){await b.evaluate(`document.querySelector('#page-next').click()`);await b.wait(200);}
  check(`Author retains its document at ${width}`,await b.evaluate('document===readerDocument'));
  await b.evaluate('readerProbe=false');
  await b.screenshot(`design/0.2/quiet-review/continuous-author-${width}.png`);
 }
 for(const sample of samples){
  check(`No empty painted page across turns at ${sample.width}`,sample.frames.length>2&&sample.frames.every(f=>f.body&&f.nodes>0));
  check(`No moving carousel target in sampled frames at ${sample.width}`,sample.frames.every(f=>JSON.stringify(f.controls)===JSON.stringify(sample.frames[0].controls)));
 }
 await b.navigate(new URL('laboratory.html?story=branch-approved&step=1',base).href);
 await b.until('!!document.querySelector(".observed-comparison pre")');
 check('Recursive JSON sorting retains nested values and array order',await b.evaluate(`HarmoniaReader.json({z:{b:2,a:1},a:[{z:0,a:1},4,2]})===JSON.stringify({a:[{a:1,z:0},4,2],z:{a:1,b:2}},null,2)`));
 check('Displayed expectation uses canonical ordering',await b.evaluate(`document.querySelector('.observed-comparison pre').textContent===HarmoniaReader.json(HarmoniaView.config.stories[HarmoniaView.state.story].presentation.units[HarmoniaView.state.step].expected)`));
 await b.evaluate(`window.readerCard=document.querySelector('.workflow-node');HarmoniaView.update({step:HarmoniaView.state.step+1})`);
 check('Observed diagram updates keep existing cards',await b.evaluate('readerCard===document.querySelector(".workflow-node")'));
 await b.navigate(new URL('chapters/01-product.html',base).href);await b.until('!!HarmoniaView.state');
 await b.evaluate('window.retainedPage=document.querySelector("main");window.retainedAddress=location.href');
 await b.cdp('Network.emulateNetworkConditions',{offline:true,latency:0,downloadThroughput:0,uploadThroughput:0});
 await b.evaluate('document.querySelector("#page-next").click()');await b.until('!!document.querySelector("#navigation-status")');
 check('A failed chapter request retains the current page and its address',await b.evaluate('retainedPage===document.querySelector("main")&&retainedAddress===location.href'));
 await b.cdp('Network.emulateNetworkConditions',{offline:false,latency:0,downloadThroughput:-1,uploadThroughput:-1});
 await b.evaluate('document.querySelector("#page-next").click()');await b.until('location.pathname.endsWith("/02-roles-and-trust.html")');
 check('The same arrow retries successfully after reconnecting',await b.evaluate('!document.querySelector("#navigation-status")'));
 await b.cdp('Network.emulateNetworkConditions',{offline:true,latency:0,downloadThroughput:0,uploadThroughput:0});
 await b.evaluate('history.back()');await b.until('location.pathname.endsWith("/01-product.html")&&HarmoniaView.config.slug==="01-product"');
 check('A previously visited chapter can be revisited offline',await b.evaluate('!document.querySelector("#navigation-status")'));
 await b.cdp('Network.emulateNetworkConditions',{offline:false,latency:0,downloadThroughput:-1,uploadThroughput:-1});
 const unexpected=b.errors.filter(e=>!e.includes('ERR_INTERNET_DISCONNECTED'));
 if(unexpected.length)console.error(unexpected);
 check('No unexpected browser errors',unexpected.length===0);
 await fs.writeFile('docs/0.2/continuous-reader.json',JSON.stringify({base,checks,frameSamples:samples.map(s=>({width:s.width,count:s.frames.length,controls:s.frames[0].controls})),errors:unexpected},null,2)+'\n');
 console.log(JSON.stringify({checks:checks.length,frames:samples.map(s=>s.frames.length)}));
}finally{await b.cdp('Network.emulateNetworkConditions',{offline:false,latency:0,downloadThroughput:-1,uploadThroughput:-1});b.close();}

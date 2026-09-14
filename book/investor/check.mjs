import fs from 'node:fs/promises';
import path from 'node:path';
import assert from 'node:assert/strict';
import {execFileSync} from 'node:child_process';
import {pathToFileURL} from 'node:url';
const [endpoint,base]=process.argv.slice(2);
assert(endpoint&&base,'Supply CDP endpoint and exported demo directory URL');
const out=path.resolve('.artifacts/investor-review');
const log=JSON.parse(await fs.readFile('.artifacts/canton-demo/events.json'));
const tab=await(await fetch(endpoint+'/json/new?about:blank',{method:'PUT'})).json();
const ws=new WebSocket(tab.webSocketDebuggerUrl);
await new Promise((resolve,reject)=>{ws.onopen=resolve;ws.onerror=reject});
let id=0;
const pending=new Map(),errors=[],checks=[];
ws.onmessage=event=>{
  const m=JSON.parse(event.data);
  if(m.method==='Runtime.exceptionThrown')errors.push(m.params.exceptionDetails);
  if(m.method==='Network.responseReceived'&&m.params.response.status>=400)errors.push(m.params.response.url);
  if(!m.id)return;
  const p=pending.get(m.id);pending.delete(m.id);
  m.error?p.reject(Error(m.error.message)):p.resolve(m.result);
};
const call=(method,params={})=>new Promise((resolve,reject)=>{const k=++id;pending.set(k,{resolve,reject});ws.send(JSON.stringify({id:k,method,params}))});
const evaluate=async expression=>{
  const r=await call('Runtime.evaluate',{expression,returnByValue:true,awaitPromise:true});
  if(r.exceptionDetails)throw Error(JSON.stringify(r.exceptionDetails));
  return r.result.value;
};
const pause=ms=>new Promise(r=>setTimeout(r,ms));
const selected=()=>evaluate('document.querySelector(".demo-slide[data-current=true]")?.dataset.key');
async function ready(key){
 for(let n=0;n<100;n++){
  if(await evaluate(`document.documentElement.dataset.playing==='true' && document.querySelector('.demo-slide[data-current=true]')?.dataset.key===${JSON.stringify(key)} && Array.from(document.images).every(i=>i.complete&&i.naturalWidth>0)`))return;
  await pause(30);
 }
 throw Error('Not ready: '+key);
}
const address=key=>{const [demo,p,step]=key.split('/');return '?'+new URLSearchParams({demo,path:p,step})};
async function navigate(url,key){await call('Page.navigate',{url});await pause(150);await ready(key);}
async function choose(key){await evaluate(`history.pushState(null,'',${JSON.stringify(address(key))});dispatchEvent(new PopStateEvent('popstate'))`);await ready(key);await pause(150);}
async function shot(name){const r=await call('Page.captureScreenshot',{format:'png'});const b=Buffer.from(r.data,'base64');await fs.writeFile(path.join(out,name+'.png'),b);return b;}
const geometry=()=>evaluate(`Object.fromEntries(['#demo-previous','#demo-next','.demo-slide[data-current=true] .contract-flow','.demo-slide[data-current=true] .demo-receipt'].map(s=>{const r=document.querySelector(s).getBoundingClientRect();return [s,[r.x,r.y,r.width,r.height]]}))`);
try{
 await fs.mkdir(out,{recursive:true});
 await call('Page.enable');await call('Runtime.enable');await call('Network.enable');
 for(const [name,width,height,mobile] of [['desktop',1440,900,false],['phone',390,844,true],['small',320,568,true]]){
  await call('Emulation.setDeviceMetricsOverride',{width,height,deviceScaleFactor:1,mobile});
  await navigate(new URL('index.html',base).href,log.scenes[0].key);
  await evaluate('window.originalSlides=Array.from(document.querySelectorAll(".demo-slide"));window.originalArrows=[document.getElementById("demo-previous"),document.getElementById("demo-next")]');
  const baseline=await geometry();
  const seen=new Set();
  for(const scene of log.scenes){
   await choose(scene.key);
   await evaluate(`document.querySelector('.carousel-step[data-stop="${scene.key}"]').click()`);
   assert.equal(await selected(),scene.key);
   assert.deepEqual(await geometry(),baseline,'Stage or arrows moved: '+name+' '+scene.key);
   assert.equal(await evaluate('document.documentElement.scrollWidth<=innerWidth && document.documentElement.scrollHeight<=innerHeight'),true,'Body overflow: '+name);
   if(name!=='small')assert.equal(await evaluate('document.getElementById("demo-stage").scrollHeight<=document.getElementById("demo-stage").clientHeight+1'),true,'Stage overflow: '+name);
   assert.equal(await evaluate('window.originalSlides.every((n,i)=>n===document.querySelectorAll(".demo-slide")[i])&&window.originalArrows[0]===document.getElementById("demo-previous")&&window.originalArrows[1]===document.getElementById("demo-next")'),true);
   assert.equal(await evaluate('document.querySelectorAll(".demo-slide[aria-hidden=false]").length'),1);
   assert.equal(await evaluate('Array.from(document.querySelectorAll(".demo-slide[aria-hidden=true]")).every(s=>s.inert)'),true);
   const text=await evaluate('document.querySelector(".demo-slide[data-current=true]").innerText');
   assert(!seen.has(text),'Identical visible stops: '+scene.key);seen.add(text);
   const event=log.runs[scene.recording].events[scene.event];
   const actual=await evaluate('document.querySelector(".demo-slide[data-current=true] .receipt-outcome").textContent');
   assert.equal(actual,event.observed.outcome??'compiled');
   if(scene.key.startsWith('settlement')){
    const values=await evaluate('Array.from(document.querySelectorAll(".demo-slide[data-current=true] .contract-value")).map(e=>e.textContent)');
    assert.equal(values[0],event.observed.source.locked+' TEST');assert.equal(values[2],event.observed.destination+' TEST');
   }
   await shot(name+'-'+scene.key.replaceAll('/','-'));
  }
  checks.push({viewport:name,scenes:seen.size,stableGeometry:true});
  await choose('settlement/accept/ready');
  await evaluate(`document.querySelector('.carousel-step[data-stop="settlement/reject/rolled-back"]').click()`);await ready('settlement/reject/rolled-back');
  await evaluate('history.back()');await ready('settlement/accept/ready');
  await evaluate('history.forward()');await ready('settlement/reject/rolled-back');
  await call('Page.reload');await pause(150);await ready('settlement/reject/rolled-back');
  await call('Input.dispatchKeyEvent',{type:'keyDown',key:'ArrowLeft',code:'ArrowLeft'});await ready('settlement/reject/ready');
  if(mobile){
   await call('Emulation.setTouchEmulationEnabled',{enabled:true,maxTouchPoints:1});
   await call('Input.dispatchTouchEvent',{type:'touchStart',touchPoints:[{x:270,y:180}]});
   await call('Input.dispatchTouchEvent',{type:'touchMove',touchPoints:[{x:80,y:181}]});
   await call('Input.dispatchTouchEvent',{type:'touchEnd',touchPoints:[]});await ready('settlement/reject/rolled-back');
  }
 }
 // Traverse the complete main presentation using the same forward arrow.
 await choose(log.scenes[0].key);
 for(const d of log.demos)for(const s of d.paths[0].steps){
  await ready([d.id,d.paths[0].id,s.id].join('/'));
  await evaluate('document.getElementById("demo-next").click()');
 }
 assert.equal(await evaluate('document.getElementById("demo-next").disabled'),true);
 // Export the actual selected state with the UI, not a separately constructed fixture.
 await call('Emulation.setDeviceMetricsOverride',{width:1440,height:900,deviceScaleFactor:1,mobile:false});
 await choose('settlement/reject/rolled-back');
 await call('Browser.setDownloadBehavior',{behavior:'allow',downloadPath:out});
 await fs.rm(path.join(out,'harmonia-event-log.json'),{force:true});
 await evaluate('document.getElementById("demo-export").click()');
 let saved;
 for(let i=0;i<100;i++){try{saved=JSON.parse(await fs.readFile(path.join(out,'harmonia-event-log.json')));break}catch{await pause(50)}}
 assert(saved,'Export did not download');assert.deepEqual(saved.cursor,{demo:'settlement',path:'reject',step:'rolled-back'});
 const original=await shot('before-export');
 const rebuild=path.join(out,'restored');
 execFileSync('node',['book/investor/build.mjs','--replay',path.join(out,'harmonia-event-log.json'),'--out',rebuild]);
 await call('Network.emulateNetworkConditions',{offline:true,latency:0,downloadThroughput:0,uploadThroughput:0});
 await navigate(pathToFileURL(path.join(rebuild,'index.html')).href,'settlement/reject/rolled-back');await pause(200);
 assert.deepEqual(await shot('offline-restored'),original,'Offline replay pixels differ from exported selection');
 // A recipient can open the JSON with the supplied HTML, without Node or this repo.
 await call('Page.navigate',{url:pathToFileURL(path.join(rebuild,'open.html')).href});await pause(200);
 const document=await call('DOM.getDocument');
 const node=await call('DOM.querySelector',{nodeId:document.root.nodeId,selector:'#recording-file'});
 await call('DOM.setFileInputFiles',{nodeId:node.nodeId,files:[path.join(out,'harmonia-event-log.json')]});
 await ready('settlement/reject/rolled-back');await pause(200);
 assert.deepEqual(await shot('opened-json'),original,'Opened JSON does not reproduce the same pixels');
 checks.push({exportAndImport:'same selected state and identical screenshot pixels',offline:true});
 assert.deepEqual(errors,[]);
 await fs.writeFile(path.join(out,'checks.json'),JSON.stringify({checks,errors},null,2));
 console.log(JSON.stringify({checks,errors}));
}finally{
 await call('Network.emulateNetworkConditions',{offline:false,latency:0,downloadThroughput:-1,uploadThroughput:-1}).catch(()=>{});
 ws.close();await fetch(endpoint+'/json/close/'+tab.id);
}

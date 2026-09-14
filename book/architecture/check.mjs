import fs from 'node:fs/promises';
import assert from 'node:assert/strict';

const [endpoint, base] = process.argv.slice(2);
if (!endpoint || !base) throw Error('Usage: node book/architecture/check.mjs <CDP endpoint> <architecture directory URL>');
const url = new URL('index.html?step=0', base).href;
const out='.artifacts/architecture-carousel-review';
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
async function navigate(target,expected=target){
  await call('Page.navigate',{url:target});
  for(let n=0;n<100;n++){
    if(await evaluate(`location.href===${JSON.stringify(expected)} && document.readyState==='complete' && !!document.querySelector('.architecture-slide h2')`))return;
    await pause(50);
  }
  throw Error('Page did not finish loading: '+target);
}
async function screenshot(name){
  const r=await call('Page.captureScreenshot',{format:'png'});
  await fs.writeFile(`${out}/${name}.png`,Buffer.from(r.data,'base64'));
  checks.push({screenshot:`${name}.png`,url:await evaluate('location.href')});
}

const ready=async index=>{
  for(let n=0;n<100;n++){
    if(await evaluate(`document.documentElement.dataset.slides==='true' && document.querySelector('.architecture-slide[data-current="true"]')?.dataset.step===${JSON.stringify(String(index))}`))return;
    await pause(25);
  }
  throw Error('Slide not ready: '+index);
};
const state=()=>evaluate(`({
  step:document.querySelector('.architecture-slide[data-current="true"]').dataset.step,
  bounds:Object.fromEntries(['#slide-previous','#slide-next','.architecture-slide[data-current="true"] figure'].map(s=>{
    const r=document.querySelector(s).getBoundingClientRect();return [s,[r.x,r.y,r.width,r.height]];
  })),
  overflow:document.documentElement.scrollWidth>innerWidth||document.documentElement.scrollHeight>innerHeight,
  innerOverflow:document.getElementById('architecture-slides').scrollHeight>document.getElementById('architecture-slides').clientHeight+1,
  activeCount:document.querySelectorAll('.architecture-slide[aria-hidden="false"]').length,
  readyImages:Array.from(document.images).every(i=>i.complete&&i.naturalWidth>0),
  progress:document.querySelector('.carousel-step[aria-current="step"]').dataset.stop
})`);
try {
  await fs.mkdir(out,{recursive:true});
  await call('Page.enable');await call('Runtime.enable');await call('Network.enable');
  for(const [name,width,height,mobile] of [['desktop',1440,900,false],['mobile',390,844,true],['small',320,568,true]]){
    await call('Emulation.setDeviceMetricsOverride',{width,height,deviceScaleFactor:1,mobile});
    await navigate(url);await ready(0);
    await evaluate('window.originalSlides=Array.from(document.querySelectorAll(".architecture-slide"));window.originalImages=Array.from(document.images);window.originalControls=Array.from(document.querySelectorAll(".quiet-paging button"))');
    const first=await state();
    assert.equal(first.activeCount,1);assert.equal(first.readyImages,true);assert.equal(first.overflow,false);
    assert.equal(await evaluate('document.getElementById("slide-previous").disabled'),true);
    for(let i=0;i<3;i++){
      await evaluate(`document.querySelector('.carousel-step[data-stop="${i}"]').click()`);await ready(i);
      const s=await state();
      assert.equal(s.progress,String(i));assert.equal(s.activeCount,1);assert.equal(s.overflow,false);
      if(name!=='small')assert.equal(s.innerOverflow,false,'Unexpected slide scrolling on '+name);
      assert.deepEqual(s.bounds,first.bounds,'Stage or navigation moved on '+name);
      assert.equal(await evaluate('window.originalSlides.every((n,i)=>n===document.querySelectorAll(".architecture-slide")[i])&&window.originalImages.every((n,i)=>n===document.images[i])&&window.originalControls.every((n,i)=>n===document.querySelectorAll(".quiet-paging button")[i])'),true);
      assert.equal(await evaluate('Array.from(document.querySelectorAll(".architecture-slide[aria-hidden=true]")).every(s=>s.inert && getComputedStyle(s).visibility==="hidden")'),true);
      await screenshot(name+'-'+i);
      checks.push({viewport:name,step:i,state:s});
      if(name==='small') {
        await evaluate('document.querySelector(".architecture-slide[data-current=true] figure").scrollIntoView({block:"center"})');
        assert.equal(await evaluate('(()=>{const r=document.querySelector(".architecture-slide[data-current=true] figure").getBoundingClientRect(),m=document.getElementById("architecture-slides").getBoundingClientRect();return r.top>=m.top && r.bottom<=m.bottom})()'),true);
        await screenshot('small-figure-'+i);
        await evaluate('document.querySelector(".architecture-slide[data-current=true] .slide-explanation").scrollIntoView({block:"end"})');
        assert.equal(await evaluate('document.querySelector(".architecture-slide[data-current=true] .slide-explanation").getBoundingClientRect().bottom <= document.getElementById("architecture-slides").getBoundingClientRect().bottom+1'),true);
        await evaluate('document.getElementById("architecture-slides").scrollTop=0');
      }
    }
    assert.equal(await evaluate('document.getElementById("slide-next").disabled'),true);
    await evaluate('document.getElementById("slide-next").click()');await ready(2);
    await evaluate('document.getElementById("slide-previous").click()');await ready(1);
    await evaluate('history.back()');await ready(2);
    await evaluate('history.forward()');await ready(1);
    await call('Page.reload');await ready(1);
    await call('Input.dispatchKeyEvent',{type:'keyDown',key:'ArrowRight',code:'ArrowRight'});
    await call('Input.dispatchKeyEvent',{type:'keyUp',key:'ArrowRight',code:'ArrowRight'});await ready(2);
    if(mobile){
      await call('Emulation.setTouchEmulationEnabled',{enabled:true,maxTouchPoints:1});
      await call('Input.dispatchTouchEvent',{type:'touchStart',touchPoints:[{x:80,y:250}]});
      await call('Input.dispatchTouchEvent',{type:'touchMove',touchPoints:[{x:240,y:251}]});
      await call('Input.dispatchTouchEvent',{type:'touchEnd',touchPoints:[]});await ready(1);
    }
    await navigate(url.replace('step=0','step=99'),url.replace('step=0','step=2'));await ready(2);
    assert.equal(await evaluate('new URLSearchParams(location.search).get("step")'),'2');
    await navigate(url.replace('step=0','step=invalid'),url);await ready(0);
  }
  await call('Emulation.setEmulatedMedia',{media:'print'});
  assert.equal(await evaluate('Array.from(document.querySelectorAll(".architecture-slide")).every(s=>getComputedStyle(s).visibility==="visible")'),true);
  const pdf=await call('Page.printToPDF',{printBackground:true,preferCSSPageSize:true});
  await fs.writeFile(out+'/architecture.pdf',Buffer.from(pdf.data,'base64'));
  await call('Emulation.setEmulatedMedia',{media:''});
  await call('Emulation.setScriptExecutionDisabled',{value:true});
  await navigate(url);
  assert.equal(await evaluate('Array.from(document.querySelectorAll(".architecture-slide")).every(s=>getComputedStyle(s).visibility==="visible")'),true);
  await call('Emulation.setScriptExecutionDisabled',{value:false});
  assert.deepEqual(errors,[]);
  await fs.writeFile(out+'/checks.json',JSON.stringify({at:new Date().toISOString(),checks,errors,assertions:['shared Scala carousel','three slides with retained DOM and decoded images','fixed stage and controls','clicks, keyboard and touch navigation','reload and browser history restore slide','bounded URLs and no endpoint wrap','only active slide available to interaction','all passages visible for print and without JavaScript']},null,2)+'\n');
  console.log(JSON.stringify({screenshots:checks.filter(c=>c.screenshot).map(c=>c.screenshot),errors}));
} finally {ws.close();await fetch(endpoint+'/json/close/'+tab.id)}

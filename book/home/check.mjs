import fs from 'node:fs/promises';
import assert from 'node:assert/strict';

const [endpoint,base]=process.argv.slice(2);
if(!endpoint||!base)throw Error('Usage: node book/home/check.mjs <CDP endpoint> <repository URL>');
const home=new URL('book/index.html',base).href;
const out='.artifacts/home-review',branches=['user','investor','architecture','implementation'];
const tab=await(await fetch(endpoint+'/json/new?about:blank',{method:'PUT'})).json();
const ws=new WebSocket(tab.webSocketDebuggerUrl);
await new Promise((resolve,reject)=>{ws.onopen=resolve;ws.onerror=reject;});
let id=0;const pending=new Map(),errors=[],checks=[];
ws.onmessage=event=>{
  const m=JSON.parse(event.data);
  if(m.method==='Runtime.exceptionThrown')errors.push(m.params.exceptionDetails);
  if(m.method==='Network.responseReceived'&&m.params.response.status>=400)errors.push(m.params.response.url);
  if(!m.id)return;const p=pending.get(m.id);pending.delete(m.id);
  m.error?p.reject(Error(m.error.message)):p.resolve(m.result);
};
const call=(method,params={})=>new Promise((resolve,reject)=>{const key=++id;pending.set(key,{resolve,reject});ws.send(JSON.stringify({id:key,method,params}));});
const evaluate=async expression=>{
  const r=await call('Runtime.evaluate',{expression,returnByValue:true,awaitPromise:true});
  if(r.exceptionDetails)throw Error(JSON.stringify(r.exceptionDetails));return r.result.value;
};
async function wait(expression){
  for(let n=0;n<100;n++){
    if(await evaluate(`document.documentElement && (${expression})`))return;
    await new Promise(r=>setTimeout(r,50));
  }
  throw Error('Not ready: '+expression+' at '+await evaluate('location.href'));
}
const ready=branch=>wait(`document.documentElement.dataset.ready==='true' && document.querySelector('.path-cover[data-current="true"]')?.dataset.branch===${JSON.stringify(branch)}`);
async function navigate(url,branch='user'){
  await call('Page.navigate',{url});await ready(branch);
}
async function screenshot(name){
  const shot=await call('Page.captureScreenshot',{format:'png'});
  await fs.writeFile(`${out}/${name}.png`,Buffer.from(shot.data,'base64'));
  checks.push({screenshot:name+'.png',url:await evaluate('location.href')});
}
const geometry=()=>evaluate(`Object.fromEntries(['#path-stage','.path-action','#path-previous','#path-next'].map(s=>{
  const r=document.querySelector(s).getBoundingClientRect();return [s,[r.x,r.y,r.width,r.height]];
}))`);
const key=async name=>{
  await call('Input.dispatchKeyEvent',{type:'keyDown',key:name,code:name});
  await call('Input.dispatchKeyEvent',{type:'keyUp',key:name,code:name});
};
try{
  await fs.mkdir(out,{recursive:true});
  await call('Page.enable');await call('Runtime.enable');await call('Network.enable');await call('Network.setCacheDisabled',{cacheDisabled:true});
  for(const [name,width,height,mobile] of [['desktop',1440,900,false],['mobile',390,844,true],['small',320,568,true]]){
    await call('Emulation.setDeviceMetricsOverride',{width,height,deviceScaleFactor:1,mobile});
    await navigate(home+'?branch=user');
    await evaluate('window.covers=[...document.querySelectorAll(".path-cover")];window.images=[...document.images];window.openAction=document.getElementById("open-path")');
    const first=await geometry();
    for(const branch of branches){
      await evaluate(`document.querySelector('.carousel-step[data-stop="${branch}"]').click()`);await ready(branch);
      assert.equal(await evaluate('document.querySelectorAll(".path-cover[aria-hidden=false]").length'),1);
      assert.equal(await evaluate('[...document.querySelectorAll(".path-cover[aria-hidden=true]")].every(n=>n.inert && getComputedStyle(n).visibility==="hidden")'),true);
      assert.equal(await evaluate('covers.every((n,i)=>n===document.querySelectorAll(".path-cover")[i])&&images.every((n,i)=>n===document.images[i])&&openAction===document.getElementById("open-path")'),true);
      assert.deepEqual(await geometry(),first,'Controls or stage moved: '+name+'/'+branch);
      assert.equal(await evaluate('document.documentElement.scrollWidth<=innerWidth && document.documentElement.scrollHeight<=innerHeight'),true,'Outer overflow: '+name);
      assert.equal(await evaluate('[...document.images].every(i=>i.complete&&i.naturalWidth>0)'),true);
      assert.equal(await evaluate('document.querySelector("main").scrollHeight<=document.querySelector("main").clientHeight+1'),true,'Unexpected scrolling: '+name);
      await screenshot(name+'-'+branch);
    }
    assert.equal(await evaluate('document.getElementById("path-next").disabled'),true);
    await evaluate('document.getElementById("path-previous").click()');await ready('architecture');
    await evaluate('history.back()');await ready('implementation');
    await evaluate('history.forward()');await ready('architecture');
    await call('Page.reload');await ready('architecture');
    await key('ArrowRight');await ready('implementation');
    await key('Home');await ready('user');
    assert.equal(await evaluate('document.getElementById("path-previous").disabled'),true);
    if(mobile){
      await call('Emulation.setTouchEmulationEnabled',{enabled:true,maxTouchPoints:1});
      await call('Input.dispatchTouchEvent',{type:'touchStart',touchPoints:[{x:240,y:250}]});
      await call('Input.dispatchTouchEvent',{type:'touchMove',touchPoints:[{x:80,y:251}]});
      await call('Input.dispatchTouchEvent',{type:'touchEnd',touchPoints:[]});await ready('investor');
    }
    await navigate(home+'?branch=unknown');
    assert.equal(await evaluate('new URLSearchParams(location.search).get("branch")'),'user');
  }
  for(const [name,width,height,mobile] of [['desktop',1440,900,false],['mobile',390,844,true]]){
    await call('Emulation.setDeviceMetricsOverride',{width,height,deviceScaleFactor:1,mobile});
    for(const branch of branches){
      await navigate(home+'?branch='+branch,branch);
      await evaluate('document.getElementById("open-path").click()');
      await wait(`!!document.querySelector('a[data-home="${branch}"]')`);
      const loaded={user:'!!document.querySelector(".scene-map")',investor:'document.documentElement.dataset.playing==="true"',architecture:'document.documentElement.dataset.slides==="true"',implementation:'document.querySelectorAll(".code-line").length>0'};
      await wait(loaded[branch]);
      await screenshot(name+'-opened-'+branch);
      checks.push({opened:branch,viewport:name,url:await evaluate('location.href')});
      if(branch==='investor'){
        assert.equal(await evaluate('document.querySelector(".demo-slide[data-current=true]").dataset.key'),'core/join/chosen');
        await evaluate('document.getElementById("demo-next").click()');
        assert.notEqual(await evaluate('document.querySelector(".demo-slide[data-current=true]").dataset.key'),'core/join/chosen');
      }
      await evaluate('document.querySelector("a[data-home]").click()');await ready(branch);
      assert.equal(await evaluate('new URLSearchParams(location.search).get("branch")'),branch);
    }
  }
  await navigate(new URL('?branch=investor',base).href,'investor');
  assert.equal(await evaluate('location.pathname'),new URL(home).pathname);
  assert.deepEqual(errors,[]);
  const report={at:new Date().toISOString(),checks,errors,assertions:['all four covers and destinations','mounted covers and fixed stage and controls','no outer scrolling, standard screens fit','query URLs, browser history, reload and unknown branch fallback','carousel clicks, arrow keys and touch','working return to the selected path','recorded Canton demo advances','repository root opens the entrance']};
  await fs.writeFile(out+'/checks.json',JSON.stringify(report,null,2)+'\n');
  console.log(JSON.stringify({screenshots:checks.filter(c=>c.screenshot).length,opened:checks.filter(c=>c.opened).length,errors}));
}finally{ws.close();await fetch(endpoint+'/json/close/'+tab.id);}

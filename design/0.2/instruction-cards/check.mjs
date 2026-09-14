// node design/0.2/instruction-cards/check.mjs <local-CDP-endpoint> <study-directory-URL>
import assert from 'node:assert/strict';
import fs from 'node:fs/promises';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
const [endpoint, base] = process.argv.slice(2);
for (const address of [endpoint,base]) assert(['127.0.0.1','localhost'].includes(new URL(address).hostname));
const output = path.join(path.dirname(fileURLToPath(import.meta.url)), 'review');
await fs.mkdir(output,{recursive:true});
const target = await (await fetch(new URL('/json/new?about:blank',endpoint),{method:'PUT'})).json();
const socket = new WebSocket(target.webSocketDebuggerUrl);
await new Promise((resolve,reject)=>{socket.onopen=resolve;socket.onerror=reject;});
let sequence=0;
const pending=new Map(), errors=[], checks=[];
socket.onmessage=event=>{
  const message=JSON.parse(event.data);
  if(message.id){const task=pending.get(message.id);pending.delete(message.id);message.error?task.reject(Error(message.error.message)):task.resolve(message.result);}
  else if(message.method==='Runtime.exceptionThrown')errors.push(message.params.exceptionDetails);
};
const call=(method,params={})=>new Promise((resolve,reject)=>{const id=++sequence;pending.set(id,{resolve,reject});socket.send(JSON.stringify({id,method,params}));});
const evaluate=async expression=>{const result=await call('Runtime.evaluate',{expression,awaitPromise:true,returnByValue:true});assert(!result.exceptionDetails,JSON.stringify(result.exceptionDetails));return result.result.value;};
const until=async expression=>{for(let i=0;i<100;i++){if(await evaluate(expression))return;await new Promise(resolve=>setTimeout(resolve,50));}throw Error('Page did not settle: '+expression);};
const navigate=async(topic,step=0)=>{
  const url=new URL(topic+'.html?step='+step,base).href;
  await call('Page.navigate',{url});
  await until(`location.href===${JSON.stringify(url)} && document.querySelectorAll('.stop').length===4 && document.querySelectorAll('.panel[aria-current=true]').length===1`);
};
const inspect=async()=>evaluate(`(()=>{
  const rect=element=>{const r=element.getBoundingClientRect();return {x:r.x,y:r.y,width:r.width,height:r.height,bottom:r.bottom,right:r.right};};
  const active=document.querySelector('.panel[aria-current=true]');
  return {url:location.href,selected:[...document.querySelectorAll('.stop')].findIndex(s=>s.getAttribute('aria-selected')==='true'),
    width:innerWidth,height:innerHeight,scrollWidth:document.documentElement.scrollWidth,scrollHeight:document.documentElement.scrollHeight,
    active:rect(active),picture:rect(active.querySelector('.picture')),caption:rect(active.querySelector('.caption')),dock:rect(document.querySelector('.dock')),panel:rect(document.querySelector('.panels')),
    visible:[...document.querySelectorAll('.panel')].filter(p=>getComputedStyle(p).visibility==='visible').length,
    outsideControls:document.querySelectorAll('a,input,select,textarea,button:not(.stop)').length,
    labels:[...active.querySelectorAll('svg text')].map(t=>({text:t.textContent,...rect(t)}))};})()`);
try{
  await call('Page.enable');await call('Runtime.enable');
  await call('Emulation.setEmulatedMedia',{features:[{name:'prefers-reduced-motion',value:'reduce'}]});
  for(const [width,height] of [[1280,900],[390,844],[320,568]]){
    await call('Emulation.setDeviceMetricsOverride',{width,height,deviceScaleFactor:1,mobile:width<620});
    for(const topic of ['product','workflow','architecture','code']){
      await navigate(topic);
      let first;
      for(let step=0;step<4;step++){
        await evaluate(`document.querySelectorAll('.stop')[${step}].click()`);
        const data=await inspect();
        assert.equal(data.selected,step);assert.equal(new URL(data.url).pathname,new URL(topic+'.html',base).pathname);
        assert.equal(data.scrollWidth,width,topic+' horizontal overflow');assert.equal(data.scrollHeight,height,topic+' vertical overflow');
        assert.equal(data.outsideControls,0);assert.equal(data.visible,width<620?1:4);
        assert(data.active.bottom<=data.dock.y+1,topic+' overlaps navigation');
        if(data.caption.height)assert(data.picture.bottom<=data.caption.y-2,topic+' illustration overlaps its caption');
        for(const label of data.labels)assert(label.x>=data.active.x-1 && label.right<=data.active.right+1 && label.y>=data.active.y && label.bottom<=data.active.bottom,label.text+' leaves the panel');
        if(first){assert.deepEqual(data.dock,first.dock);assert.deepEqual(data.panel,first.panel);}else first=data;
        checks.push({topic,step,...data});
        if(width!==320){const shot=await call('Page.captureScreenshot',{format:'png'});await fs.writeFile(path.join(output,`${topic}-${width}-${step}.png`),Buffer.from(shot.data,'base64'));}
      }
      await call('Page.reload');await until('document.querySelectorAll(".stop").length===4');assert.equal((await inspect()).selected,3);
      await evaluate('document.querySelectorAll(".stop")[1].click();history.back()');
      await until('document.querySelectorAll(".stop")[3].getAttribute("aria-selected")==="true"');
      await evaluate('history.forward()');await until('document.querySelectorAll(".stop")[1].getAttribute("aria-selected")==="true"');
      await evaluate('document.querySelectorAll(".stop")[1].focus()');
      await call('Input.dispatchKeyEvent',{type:'keyDown',key:'ArrowRight',code:'ArrowRight'});
      assert.equal((await inspect()).selected,2);
      if(width<620){
        await evaluate(`document.querySelector('.panels').dispatchEvent(new TouchEvent('touchstart',{touches:[new Touch({identifier:1,target:document.querySelector('.panels'),clientX:250,clientY:300})]}));document.querySelector('.panels').dispatchEvent(new TouchEvent('touchend',{changedTouches:[new Touch({identifier:1,target:document.querySelector('.panels'),clientX:100,clientY:300})]}));`);
        assert.equal((await inspect()).selected,3);
      }
    }
  }
  await call('Emulation.setDeviceMetricsOverride',{width:1280,height:900,deviceScaleFactor:1,mobile:false});
  for(const topic of ['product','workflow','architecture','code']){
    await navigate(topic,3);
    await call('Emulation.setEmulatedMedia',{media:'print'});
    const print=await evaluate(`({visible:[...document.querySelectorAll('.panel')].filter(p=>getComputedStyle(p).visibility==='visible').length,dock:getComputedStyle(document.querySelector('.dock')).display})`);
    assert.equal(print.visible,4);assert.equal(print.dock,'none');
    const pdf=await call('Page.printToPDF',{printBackground:true,preferCSSPageSize:true});
    await fs.writeFile(path.join(output,topic+'.pdf'),Buffer.from(pdf.data,'base64'));
    await call('Emulation.setEmulatedMedia',{media:''});
  }
  assert.deepEqual(errors,[]);
  await fs.writeFile(path.join(output,'checks.json'),JSON.stringify({checks,errors,history:true,keyboard:true,touch:true,print:true},null,2)+'\n');
  console.log(JSON.stringify({checkedStates:checks.length,screenshots:32,printCards:4,errors:errors.length}));
}finally{socket.close();await fetch(new URL('/json/close/'+target.id,endpoint));}

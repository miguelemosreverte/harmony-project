import fs from 'node:fs/promises';
import assert from 'node:assert/strict';

export async function browser(endpoint) {
  const url=new URL(endpoint);
  assert(['127.0.0.1','localhost'].includes(url.hostname));
  const tabs=await(await fetch(new URL('/json/list',url))).json();
  const target=tabs.find(t=>t.type==='page'&&(t.url.includes('/design/0.2/')||t.url.startsWith('http://127.0.0.1:')));
  assert(target,'Open a local preview in the owned test browser');
  const socket=new WebSocket(target.webSocketDebuggerUrl);
  await new Promise((resolve,reject)=>{socket.onopen=resolve;socket.onerror=reject;});
  let sequence=0; const pending=new Map(),errors=[];
  socket.onmessage=e=>{const m=JSON.parse(e.data);if(m.id){const p=pending.get(m.id);pending.delete(m.id);m.error?p.reject(Error(m.error.message)):p.resolve(m.result);}else if(m.method==='Runtime.exceptionThrown')errors.push(m.params.exceptionDetails.text);else if(m.method==='Log.entryAdded'&&m.params.entry.level==='error')errors.push(m.params.entry.text + (m.params.entry.url ? ' ['+new URL(m.params.entry.url).pathname+']' : '')); };
  const cdp=(method,params={})=>new Promise((resolve,reject)=>{const id=++sequence;pending.set(id,{resolve,reject});socket.send(JSON.stringify({id,method,params}));});
  const evaluate=async expression=>{const r=await cdp('Runtime.evaluate',{expression,awaitPromise:true,returnByValue:true});if(r.exceptionDetails)throw Error(r.exceptionDetails.exception?.description||r.exceptionDetails.text);return r.result.value;};
  const wait=ms=>new Promise(r=>setTimeout(r,ms));
  const until=async expression=>{for(let i=0;i<160;i++){if(await evaluate(expression))return;await wait(100);}throw Error('Browser state did not arrive: '+expression);};
  const navigate=async (destination,expectedPath=new URL(destination).pathname)=>{await cdp('Page.navigate',{url:destination});await wait(60);await until(`document.readyState==='complete' && location.pathname === ${JSON.stringify(expectedPath)}`);};
  const viewport=async(width,height=1000)=>cdp('Emulation.setDeviceMetricsOverride',{width,height,deviceScaleFactor:1,mobile:false});
  const screenshot=async(file)=>{await wait(850);await fs.writeFile(file,Buffer.from((await cdp('Page.captureScreenshot',{format:'png'})).data,'base64'));};
  await cdp('Page.enable');await cdp('Page.navigate',{url:'about:blank'});await wait(100);await cdp('Runtime.enable');await cdp('Log.enable');await cdp('Log.clear');errors.length=0;
  return {cdp,evaluate,wait,until,navigate,viewport,screenshot,errors,close:()=>socket.close()};
}

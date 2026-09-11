// Attach only to an explicitly supplied local debugging endpoint and its existing preview tab.
// Usage: node design/0.2/checks/run.mjs http://127.0.0.1:DEBUG_PORT
import fs from 'node:fs/promises';
import path from 'node:path';
import assert from 'node:assert/strict';
const endpoint=new URL(process.argv[2]);
assert(['127.0.0.1','localhost'].includes(endpoint.hostname),'Use a local browser endpoint');
const base='http://127.0.0.1:56202/design/0.2/';
const tabs=await(await fetch(new URL('/json/list',endpoint))).json();
const target=tabs.find(tab=>tab.type==='page'&&tab.url.startsWith(base));
assert(target,'Open the local design preview in the owned test browser first');
const socket=new WebSocket(target.webSocketDebuggerUrl);
await new Promise((resolve,reject)=>{socket.onopen=resolve;socket.onerror=reject;});
let sequence=0;
const pending=new Map(), errors=[];
socket.onmessage=event=>{
  const message=JSON.parse(event.data);
  if(message.id){const request=pending.get(message.id);pending.delete(message.id);message.error?request.reject(Error(message.error.message)):request.resolve(message.result);}
  else if(message.method==='Runtime.exceptionThrown')errors.push(message.params.exceptionDetails);
};
function cdp(method,params={}) {return new Promise((resolve,reject)=>{const id=++sequence;pending.set(id,{resolve,reject});socket.send(JSON.stringify({id,method,params}));});}
async function evaluate(expression) {
  const result=await cdp('Runtime.evaluate',{expression,returnByValue:true,awaitPromise:true});
  if(result.exceptionDetails)throw Error(result.exceptionDetails.exception?.description||result.exceptionDetails.text);
  return result.result.value;
}
const checks=await fs.readFile('design/0.2/checks/browser.js','utf8');
async function navigate(url) {
  await cdp('Page.navigate',{url:new URL(url,base).href});
  for(let index=0;index<100;index++) {
    await new Promise(resolve=>setTimeout(resolve,30));
    if(await evaluate(`document.readyState==='complete'&&!!window.HarmoniaView&&location.href.startsWith(${JSON.stringify(new URL(url,base).href.split('?')[0].replace('/book-chapter.html','/chapters/03-financing-to-offer.html'))})`))break;
    if(index===99)throw Error(`Navigation did not settle: ${url}`);
  }
  await evaluate(checks);await evaluate('new Promise(resolve=>setTimeout(resolve,60))');
}
async function viewport(width,height=900) {await cdp('Emulation.setDeviceMetricsOverride',{width,height,deviceScaleFactor:1,mobile:false});}
async function replay() {
  await evaluate('new Promise(resolve=>setTimeout(resolve,60))');
  const url=await evaluate('location.href'), before=await evaluate('harmoniaDesignChecks.snapshot()');
  await navigate(url);
  assert.deepEqual(await evaluate('harmoniaDesignChecks.snapshot()'),before,`Cold replay differs: ${url}`);
  return url;
}
const report={scope:'Second UX pass; browser rendering and recorded playback, not fresh ledger execution',checks:[],screenshots:[],errors};
await cdp('Runtime.enable');await cdp('Page.enable');
try {
  await viewport(1280);
  for(const [page,test] of [['book-overview.html','welcome'],['chapters/03-financing-to-offer.html?view=try','chapter'],['chapters/04-four-party-transfer.html?view=try','chapter'],['application.html','application'],['application-builder.html','builder']]) {
    await navigate(page);report.checks.push({page,test,result:await evaluate(`harmoniaDesignChecks.${test}()`),replay:await replay()});
  }
  await navigate('book-overview.html');report.checks.push({test:'appearance-and-history',result:await evaluate('harmoniaDesignChecks.common()'),replay:await replay()});
  // A malformed link must settle on usable canonical state.
  await navigate('chapters/03-financing-to-offer.html?view=try&story=missing&step=999&theme=purple&theme=dark&text=huge&unknown=x#%XX');
  assert.equal(await evaluate('HarmoniaView.state.step'),8);assert.equal(await evaluate('HarmoniaView.state.theme'),'light');
  assert.equal(await evaluate('location.search.includes("unknown")'),false);report.checks.push({test:'malformed-url',replay:await replay()});
  await navigate('chapters/01-product.html?view=sources&open=exact-proposal-L160');
  assert.equal(await evaluate('document.getElementById("proposal-L160").open'),true);
  report.checks.push({test:'nested-source-link',replay:await replay()});
  await navigate('book-chapter.html?view=try&step=2');
  assert.equal(await evaluate('HarmoniaView.state.step'),2);report.checks.push({test:'legacy-chapter-redirect',url:await evaluate('location.href')});
  const chapters=(await(await fetch(base+'coverage.json')).json()).chapters;
  const pages=['book-overview.html','application.html','application-builder.html','coverage.html','sources/proposal.html','sources/architecture.html',...chapters.map(c=>`chapters/${c.id}.html`)];
  for(const width of [390,768,1280,1536]) {
    await viewport(width,width===390?844:900);
    for(const page of pages){await navigate(page);const result=await evaluate('harmoniaDesignChecks.layout()');report.checks.push({test:'layout',page,...result});}
  }
  await viewport(390,844);
  for(const theme of ['light','dark','paper']) for(const text of ['compact','standard','large']) {
    await navigate(`chapters/01-product.html?view=sources&theme=${theme}&text=${text}`);
    report.checks.push({test:'source-disclosures',theme,text,result:await evaluate('harmoniaDesignChecks.sources()')});
  }
  for(const chapter of chapters) {
    await navigate(`chapters/${chapter.id}.html?view=sources&text=large`);
    report.checks.push({test:'all-source-passages',chapter:chapter.id,result:await evaluate('harmoniaDesignChecks.sources()')});
  }
  await navigate('file://' + path.resolve('design/0.2/chapters/03-financing-to-offer.html') + '?view=try&story=purchase-rejected&step=3');
  assert.equal(await evaluate('document.getElementById("recorded-outcome").textContent'),'Action refused');
  report.checks.push({test:'file-url-offline-playback',replay:await replay()});
  // Print rendering and no-JavaScript reading use the same committed HTML.
  await viewport(1280,900);await navigate('chapters/04-four-party-transfer.html?view=try&story=transfer-final-leg-rejected&step=6&theme=dark');
  await cdp('Emulation.setEmulatedMedia',{media:'print'});
  assert.equal(await evaluate('getComputedStyle(document.body).backgroundColor'),'rgb(255, 255, 255)');
  assert.equal(await evaluate('getComputedStyle(document.querySelector("[data-recording=transfer-final-leg-rejected]")).display'),'block');
  assert.equal(await evaluate('getComputedStyle(document.querySelector("[data-recording=transfer-approved]")).display'),'none');
  await fs.mkdir('design/0.2/review-2',{recursive:true});
  const pdf=await cdp('Page.printToPDF',{printBackground:true,preferCSSPageSize:true,displayHeaderFooter:false});
  await fs.writeFile('design/0.2/review-2/transfer-rollback.pdf',Buffer.from(pdf.data,'base64'));
  report.checks.push({test:'print',file:'transfer-rollback.pdf'});
  await cdp('Emulation.setEmulatedMedia',{media:''});
  await cdp('Emulation.setScriptExecutionDisabled',{value:true});
  await cdp('Page.navigate',{url:base+'chapters/03-financing-to-offer.html'});
  await new Promise(resolve=>setTimeout(resolve,300));
  const accessibility=await cdp('Accessibility.getFullAXTree');
  assert(accessibility.nodes.some(n=>n.name?.value.includes('Recorded actions:')),'No static recording table without JavaScript');
  report.checks.push({test:'javascript-disabled-reading',staticRecordingTables:true});
  await cdp('Emulation.setScriptExecutionDisabled',{value:false});
  const shots=[
    ['welcome','book-overview.html',1280,1000],['purchase','chapters/03-financing-to-offer.html?view=try&step=2',1280,1000],
    ['source','chapters/01-product.html?audience=author&view=sources&open=proposal-L160',1280,1000],
    ['workspace','application.html?audience=operator',1280,1000],['builder','application-builder.html?phase=3',1280,1000],
    ['coverage','coverage.html?audience=author',1280,1000],['mobile','chapters/03-financing-to-offer.html?view=try&step=2&text=large',390,844],
    ['dark','chapters/04-four-party-transfer.html?view=try&story=transfer-final-leg-rejected&step=6&theme=dark',1280,1100],
    ['paper','chapters/03-financing-to-offer.html?theme=paper',1280,1000]
  ];
  for(const [name,url,width,height]of shots){await viewport(width,height);await navigate(url);const screenshot=await cdp('Page.captureScreenshot',{format:'png'});await fs.writeFile(`design/0.2/review-2/${name}.png`,Buffer.from(screenshot.data,'base64'));report.screenshots.push({name,url:new URL(url,base).href,width,height});}
  assert.equal(errors.length,0,`Browser exceptions: ${JSON.stringify(errors)}`);
  await fs.writeFile('docs/0.2/browser-results-2.json',JSON.stringify(report,null,2)+'\n');
  console.log(JSON.stringify({checks:report.checks.length,screenshots:report.screenshots.length,errors:errors.length}));
} finally {await cdp('Emulation.setScriptExecutionDisabled',{value:false});await cdp('Emulation.setEmulatedMedia',{media:''});socket.close();}

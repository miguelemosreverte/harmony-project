// Run against a relocated export or the repository preview. No network transaction is submitted.
import fs from 'node:fs/promises';
import assert from 'node:assert/strict';
import path from 'node:path';
import {browser} from './cdp.mjs';
const b=await browser(process.argv[2]);
const base=process.argv[3]||'http://127.0.0.1:56202/design/0.2/';
const output='design/0.2/infographic/reference-review';await fs.mkdir(output,{recursive:true});
const report={scope:'HTML infographic playback, mobile, deterministic navigation, source rendering, offline and print',checks:[],screenshots:[]};
const check=(name,value)=>{assert(value,name);report.checks.push(name);};
const go=async page=>{await b.navigate(new URL(page,base).href);await b.until('!!window.HarmoniaView');};
const click=selector=>b.evaluate(`document.querySelector(${JSON.stringify(selector)}).click()`);
const snapshot=()=>b.evaluate(`JSON.stringify({state:HarmoniaView.state,caption:document.querySelector('.scene-caption')?.textContent,json:document.querySelector('#recorded-json')?.textContent})`);
try {
  await b.viewport(1280);
  await go('chapters/03-financing-to-offer.html');
  check('The story opens directly on a scene',await b.evaluate(`!document.querySelector('#chapter-try').hidden && !!document.querySelector('.harmonia-scene')`));
  await click('#next-step');check('First advance is the bank assessment',await b.evaluate('HarmoniaView.state.step===2'));
  await b.evaluate(`document.querySelector('#story-carousel').dispatchEvent(new KeyboardEvent('keydown',{key:'ArrowRight',bubbles:true}))`);
  check('Keyboard advances to the proposal, the third of four beats',await b.evaluate("HarmoniaView.state.step===5 && document.querySelector('#recorded-step').textContent==='3 / 4'"));
  await b.evaluate(`const scene=document.querySelector('#story-carousel'); for(const [type,x] of [['touchstart',260],['touchend',80]]) {const e=new Event(type);Object.defineProperty(e,type==='touchstart'?'touches':'changedTouches',{value:[{clientX:x,clientY:400}]});scene.dispatchEvent(e);}`);
  check('A horizontal swipe reaches the offer',await b.evaluate('HarmoniaView.state.step===8'));
  await click('#next-step');check('The carousel wraps to the beginning',await b.evaluate('HarmoniaView.state.step===0'));
  await click('#previous-step');check('Previous wraps to the final beat',await b.evaluate('HarmoniaView.state.step===8'));
  await click('#open-evidence');
  check('Evidence opens as a modal without hiding the story',await b.evaluate("document.querySelector('#reader-drawer').open && !document.querySelector('#chapter-try').hidden && HarmoniaView.state.panel==='evidence'"));
  const drawerUrl=await b.evaluate('location.href');await go(drawerUrl);
  check('A shared URL restores the evidence drawer',await b.evaluate("document.querySelector('#reader-drawer').open"));
  await b.cdp('Input.dispatchKeyEvent',{type:'keyDown',key:'Escape',code:'Escape',windowsVirtualKeyCode:27});
  await b.cdp('Input.dispatchKeyEvent',{type:'keyUp',key:'Escape',code:'Escape',windowsVirtualKeyCode:27});
  check('Escape closes the drawer and restores keyboard focus',await b.evaluate("!document.querySelector('#reader-drawer').open && document.activeElement.id==='open-evidence'"));
  const before=await snapshot(),url=await b.evaluate('location.href');await go(url);check('A cold URL reproduces the scene and evidence',await snapshot()===before);
  await click('#previous-step');await b.evaluate('history.back()');await b.until('HarmoniaView.state.step===8');check('Browser Back restores the scene',true);
  for(const key of ['purchase-approved','purchase-rejected','transfer-approved','transfer-final-leg-rejected']) {
    const story=JSON.parse(await fs.readFile(`book/edition-0.2/recordings/${key}.json`,'utf8'));
    const chapter=key.startsWith('purchase')?'03-financing-to-offer':'04-four-party-transfer';
    await go(`chapters/${chapter}.html?story=${key}&tab=observed`);
    for(let i=1;i<=story.presentation.units.length;i++) {
      await b.evaluate(`HarmoniaView.update({step:${i},tab:'observed'})`);
      const actual=await b.evaluate(`JSON.parse(document.querySelector('#recorded-json').textContent)`);
      assert.deepEqual(actual,story.presentation.units[i-1].actual);
      check(`${key} action ${i} preserves the committed recorded result`,true);
    }
  }
  await go('chapters/04-four-party-transfer.html?story=transfer-final-leg-rejected&step=6&theme=dark');
  check('Rollback retains the source lock and zero destination receipt',await b.evaluate(`document.querySelector('.scene-title').textContent==='Settlement rolls back.' && [...document.querySelectorAll('.scene-amount strong')].map(n=>n.textContent).join('|')==='0 TEST|10 TEST|0 TEST'`));
  check('No disabled role controls appear in playback',await b.evaluate('document.querySelectorAll("#chapter-try button:disabled").length===0'));
  for(const width of [304,360,390,700,768,1024,1280]) {
    await b.viewport(width);
    for(const page of ['book-overview.html','chapters/03-financing-to-offer.html?step=8&text=large','chapters/04-four-party-transfer.html?step=8','coverage.html','sources/architecture.html','sources/proposal.html','sandbox.html']) {
      await go(page);await b.wait(800);
      check(`No page overflow at ${width}: ${page}`,await b.evaluate('document.documentElement.scrollWidth<=innerWidth+1'));
      if(page.includes('chapters/'))check(`The artifact clears people at ${width}: ${page}`,await b.evaluate(`(()=>{const a=document.querySelector('.scene-artifact').getBoundingClientRect();return [...document.querySelectorAll('.scene-person')].every(p=>{const r=p.getBoundingClientRect();return a.right<=r.left||a.left>=r.right||a.bottom<=r.top||a.top>=r.bottom;});})()`));
    }
  }
  for(const theme of ['light','dark','paper']) {
    await go(`chapters/01-product.html?view=sources&open=exact-proposal-L160&theme=${theme}`);
    check(`${theme}: exact source deep link expands its parent`,await b.evaluate('document.querySelector("#proposal-L160").open && document.querySelector("#exact-proposal-L160").open'));
  }
  await go('file://'+path.resolve('design/0.2/chapters/03-financing-to-offer.html')+'?story=purchase-rejected&step=3');
  check('Playback works directly from a local HTML file',await b.evaluate(`document.querySelector('.scene-title').textContent==='The offer cannot proceed.'`));
  await b.viewport(1280);await go('chapters/04-four-party-transfer.html?story=transfer-final-leg-rejected&step=6&theme=dark');
  const beforePrint=await snapshot();
  const pdf=await b.cdp('Page.printToPDF',{printBackground:true,preferCSSPageSize:true});await fs.writeFile(`${output}/transfer.pdf`,Buffer.from(pdf.data,'base64'));await b.wait(150);
  check('Printing preserves the interactive URL and selection',await snapshot()===beforePrint);
  await b.cdp('Emulation.setScriptExecutionDisabled',{value:true});await b.cdp('Page.navigate',{url:new URL('chapters/03-financing-to-offer.html',base).href});await b.wait(400);
  const ax=await b.cdp('Accessibility.getFullAXTree');check('Readable recorded tables remain without JavaScript',ax.nodes.some(n=>n.name?.value.includes('Recorded actions:')));
  await b.cdp('Emulation.setScriptExecutionDisabled',{value:false});
  for(const [name,page,width,height] of [
    ['purchase','chapters/03-financing-to-offer.html?step=2',1280,1024],
    ['mobile','chapters/03-financing-to-offer.html?step=2',304,816],
    ['transfer-dark','chapters/04-four-party-transfer.html?story=transfer-final-leg-rejected&step=6&theme=dark',1280,1200],
    ['mobile-paper','chapters/03-financing-to-offer.html?step=8&theme=paper&text=large',360,1200],
    ['source','chapters/01-product.html?view=sources&open=proposal-L160',1280,1000]]) {
    await b.viewport(width,height);await go(page);await b.screenshot(`${output}/${name}.png`);report.screenshots.push({name,page,width,height});
  }
  check('No browser script errors',b.errors.filter(e=>!e.includes('favicon.ico')).length===0);
  await fs.writeFile('docs/0.2/reference-browser.json',JSON.stringify({...report,errors:b.errors},null,2)+'\n');
  console.log(JSON.stringify({checks:report.checks.length,screenshots:report.screenshots.length,errors:b.errors}));
} finally {await b.cdp('Emulation.setScriptExecutionDisabled',{value:false});b.close();}

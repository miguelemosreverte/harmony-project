// Functional and geometric checks of the generated reader; no ledger commands are submitted.
import fs from 'node:fs/promises';
import path from 'node:path';
import assert from 'node:assert/strict';
import {browser} from './cdp.mjs';
const b=await browser(process.argv[2]),base=process.argv[3]||'http://127.0.0.1:56202/design/0.2/';
const output='design/0.2/reader/review';await fs.mkdir(output,{recursive:true});
const checks=[],screenshots=[];const check=(name,value)=>{assert(value,name);checks.push(name);};
const go=async p=>{await b.navigate(new URL(p,base).href);await b.until('!!window.HarmoniaView');};
const click=s=>b.evaluate(`document.querySelector(${JSON.stringify(s)}).click()`);
const file='product/server/src/main/scala/harmonia/financing/FinancingObservation.scala';
try {
  await b.cdp('Network.enable');await b.cdp('Network.setCacheDisabled',{cacheDisabled:true});
  await b.viewport(1440,1080);await go('book-overview.html');
  check('Each reader has a direct entry',await b.evaluate(`['workflows.html','code.html','reviewer.html','author.html'].every(p=>[...document.querySelectorAll('.workflow-story')].some(a=>a.href.includes(p)))`));
  await go('code.html?file='+encodeURIComponent(file)+'&line=24');await b.until('!!document.querySelector("#code-L24.selected-line")');
  check('A file URL loads exact, syntax-colored source',await b.evaluate(`document.querySelector('#source-code').textContent.includes('object FinancingObservation')&&!!document.querySelector('.token-keyword')`));
  check('The selected source line is visible inside its pane',await b.evaluate(`(()=>{const a=document.querySelector('#code-L24').getBoundingClientRect(),b=document.querySelector('#source-code').getBoundingClientRect();return a.top>=b.top&&a.bottom<=b.bottom;})()`));
  await b.evaluate(`HarmoniaView.update({q:'Composer.scala'})`);
  check('Search expands matching folders without another click',await b.evaluate(`(()=>{const a=document.querySelector('#file-tree [data-file]');return a&&a.getBoundingClientRect().height>0;})()`));
  await b.evaluate(`HarmoniaView.update({q:''});HarmoniaView.update({file:'product/ledger/composition/daml/Composer.daml'});HarmoniaView.update({file:${JSON.stringify(file)}})`);
  await b.until(`document.querySelector('#source-code').textContent.includes('object FinancingObservation')`);
  check('Rapid file navigation cannot leave a stale loading pane',true);
  await click('[data-code-tab=diagram]');await b.until('document.querySelectorAll("#source-diagram .workflow-node").length===5');
  await click('#source-diagram [data-node=private]');await b.until('document.querySelector("#source-code").textContent.includes("template")');
  check('A slice node opens its actual Daml file',await b.evaluate(`HarmoniaView.state.file.endsWith('PrivateFinancing.daml')&&HarmoniaView.state.codeTab==='code'`));
  await b.evaluate('HarmoniaView.update({line:17})');const sourceUrl=await b.evaluate('location.href');await go(sourceUrl);await b.until('!!document.querySelector("#code-L17.selected-line")');check('Source selection survives a cold shared URL',true);
  await go('reviewer.html?slice=transfer&node=settlement');
  check('A diagram URL selects the documented implementation',await b.evaluate(`document.querySelector('#review-node').textContent.includes('AtomicTransfer.daml')&&document.querySelector('[data-node=settlement]').getAttribute('aria-pressed')==='true'`));
  await click('[data-node=source]');await b.evaluate('history.back()');await b.until(`HarmoniaView.state.node==='settlement'`);check('Browser Back restores the reviewed node',true);
  await go('author.html?passage=proposal-133&companion=code');
  await b.until(`document.querySelector('iframe').contentDocument?.querySelector('#source-code .code-line')`);
  await b.evaluate('document.fonts.ready');await b.wait(150);
  check('Original passage is highlighted and visible',await b.evaluate(`(()=>{const p=document.querySelector('#author-document-pane'),s=p.querySelector('.selected-passage');return s.dataset.passage==='proposal-133'&&Math.abs(s.getBoundingClientRect().top-p.getBoundingClientRect().top)<20;})()`));
  await b.evaluate(`document.querySelector('iframe').contentWindow.HarmoniaView.update({file:${JSON.stringify(file)},line:24})`);
  await b.until(`HarmoniaView.state.detail.includes('line=24')`);const authorUrl=await b.evaluate('location.href');
  await go(authorUrl);await b.until(`document.querySelector('iframe').contentDocument?.querySelector('#code-L24.selected-line')`);
  check('A shared author URL restores the companion file and line',await b.evaluate(`document.querySelector('iframe').contentWindow.HarmoniaView.state.file===${JSON.stringify(file)}`));
  await b.evaluate(`HarmoniaView.update({theme:'dark'})`);await b.until(`document.querySelector('iframe').contentWindow.HarmoniaView?.state.theme==='dark'`);check('Companion appearance follows the parent reader',true);
  await b.evaluate(`HarmoniaView.update({theme:'light'})`);
  await click('[data-companion=workflow]');await b.until(`!!document.querySelector('iframe').contentDocument?.querySelector('#next-step')`);
  await b.evaluate(`document.querySelector('iframe').contentDocument.querySelector('#next-step').click()`);await b.until(`HarmoniaView.state.detail.includes('step=2')`);
  check('Embedded workflow navigation updates the parent URL',true);
  for(const width of [304,390,768,1024,1280]) {
    await b.viewport(width,1080);
    for(const page of ['book-overview.html','code.html','author.html?passage=proposal-133','workflows.html','reviewer.html?slice=transfer','chapters/03-financing-to-offer.html?step=8']) {
      await go(page);await b.wait(180);
      check(`Page fits at ${width}: ${page}`,await b.evaluate('document.documentElement.scrollWidth<=innerWidth+1'));
      if(page.includes('03-financing')) {
        check(`Sofia clears the arrowhead at ${width}`,await b.evaluate(`(()=>{const a=document.querySelector('.connector-head[data-edge=receipt]').getBoundingClientRect(),b=document.querySelector('[data-person=Sofia] .person-icon').getBoundingClientRect();return a.right<=b.left||a.left>=b.right||a.bottom<=b.top||a.top>=b.bottom;})()`));
        check(`Proposal to Ben remains blue at ${width}`,await b.evaluate(`getComputedStyle(document.querySelector('[data-edge=relay]')).stroke==='rgb(0, 132, 255)'`));
      }
    }
    for(const slice of ['financing','process','transfer','composition','packages','book']) {
      await go('reviewer.html?slice='+slice);await b.wait(120);
      check(`All diagram cards fit at ${width}: ${slice}`,await b.evaluate(`(()=>{const cards=[...document.querySelectorAll('.workflow-node')],r=document.querySelector('.workflow-map').getBoundingClientRect();return cards.length>0&&cards.every(c=>{const b=c.getBoundingClientRect();return b.left>=r.left&&b.right<=r.right+1&&c.scrollWidth<=c.clientWidth+1;});})()`));
      check(`Connectors clear intermediate nodes at ${width}: ${slice}`,await b.evaluate(`(()=>{const cards=[...document.querySelectorAll('.workflow-node')];return [...document.querySelectorAll('.measured-connections path:not(.connector-head)')].every(p=>{const [from,to]=p.dataset.edge.split('--'),m=p.getScreenCTM(),l=p.getTotalLength();return Array.from({length:40},(_,i)=>{const q=p.getPointAtLength(l*i/39),point=new DOMPoint(q.x,q.y).matrixTransform(m);return cards.filter(c=>![from,to].includes(c.dataset.node)).every(c=>{const b=c.getBoundingClientRect();return point.x<b.left||point.x>b.right||point.y<b.top||point.y>b.bottom;});}).every(Boolean);});})()`));
    }
  }
  await b.viewport(1280,1000);await go('chapters/03-financing-to-offer.html?present=1');
  await b.cdp('Input.dispatchKeyEvent',{type:'keyDown',key:'ArrowRight',code:'ArrowRight',windowsVirtualKeyCode:39});await b.cdp('Input.dispatchKeyEvent',{type:'keyUp',key:'ArrowRight',code:'ArrowRight',windowsVirtualKeyCode:39});
  check('Presentation can advance from keyboard focus outside the carousel',await b.evaluate('HarmoniaView.state.step===2'));
  await b.cdp('Emulation.setEmulatedMedia',{features:[{name:'prefers-reduced-motion',value:'reduce'}]});await b.evaluate('HarmoniaView.update({autoplay:1})');
  check('Reduced motion prevents automatic advancement',await b.evaluate('HarmoniaView.state.autoplay===0'));await b.cdp('Emulation.setEmulatedMedia',{features:[]});
  await go('file://'+path.resolve('design/0.2/code.html')+'?line=24');await b.until('!!document.querySelector("#code-L24.selected-line")');check('Source chunks load from a local HTML file without a server',true);
  for(const [name,page,width,height] of [
    ['welcome','book-overview.html',1440,1080],['developer','code.html?line=24',1440,1080],['reviewer','reviewer.html?slice=transfer&node=settlement',1440,1250],['author','author.html?passage=proposal-133&companion=code',1440,1200],['purchase-mobile','chapters/03-financing-to-offer.html?step=8',390,1160],['reviewer-mobile','reviewer.html?slice=transfer',390,1280],['workflows','workflows.html',1440,1080]]) {
    await b.viewport(width,height);await go(page);await b.screenshot(`${output}/${name}.png`);screenshots.push({name,page,width,height});
  }
  await b.viewport(1280,1000);await go('reviewer.html?slice=transfer');
  await b.cdp('Emulation.setEmulatedMedia',{media:'print'});await b.wait(150);
  check('Printed diagram remains visible and fits one page',await b.evaluate(`document.querySelector('.workflow-map').getBoundingClientRect().height<500&&[...document.querySelectorAll('.workflow-node')].every(n=>getComputedStyle(n).display!=='none')`));
  const pdf=await b.cdp('Page.printToPDF',{printBackground:true,preferCSSPageSize:true});await fs.writeFile(`${output}/reviewer.pdf`,Buffer.from(pdf.data,'base64'));check('Review diagram exports to PDF',pdf.data.length>10000);await b.cdp('Emulation.setEmulatedMedia',{media:''});
  check('No script or resource errors',b.errors.filter(e=>!e.includes('favicon.ico')).length===0);
  await fs.writeFile('docs/0.2/reader-browser.json',JSON.stringify({scope:'Reader navigation, source fidelity, author companion sharing, responsive diagrams, presentation, offline source and PDF',checks,screenshots,errors:b.errors},null,2)+'\n');
  console.log(JSON.stringify({checks:checks.length,screenshots:screenshots.length,errors:b.errors}));
} finally {await b.cdp('Emulation.setEmulatedMedia',{media:'',features:[]});b.close();}

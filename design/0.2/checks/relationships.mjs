import fs from 'node:fs/promises';
import assert from 'node:assert/strict';
import {browser} from './cdp.mjs';
const b=await browser(process.argv[2]),base=process.argv[3]||'http://127.0.0.1:56202/design/0.2/';
const checks=[],screenshots=[];const check=(name,value)=>{assert(value,name);checks.push(name);};
const go=async page=>{await b.navigate(new URL(page,base).href);await b.until('!!window.HarmoniaView');};
try {
 await b.cdp('Network.enable');await b.cdp('Network.setCacheDisabled',{cacheDisabled:true});
 await go('code.html?file=product/server/src/main/scala/harmonia/financing/Financing.scala');await b.until('document.querySelector("#source-code .code-line")');
 check('Unannotated feature file has precise package context',await b.evaluate(`document.querySelector('#source-notes').textContent.includes('Financing vertical slice')&&!document.querySelector('#source-notes').textContent.includes('no file-specific')`));
 check('Package links are real catalog files',await b.evaluate(`Array.from(document.querySelectorAll('#source-notes details a')).every(a=>!!HarmoniaAtlas.files[new URL(a.href).searchParams.get('file')])`));
 check('Every source is assigned to a documented group',await b.evaluate(`Object.values(HarmoniaAtlas.files).every(f=>HarmoniaAtlas.contexts[f.context]?.files.includes(f.path))`));
 for(const width of [304,390,768,1280]) {
  await b.viewport(width,1080);
  for(const [relationship,slices] of [['dependencies',['financing','process','transfer','composition','packages']],['execution',['financing','composition','packages']]]) {
   for(const slice of slices) {
    await go(`reviewer.html?slice=${slice}&relationship=${relationship}`);await b.wait(200);
    check(`${relationship} ${slice} restores the right graph at ${width}`,await b.evaluate(`HarmoniaView.state.relationship===${JSON.stringify(relationship)}&&document.querySelectorAll('.workflow-node').length===HarmoniaAtlas.relationships[${JSON.stringify(relationship)}][${JSON.stringify(slice)}].nodes.length`));
    check(`${relationship} ${slice} fits at ${width}`,await b.evaluate(`document.documentElement.scrollWidth<=innerWidth+1&&Array.from(document.querySelectorAll('.workflow-node')).every(c=>c.scrollWidth<=c.clientWidth+1)`));
    check(`${relationship} ${slice} arrows clear intervening cards at ${width}`,await b.evaluate(`(()=>{const cards=[...document.querySelectorAll('.workflow-node')];return [...document.querySelectorAll('.measured-connections path:not(.connector-head)')].every(p=>{const [from,to]=p.dataset.edge.split('--'),m=p.getScreenCTM(),l=p.getTotalLength();return Array.from({length:80},(_,i)=>{const q=p.getPointAtLength(l*i/79),v=new DOMPoint(q.x,q.y).matrixTransform(m);return cards.filter(c=>![from,to].includes(c.dataset.node)).every(c=>{const r=c.getBoundingClientRect();return v.x<r.left||v.x>r.right||v.y<r.top||v.y>r.bottom;});}).every(Boolean);});})()`));
    check(`${relationship} ${slice} mobile order follows dependencies at ${width}`,await b.evaluate(`(()=>{const cards=[...document.querySelectorAll('.workflow-node')],graph=HarmoniaAtlas.relationships[HarmoniaView.state.relationship][HarmoniaView.state.slice];return innerWidth>700||graph.edges.every(([from,to])=>cards.findIndex(c=>c.dataset.node===from)<cards.findIndex(c=>c.dataset.node===to));})()`));
   }
  }
 }
 await b.viewport(1280,1080);await go('reviewer.html?slice=process&relationship=dependencies');
 await b.evaluate(`document.querySelector('[data-node="product/ledger/core/daml.yaml"]').click()`);const url=await b.evaluate('location.href');await go(url);
 check('Manifest node selection survives a shared URL',await b.evaluate(`document.querySelector('#review-node').textContent.includes('product/ledger/core/daml.yaml')&&HarmoniaView.state.relationship==='dependencies'`));
 for(const [page,name] of [['reviewer.html?slice=transfer&relationship=dependencies','review-manifests'],['reviewer.html?slice=financing&relationship=execution','review-execution'],['code.html?file=product/server/src/main/scala/harmonia/financing/Financing.scala','source-package-context']]) {
  await go(page);await b.wait(200);const file='design/0.2/reader/review/'+name+'.png';await b.screenshot(file);screenshots.push(file);
 }
 check('No browser exceptions or resource errors',b.errors.length===0);
 await fs.writeFile('docs/0.2/relationships-browser.json',JSON.stringify({scope:'Parsed Daml dependencies, source-reviewed execution handoffs, and package context',checks,screenshots,errors:b.errors},null,2)+'\n');console.log(checks.length+' checks passed');
} finally {if(b.errors.length)console.log(b.errors);b.close();}

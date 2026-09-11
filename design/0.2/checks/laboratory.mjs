// Exercise the complete registered recording library, not only its featured examples.
import fs from 'node:fs/promises';
import assert from 'node:assert/strict';
import {browser} from './cdp.mjs';
const b=await browser(process.argv[2]),base=process.argv[3]||'http://127.0.0.1:56202/design/0.2/';
const manifest=JSON.parse(await fs.readFile('book/edition-0.2/recordings/manifest.json','utf8'));
const checks=[],screenshots=[],output='design/0.2/reader/review';await fs.mkdir(output,{recursive:true});
const check=(name,value)=>{assert(value,name);checks.push(name);};
const go=async (page)=>{await b.navigate(new URL(page,base).href);await b.until('document.querySelector("#laboratory-stage, .chapter")');};
const capture=async name=>{const file=output+'/'+name+'.png';await b.screenshot(file);screenshots.push(file);};
try {
 await b.cdp('Network.enable');await b.cdp('Network.setCacheDisabled',{cacheDisabled:true});
 for(const width of [1280,390]) {
  await b.viewport(width,1050);
  for(const id of Object.keys(manifest.stories)) {
   await go('laboratory.html?story='+id);
   const count=await b.evaluate('document.querySelector("#attempt-select").options.length');
   for(let step=0;step<count;step++) {
    await b.evaluate(`{const e=document.querySelector('#attempt-select');e.value=${JSON.stringify(String(step))};e.dispatchEvent(new Event('change'));}`);
    await b.until(`document.querySelector('#attempt-select').value===${JSON.stringify(String(step))}&&new URLSearchParams(location.search).get('step')===${JSON.stringify(String(step))}`);
    await b.wait(50);
    check(`${id} attempt ${step+1} fits at ${width}`,await b.evaluate(`document.documentElement.scrollWidth<=innerWidth+1&&document.querySelectorAll('#laboratory-stage .workflow-node,#laboratory-stage .scene-person').length>0`));
    check(`${id} comparison remains independent at ${width}:${step}`,await b.evaluate(`document.querySelector('#comparison-status').textContent.includes('All observations match')`));
    check(`${id} connectors avoid unrelated cards at ${width}:${step}`,await b.evaluate(`(()=>{const cards=[...document.querySelectorAll('#laboratory-stage .workflow-node')];return [...document.querySelectorAll('#laboratory-stage .measured-connections path:not(.connector-head)')].every(p=>{const [from,to]=p.dataset.edge.split('--'),m=p.getScreenCTM(),l=p.getTotalLength();return Array.from({length:50},(_,i)=>{const q=p.getPointAtLength(l*i/49),v=new DOMPoint(q.x,q.y).matrixTransform(m);return cards.filter(c=>![from,to].includes(c.dataset.node)).every(c=>{const r=c.getBoundingClientRect();return v.x<r.left||v.x>r.right||v.y<r.top||v.y>r.bottom;});}).every(Boolean);});})()`));
   }
  }
  await go('laboratory.html?story=branch-approved&step=1');await capture('laboratory-branch-'+width);
 }
 await b.viewport(1280,1000);await go('laboratory.html?story=branch-approved&step=1');
 await b.evaluate(`document.querySelector('[data-node=review]').click()`);await b.until(`new URLSearchParams(location.search).get('node')==='review'`);
 const shared=await b.evaluate('location.href');await go(shared);
 check('Selected node survives a cold URL',await b.evaluate(`document.querySelector('[data-node=review]').getAttribute('aria-pressed')==='true'&&document.querySelector('.workflow-caption').textContent==='pending'`));
 await b.evaluate(`document.querySelector('#laboratory-evidence summary').click()`);await b.until(`new URLSearchParams(location.search).get('evidence')==='true'`);
 await b.evaluate(`document.querySelector('a[href="evidence/branch-approved/run.json"]').click()`);await b.until(`document.querySelector('dialog[open] pre').textContent.includes('0014c0d7')`);
 check('Raw provenance opens from the pinned bundle',true);
 await go('laboratory.html?story=package-builder&step=2');await capture('laboratory-packages');
 await go('laboratory.html?story=transfer-final-leg-rejected&step=5');await capture('laboratory-transfer');
 await go('laboratory.html?chapter=04-progression.md');
 check('Authored chapter uses the shared diagram component',await b.evaluate(`document.querySelectorAll('.chapter .workflow-node').length>0&&!document.querySelector('.chapter code.language-mermaid')`));
 check('Chapter code links open the source browser',await b.evaluate(`Array.from(document.querySelectorAll('.chapter a')).some(a=>a.href.includes('code.html?file='))`));
 await capture('laboratory-chapter');
 await b.navigate(new URL('author.html?passage=proposal-53&companion=workflow',base).href);
 await b.until(`document.querySelector('iframe').contentDocument?.querySelector('#laboratory-stage .workflow-node')`);
 await b.evaluate(`document.querySelector('iframe').contentDocument.querySelector('#next').click()`);
 await b.until(`HarmoniaView.state.detail.includes('step=1')`);const author=await b.evaluate('location.href');
 await b.navigate(author);await b.until(`document.querySelector('iframe').contentDocument?.querySelector('#attempt-select')?.value==='1'`);
 check('Author companion preserves a branch recording in its parent URL',true);
 await b.evaluate(`HarmoniaView.update({theme:'dark',text:'large'})`);await b.until(`document.querySelector('iframe').contentDocument?.documentElement.dataset.theme==='dark'`);
 check('Embedded laboratory follows parent appearance',await b.evaluate(`document.querySelector('iframe').contentDocument.documentElement.dataset.text==='large'`));
 await capture('laboratory-author');
 const local=new URL('file://'+process.cwd()+'/design/0.2/laboratory.html?story=branch-approved&evidence=true');
 await go(local.href);await b.evaluate(`document.querySelector('a[href="evidence/branch-approved/actual.md"]').click()`);await b.until(`document.querySelector('dialog[open] pre').textContent.includes('choose-approval')`);
 check('Offline playback and raw evidence work without fetch',true);
 check('No browser exceptions or resource errors',b.errors.length===0);
 const report={scope:'32 recordings and 130 moments, desktop and mobile, plus deep links, original-author embedding and offline evidence',checks,screenshots,errors:b.errors};
 await fs.writeFile('docs/0.2/laboratory-browser.json',JSON.stringify(report,null,2)+'\n');console.log(`${checks.length} checks passed`);
} finally {if(b.errors.length)console.log(b.errors);b.close();}

import fs from 'node:fs/promises';
import assert from 'node:assert/strict';
import {browser} from './cdp.mjs';
const b=await browser(process.argv[2]),base=process.argv[3]||'http://127.0.0.1:56202/design/0.2/';
const checks=[];
const ok=(name,result)=>{assert(result,name);checks.push(name);};
const budget=`(()=>{function count(d){const visible=n=>n.getClientRects().length&&getComputedStyle(n).visibility!=='hidden';const tree=d.querySelector('[data-file-tree]');return [...d.querySelectorAll('a[href],button,input:not([type=hidden]),select,textarea,summary,[role=button],[onclick]')].filter(visible).filter(n=>!tree?.contains(n)).length+(tree&&visible(tree)?1:0)+[...d.querySelectorAll('iframe')].filter(visible).reduce((s,f)=>s+(f.contentDocument?count(f.contentDocument):0),0);}return count(document)})()`;
async function inspect(label){ok(label+' interaction budget',await b.evaluate(budget)<=2);ok(label+' viewport',await b.evaluate('document.documentElement.scrollWidth<=innerWidth+1'));}
async function go(path){await b.navigate(new URL(path,base).href);await b.until('!!window.HarmoniaView');}
async function click(selector){const path=await b.evaluate(`document.querySelector(${JSON.stringify(selector)}).getAttribute('href')`);await b.evaluate(`document.querySelector(${JSON.stringify(selector)}).click()`);await b.wait(80);if(path)await b.until(`document.readyState==='complete'&&location.pathname===${JSON.stringify(new URL(path,base).pathname)}`);}
try{
 await b.viewport(390,1100);await go('book-overview.html');await click('.quiet-choices a:first-child');ok('Understand choice reaches its page',await b.evaluate("location.pathname.endsWith('/understand.html')"));await click('.quiet-choices a:first-child');ok('Public choice reaches product',await b.evaluate("location.pathname.endsWith('/workflows.html')"));await click('.quiet-choices a:first-child');await b.until("!!document.getElementById('next-step')");await click('#next-step');ok('The initial scene asks for an outcome',await b.evaluate("location.pathname.endsWith('/purchase-outcome.html')"));
 for(const branch of [1,2]){
  await go('purchase-outcome.html');await click(`.quiet-choices a:nth-child(${branch})`);await b.until("!!document.getElementById('next-step')");const seen=new Set();for(let n=0;n<20;n++){
   const current=await b.evaluate('location.href');if(!current.includes('/03-financing-to-offer.html'))break;ok(`Purchase branch ${branch} never loops at scene ${n}`,!seen.has(current));seen.add(current);await inspect(`Purchase ${branch}:${n}`);await click('#next-step');
  }ok(`Purchase branch ${branch} reaches transfer`,await b.evaluate("location.pathname.endsWith('/04-four-party-transfer.html')"));
 }
 await go('reviewer.html');const relationships=[];
 for(let n=0;n<20;n++){
  if(await b.evaluate("location.pathname.endsWith('/coverage.html')"))break;
  const label=await b.evaluate("document.getElementById('review-position').textContent");relationships.push(label);await inspect('Relationship '+label);await click('#page-next');
 }ok('Reviewer traverses all 14 frames and ends',relationships.length===14&&await b.evaluate("location.pathname.endsWith('/coverage.html')"));
 await go('author.html');const passages=await b.evaluate('HarmoniaAtlas.passages.map(p=>p.id)');
 for(let i=0;i<passages.length;i++){
  await b.wait(380);ok('Author selects '+passages[i],await b.evaluate('HarmoniaView.state.passage')===passages[i]);await b.until("!!document.querySelector('iframe').contentDocument?.querySelector('main')");await inspect('Original '+passages[i]);
  if(i===passages.length-1)await b.screenshot('design/0.2/quiet-review/author-final-390.png');await click('#page-next');
 }ok('Author reaches coverage after 26 passages',await b.evaluate("location.pathname.endsWith('/coverage.html')"));
 await go('laboratory.html');const recordings=await b.evaluate('Object.values(HarmoniaView.config.stories).map(s=>({id:s.id,count:s.presentation.units.length}))');let observations=0;
 for(const recording of recordings){for(let step=0;step<recording.count;step++){
  ok('Recording '+recording.id+':'+step,await b.evaluate(`HarmoniaView.state.story===${JSON.stringify(recording.id)}&&HarmoniaView.state.step===${step}`));await inspect('Observation '+recording.id+':'+step);observations++;await click('#next-step');
 }}ok('All 32 recordings and 130 observations end at coverage',recordings.length===32&&observations===130&&await b.evaluate("location.pathname.endsWith('/coverage.html')"));
 const chapters=['01-product','02-roles-and-trust','03-financing-to-offer','04-four-party-transfer','05-bring-an-application','06-compose-a-workflow','07-evidence-and-boundaries','08-release-and-adoption','09-proposal-context','10-context-and-references'];
 for(const chapter of chapters)for(const view of ['read','sources','evidence']){await go('chapters/'+chapter+'.html?view='+view);await inspect(chapter+' '+view);}
 for(const source of ['proposal','architecture'])for(const view of ['read','source']){await go('sources/'+source+'.html?view='+view);await inspect(source+' '+view);}
 await go('code.html');await b.until("!!document.querySelector('.code-line')");const selected=await b.evaluate('HarmoniaView.state.file');await b.evaluate("document.querySelector('#file-tree a:not([aria-current=page])').click()");await b.wait(120);const second=await b.evaluate('HarmoniaView.state.file');ok('File tree selects an actual different file',second!==selected);await b.evaluate('history.back()');await b.until(`HarmoniaView.state.file===${JSON.stringify(selected)}`);await b.evaluate('history.forward()');await b.until(`HarmoniaView.state.file===${JSON.stringify(second)}`);const address=await b.evaluate('location.href');await b.navigate(address);await b.until("!!document.querySelector('.code-line')");ok('Cold source URL restores selected file',await b.evaluate('HarmoniaView.state.file')===second);
 await go('chapters/03-financing-to-offer.html?step=2&theme=dark&text=large');await inspect('Dark large purchase');await b.screenshot('design/0.2/quiet-review/purchase-dark-large-390.png');await click('#next-step');await b.evaluate('history.back()');await b.until('HarmoniaView.state.step===2');ok('Scene Back preserves appearance',await b.evaluate("HarmoniaView.state.theme==='dark'&&HarmoniaView.state.text==='large'"));
 await go('chapters/01-product.html?view=read');const pdf=await b.cdp('Page.printToPDF',{printBackground:true});await fs.writeFile('design/0.2/quiet-review/product-chapter.pdf',Buffer.from(pdf.data,'base64'));ok('PDF contains exported reading pages',pdf.data.length>1000);
 ok('No browser errors',b.errors.length===0);await fs.writeFile('docs/0.2/quiet-reader-routes.json',JSON.stringify({base,checks,relationships,passages,recordings,observations,errors:b.errors},null,2)+'\n');console.log(JSON.stringify({checks:checks.length,relationships:relationships.length,passages:passages.length,recordings:recordings.length,observations}));
}finally{b.close();}

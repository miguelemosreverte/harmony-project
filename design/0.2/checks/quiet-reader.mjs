import fs from 'node:fs/promises';
import assert from 'node:assert/strict';
import {browser} from './cdp.mjs';
const b=await browser(process.argv[2]),base=process.argv[3]||'http://127.0.0.1:56202/design/0.2/';
const directory='design/0.2/quiet-review';await fs.mkdir(directory,{recursive:true});
const checks=[],pages=[
 ['welcome','book-overview.html','Choose whether to understand the product or verify it.','Understand or Verify; both lead to a second, specific choice.'],
 ['understand','understand.html','Choose product behavior or implementation.','Follow the product or open the actual source tree.'],
 ['verify','verify.html','Choose architecture review or original-document review.','Enter the reviewer path or the author path.'],
 ['product','workflows.html','Explain application authority through a concrete handoff.','Follow Alice or read the product mechanism.'],
 ['purchase','chapters/03-financing-to-offer.html?step=2','Understand Northbank’s observed approval.','Go back to the recorded outcomes or follow Alice’s proposal.'],
 ['outcomes','purchase-outcome.html','Choose an observed approval or refusal.','Two recorded branches, both with a finite continuation.'],
 ['transfer','chapters/04-four-party-transfer.html?step=3','Understand preparation before settlement.','Previous or next recorded scene.'],
 ['developer','code.html','Select a file and read its purpose, exact source and slice.','Use the one file tree or return to the reading paths.'],
 ['reviewer','reviewer.html','Read the first implementation relationship and its limits.','Previous or next authored relationship.'],
 ['author','author.html?passage=proposal-53','Compare original wording with its implementation context.','Previous or next original passage; the companion follows automatically.'],
 ['chapter','chapters/01-product.html','Explain what Harmonia provides in readable prose.','Previous or next chapter.'],
 ['coverage','coverage.html','Distinguish exact quotation coverage from implementation.','Return to the documents or finish the reading path.'],
 ['laboratory','laboratory.html?story=branch-approved&step=1','Inspect one actual branch observation against its committed expectation.','Previous or next observation in a finite recording sequence.'],
 ['sandbox','sandbox.html','Explain the separate live application and its participant identity.','Enter the live application when available, or follow the recording; return to Alice.']
];
const visible=`n=>n.getClientRects().length>0&&getComputedStyle(n).visibility!=='hidden'&&!n.closest('[inert]')`;
const interactions=`(()=>{const visible=${visible};function count(doc){const tree=doc.querySelector('[data-file-tree]');let nodes=[...doc.querySelectorAll('a[href],button,input:not([type=hidden]),select,textarea,summary,[role=button]')].filter(visible).filter(n=>!tree?.contains(n));if(tree&&visible(tree))nodes.push(tree);const frames=[...doc.querySelectorAll('iframe')].filter(visible);return {count:nodes.length+frames.reduce((s,f)=>s+(f.contentDocument?count(f.contentDocument).count:0),0),labels:nodes.map(n=>n===tree?'Repository file tree':n.textContent.trim().slice(0,90)||n.getAttribute('aria-label')||n.id)};}return count(document)})()`;
const check=(label,value)=>{assert(value,label);checks.push(label);};
const reports=[];
try{
 await b.cdp('Network.enable');await b.cdp('Network.setCacheDisabled',{cacheDisabled:true});
 for(const width of [1280,390])for(const [id,page,purpose,next] of pages){
  await b.viewport(width,1100);await b.navigate(new URL(page,base).href);await b.until('!!window.HarmoniaView');
  if(id==='developer')await b.until("!!document.querySelector('.code-line')");
  if(id==='author')await b.until("!!document.querySelector('iframe').contentDocument?.querySelector('main')");
  await b.wait(250);
  const controls=await b.evaluate(interactions);
  check(id+' at '+width+' has at most two interactions: '+JSON.stringify(controls.labels),controls.count<=2);
  check(id+' at '+width+' fits the viewport',await b.evaluate('document.documentElement.scrollWidth<=innerWidth+1'));
  const shot=id+'-'+width+'.png';await b.screenshot(directory+'/'+shot);
  reports.push({id,width,page,purpose,next,interactions:controls,screenshot:shot});
 }
 check('No browser exceptions or failed assets',b.errors.length===0);
 await fs.writeFile('docs/0.2/quiet-reader-browser.json',JSON.stringify({base,checks,reports,errors:b.errors},null,2)+'\n');
 const text=['# Page-by-page reader review','',...reports.filter(r=>r.width===1280).flatMap(r=>['## '+r.id,'','Purpose: '+r.purpose,'','Expected next action: '+r.next,'','Visible interactions: '+r.interactions.labels.join(' / ')+'.','',`![Desktop ${r.id}](../../design/0.2/quiet-review/${r.id}-1280.png)`,`![Mobile ${r.id}](../../design/0.2/quiet-review/${r.id}-390.png)`,''])];
 await fs.writeFile('docs/0.2/PAGE-REVIEW.md',text.join('\n')+'\n');console.log(JSON.stringify({checks:checks.length,pages:reports.length,errors:b.errors}));
}finally{b.close();}

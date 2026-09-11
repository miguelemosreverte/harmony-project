// Read-only acceptance of the final mounted book and a fresh, untouched sandbox.
import fs from 'node:fs/promises';
import assert from 'node:assert/strict';
import {createHash} from 'node:crypto';
import {execFileSync} from 'node:child_process';
import {browser} from './cdp.mjs';
const b=await browser(process.argv[2]),sessions=JSON.parse(await fs.readFile(process.argv[3],'utf8'));
const origin=new URL(sessions.bank).origin,base=origin+'/book/source/design/0.2/',checks=[];
const check=(name,value)=>{assert(value,name);checks.push(name);};
const go=async page=>{await b.navigate(new URL(page,base).href);await b.until('!!window.HarmoniaView');};
try {
 await b.cdp('Network.enable');await b.cdp('Network.setCacheDisabled',{cacheDisabled:true});
 await b.viewport(1280,1050);await go('book-overview.html');
 check('Four reader entrances are mounted by the actual server',await b.evaluate(`document.querySelectorAll('.workflow-stories>.workflow-story').length===4`));
 const atlas=await b.evaluate('HarmoniaAtlas');
 const entries=Object.entries(atlas.files);
 for(let start=0;start<entries.length;start+=8)await Promise.all(entries.slice(start,start+8).map(async ([file,metadata])=>{
  const response=await fetch(origin+'/book/source/'+file);assert.equal(response.status,200,file);
  assert.equal(createHash('sha256').update(Buffer.from(await response.arrayBuffer())).digest('hex'),metadata.sha256,file);
 }));
 check('All '+entries.length+' mounted sources match their catalog fingerprints',true);
 await go('code.html?file=product/web/site/index.html');await b.until(`document.querySelector('#source-code').textContent.toLowerCase().includes('<!doctype html>')`);
 check('HTML entry files are readable as colored source',await b.evaluate(`!!document.querySelector('#source-code .token-string')`));
 await go('laboratory.html?story=branch-approved&step=1&artifact=provenance&evidence=true');await b.until(`document.querySelector('dialog[open] pre')?.textContent.includes('0014c0d7')`);
 check('Mounted laboratory contains all 32 registered examples',await b.evaluate(`document.querySelector('#story-select').options.length===32`));
 check('Mounted evidence URL opens its historical provenance',true);
 await go('reviewer.html?slice=transfer&relationship=dependencies');check('Mounted dependency view is parsed from the manifests',await b.evaluate(`HarmoniaView.state.relationship==='dependencies'&&document.querySelector('#review-status').textContent.startsWith('Parsed manifest')`));
 await go('author.html?passage=proposal-53&companion=workflow');await b.until(`document.querySelector('iframe').contentDocument?.querySelector('#laboratory-stage .workflow-node')`);
 check('The mounted author can explore a branch beside the original passage',true);
 await b.evaluate(`document.querySelector('iframe').contentDocument.querySelector('#next').click()`);await b.until(`HarmoniaView.state.detail.includes('step=1')`);
 check('Mounted companion interaction becomes part of the parent URL',true);
 await go('reviewer.html?slice=process&relationship=dependencies');
 await b.cdp('Emulation.setEmulatedMedia',{media:'print'});await b.wait(250);
 check('Print geometry uses the same fixed A4 content width as the PDF',await b.evaluate(`Math.abs(document.body.getBoundingClientRect().width-186*96/25.4)<1&&Math.abs(Number(document.querySelector('.measured-connections').getAttribute('viewBox').split(' ')[2])-document.querySelector('.workflow-map').getBoundingClientRect().width)<1`));
 const pdf=await b.cdp('Page.printToPDF',{printBackground:true,preferCSSPageSize:true});
 await fs.writeFile('design/0.2/reader/review/manifest-review.pdf',Buffer.from(pdf.data,'base64'));
 await b.cdp('Emulation.setEmulatedMedia',{media:''});
 check('The current manifest review exports to PDF',Buffer.from(pdf.data,'base64').subarray(0,4).toString()==='%PDF');
 const launcher=new URL(sessions.bank),cap=new URLSearchParams(launcher.hash.slice(1)).get('session');
 const state=await (await fetch(origin+'/api/state',{headers:{Authorization:'Bearer '+cap}})).json();
 check('The delivered sandbox starts pending with no submitted jobs',state.application==='pending'&&state.workflow==='waiting'&&state.jobs.length===0);
 check('No browser or CSP errors',b.errors.length===0);
 const report={scope:'Read-only acceptance of the final mounted book and fresh sandbox; no commands submitted',code_revision:execFileSync('git',['rev-parse','HEAD'],{encoding:'utf8'}).trim(),source_catalog_files:entries.length,source_catalog_sha256:atlas.sha256,checks,errors:b.errors};
 await fs.writeFile('docs/0.2/reader-delivery.json',JSON.stringify(report,null,2)+'\n');
 console.log(JSON.stringify({checks:checks.length,source_files:entries.length,origin}));
} finally {if(b.errors.length)console.log(b.errors);b.close();}

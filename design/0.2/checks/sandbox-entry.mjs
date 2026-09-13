// Real browser entry, no pasted credentials and no ledger commands.
import fs from 'node:fs/promises';
import assert from 'node:assert/strict';
import {browser} from './cdp.mjs';
const b=await browser(process.argv[2]),origin=process.argv[3];
const checks=[];const check=(name,value)=>{assert(value,name);checks.push(name)};
try {
 await b.cdp('Page.addScriptToEvaluateOnNewDocument',{source:`window.requests=[];const originalFetch=window.fetch;window.fetch=(path,options)=>{requests.push({path:String(path),method:options?.method||'GET'});return originalFetch(path,options)}`});
 await b.navigate(origin);await b.evaluate('sessionStorage.clear()');await b.navigate(origin);
 for(const width of [1280,390]) {
  await b.viewport(width,width===1280?1000:844);
  await b.until('document.querySelectorAll(".entry-choices a").length===2');
  check(width+': two workspace choices, no input',await b.evaluate('document.querySelectorAll("main input,main select,main textarea").length===0&&document.querySelectorAll(".session-entry-content a,.session-entry-content button").length===2'));
  check(width+': no credential instruction',await b.evaluate('!document.body.textContent.includes("participant link")&&!document.body.textContent.includes("Paste")'));
  check(width+': no horizontal overflow',await b.evaluate('document.documentElement.scrollWidth<=innerWidth'));
  await b.screenshot(`design/0.2/handcrafted-review/${width}-home.png`);
 }
 for(const actor of ['bank','buyer','reviewer']) {
  await b.navigate(origin+'?actor='+actor+'&view=financing');
  await b.until('document.querySelector("#live-connection")?.textContent==="Connected"');
  check(actor+': proper identity',await b.evaluate(`document.querySelector('#live-identity').textContent.includes(${JSON.stringify(actor==='reviewer'?'Olivia':actor==='bank'?'Bank':'Buyer')})`));
  check(actor+': credentials remain outside the URL',await b.evaluate('!location.hash&&!location.search.includes("session")'));
  check(actor+': task links preserve character',await b.evaluate(`[...document.querySelectorAll('a[href^="?"]')].filter(a=>a.href.includes('view=')).every(a=>new URL(a.href).searchParams.get('actor')===${JSON.stringify(actor)})`));
  check(actor+': no ledger command on entry',await b.evaluate('requests.every(r=>!r.path.includes("/api/actions"))'));
 }
 await b.navigate(origin+'?actor=buyer&view=composer');await b.until('document.querySelector("#live-connection")?.textContent==="Connected"');
 check('Shared composer address selects its task',await b.evaluate('document.querySelector("#composer").getClientRects().length>0'));
 await b.navigate(origin+'?actor=bank&view=financing');await b.until('document.querySelector("#live-connection")?.textContent==="Connected"');
 // Force an expired local browser credential once; the resulting page must offer recovery, not paste instructions.
 await b.evaluate('sessionStorage.setItem("harmonia-live","expired");sessionStorage.setItem("harmonia-actor","bank")');
 await b.navigate(origin+'?actor=bank&view=financing');await b.until('document.querySelectorAll(".entry-choices a").length===2');
 await b.wait(1200);check('Expired access stops polling',await b.evaluate('requests.filter(r=>r.path==="/api/state").length===1'));
 await b.evaluate('document.querySelector(".entry-choices a").click()');await b.until('document.querySelector("#live-connection")?.textContent==="Connected"');
 check('One click recovers access',await b.evaluate('!location.hash&&location.search.includes("actor=bank")'));
 await b.navigate(origin);await b.until('document.querySelectorAll(".entry-choices a").length===2');
 check('Home remains a home screen after using a workspace',true);
 const unexpected=b.errors.filter(e=>!e.includes('401'));
 check('No unexpected browser errors',unexpected.length===0);
 await fs.writeFile('docs/0.2/sandbox-entry.json',JSON.stringify({origin,checks,errors:unexpected,expected:'One expired credential deliberately returns HTTP 401; no ledger commands submitted.'},null,2)+'\n');
 console.log(JSON.stringify({checks:checks.length}));
} finally {b.close()}

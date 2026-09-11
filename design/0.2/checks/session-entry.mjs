// Real HTTP session entry checks. Private links stay in memory and never enter reports.
import fs from 'node:fs/promises';
import assert from 'node:assert/strict';
import {browser} from './cdp.mjs';

const b=await browser(process.argv[2]);
const sessions=JSON.parse(await fs.readFile(process.argv[3],'utf8'));
const origin=new URL(sessions.bank).origin,checks=[];
const check=(name,value)=>{assert(value,name);checks.push(name);};
const go=page=>b.navigate(new URL(page,origin).href);
const click=selector=>b.evaluate(`document.querySelector(${JSON.stringify(selector)}).click()`);
const paste=async value=>{
  await b.evaluate(`document.querySelector('#participant-link').value=${JSON.stringify(value)};document.querySelector('.session-entry form').requestSubmit()`);
};
const snapshot=async()=>{
  const token=new URLSearchParams(new URL(sessions.bank).hash.slice(1)).get('session');
  const response=await fetch(origin+'/api/state',{headers:{Authorization:'Bearer '+token}});
  assert.equal(response.status,200,'The running sandbox accepts its current bank link');
  const state=await response.json();return {application:state.application,workflow:state.workflow,jobs:state.jobs};
};
let probe;
try {
  const before=await snapshot();
  await b.cdp('Network.enable');await b.cdp('Network.setCacheDisabled',{cacheDisabled:true});
  probe=(await b.cdp('Page.addScriptToEvaluateOnNewDocument',{source:`
    window.sessionStateRequests=0;
    const realFetch=window.fetch;
    window.fetch=function(url,...args){
      if(new URL(url,location.href).pathname==='/api/state')window.sessionStateRequests++;
      return realFetch.call(this,url,...args);
    };
  `})).identifier;
  await go('/?view=financing');
  await b.evaluate('sessionStorage.clear()');await go('/?view=financing');
  await b.until(`!!document.querySelector('.session-entry')`);
  await b.wait(2400);
  check('A shared financing URL shows an entry screen and sends no unauthenticated requests',await b.evaluate(`!!document.querySelector('#participant-link')&&sessionStateRequests===0`));
  check('The entry screen has no ineffective reconnect or workspace evidence controls',await b.evaluate(`!document.querySelector('#live-refresh')&&!document.querySelector('#open-workspace-evidence')&&!document.querySelector('dialog[open]')`));
  for(const width of [1280,390,304]){
    await b.viewport(width,1000);
    check('The welcome screen fits at '+width+'px',await b.evaluate(`document.documentElement.scrollWidth<=innerWidth`));
    if(width!==304)await b.screenshot('.artifacts/session-entry-'+width+'.png');
  }
  await paste('https://example.invalid/#session='+'A'.repeat(43));
  check('A participant link for another server is rejected locally',await b.evaluate(`location.origin===${JSON.stringify(origin)}&&document.querySelector('#participant-link').getAttribute('aria-invalid')==='true'&&sessionStateRequests===0`));
  await click('#session-recorded-handoff');
  await b.until(`!!document.querySelector('#story-select')`);
  check('The recorded handoff works without participant access',await b.evaluate(`new URLSearchParams(location.search).get('story')==='live-handoff'&&document.querySelector('#story-select').selectedOptions[0].textContent.includes('Approve privately, continue in another session')`));
  await go('/?view=composer&theme=dark&text=large');
  await b.until(`!!document.querySelector('#participant-link')`);
  await paste(sessions.bank);
  await b.until(`document.querySelector('#live-identity')?.textContent==='Authenticated as Bank'`);
  check('Pasting a real bank link enters the selected task and preserves appearance',await b.evaluate(`location.search==='?view=composer&theme=dark&text=large'&&location.hash===''&&document.documentElement.dataset.theme==='dark'&&document.documentElement.dataset.text==='large'&&!!document.querySelector('dialog[open] #composition-editor')`));
  await go('/book/source/design/0.2/sandbox.html?task=financing');
  await b.until(`!!document.querySelector('#live-entry a')`);
  await click('#live-entry a');
  await b.until(`document.querySelector('#live-identity')?.textContent==='Authenticated as Bank'`);
  check('Book to workspace navigation reuses the authenticated participant tab',await b.evaluate(`location.search==='?view=financing'&&location.hash===''`));
  const stale='A'.repeat(43);
  await b.evaluate(`sessionStorage.setItem('harmonia-live',${JSON.stringify(stale)});sessionStorage.setItem('harmonia-request-'+${JSON.stringify(stale)},'{}')`);
  await go('/?view=financing');await b.until(`!!document.querySelector('.session-entry')`);
  await b.wait(2500);
  check('An expired session stops after one rejection and explains how to recover',await b.evaluate(`sessionStateRequests===1&&document.querySelector('h1').textContent==='Open a current participant link.'&&!document.querySelector('#live-refresh')`));
  check('Session expiry preserves the unconfirmed request for reconciliation',await b.evaluate(`sessionStorage.getItem('harmonia-request-'+${JSON.stringify(stale)})==='{}'`));
  const expected401=b.errors.filter(error=>error.includes('401')&&error.includes('/api/state'));
  check('The stale-session probe accounts for exactly one expected HTTP 401',expected401.length===1);
  b.errors.splice(0,b.errors.length,...b.errors.filter(error=>!expected401.includes(error)));
  await paste(sessions.buyer);
  await b.until(`document.querySelector('#live-identity')?.textContent==='Authenticated as Buyer'`);
  check('A current buyer link recovers an expired session',await b.evaluate(`location.hash===''&&!document.querySelector('.session-entry')`));
  await b.evaluate('sessionStorage.clear()');await go('/?view=evidence');
  await b.until(`!!document.querySelector('#participant-link')`);
  // Opening a provisioned fragment must also recover a tab already at the same address.
  await b.cdp('Page.navigate',{url:origin+'/?view=evidence'+new URL(sessions.reviewer).hash});
  await b.until(`document.querySelector('#live-identity')?.textContent==='Authenticated as Olivia · observer'`);
  check('A direct reviewer fragment enters without granting financing actions',await b.evaluate(`location.hash===''&&!!document.querySelector('dialog[open]')&&document.querySelectorAll('.financing-action button').length===0`));
  check('No ledger commands were submitted by the entry checks',JSON.stringify(before)===JSON.stringify(await snapshot()));
  check('No unexpected browser or CSP errors',b.errors.length===0);
  await fs.writeFile('docs/0.2/session-entry-browser.json',JSON.stringify({scope:'Unauthenticated URLs, private participant links, expired-session recovery and book navigation against the running server; no ledger commands submitted',checks,errors:b.errors},null,2)+'\n');
  console.log(JSON.stringify({checks:checks.length,errors:b.errors}));
} finally {
  if(probe)await b.cdp('Page.removeScriptToEvaluateOnNewDocument',{identifier:probe});
  b.close();
}

// Exercise submissions through the carousel-based editor on the disposable local sandbox.
import fs from 'node:fs/promises';import assert from 'node:assert/strict';import {browser} from './cdp.mjs';
const b=await browser(process.argv[2]),sessions=JSON.parse(await fs.readFile(process.argv[3],'utf8')),checks=[];
const check=(name,value)=>{assert(value,name);checks.push(name);};
const click=s=>b.evaluate(`document.querySelector(${JSON.stringify(s)}).click()`);
const actor=async(role,query)=>{const u=new URL(sessions[role]);u.search=query;await b.navigate(u.href);await b.until('document.querySelector("#live-connection")?.textContent.includes("Connected")');};
const input=async(id,value)=>b.evaluate(`(()=>{const n=document.getElementById(${JSON.stringify(id)});n.value=${JSON.stringify(value)};n.dispatchEvent(new Event('input'))})()`);
try{
 await b.cdp('Network.enable');await b.cdp('Network.setCacheDisabled',{cacheDisabled:true});await b.viewport(1280,1000);
 await actor('bank','view=financing');await b.until('!!document.getElementById("live-approve-financing")');await click('#live-approve-financing');await b.until('document.querySelector(".scene-title")?.textContent==="Your approval is issued."');check('Bank approval becomes observed approval',await b.evaluate('document.querySelector(".scene-approval").dataset.status==="complete"'));
 await actor('buyer','view=financing');await b.until('!!document.getElementById("live-publish-approval")');await click('#live-publish-approval');await b.until('document.querySelector("#live-workflow")?.textContent==="Workflow: complete"');check('Buyer continuation completes the handoff',true);
 await actor('bank','view=composer&new=1');await b.until('!!document.querySelector("#composition-name")');await input('composition-name','Carousel agreement');await click('[data-stop="Reference"]');await input('composition-reference','carousel-'+Date.now());
 await click('[data-stop="Actor(0)"]');await click('[data-stop="answer-0"]');await click('[data-stop="answer-0"]');await click('[data-stop="answer-0"]');
 await click('[data-stop="Actor(1)"]');await click('[data-stop="answer-1"]');await click('[data-stop="answer-1"]');await b.until('!!document.querySelector("#composition-propose")');
 check('Branch choices preserve the intended typed plan',await b.evaluate('(()=>{const p=JSON.parse(new URL(location.href).searchParams.get("draft"));return p.name==="Carousel agreement"&&p.steps[0].actor==="bank"&&p.steps[1].actor==="buyer"})()'));
 await click('#composition-propose');await b.until('document.querySelector("#composition-observations")?.textContent.includes("Awaiting buyer consent")');check('Proposed plan is recorded without bypassing consent',true);
 await actor('buyer','view=composer');await b.until('!!document.querySelector("button[id^=compose-accept]")');await click('button[id^=compose-accept]');await b.until('document.querySelector(".composition-status")?.textContent.includes("In progress")');
 await actor('bank','view=composer');await b.until('!!document.querySelector("button[id^=compose-execute]")');await click('button[id^=compose-execute]');await b.until('!document.querySelector("button[id^=compose-execute]")');
 await actor('buyer','view=composer');await b.until('!!document.querySelector("button[id^=compose-execute]")');await click('button[id^=compose-execute]');await b.until('document.querySelector(".composition-status")?.textContent.includes("Complete")');check('Both assigned actions complete after participant consent',true);
 await b.screenshot('design/0.2/carousel-review/live-consented-complete.png');
 await actor('bank','view=packages');await b.until('!!document.querySelector("#builder-remote")');await click('#builder-remote');await click('#builder-retrieve');await b.until('!!document.querySelector("button[id^=builder-generate]")');await b.evaluate('window.retained=document.querySelector(".package-stage .workflow-diagram")');await click('button[id^=builder-generate]');await b.until('!!document.querySelector("button[id^=builder-download]")');check('Compiler result retains the package infographic',await b.evaluate('retained===document.querySelector(".package-stage .workflow-diagram")'));check('Compilation is observed separately from registration',await b.evaluate('document.querySelector("[data-node=compile]").dataset.state==="complete"'));
 await b.screenshot('design/0.2/carousel-review/live-package-compiled.png');check('No browser errors',b.errors.length===0);
 await fs.writeFile('docs/0.2/carousel-ledger.json',JSON.stringify({scope:'Real commands through the Scala UI, service, Canton and Daml compiler on one disposable local network',checks,errors:b.errors},null,2)+'\n');console.log(JSON.stringify({checks:checks.length}));
}finally{b.close()}

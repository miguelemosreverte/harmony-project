// Capture the four narrated purchase beats from the actual HTML renderer.
// Usage: node book/edition-0.2/presentation.mjs <owned-local-CDP-endpoint> [book-URL] [output-directory]
import fs from 'node:fs/promises';
import path from 'node:path';
import {spawnSync} from 'node:child_process';
import assert from 'node:assert/strict';
import {browser} from '../../design/0.2/checks/cdp.mjs';
const endpoint=process.argv[2];
const entry=new URL(process.argv[3]||'http://127.0.0.1:56202/design/0.2/chapters/03-financing-to-offer.html');
assert(['127.0.0.1','localhost'].includes(entry.hostname),'Record a local book, without participant capabilities.');
assert(entry.pathname.endsWith('/chapters/03-financing-to-offer.html'),'Use the recorded purchase chapter.');
entry.search='?story=purchase-approved&present=1';entry.hash='';
const output=path.resolve(process.argv[4]||'.artifacts/presentation');await fs.mkdir(output,{recursive:true});
const b=await browser(endpoint),frames=[];
try {
  await b.viewport(1280,1000);
  for(const [index,step] of [0,2,5,8].entries()) {
    entry.searchParams.set('step',String(step));await b.navigate(entry.href);
    await b.until('!!document.querySelector(".scene-title")');
    const file=path.join(output,`scene-${index}.png`);await b.screenshot(file);
    frames.push({step,seconds:6,caption:await b.evaluate('document.querySelector(".scene-title").textContent'),url:entry.href});
  }
  const result=spawnSync('ffmpeg',['-hide_banner','-loglevel','error','-y','-framerate','1/6','-i',path.join(output,'scene-%d.png'),'-c:v','libx264','-threads','2','-r','30','-pix_fmt','yuv420p','-movflags','+faststart',path.join(output,'purchase.mp4')],{stdio:'inherit'});
  if(result.error)throw result.error;
  assert.equal(result.status,0,'ffmpeg could not encode the presentation');
  await fs.writeFile(path.join(output,'presentation.json'),JSON.stringify({kind:'Recorded ledger presentation; HTML screenshots, no live commands',width:1280,height:1000,frames},null,2)+'\n');
  console.log(path.join(output,'purchase.mp4'));
} finally {b.close();}

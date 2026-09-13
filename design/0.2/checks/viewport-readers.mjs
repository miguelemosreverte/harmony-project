import fs from 'node:fs/promises';
import {browser} from './cdp.mjs';
const b=await browser(process.argv[2]),base=process.argv[3],exportRoot=process.argv[4];
const checks=[],failures=[];
async function inspect(name,canvas=true){
 await b.wait(200);
 const d=await b.evaluate(`(async()=>{const s=[...document.querySelectorAll('.presentation-stage')].find(n=>n.getClientRects().length),a=s?.querySelector('.support-art'),url=a&&getComputedStyle(a).backgroundImage.match(/url\\("?([^"\\)]+)"?\\)/)?.[1];if(url){const i=new Image();i.src=url;await i.decode();}const r=n=>n?.getBoundingClientRect().toJSON(),nav=[...document.querySelectorAll('.quiet-paging')].find(n=>n.getClientRects().length);return {w:innerWidth,h:innerHeight,height:document.documentElement.scrollHeight,width:document.documentElement.scrollWidth,y:scrollY,stage:r(s),art:r(a),support:r(s?.querySelector('.workflow-support')),nav:r(nav),canvas:document.documentElement.dataset.canvas,illustration:a?.dataset.illustration}})()`);
 checks.push({name,...d});if(d.width>d.w+1||(canvas&&(d.height>d.h+1||d.y||!d.art||d.art.height<65||d.support.bottom>d.nav.top-2)))failures.push({name,...d});
 const shot=await b.cdp('Page.captureScreenshot',{format:'png',clip:{x:0,y:0,width:d.w,height:d.h,scale:1}});await fs.writeFile('design/0.2/moment-review/'+name+'.png',Buffer.from(shot.data,'base64'));
}
try{
 await b.cdp('Network.enable');await b.cdp('Network.setCacheDisabled',{cacheDisabled:true});
 for(const [w,h] of [[1280,900],[390,844],[320,568]]){
 await b.viewport(w,h);await b.navigate(base+'reviewer.html');await b.until('!!window.HarmoniaAtlas');
 const chapters=await b.evaluate('Object.keys(HarmoniaAtlas.chapterDiagrams)');
 for(const c of chapters){await b.navigate(base+'chapters/'+c+'.html');await b.until('!!document.querySelector(".support-art[data-illustration]")');await inspect(`readers-${w}-${c}`)}
 await b.navigate(base+'reviewer.html?slice=process&relationship=execution');await b.until('!!document.querySelector("#review-diagram .support-art")');await inspect(`readers-${w}-reviewer`);
 for(const [id,q] of [['code','code.html?file=product/web/src/main/scala/harmonia/composition/ComposerView.scala'],['author','author.html?passage=architecture-88']]){await b.navigate(base+q);await b.until('!!document.querySelector(".support-art[data-illustration]")');await inspect(`readers-${w}-${id}`,false)}
 for(const story of ['branch-approved','purchase-approved','transfer-approved']){await b.navigate(exportRoot+'laboratory.html?story='+story+'&step=1');await b.until('!!document.querySelector("#laboratory-stage .support-art[data-illustration]")');await inspect(`export-${w}-${story}`);}
 // The header switches to the full evidence without losing the selected observation.
 await b.evaluate('document.querySelector("header a").click()');await b.until('!!document.querySelector(".observed-comparison")?.getClientRects().length');checks.push({name:`export-${w}-evidence`,passed:await b.evaluate('new URLSearchParams(location.search).get("evidence")==="true"&&document.querySelector("#laboratory-stage").hidden')});
 }
 await fs.writeFile('docs/0.2/viewport-readers.json',JSON.stringify({checks,failures,errors:b.errors},null,2)+'\n');console.log(JSON.stringify({checks:checks.length,failures,errors:b.errors}));
}finally{b.close()}

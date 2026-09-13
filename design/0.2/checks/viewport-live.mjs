import fs from 'node:fs/promises';
import {browser} from './cdp.mjs';
const b=await browser(process.argv[2]),moments=[],failures=[];
try{
 await b.cdp('Network.enable');await b.cdp('Network.setCacheDisabled',{cacheDisabled:true});
 await b.cdp('Page.addScriptToEvaluateOnNewDocument',{source:`window.commands=[];const f=window.fetch;window.fetch=(p,o)=>{if(o?.method==='POST'&&!String(p).includes('/api/sandbox/entry/'))commands.push(String(p));return f(p,o)}`});
 for(const [w,h] of [[1280,900],[390,844],[320,568],[1024,768]]){
 await b.viewport(w,h);
 for(const view of ['financing','composer','packages']){
 await b.navigate(process.argv[3]+'?actor=bank&view='+view);await b.until('document.querySelector("#live-connection")?.textContent==="Connected"');
 const stops=await b.evaluate(`[...document.querySelectorAll('.workflow-dock .carousel-step')].filter(n=>n.getClientRects().length).map(n=>n.dataset.stop)`);
 for(const stop of stops){await b.evaluate(`[...document.querySelectorAll('.carousel-step')].find(n=>n.getClientRects().length&&n.dataset.stop===${JSON.stringify(stop)}).click()`);await b.wait(100);
 const d=await b.evaluate(`(async()=>{const stage=[...document.querySelectorAll('.presentation-stage')].find(n=>n.getClientRects().length),art=stage.querySelector('.support-art'),url=getComputedStyle(art).backgroundImage.match(/url\\("?([^"\\)]+)"?\\)/)?.[1];if(url){const i=new Image();i.src=url;await i.decode();}const rect=n=>n.getBoundingClientRect().toJSON(),support=stage.querySelector('.workflow-support'),dock=[...document.querySelectorAll('.workflow-dock')].find(n=>n.getClientRects().length);return {h:innerHeight,scroll:document.documentElement.scrollHeight,scrollWidth:document.documentElement.scrollWidth,y:scrollY,stage:rect(stage),art:rect(art),support:rect(support),dock:rect(dock),illustration:art.dataset.illustration,size:getComputedStyle(art).backgroundSize,commands:commands.length};})()`);
 moments.push({w,view,stop,...d});if(d.scroll>h+1||d.scrollWidth>w+1||d.y||d.support.bottom>d.dock.top-2||d.art.height<65||d.size.split(',').some(s=>s.trim()!=='contain')||d.commands)failures.push({w,view,stop,...d});
 const shot=await b.cdp('Page.captureScreenshot',{format:'png',clip:{x:0,y:0,width:w,height:h,scale:1}});await fs.writeFile(`design/0.2/moment-review/live-${w}-${view}-${stop}.png`,Buffer.from(shot.data,'base64'));
 }
 }
 }
 await fs.writeFile('docs/0.2/viewport-live.json',JSON.stringify({scope:'Current provisioned Bank workspace; carousel inspection only. The existing composition is awaiting consent. No ledger commands.',moments,failures,errors:b.errors},null,2)+'\n');console.log(JSON.stringify({moments:moments.length,failures,errors:b.errors}));
}finally{b.close()}

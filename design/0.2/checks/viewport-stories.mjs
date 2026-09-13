import fs from 'node:fs/promises';
import {browser} from './cdp.mjs';
const b=await browser(process.argv[2]),base=process.argv[3]||'http://127.0.0.1:56202/design/0.2/';
const out='design/0.2/moment-review';await fs.mkdir(out,{recursive:true});
const moments=[],failures=[],repeats=[],shots=[];
const visible='[...document.querySelectorAll(".presentation-stage")].find(n=>n.getClientRects().length)';
async function inspect(name,shot){
 const data=await b.evaluate(`(async()=>{const stage=${visible},art=stage.querySelector('.support-art'),support=stage.querySelector('.workflow-support');const url=getComputedStyle(art).backgroundImage.match(/url\\("?([^"\\)]+)"?\\)/)?.[1];if(url){const i=new Image();i.src=url;await i.decode();}await new Promise(r=>requestAnimationFrame(()=>requestAnimationFrame(r)));const nav=[...document.querySelectorAll('.quiet-paging')].find(n=>n.getClientRects().length);const rect=n=>n.getBoundingClientRect().toJSON();return {w:innerWidth,h:innerHeight,scroll:document.documentElement.scrollHeight,scrollWidth:document.documentElement.scrollWidth,y:scrollY,stage:rect(stage),art:rect(art),support:rect(support),nav:rect(nav),illustration:art.dataset.illustration,lines:[...support.querySelectorAll('.support-speech')].map(n=>n.textContent),nodes:[...stage.querySelectorAll('.workflow-node')].map(n=>({id:n.dataset.node,rect:rect(n),overflow:n.scrollHeight>n.clientHeight+1})),style:getComputedStyle(art).backgroundSize,url:location.href};})()`);
 const issues=[];
 if(data.scroll>data.h+1||data.scrollWidth>data.w+1||data.y!==0)issues.push('page scroll');
 if(data.support.bottom>data.nav.top-2)issues.push('support touches dock');
 if(data.art.height<65||data.art.width<100)issues.push('image too small');
 if(data.stage.top<40||data.stage.bottom>data.h)issues.push('stage outside viewport');
 if(data.nodes.some(n=>n.overflow||n.rect.bottom>data.art.top+1))issues.push('diagram content overflow');
 if(data.style.split(',').some(s=>s.trim()!=='contain'))issues.push('image is not contained');
 moments.push({name,...data});if(issues.length)failures.push({name,issues,...data});
 if(shot){const path=out+'/'+name+'.png';const r=await b.cdp('Page.captureScreenshot',{format:'png',clip:{x:0,y:0,width:data.w,height:data.h,scale:1}});await fs.writeFile(path,Buffer.from(r.data,'base64'));shots.push({name,path,url:data.url});}
 return data;
}
try{
 await b.cdp('Network.enable');await b.cdp('Network.setCacheDisabled',{cacheDisabled:true});
 for(const [w,h] of [[1280,900],[390,844],[320,568],[1024,768]]){
  await b.viewport(w,h);await b.navigate(base+'laboratory.html');await b.until('!!document.querySelector("#laboratory-stage .support-art")');
  const stories=await b.evaluate('Object.keys(HarmoniaView.config.stories)');
  for(const story of stories){
   await b.evaluate(`HarmoniaView.update({story:${JSON.stringify(story)},step:0})`);let last;
   const count=await b.evaluate('HarmoniaView.config.stories[HarmoniaView.state.story].presentation.units.length');
   for(let step=0;step<count;step++){
    await b.evaluate(`document.querySelector('.carousel-step[data-stop="${step}"]').click()`);
    const d=await inspect(`${w}-${story}-${step}`,w===390||w===1280);
    if(last&&last.illustration===d.illustration)repeats.push({story,step,illustration:d.illustration,lines:d.lines});last=d;
   }
  }
  for(const chapter of ['03-financing-to-offer','04-four-party-transfer']){
   await b.navigate(base+'chapters/'+chapter+'.html');await b.until('!!document.querySelector("#story-scene .support-art")');
   const stops=await b.evaluate('[...document.querySelectorAll("#chapter-try .carousel-step")].map(n=>n.dataset.stop)');
   for(const stop of stops){await b.evaluate(`document.querySelector('#chapter-try .carousel-step[data-stop="${stop}"]').click()`);await inspect(`${w}-${chapter}-${stop.replaceAll(':','-')}`,w===390||w===1280);}
  }
 }
 await fs.writeFile('docs/0.2/viewport-stories.json',JSON.stringify({moments,failures,repeats,shots,errors:b.errors},null,2)+'\n');
 console.log(JSON.stringify({moments:moments.length,failures:failures.filter((f,i)=>i<20).map(f=>({name:f.name,issues:f.issues})),failureCount:failures.length,repeats:repeats.filter((r,i)=>repeats.findIndex(o=>o.story===r.story&&o.step===r.step)===i),screenshots:shots.length,errors:b.errors},null,2));
}finally{b.close()}

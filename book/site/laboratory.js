// The Scala reader owns navigation. This bridge lets the author pane follow its URL.
(() => {
  window.HarmoniaGuide = document.currentScript.dataset.guide;
  const evidence=window.HarmoniaLaboratoryEvidence, fresh=window.HarmoniaRunRecordings||{};
  evidence.stories=[...evidence.stories.map(story=>fresh[story.id]||story),...Object.values(fresh).filter(story=>!evidence.stories.some(old=>old.id===story.id))];
  window.HarmoniaEvidenceFiles={...window.HarmoniaEvidenceFiles,...window.HarmoniaRunEvidenceFiles};
  const listeners=new Set();
  window.HarmoniaView={subscribe(listener){listeners.add(listener);listener();}};
  addEventListener('harmonia-view',()=>{for(const listener of listeners)listener();});
  const turn=direction=>{const nav=document.querySelector('.quiet-paging');if(nav?.getClientRects().length)nav.querySelectorAll(':scope > button, :scope > a')[direction]?.click();};
  addEventListener('keydown',e=>{if(e.target.closest('input,textarea,pre,.step-carousel')||e.altKey||e.metaKey||e.ctrlKey)return;if(e.key==='ArrowLeft'||e.key==='ArrowRight'){e.preventDefault();turn(e.key==='ArrowLeft'?0:1);}});
  let start;
  addEventListener('touchstart',e=>{if(!e.target.closest('main')||e.target.closest('pre'))return;start={x:e.touches[0].clientX,y:e.touches[0].clientY};},{passive:true});
  addEventListener('touchend',e=>{if(!start)return;const dx=e.changedTouches[0].clientX-start.x,dy=e.changedTouches[0].clientY-start.y;start=null;if(Math.abs(dx)>55&&Math.abs(dx)>Math.abs(dy)*1.5)turn(dx<0?1:0);},{passive:true});
})();

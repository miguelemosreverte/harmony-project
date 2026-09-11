(() => {
  const scene=document.getElementById('story-carousel');if(!scene)return;
  const view=HarmoniaView,{node}=HarmoniaReader;
  const controls=node('nav','','presentation-controls');controls.setAttribute('aria-label','Presentation');
  const label=node('span','Recorded ledger demonstration'),play=node('button','Play'),exit=node('button','Leave presentation');
  controls.append(label,play,exit);scene.after(controls);
  let timer;
  play.addEventListener('click',()=>view.update({autoplay:view.state.autoplay?0:1}));
  exit.addEventListener('click',()=>view.update({present:0,autoplay:0}));
  view.subscribe(s=>{
    controls.hidden=!s.present;play.textContent=s.autoplay?'Pause':'Play';clearInterval(timer);
    if(s.present&&s.autoplay&&!matchMedia('(prefers-reduced-motion: reduce)').matches)timer=setInterval(()=>document.getElementById('next-step').click(),6000);
    else if(s.autoplay&&matchMedia('(prefers-reduced-motion: reduce)').matches){view.update({autoplay:0},{replace:true});}
  });
  addEventListener('keydown',e=>{if(!view.state.present||scene.contains(e.target)||e.target.closest('input,select,textarea'))return;if(['ArrowLeft','ArrowRight'].includes(e.key)){e.preventDefault();document.getElementById(e.key==='ArrowLeft'?'previous-step':'next-step').click();}});
  addEventListener('pagehide',()=>clearInterval(timer));
})();

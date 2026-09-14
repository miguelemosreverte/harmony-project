/* This reads recorded states. It never submits a ledger command. */
(() => {
  const log = JSON.parse(document.getElementById('event-log').textContent);
  const stage = document.getElementById('demo-stage');
  const slides = [...stage.querySelectorAll('.demo-slide')];
  const previous = document.getElementById('demo-previous');
  const next = document.getElementById('demo-next');
  const progress = document.getElementById('demo-progress');
  const announce = document.getElementById('demo-announcement');
  let demo, path, step;
  const address = (d, p, s) => ({demo:d.id, path:p.id, step:s.id});
  const current = () => address(demo, path, step);
  const key = v => [v.demo, v.path, v.step].join('/');
  function read() {
    const url = new URL(location.href), q = url.searchParams, fallback = log.cursor;
    demo = log.demos.find(d => d.id === (q.get('demo') ?? fallback.demo)) ?? log.demos[0];
    path = demo.paths.find(p => p.id === (q.get('path') ?? fallback.path)) ?? demo.paths[0];
    step = path.steps.find(s => s.id === (q.get('step') ?? fallback.step)) ?? path.steps[0];
    for (const [name,value] of Object.entries(current())) q.set(name,value);
    if (url.href !== location.href) history.replaceState(null,'',url);
    for (const slide of slides) {
      const visible = slide.dataset.key === key(current());
      slide.dataset.current = String(visible);
      slide.inert = !visible; slide.setAttribute('aria-hidden',String(!visible));
    }
    const chapter = log.demos.indexOf(demo), index = path.steps.indexOf(step);
    previous.disabled = chapter === 0 && index === 0;
    next.disabled = chapter === log.demos.length-1 && index === path.steps.length-1;
    previous.title = index ? path.steps[index-1].title : chapter ? log.demos[chapter-1].title : 'Beginning of demonstrations';
    next.title = index < path.steps.length-1 ? path.steps[index+1].title : chapter < log.demos.length-1 ? log.demos[chapter+1].title : 'End of demonstrations';
    renderHarmoniaCarousel(progress,JSON.stringify({selected:key(current()),paths:demo.paths.map(p => ({
      id:p.id,label:p.label,steps:p.steps.map((s,i) => ({id:key(address(demo,p,s)),label:s.title,
        state:p === path && i <= index ? (s.outcome === 'rejected' ? 'refused' : i === index ? 'current' : 'complete') : 'pending'}))
    }))}), id => { const [d,p,s]=id.split('/'); select({demo:d,path:p,step:s}); });
    document.getElementById('demo-evidence').href = log.presentation.evidence_url+'#'+demo.id;
    document.title = step.title+' · Harmonia Canton demos';
    announce.textContent = `${chapter+1} of ${log.demos.length}. ${demo.title} ${step.title}`;
    document.documentElement.dataset.playing='true';
  }
  function select(value) {
    if(key(value) === key(current()))return;
    const url=new URL(location.href);
    for(const [name,v] of Object.entries(value))url.searchParams.set(name,v);
    history.pushState(null,'',url);read();stage.scrollTop=0;
  }
  function move(delta) {
    const index=path.steps.indexOf(step)+delta,chapter=log.demos.indexOf(demo)+delta;
    if(index>=0 && index<path.steps.length)select(address(demo,path,path.steps[index]));
    else if(chapter>=0 && chapter<log.demos.length){
      const d=log.demos[chapter],p=d.paths[0],s=delta>0?p.steps[0]:p.steps.at(-1);
      select(address(d,p,s));
    }
  }
  previous.addEventListener('click',()=>move(-1));next.addEventListener('click',()=>move(1));
  addEventListener('popstate',read);
  addEventListener('keydown',e=>{
    if(e.defaultPrevented||e.altKey||e.ctrlKey||e.metaKey||e.target.closest('input,textarea,.step-carousel'))return;
    if(e.key==='ArrowLeft'||e.key==='ArrowRight'){e.preventDefault();move(e.key==='ArrowRight'?1:-1);}
    if(e.key==='Home'||e.key==='End'){e.preventDefault();select(address(demo,path,e.key==='Home'?path.steps[0]:path.steps.at(-1)));}
  });
  let touch;
  stage.addEventListener('touchstart',e=>{touch=e.touches.length===1?{x:e.touches[0].clientX,y:e.touches[0].clientY}:null;},{passive:true});
  stage.addEventListener('touchcancel',()=>{touch=null;},{passive:true});
  stage.addEventListener('touchend',e=>{
    if(!touch)return;
    const dx=e.changedTouches[0].clientX-touch.x,dy=e.changedTouches[0].clientY-touch.y;touch=null;
    if(Math.abs(dx)>55&&Math.abs(dx)>Math.abs(dy)*1.5)move(dx<0?1:-1);
  },{passive:true});
  document.getElementById('demo-export').addEventListener('click',e=>{
    e.preventDefault();
    const url=URL.createObjectURL(new Blob([JSON.stringify({...log,cursor:current()},null,2)+'\n'],{type:'application/json'}));
    const a=document.createElement('a');a.href=url;a.download='harmonia-event-log.json';a.click();
    setTimeout(()=>URL.revokeObjectURL(url),1000);
  });
  document.getElementById('demo-evidence').addEventListener('click',e=>{
    const back=location.href.replaceAll('&','&amp;').replaceAll('"','&quot;');
    const html=log.presentation.evidence_html.replace('href="index.html"',`href="${back}"`);
    e.currentTarget.href=URL.createObjectURL(new Blob([html],{type:'text/html'}))+'#'+demo.id;
  });
  addEventListener('beforeprint',()=>slides.forEach(s=>{s.inert=false;s.removeAttribute('aria-hidden');}));
  addEventListener('afterprint',read);
  read();history.scrollRestoration='manual';
})();

(() => {
  const covers=[...document.querySelectorAll('.path-cover')];
  const stage=document.getElementById('path-stage');
  const previous=document.getElementById('path-previous');
  const next=document.getElementById('path-next');
  const open=document.getElementById('open-path');
  const destinations={
    user:['../design/0.2/chapters/03-financing-to-offer.html?view=try&story=purchase-approved&step=0','Open the user path'],
    investor:['investor/index.html?demo=core&path=join&step=chosen','Open the Canton demos'],
    architecture:['architecture/index.html?step=0','Open the architecture'],
    implementation:['../design/0.2/code.html?file=product/ledger/interfaces/daml/Harmonia/Action.daml','Open the implementation']
  };
  let selected=0;
  function read(){
    const url=new URL(location.href);
    selected=Math.max(0,covers.findIndex(c=>c.dataset.branch===url.searchParams.get('branch')));
    const cover=covers[selected],branch=cover.dataset.branch;
    url.searchParams.set('branch',branch);
    if(url.href!==location.href)history.replaceState(null,'',url);
    covers.forEach((c,i)=>{c.dataset.current=String(i===selected);c.inert=i!==selected;c.setAttribute('aria-hidden',String(i!==selected));});
    const [href,label]=destinations[branch];open.href=href;open.firstChild.textContent=label+' ';
    previous.disabled=selected===0;next.disabled=selected===covers.length-1;
    previous.title=covers[selected-1]?.dataset.title??'First path';next.title=covers[selected+1]?.dataset.title??'Last path';
    renderHarmoniaCarousel(document.getElementById('path-progress'),JSON.stringify({selected:branch,paths:[{
      id:'readers',label:'Reading paths',steps:covers.map(c=>({id:c.dataset.branch,label:c.dataset.title,state:c===cover?'current':'pending'}))
    }]}),id=>select(covers.findIndex(c=>c.dataset.branch===id)));
    document.getElementById('path-announcement').textContent=`${selected+1} of 4: ${cover.dataset.title}`;
    document.title=cover.dataset.title+' · Choose your path · Harmonia';
    document.documentElement.dataset.ready='true';
  }
  function select(index){
    if(index<0||index>=covers.length||index===selected)return;
    const url=new URL(location.href);url.searchParams.set('branch',covers[index].dataset.branch);
    history.pushState(null,'',url);read();
  }
  previous.addEventListener('click',()=>select(selected-1));next.addEventListener('click',()=>select(selected+1));
  addEventListener('popstate',read);
  addEventListener('keydown',e=>{
    if(e.defaultPrevented||e.altKey||e.ctrlKey||e.metaKey||e.target.closest('.step-carousel'))return;
    const index={ArrowLeft:selected-1,ArrowRight:selected+1,Home:0,End:covers.length-1}[e.key];
    if(index!==undefined){e.preventDefault();select(index);}
  });
  let touch;
  stage.addEventListener('touchstart',e=>{touch=e.touches.length===1?{x:e.touches[0].clientX,y:e.touches[0].clientY}:null;},{passive:true});
  stage.addEventListener('touchcancel',()=>touch=null,{passive:true});
  stage.addEventListener('touchend',e=>{if(!touch)return;const dx=e.changedTouches[0].clientX-touch.x,dy=e.changedTouches[0].clientY-touch.y;touch=null;if(Math.abs(dx)>55&&Math.abs(dx)>Math.abs(dy)*1.5)select(selected+(dx<0?1:-1));},{passive:true});
  Promise.all([...document.images].map(i=>i.decode().catch(()=>{}))).then(read);
})();

// Keep the shell and renderer loaded. A chapter owns its listeners until the next mount.
(() => {
  const view=HarmoniaView;
  // History changes the document base URL; persistent head assets need absolute addresses.
  for(const link of document.head.querySelectorAll('link[href]'))link.href=link.href;
  const initial=JSON.parse(document.getElementById('view-config').textContent);
  const base=new URL(initial.base,location.href);
  let path=location.pathname, shown=location.href, request=0;
  const chapters=new Map([[path,document.documentElement.outerHTML]]);
  history.scrollRestoration='manual';

  function decorate(s){
    shown=location.href;
    Object.assign(document.documentElement.dataset,{theme:s.theme,text:s.text,embed:String(s.embed),presentation:String(s.present)});
    for(const panel of document.querySelectorAll('[data-mode]'))panel.hidden=panel.dataset.mode!==s.view;
    document.querySelector('.quiet-location').textContent=document.getElementById('story-scene')&&s.view==='try'?'Recorded example':'The field guide';
    const heading=document.querySelector('.chapter-heading');if(heading)heading.hidden=s.view==='try';
    const lab=document.getElementById('laboratory-stage');
    const review=document.getElementById('review-diagram');
    const illustrated=document.querySelector('#chapter-read [data-chapter-diagram]');
    const canvas=(!s.embed&&s.text!=='large'&&s.detail!=='text')&&
      ((document.getElementById('story-scene')&&s.view==='try')||(lab&&s.view!=='evidence')||(illustrated&&s.view==='read')||review);
    document.documentElement.dataset.canvas=String(!!canvas);
    if(lab){lab.hidden=s.view==='evidence';document.getElementById('laboratory-observation').hidden=s.view!=='evidence';}
    let reading=document.getElementById('reading-detail');
    if((lab||illustrated||review)&&!reading){reading=document.createElement('a');reading.id='reading-detail';document.querySelector('.reference-header').append(reading);}
    if(reading){reading.hidden=!(lab||illustrated||review);reading.textContent=lab?(s.view==='evidence'?'Story':'Evidence'):(s.detail==='text'?'Illustration':'Read');const u=new URL(location.href);if(lab)u.searchParams.set('view',s.view==='evidence'?'try':'evidence');else if(s.detail==='text')u.searchParams.delete('detail');else u.searchParams.set('detail','text');if(document.getElementById('story-scene')&&!lab){u.searchParams.set('view',s.detail==='text'?'try':'read');}reading.href=u.href;}
    for(const nav of document.querySelectorAll('.quiet-paging')){
      nav.setAttribute('aria-label','Story carousel');
      [...nav.querySelectorAll(':scope > a,:scope > button')].forEach((control,i)=>{
        const label=control.textContent.replace(/[←→‹›]/g,'').trim();
        if(label){control.setAttribute('aria-label',label);control.title=label;}
        control.textContent=i===0?'‹':'›';
      });
    }
    for(const img of document.querySelectorAll('img[src]')){const url=new URL(img.getAttribute('src'),location.href).href;if(img.getAttribute('src')!==url)img.src=url;}
    for(const a of document.querySelectorAll('a[href]')){
      const u=new URL(a.getAttribute('href'),location.href);
      if(u.origin!==location.origin)continue;
      for(const key of ['theme','text'])if(s[key]!==({theme:'light',text:'standard'})[key])u.searchParams.set(key,s[key]);
      a.href=u.href;
    }
  }
  view.mounts.push(()=>{
    view.subscribe(decorate);
    if(view.state.embed)return;
    const main=document.querySelector('main');let touch;
    const turn=direction=>{
      const nav=[...main.querySelectorAll('.quiet-paging')].find(n=>n.getClientRects().length);
      nav?.querySelectorAll(':scope > a,:scope > button')[direction]?.click();
    };
    addEventListener('keydown',e=>{
      if(e.defaultPrevented||e.altKey||e.ctrlKey||e.metaKey||e.target.closest('input,textarea,select,[role=tree],#file-tree,pre,.colored-source,.step-carousel'))return;
      if(e.key==='ArrowLeft'||e.key==='ArrowRight'){e.preventDefault();turn(e.key==='ArrowLeft'?0:1);}
    },{signal:view.signal});
    main.addEventListener('touchstart',e=>{if(e.target.closest('input,textarea,pre,.colored-source,#file-tree,.step-carousel'))return;touch={x:e.touches[0].clientX,y:e.touches[0].clientY};},{passive:true,signal:view.signal});
    main.addEventListener('touchend',e=>{if(!touch)return;const dx=e.changedTouches[0].clientX-touch.x,dy=e.changedTouches[0].clientY-touch.y;touch=null;if(Math.abs(dx)>55&&Math.abs(dx)>Math.abs(dy)*1.5)turn(dx<0?1:0);},{passive:true,signal:view.signal});
  });

  const local=url=>url.origin===base.origin&&url.pathname.startsWith(base.pathname)&&url.pathname.endsWith('.html');
  async function navigate(destination,{historyMove=false}={}){
    const url=new URL(destination,location.href),ticket=++request;
    if(!local(url)||location.protocol==='file:'){location.href=url.href;return;}
    if(url.pathname===path){
      if(!historyMove)history.pushState(null,'',url);
      view.read();return;
    }
    try{
      let html=chapters.get(url.pathname);
      if(!html){const response=await fetch(url);if(!response.ok)throw Error('Chapter unavailable');html=await response.text();}
      const page=new DOMParser().parseFromString(html,'text/html');
      const config=page.getElementById('view-config'),main=page.querySelector('main');
      if(!config||!main)throw Error('Not a reader chapter');
      if(ticket!==request)return;
      chapters.set(url.pathname,html);
      if(chapters.size>32)chapters.delete(chapters.keys().next().value);
      // Replacement and synchronous scene rendering happen before the browser paints.
      if(!historyMove)history.pushState(null,'',url);
      document.querySelector('main').replaceWith(main);
      document.title=page.title;document.body.className=page.body.className;
      document.getElementById('view-config').textContent=config.textContent;
      path=url.pathname;view.mount(JSON.parse(config.textContent));
      pruneHarmoniaScenes();pruneHarmoniaDiagrams();pruneHarmoniaCarousels();
      window.scrollTo(0,0);
      const heading=main.querySelector('h1')||main;heading.tabIndex=-1;heading.focus({preventScroll:true});
    }catch(error){
      if(ticket!==request)return;
      if(historyMove)history.replaceState(null,'',shown);
      let notice=document.getElementById('navigation-status');
      if(!notice){notice=document.createElement('p');notice.id='navigation-status';notice.className='quiet-note';notice.setAttribute('role','alert');document.querySelector('main').prepend(notice);}
      notice.textContent='This chapter could not be loaded. Your current page is kept; try the same arrow again.';
    }
  }
  view.go=navigate;
  document.addEventListener('click',e=>{
    const a=e.target.closest('a[href]');
    if(!a||e.defaultPrevented||e.button!==0||e.metaKey||e.ctrlKey||e.shiftKey||e.altKey||a.download||a.target)return;
    const url=new URL(a.href);
    if(local(url)){e.preventDefault();navigate(url);}
  });
  addEventListener('popstate',()=>navigate(location.href,{historyMove:true}));
  addEventListener('hashchange',()=>view.read());
  view.mount(initial);
  const home=document.createElement('script');
  home.src=new URL('../../book/home/return.js',base).href;
  document.head.appendChild(home);
})();

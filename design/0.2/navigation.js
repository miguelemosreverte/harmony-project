// Native links and two authored actions own navigation; there is no generated control panel.
(() => {
  const view=HarmoniaView;
  view.subscribe(s=>{
    document.documentElement.dataset.theme=s.theme;
    document.documentElement.dataset.text=s.text;
    document.documentElement.dataset.embed=String(s.embed);
    document.documentElement.dataset.presentation=String(s.present);
    for(const panel of document.querySelectorAll('[data-mode]'))panel.hidden=panel.dataset.mode!==s.view;
    const heading=document.querySelector('.chapter-heading');if(heading)heading.hidden=s.view==='try';
  });
  history.scrollRestoration="manual";
  if(!location.hash)addEventListener("load",()=>window.scrollTo(0,0),{once:true});
  for(const a of document.querySelectorAll('a[href]')){
    const raw=a.getAttribute('href');if(!raw.startsWith('#')&&!raw.includes('://')){
      const u=new URL(raw,location.href);
      for(const key of ['theme','text'])if(view.state[key]!==({theme:'light',text:'standard'})[key])u.searchParams.set(key,view.state[key]);
      a.href=u.href;
    }
  }
})();

// Host the unchanged portable recording inside the website's navigation.
(async () => {
  const root=new URL('../../',document.currentScript.src);
  try{
    const response=await fetch(new URL('book/investor/recorded/events.json',root));
    if(!response.ok)throw Error('The recorded demonstrations are unavailable.');
    const log=await response.json();
    if(log.format!=='harmonia-event-log/1')throw Error('This recording format is unavailable.');
    const data=document.createElement('script');data.id='event-log';data.type='application/json';data.textContent=JSON.stringify(log);document.body.append(data);
    for(const asset of log.presentation.styles){const style=document.createElement('style');style.textContent=asset.contents;document.head.append(style);}
    document.body.insertAdjacentHTML('beforeend',log.presentation.shell);
    for(const img of document.querySelectorAll('img[data-image]'))img.src=log.presentation.images[img.dataset.image];
    for(const asset of log.presentation.scripts){const script=document.createElement('script');script.textContent=asset.contents;document.body.append(script);}
    const back=document.createElement('script');back.src=new URL('book/home/return.js',root);back.dataset.branch='investor';document.head.append(back);
    document.getElementById('loading').remove();
  }catch(error){document.getElementById('loading').textContent=error.message;}
})();

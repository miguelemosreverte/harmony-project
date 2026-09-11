// The Scala reader owns navigation. This bridge lets the author pane follow its URL.
(() => {
  window.HarmoniaGuide = document.currentScript.dataset.guide;
  const evidence=window.HarmoniaLaboratoryEvidence, fresh=window.HarmoniaRunRecordings||{};
  evidence.stories=[...evidence.stories.map(story=>fresh[story.id]||story),...Object.values(fresh).filter(story=>!evidence.stories.some(old=>old.id===story.id))];
  window.HarmoniaEvidenceFiles={...window.HarmoniaEvidenceFiles,...window.HarmoniaRunEvidenceFiles};
  const listeners=new Set();
  window.HarmoniaView={subscribe(listener){listeners.add(listener);listener();}};
  addEventListener('harmonia-view',()=>{for(const listener of listeners)listener();});
})();

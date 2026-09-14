import fs from 'node:fs/promises';
import path from 'node:path';
import assert from 'node:assert/strict';
import {fileURLToPath} from 'node:url';
import {diagram, escape} from './diagrams.mjs';
import {recordings, hash, json} from './recordings.mjs';
import {evidence} from './evidence.mjs';
import {playerHTML, openerHTML} from './portable.mjs';

const folder=path.dirname(fileURLToPath(import.meta.url)),root=path.resolve(folder,'../..');
const args=process.argv.slice(2),exports=[];
let output=path.join(root,'.artifacts/canton-demo'),replay,checking=false;
for(let i=0;i<args.length;i++){
  if(args[i]==='--out')output=path.resolve(args[++i]);
  else if(args[i]==='--recordings')exports.push(path.resolve(args[++i]));
  else if(args[i]==='--replay')replay=path.resolve(args[++i]);
  else if(args[i]==='--check')checking=true;
  else throw Error('Usage: node book/investor/build.mjs [--out DIR] [--recordings evidence.json ...] [--replay events.json] [--check]');
}
const read=rel=>fs.readFile(path.join(root,rel),'utf8');
assert(!(replay && exports.length),'Choose either a saved log or fresh recording exports');
const parse=async rel=>JSON.parse(await read(rel));
const asset=async file=>({file,contents:await read(file)});
const dataURL=async(file,mime)=>`data:${mime};base64,${(await fs.readFile(path.join(root,file))).toString('base64')}`;
const shell=slides=>`<div class="reference-shell"><header class="reference-header"><span class="reference-brand">Harmonia</span><span class="quiet-location">Canton demos</span><a id="demo-evidence" href="evidence.html">Evidence</a><a id="demo-export" href="events.json" download>Export log</a></header><main id="demo-stage" aria-label="Recorded Canton demonstrations">${slides}</main></div><nav class="quiet-paging has-carousel" aria-label="Demonstration steps"><button type="button" id="demo-previous" aria-label="Previous step">‹</button><div id="demo-progress"></div><button type="button" id="demo-next" aria-label="Next step">›</button></nav><p class="visually-hidden" id="demo-announcement" role="status" aria-live="polite"></p>`;

async function capture(){
  const plan=await parse('book/investor/demos.json');
  const ids=[...new Set(plan.demos.flatMap(d=>d.paths.flatMap(p=>p.steps.map(s=>s.recording))))];
  const runs=await recordings(root,ids,exports);
  const proposal=await read('docs/proposal/harmonia.md'),lines=proposal.split('\n');
  const milestones=await parse('book/investor/milestones.json');
  for(const m of milestones){
    m.original=lines.slice(m.lines[0]-1,m.lines[1]).join('\n');
    assert(m.original.startsWith(`### Milestone ${m.id.slice(1)}:`));
    for(const file of m.implementation)await fs.access(path.join(root,file));
    for(const id of m.demos)assert(plan.demos.some(d=>d.id===id));
  }
  const scenes=[];
  for(const [chapter,d] of plan.demos.entries()){
    assert(lines[d.source.line-1].includes(d.source.quote),`Citation moved: ${d.id}`);
    d.milestones=milestones.filter(m=>m.demos.includes(d.id)).map(m=>m.id);
    for(const p of d.paths)for(const s of p.steps){
      const r=runs[s.recording],event=r.events.find(e=>e.id===s.action);
      assert(event,`Missing event: ${s.recording}/${s.action}`);
      s.event=event.sequence;s.outcome=event.observed.outcome??'compiled';
      const frame={...event,recording:r.id,setup:r.input.setup};
      const key=[d.id,p.id,s.id].join('/');
      scenes.push({key,recording:r.id,event:event.sequence,html:`<section class="demo-slide" data-key="${escape(key)}" data-current="${scenes.length===0}"><p class="demo-kicker">${chapter+1} / ${plan.demos.length} · ${escape(d.milestones.join(' · '))}</p><h2>${escape(s.title)}</h2><p class="demo-explanation">${escape(s.explanation)}</p>${diagram(d,frame)}<p class="recording-note">Recorded Canton demo · ${escape(r.provenance.recorded_at.slice(0,10))} · matches committed expectation</p></section>`});
    }
  }
  const images={
    '../navigation/assets/canton.svg':await dataURL('book/navigation/assets/canton.svg','image/svg+xml'),
    '../navigation/assets/daml.png':await dataURL('book/navigation/assets/daml.png','image/png')
  };
  let body=shell(scenes.map(s=>s.html).join('\n'));
  for(const name of Object.keys(images))body=body.replaceAll(`src="${name}"`,`data-image="${name}"`);
  const styles=await Promise.all(['product/scene/site/scene.css','product/scene/site/surface.css','design/0.2/quiet.css','book/investor/player.css'].map(asset));
  const scripts=await Promise.all(['product/scene/target/scala-3.3.6/harmonia-scene-fastopt/main.js','book/investor/player.js'].map(asset));
  const first=plan.demos[0],route=first.paths[0];
  const log={format:'harmonia-event-log/1',mode:'recorded',cursor:{demo:first.id,path:route.id,step:route.steps[0].id},
    scope:'Selected moments from independent local Canton runs. Complete ordered observations are retained per run. No ledger commands execute during replay.',
    demos:plan.demos,runs,scenes,milestones,proposal:{file:'docs/proposal/harmonia.md',sha256:hash(proposal),contents:proposal},
    presentation:{shell:body,styles,scripts,images,evidence_url:'evidence.html'}
  };
  for(const a of [...styles,...scripts])a.sha256=hash(a.contents);
  log.presentation.sha256=hash(body);
  log.presentation.evidence_html=evidence(log);
  log.presentation.evidence_sha256=hash(log.presentation.evidence_html);
  log.content_sha256=hash(json({...log,cursor:null,content_sha256:null}));
  return log;
}

function validate(log){
  assert.equal(log.format,'harmonia-event-log/1');assert.equal(log.mode,'recorded');
  assert.equal(hash(json({...log,cursor:null,content_sha256:null})),log.content_sha256,'Saved event log changed');
  assert(log.scenes.some(s=>s.key===[log.cursor.demo,log.cursor.path,log.cursor.step].join('/')),'Invalid saved selection');
  assert.equal(hash(log.proposal.contents),log.proposal.sha256);
  assert.equal(hash(log.presentation.shell),log.presentation.sha256,'Presentation changed');
  assert.equal(hash(log.presentation.evidence_html),log.presentation.evidence_sha256,'Evidence document changed');
  for(const a of [...log.presentation.styles,...log.presentation.scripts])assert.equal(hash(a.contents),a.sha256,`Asset changed: ${a.file}`);
  for(const r of Object.values(log.runs)){
    assert.deepEqual(r.actual,r.expected,`Golden mismatch: ${r.id}`);
    for(const e of r.events){assert.deepEqual(e.observed,e.expected);assert.deepEqual(e.observed,r.actual.actions[e.sequence]);assert.deepEqual(e.input,r.input.actions[e.sequence]);}
    for(const [file,field] of Object.entries({'input.md':'input_sha256','expected.md':'expected_sha256','actual.md':'actual_sha256','observation.json':'observation_sha256'}))assert.equal(hash(r.artifacts[file]),r.provenance[field]);
  }
  for(const s of log.scenes)assert(log.runs[s.recording].events[s.event],`Missing scene event: ${s.key}`);
}

const log=replay?JSON.parse(await fs.readFile(replay,'utf8')):await capture();
validate(log);
const outputs={'events.json':json(log),'index.html':playerHTML(log),'evidence.html':log.presentation.evidence_html,'open.html':openerHTML()};
await fs.mkdir(output,{recursive:true});
for(const [name,contents] of Object.entries(outputs)){
  if(checking)assert.equal(await fs.readFile(path.join(output,name),'utf8'),contents,`Stale output: ${name}`);
  else await fs.writeFile(path.join(output,name),contents);
}
console.log(`${log.demos.length} demos; ${log.scenes.length} selected scenes; ${Object.values(log.runs).reduce((n,r)=>n+r.events.length,0)} persisted events; ${log.milestones.length} milestones mapped. ${output}`);

// Own a sequence of existing checks; export only their explicitly identified outputs.
import fs from 'node:fs/promises';
import path from 'node:path';
import assert from 'node:assert/strict';
import {execFileSync} from 'node:child_process';
import {fileURLToPath} from 'node:url';

const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'../..');
const options=process.argv.slice(2);
assert(options.length===0 || (options.length===1 && options[0]==='--plan'),'Usage: node book/investor/record.mjs [--plan]');
const read=async file=>JSON.parse(await fs.readFile(path.join(root,file)));
const demos=await read('book/investor/demos.json');
const manifest=await read('book/edition-0.2/recordings/manifest.json');
const ids=[...new Set(demos.demos.flatMap(d=>d.paths.flatMap(p=>p.steps.map(s=>s.recording))))];
const ordinary=ids.filter(id=>manifest.stories[id].collection==='stories');
const checks=[
  {command:['check',...ordinary.map(id=>'examples/stories/'+id)],ids:ordinary},
  {command:['composer-check'],ids:['composer-direct']},
  {command:['builder-check'],ids:['package-builder']}
];
assert.deepEqual(new Set(checks.flatMap(c=>c.ids)),new Set(ids),'Every demo needs an owning recording command');
if(process.argv.includes('--plan')){
  console.log('scripts/build');
  for(const c of checks)console.log('scripts/harmonia '+c.command.join(' '));
  console.log('Collect those runs → verify/export evidence → package the event log and offline reader.');
}else{
  const artifacts=path.join(root,'.artifacts');
  await fs.mkdir(artifacts,{recursive:true});
  const session=await fs.mkdtemp(path.join(artifacts,'investor-record-'));
  const collected=path.join(session,'observations');
  await fs.mkdir(collected);
  const run=(script,args=[])=>execFileSync(path.join(root,script),args,{cwd:root,stdio:'inherit'});
  run('scripts/build');
  for(const c of checks){
    const before=new Set(await fs.readdir(artifacts));
    run('scripts/harmonia',c.command);
    const created=(await fs.readdir(artifacts)).filter(name=>!before.has(name));
    const candidates=[];
    for(const name of created){
      const directory=path.join(artifacts,name);
      const complete=await Promise.all(c.ids.map(id=>fs.access(path.join(directory,id,'run.json')).then(()=>true,()=>false)));
      if(complete.every(Boolean))candidates.push(directory);
    }
    assert.equal(candidates.length,1,'Expected exactly one new matching run; refusing to select the latest directory');
    for(const id of c.ids){
      const destination=path.join(collected,id);await fs.mkdir(destination);
      // Never copy participant configuration, capability files, databases or process logs.
      for(const name of ['input.md','expected.md','actual.md','diff.md','observation.json','run.json'])
        await fs.copyFile(path.join(candidates[0],id,name),path.join(destination,name));
    }
  }
  const exported=path.join(session,'verified-book');
  run('scripts/harmonia',['export-book',collected,exported]);
  const deliverable=path.join(session,'demo');
  execFileSync(process.execPath,['book/investor/build.mjs','--recordings',path.join(exported,'evidence.json'),'--out',deliverable],{cwd:root,stdio:'inherit'});
  execFileSync('python3',['book/investor/package.py',deliverable],{cwd:root,stdio:'inherit'});
  console.log('Recorded demo: '+path.join(deliverable,'index.html'));
}

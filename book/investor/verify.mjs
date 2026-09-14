import fs from 'node:fs/promises';
import path from 'node:path';
import assert from 'node:assert/strict';
import {execFileSync} from 'node:child_process';
import {recordings,hash,json} from './recordings.mjs';

const root=process.cwd(),out=path.join(root,'.artifacts/investor-verification');
await fs.mkdir(out,{recursive:true});
const log=JSON.parse(await fs.readFile('.artifacts/canton-demo/events.json'));
execFileSync(process.execPath,['book/investor/build.mjs','--check']);
const fixture=path.join(out,'existing-export');
await fs.mkdir(fixture,{recursive:true});
const stories=[];
for(const r of Object.values(log.runs)){
 stories.push(JSON.parse(await fs.readFile(`book/edition-0.2/recordings/${r.id}.json`)));
 const dir=path.join(fixture,'evidence',r.id);await fs.mkdir(dir,{recursive:true});
 for(const [file,contents] of Object.entries(r.artifacts))await fs.writeFile(path.join(dir,file),contents);
}
const input=path.join(fixture,'evidence.json');
await fs.writeFile(input,json({stories}));
assert.deepEqual(await recordings(root,Object.keys(log.runs),[input]),log.runs,'Existing export import differs');
// A supplied run must be complete. It must never borrow a passing historical story.
await fs.writeFile(input,json({stories:stories.slice(1)}));
await assert.rejects(recordings(root,Object.keys(log.runs),[input]),/no historical fallback/);
await fs.writeFile(input,json({stories}));
const golden=path.join(fixture,'evidence',stories[0].id,'expected.md');
await fs.appendFile(golden,'\nChanged baseline\n');
await assert.rejects(recordings(root,Object.keys(log.runs),[input]),/provenance mismatch/);

for(const [name,change] of [
 ['result',l=>{l.runs['transfer-approved'].events[7].observed.destination='999';}],
 ['renderer',l=>{l.presentation.styles[0].contents+='body{display:none}';}],
 ['scene',l=>{l.scenes[0].html='<p>Invented success</p>';}],
 ['format',l=>{l.format='unknown';}]
]){
 const altered=structuredClone(log);change(altered);
 const file=path.join(out,name+'.json');await fs.writeFile(file,json(altered));
 assert.throws(()=>execFileSync(process.execPath,['book/investor/build.mjs','--replay',file,'--out',path.join(out,'invalid')],{stdio:'pipe'}));
}
await assert.rejects(fs.access(path.join(out,'invalid','index.html')));
execFileSync('python3',['book/investor/package.py']);
const first=hash(await fs.readFile('.artifacts/harmonia-canton-demo.zip'));
execFileSync('python3',['book/investor/package.py']);
assert.equal(hash(await fs.readFile('.artifacts/harmonia-canton-demo.zip')),first,'Archive changed between identical builds');
console.log('Verified: deterministic build/archive; complete imported runs; missing-run and changed-baseline rejection; changed events/assets/scenes rejected before output.');

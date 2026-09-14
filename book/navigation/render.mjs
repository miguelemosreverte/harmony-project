// Render the content sheet without changing the product or its approved UI.
import assert from 'node:assert/strict';
import fs from 'node:fs/promises';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
import {createHash} from 'node:crypto';
import {figureView} from './figures.mjs';
import {originalDiagramView} from './original-diagrams.mjs';

const folder = path.dirname(fileURLToPath(import.meta.url));
const root = path.resolve(folder, '../..');
const plan = JSON.parse(await fs.readFile(path.join(folder, 'plan.json'), 'utf8'));
const revision = async file => createHash('sha256').update(await fs.readFile(file)).digest('hex').slice(0,12);
const versions = Object.fromEntries(await Promise.all(['sheet.css','sheet.js','original-diagrams.css'].map(async file => [file,await revision(path.join(folder,file))])));
const escape = value => String(value).replace(/[&<>"']/g, c => ({
  '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
})[c]);

assert.deepEqual(plan.branches.map(branch => branch.id), ['user', 'investor', 'architecture', 'implementation']);
assert.deepEqual(plan.navigation.cross_branch_links, []);
for (const file of [...Object.values(plan.sources), ...Object.values(plan.images)]) {
  await fs.access(path.join(root, file));
}

async function demoView(demo) {
  const record = JSON.parse(await fs.readFile(path.join(root, plan.sources[demo.recording]), 'utf8'));
  assert.equal(record.provenance.mode, 'live-canton');
  assert.deepEqual(record.actual, record.expected, 'Demo must match the committed expectation');
  const valueAt = (value, field) => {
    const result = field.split('.').reduce((value, key) => value?.[key], value);
    assert(result !== undefined, `Missing recorded value: ${field}`);
    return Array.isArray(result) ? result.join(', ') : String(result);
  };
  const beats = demo.steps.map((step, index) => {
    const actual = record.actual.actions.find(action => action.id === step.id);
    assert(actual, `Missing demo action: ${step.id}`);
    return `<li data-demo-step="${escape(step.id)}"><h4>${index+1}. ${escape(step.label)} <span class="demo-outcome" data-outcome="${escape(actual.outcome)}">${escape(actual.outcome)}</span></h4><dl>${demo.fields.map(field => `<div><dt>${escape(field.label)}</dt><dd>${escape(valueAt(actual,field.path))}</dd></div>`).join('')}</dl></li>`;
  }).join('');
  const visibility = demo.visibility ? `<table class="visibility"><caption>Recorded participant views after continuation</caption><thead><tr><th>Party</th><th>Private application</th><th>Shared progress</th></tr></thead><tbody>${Object.entries(record.actual.visibility).map(([actor,view]) => `<tr><th>${escape(actor)}</th><td>${view.application ? 'visible' : 'not visible'}</td><td>${view.progress ? 'visible' : 'not visible'}</td></tr>`).join('')}</tbody></table>` : '';
  return `<section class="demo" data-recording="${escape(demo.recording)}"><p class="demo-environment"><strong>Recorded Canton environment</strong>${escape(record.provenance.topology)}</p><p class="demo-model">${escape(demo.model)}</p><ol class="demo-beats">${beats}</ol>${visibility}<p class="demo-explain">${escape(demo.explain)}</p></section>`;
}

async function explorerView(explorer) {
  assert.equal(explorer.entry, 'design/0.2/code.html');
  assert(explorer.file.startsWith('product/') && !explorer.file.includes('/src/test/'));
  const url = `../../${explorer.entry}?v=${await revision(path.join(root,explorer.entry))}&file=${encodeURIComponent(explorer.file)}&line=${explorer.line}`;
  return `<iframe data-source-explorer src="${url}" title="Production source explorer" class="source-explorer"></iframe>`;
}

async function pageView(page, index, branch) {
  for (const ref of page.sources) assert(plan.sources[ref], `Unknown source: ${ref}`);
  if (branch.production_only) {
    assert.equal(page.frames.length, 0, 'Implementation must not show screenshots containing harness code');
    for (const ref of page.sources) {
      const source = plan.sources[ref];
      assert(source?.startsWith('product/') && !source.includes('/src/test/') && /\.(scala|daml)$/.test(source), `Non-production source in Implementation: ${source}`);
    }
  }
  if (page.moments) {
    const story = JSON.parse(await fs.readFile(path.join(root, plan.sources[page.moments.source]), 'utf8'));
    for (const id of page.moments.steps) assert(story.steps.some(step => step.id === id), `Missing recorded step: ${id}`);
  }
  const frames = page.frames.map(frame => {
    const file = plan.images[frame.image];
    assert(file, `Unknown image: ${frame.image}`);
    return `<figure data-image="${escape(frame.image)}"><img src="../../${escape(file)}" alt="${escape(frame.label)} — existing UI reference" width="1280" height="900" draggable="false"><figcaption>${escape(frame.label)}</figcaption></figure>`;
  }).join('');
  const diagrams = (page.diagrams || []).map(figureView).join('');
  const demo = page.demo ? await demoView(page.demo) : '';
  return `<article class="page" data-page="${escape(page.id)}">
    <header class="page-heading"><span class="number">${index + 1}</span><h3>${escape(page.title)}</h3></header>
    <p class="message">${escape(page.message)}</p>
    <div class="frames" data-count="${page.frames.length}">${frames}</div>
${diagrams}
${page.original_diagram ? originalDiagramView(page.original_diagram) : ''}
${demo}
${page.explorer ? await explorerView(page.explorer) : ''}
${page.demo || !page.show ? '' : `    <p class="show">${escape(page.show)}</p>`}
    <p class="sources">Sources: ${page.sources.map(escape).join(' · ')}</p>
  </article>`;
}

const regions = await Promise.all(plan.branches.map(async branch => {
  assert.equal(new Set(branch.pages.map(page => page.id)).size, branch.pages.length);
  assert(branch.finish);
  const pages = await Promise.all(branch.pages.map((page,index) => pageView(page,index,branch)));
  return `<section class="region" aria-labelledby="${escape(branch.id)}" data-branch="${escape(branch.id)}">
    <header class="region-heading"><h2 id="${escape(branch.id)}">${escape(branch.title)}</h2>
    <p class="question">${escape(branch.question)}</p><p class="perspective">${escape(branch.perspective)}</p></header>
    <div class="pages">${pages.join('')}</div>
    <p class="finish"><strong>This region ends here.</strong> ${escape(branch.finish)}</p>
  </section>`;
}));

const html = `<!doctype html>
<html lang="en"><head>
<meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Four reading regions · Harmonia content sheet</title>
<link rel="icon" href="../../product/web/site/favicon.svg">
<link rel="stylesheet" href="../../product/scene/site/surface.css">
<link rel="stylesheet" href="../../design/0.2/quiet.css">
<link rel="stylesheet" href="sheet.css?v=${versions['sheet.css']}">
<link rel="stylesheet" href="original-diagrams.css?v=${versions['original-diagrams.css']}">
<script type="module" src="sheet.js?v=${versions['sheet.js']}"></script>
</head><body>
<header class="reference-header"><span class="reference-brand">Harmonia</span><span class="quiet-location">${escape(plan.status)}</span></header>
<main id="viewport" tabindex="0" aria-label="Content sheet. Drag or use arrow keys to pan. Pinch, Control plus scroll, or plus and minus keys to zoom. Home fits the whole sheet." aria-describedby="gestures">
  <div id="paper" class="paper">
    <header class="paper-heading"><h1>${escape(plan.title)}</h1><p>${escape(plan.intent)}</p><p class="sheet-note">All four regions are unfolded here. Pan and zoom; choose production files in the existing source explorer.</p></header>
    <div class="regions">${regions.join('')}</div>
    <footer class="paper-notes"><p>${escape(plan.shared.scope)}</p><p>${escape(plan.shared.evidence)}</p>
      <dl>${Object.entries(plan.sources).map(([key, file]) => `<div><dt>${escape(key)}</dt><dd>${escape(file)}</dd></div>`).join('')}</dl>
      <dl>${Object.values(plan.references || {}).map(ref => `<div><dt>${escape(ref.title)}</dt><dd>${escape(ref.url)}</dd></div>`).join('')}</dl>
    </footer>
  </div>
</main>
<footer class="viewer-footer"><span id="gestures">Drag / scroll to pan · Pinch / Ctrl + scroll to zoom · Home to fit</span><output id="zoom" aria-label="Zoom level"></output></footer>
</body></html>
`;

const output = path.join(folder, 'index.html');
if (process.argv.includes('--check')) assert.equal(await fs.readFile(output, 'utf8'), html);
else await fs.writeFile(output, html);
console.log(`${plan.branches.length} regions; ${plan.branches.reduce((n, b) => n + b.pages.length, 0)} pages; sources, frames, diagrams and production explorer verified.`);

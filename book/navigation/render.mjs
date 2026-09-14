// Render the content sheet without changing the product or its approved UI.
import assert from 'node:assert/strict';
import fs from 'node:fs/promises';
import path from 'node:path';
import {fileURLToPath} from 'node:url';

const folder = path.dirname(fileURLToPath(import.meta.url));
const root = path.resolve(folder, '../..');
const plan = JSON.parse(await fs.readFile(path.join(folder, 'plan.json'), 'utf8'));
const escape = value => String(value).replace(/[&<>"']/g, c => ({
  '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
})[c]);

assert.deepEqual(plan.branches.map(branch => branch.id), ['user', 'investor', 'architecture', 'implementation']);
assert.deepEqual(plan.navigation.cross_branch_links, []);
for (const file of [...Object.values(plan.sources), ...Object.values(plan.images)]) {
  await fs.access(path.join(root, file));
}

async function excerptView(excerpt) {
  const source = plan.sources[excerpt.source];
  assert(source, `Unknown excerpt source: ${excerpt.source}`);
  const lines = (await fs.readFile(path.join(root, source), 'utf8')).trimEnd().split('\n');
  const [start, end] = excerpt.lines;
  assert(start > 0 && end >= start && end <= lines.length, `Invalid lines in ${source}`);
  const selected = lines.slice(start - 1, end);
  return `<figure class="excerpt"><figcaption>${escape(path.basename(source))} · lines ${start}–${end}</figcaption>
    <pre><code>${selected.map((line, i) => `<span class="excerpt-line"><span class="excerpt-number" aria-hidden="true">${start + i}</span>${escape(line)}</span>`).join('')}</code></pre></figure>`;
}

async function pageView(page, index) {
  for (const ref of page.sources) assert(plan.sources[ref], `Unknown source: ${ref}`);
  if (page.moments) {
    const story = JSON.parse(await fs.readFile(path.join(root, plan.sources[page.moments.source]), 'utf8'));
    for (const id of page.moments.steps) assert(story.steps.some(step => step.id === id), `Missing recorded step: ${id}`);
  }
  const frames = page.frames.map(frame => {
    const file = plan.images[frame.image];
    assert(file, `Unknown image: ${frame.image}`);
    return `<figure data-image="${escape(frame.image)}"><img src="../../${escape(file)}" alt="${escape(frame.label)} — existing UI reference" width="1280" height="900" draggable="false"><figcaption>${escape(frame.label)}</figcaption></figure>`;
  }).join('');
  const excerpts = await Promise.all((page.excerpts || []).map(excerptView));
  return `<article class="page" data-page="${escape(page.id)}">
    <header class="page-heading"><span class="number">${index + 1}</span><h3>${escape(page.title)}</h3></header>
    <p class="message">${escape(page.message)}</p>
    <div class="frames" data-count="${page.frames.length}">${frames}</div>
${excerpts.join('')}
    <p class="show">${escape(page.show)}</p>
    <p class="sources">Sources: ${page.sources.map(escape).join(' · ')}</p>
  </article>`;
}

const regions = await Promise.all(plan.branches.map(async branch => {
  assert.equal(new Set(branch.pages.map(page => page.id)).size, branch.pages.length);
  assert(branch.finish);
  const pages = await Promise.all(branch.pages.map(pageView));
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
<link rel="stylesheet" href="sheet.css">
<script type="module" src="sheet.js"></script>
</head><body>
<header class="reference-header"><span class="reference-brand">Harmonia</span><span class="quiet-location">${escape(plan.status)}</span></header>
<main id="viewport" tabindex="0" aria-label="Content sheet. Drag or use arrow keys to pan. Pinch, Control plus scroll, or plus and minus keys to zoom. Home fits the whole sheet." aria-describedby="gestures">
  <div id="paper" class="paper">
    <header class="paper-heading"><h1>${escape(plan.title)}</h1><p>${escape(plan.intent)}</p><p class="sheet-note">All twelve page summaries and their selected references are unfolded here. Read each region left to right.</p></header>
    <div class="regions">${regions.join('')}</div>
    <footer class="paper-notes"><p>${escape(plan.shared.scope)}</p><p>${escape(plan.shared.evidence)}</p>
      <dl>${Object.entries(plan.sources).map(([key, file]) => `<div><dt>${escape(key)}</dt><dd>${escape(file)}</dd></div>`).join('')}</dl>
    </footer>
  </div>
</main>
<footer class="viewer-footer"><span id="gestures">Drag / scroll to pan · Pinch / Ctrl + scroll to zoom · Home to fit</span><output id="zoom" aria-label="Zoom level"></output></footer>
</body></html>
`;

const output = path.join(folder, 'index.html');
if (process.argv.includes('--check')) assert.equal(await fs.readFile(output, 'utf8'), html);
else await fs.writeFile(output, html);
console.log(`${plan.branches.length} regions; ${plan.branches.reduce((n, b) => n + b.pages.length, 0)} pages; sources, frames and excerpts verified.`);

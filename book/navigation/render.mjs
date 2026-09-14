// Render the content proposal as a flat review document, without changing the book.
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
for (const source of Object.values(plan.sources)) await fs.access(path.join(root, source));
for (const branch of plan.branches) {
  await fs.access(path.join(root, branch.visual_reference));
  assert.equal(new Set(branch.pages.map(page => page.id)).size, branch.pages.length);
  assert(branch.finish);
  for (const page of branch.pages) {
    for (const ref of page.sources) assert(plan.sources[ref], `Unknown source: ${ref}`);
    if (page.moments) {
      const story = JSON.parse(await fs.readFile(path.join(root, plan.sources[page.moments.source]), 'utf8'));
      for (const id of page.moments.steps) assert(story.steps.some(step => step.id === id), `Missing recorded step: ${id}`);
    }
  }
}

const branches = plan.branches.map(branch => `
<section class="branch" aria-labelledby="${escape(branch.id)}">
  <header class="branch-heading">
    <h2 id="${escape(branch.id)}">${escape(branch.title)}</h2>
    <p class="question">${escape(branch.question)}</p>
    <p class="perspective">${escape(branch.perspective)}</p>
  </header>
  <figure><img src="../../${escape(branch.visual_reference)}" alt="Existing ${escape(branch.title.toLowerCase())} view used as a visual reference"><figcaption>Existing UI reference</figcaption></figure>
  <ol>${branch.pages.map(page => `
    <li data-page="${escape(page.id)}">
      <h3>${escape(page.title)}</h3>
      <p>${escape(page.show)}</p>
      <p class="message">${escape(page.message)}</p>
    </li>`).join('')}
  </ol>
  <p class="finish"><strong>End of this path</strong>${escape(branch.finish)}</p>
</section>`).join('');

const html = `<!doctype html>
<html lang="en"><head>
<meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Four reading paths · Harmonia content proposal</title>
<link rel="icon" href="../../product/web/site/favicon.svg">
<link rel="stylesheet" href="../../product/scene/site/surface.css">
<link rel="stylesheet" href="../../design/0.2/quiet.css">
<style>
  body { padding: 0; }
  .overview { max-width: 1600px; margin: 0 auto; background: var(--surface); }
  .reference-header { height: 64px; border-radius: 0; padding: 12px 28px; }
  main { padding: 24px 28px 28px; }
  h1 { margin: 0 0 8px; font-size: 30px; letter-spacing: -.035em; }
  .intro { margin: 0 0 16px; color: var(--muted); font-size: 15px; }
  .reading-rule { margin: 0 0 24px; padding: 12px 16px; background: var(--pale); border-radius: 10px; font-size: 13px; }
  .branches { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 24px; }
  .branch { min-width: 0; display: flex; flex-direction: column; }
  .branch + .branch { border-left: 1px solid var(--line); padding-left: 24px; }
  .branch-heading { min-height: 146px; }
  h2 { margin: 0 0 8px; color: var(--accent); font-size: 21px; }
  .question { margin: 0 0 6px; font-size: 15px; font-weight: 600; line-height: 1.4; }
  .perspective { font-size: 12px; line-height: 1.5; margin: 0; color: var(--muted); }
  figure { margin: 0 0 18px; }
  figure img { width: 100%; height: auto; aspect-ratio: 1280 / 900; object-fit: contain; border: 1px solid var(--line); border-radius: 8px; display: block; }
  figcaption { font-size: 10px; color: var(--muted); padding-top: 4px; }
  ol { padding: 0 0 0 22px; margin: 0; flex: 1; }
  li { padding-left: 3px; margin: 0 0 20px; }
  li::marker { color: var(--accent); font-weight: 650; }
  h3 { font-size: 15px; line-height: 1.35; margin: 0 0 7px; }
  li p { font-size: 12px; line-height: 1.55; margin: 0 0 7px; }
  .message { color: var(--muted); }
  .finish { font-size: 12px; line-height: 1.5; padding-top: 12px; border-top: 1px solid var(--line); margin: 0; }
  .finish strong { display: block; font-size: 11px; color: var(--accent); margin-bottom: 4px; }
  .notes { border-top: 1px solid var(--line); margin-top: 24px; padding-top: 14px; display: grid; grid-template-columns: 1fr 1fr; gap: 12px 28px; }
  .notes p { margin: 0; font-size: 12px; line-height: 1.5; color: var(--muted); }
  @media (max-width: 1000px) {
    .branches { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 28px; }
    .branch:nth-child(3) { padding-left: 0; border-left: 0; }
    .branch-heading { min-height: 128px; }
  }
  @media (max-width: 600px) {
    main { padding: 22px 18px; }
    .reference-header { padding: 12px 18px; }
    .reference-brand { font-size: 24px; }
    .quiet-location { max-width: 160px; text-align: right; font-size: 10px; }
    h1 { font-size: 26px; }
    .branches, .notes { grid-template-columns: 1fr; }
    .branch + .branch { padding: 22px 0 0; border-left: 0; border-top: 1px solid var(--line); }
    .branch-heading { min-height: 0; margin-bottom: 14px; }
    li p, .perspective, .finish, .notes p { font-size: 14px; }
  }
  @media print {
    @page { size: A3 landscape; margin: 12mm; }
    .reference-header { display: flex !important; }
    .overview { max-width: none; }
    main { padding: 12px 0; }
    .branches { grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 20px; }
    .branch { break-inside: avoid; }
  }
</style></head><body>
<div class="overview">
<header class="reference-header"><span class="reference-brand">Harmonia</span><span class="quiet-location">${escape(plan.status)}</span></header>
<main>
<h1>${escape(plan.title)}</h1>
<p class="intro">${escape(plan.intent)}</p>
<p class="reading-rule">Read each column downward. Three proposed pages, then an end. No automatic jump between paths.</p>
<div class="branches">${branches}</div>
<footer class="notes">
<p><strong>Appearance.</strong> ${escape(plan.shared.appearance)}</p>
<p><strong>Evidence.</strong> ${escape(plan.shared.evidence)}</p>
<p><strong>Reuse.</strong> ${escape(plan.shared.reuse)}</p>
<p><strong>Proposal.</strong> ${escape(plan.shared.scope)}</p>
</footer>
</main></div></body></html>
`;

const output = path.join(folder, 'index.html');
if (process.argv.includes('--check')) assert.equal(await fs.readFile(output, 'utf8'), html);
else await fs.writeFile(output, html);
console.log(`${plan.branches.length} independent paths; ${plan.branches.reduce((n, b) => n + b.pages.length, 0)} proposed pages; references verified.`);

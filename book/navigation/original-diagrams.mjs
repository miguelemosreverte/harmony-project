// The supplied SVGs are the model. Only their visual skin and local IDs change.
import assert from 'node:assert/strict';
import fs from 'node:fs/promises';

const source = await fs.readFile(new URL('../../docs/proposal/harmonia-architecture.html', import.meta.url), 'utf8');
const diagrams = [...source.matchAll(/<svg\b[\s\S]*?<\/svg>/g)].map(match => match[0]);
assert.equal(diagrams.length, 2, 'Expected the two diagrams in the original HTML');
const sharedMarkers = diagrams[0].match(/<defs>([\s\S]*?)<\/defs>/)[1];
const names = ['component-map', 'contract-model'];

export function originalDiagramView(name) {
  const index = names.indexOf(name);
  assert(index >= 0, 'Unknown original diagram');
  let svg = diagrams[index].replace(/<style>[\s\S]*?<\/style>/g, '');
  // The second original SVG references an arrow marker defined in the first.
  if (index === 1) svg = svg.replace('<defs>', `<defs>${sharedMarkers}`);
  svg = svg.replace('<svg ', '<svg class="source-architecture" ')
    .replace(/\bid="([^"]+)"/g, (_, id) => `id="original-${name}-${id}"`)
    .replace(/url\(#([^)]*)\)/g, (_, id) => `url(#original-${name}-${id})`)
    .replace(/^[\t ]+$/gm, '');
  return `<figure class="original-figure" data-original-diagram="${name}">${svg}<figcaption>Original: harmonia-architecture.html · §${index + 1} · labels and connections preserved</figcaption></figure>`;
}

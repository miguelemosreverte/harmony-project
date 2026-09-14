// Native vector figures. Geometry and labels are authored in figures.json.
import assert from 'node:assert/strict';
import fs from 'node:fs/promises';

const figures = JSON.parse(await fs.readFile(new URL('./figures.json', import.meta.url), 'utf8'));
const esc = value => String(value).replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
let sequence = 0;

function point(node, port) {
  const [x, y, w, h] = node.box;
  const ports = {top:[x+w/2,y-5], bottom:[x+w/2,y+h+5], left:[x-5,y+h/2], right:[x+w+5,y+h/2]};
  assert(ports[port], `Unknown port: ${port}`);
  return ports[port];
}

function graph(figure, marker) {
  const nodes = new Map(figure.nodes.map(node => [node.id, node]));
  assert.equal(nodes.size, figure.nodes.length);
  const zones = figure.zones.map(zone => {
    const [x,y,w,h] = zone.box;
    const [tx,ty] = zone.label_at || [x+14,y+25];
    return `<g class="diagram-zone"><rect x="${x}" y="${y}" width="${w}" height="${h}" rx="15"/><text x="${tx}" y="${ty}" text-anchor="${zone.anchor || 'start'}">${esc(zone.label)}</text></g>`;
  }).join('');
  const edges = figure.edges.map(edge => {
    assert(nodes.has(edge.source) && nodes.has(edge.target), 'Diagram edge has an unknown node');
    const points = [point(nodes.get(edge.source), edge.ports[0]), ...(edge.via || []), point(nodes.get(edge.target), edge.ports[1])];
    return `<g class="diagram-edge"><polyline points="${points.map(p => p.join(',')).join(' ')}" marker-end="url(#${marker})" ${edge.dashed ? 'stroke-dasharray="6 5"' : ''}/>${edge.label ? `<text x="${edge.label_at[0]}" y="${edge.label_at[1]}">${esc(edge.label)}</text>` : ''}</g>`;
  }).join('');
  const boxes = figure.nodes.map(node => {
    const [x,y,w,h] = node.box;
    assert(x >= 0 && y >= 0 && x+w <= figure.size[0] && y+h <= figure.size[1], `Node outside figure: ${node.id}`);
    return `<g class="diagram-node" data-tone="${esc(node.tone)}" data-node="${esc(node.id)}"><rect x="${x}" y="${y}" width="${w}" height="${h}" rx="13"/><text class="diagram-node-title" x="${x+w/2}" y="${y+30}">${esc(node.title)}</text>${node.detail.map((line,i) => `<text class="diagram-detail" x="${x+w/2}" y="${y+56+i*21}">${esc(line)}</text>`).join('')}</g>`;
  }).join('');
  return zones + edges + boxes;
}

function sequenceDiagram(figure, marker) {
  const lanes = new Map(figure.lanes.map(lane => [lane.id, lane]));
  return figure.lanes.map(lane => `<g class="diagram-lane"><rect x="${lane.x-80}" y="15" width="160" height="55" rx="12"/><text x="${lane.x}" y="49">${esc(lane.label)}</text><line x1="${lane.x}" y1="80" x2="${lane.x}" y2="${figure.size[1]-15}"/></g>`).join('') +
    figure.messages.map(message => {
      assert(lanes.has(message.from) && lanes.has(message.to));
      const from=lanes.get(message.from).x, to=lanes.get(message.to).x;
      return `<g class="diagram-edge"><line x1="${from}" y1="${message.y}" x2="${to+(from<to?-5:5)}" y2="${message.y}" marker-end="url(#${marker})" ${message.return ? 'stroke-dasharray="6 5"' : ''}/><text x="${(from+to)/2}" y="${message.y-12}">${esc(message.label)}</text></g>`;
    }).join('');
}

export function figureView(id) {
  const figure = figures[id];
  assert(figure, `Unknown figure: ${id}`);
  const unique = `figure-${++sequence}`, marker = unique+'-arrow';
  return `<figure class="engineering-figure" data-figure="${esc(id)}"><figcaption>${esc(figure.title)}</figcaption>
    <svg viewBox="0 0 ${figure.size.join(' ')}" role="img" aria-labelledby="${unique}-title"><title id="${unique}-title">${esc(figure.title)}</title><defs><marker id="${marker}" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse"><path d="M 1 1 L 9 5 L 1 9" fill="none" stroke="#0079ff" stroke-width="1.6"/></marker></defs>${figure.kind === 'sequence' ? sequenceDiagram(figure,marker) : graph(figure,marker)}</svg>
    <p class="diagram-caption">${esc(figure.caption)}</p></figure>`;
}

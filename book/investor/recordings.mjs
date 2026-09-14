import fs from 'node:fs/promises';
import path from 'node:path';
import assert from 'node:assert/strict';
import {createHash} from 'node:crypto';

export const hash = bytes => createHash('sha256').update(bytes).digest('hex');
export const sorted = value => Array.isArray(value) ? value.map(sorted)
  : value && typeof value === 'object' ? Object.fromEntries(Object.keys(value).sort().map(k => [k, sorted(value[k])])) : value;
export const json = value => JSON.stringify(sorted(value), null, 2) + '\n';

/** The existing exporter verifies/parses Markdown with the product's typed readers.
 * Keep its input, actual, expected and raw observation snapshots together here.
 * An explicit fresh export never silently falls back to a historical run.
 */
export async function recordings(root, ids, exports = []) {
  const historical = path.join(root, 'book/edition-0.2');
  const manifest = JSON.parse(await fs.readFile(path.join(historical, 'recordings/manifest.json')));
  const supplied = new Map();
  for (const file of exports) {
    const payload = JSON.parse(await fs.readFile(file));
    for (const record of payload.stories) {
      assert(!supplied.has(record.id), `Duplicate supplied recording: ${record.id}`);
      supplied.set(record.id, {record, directory:path.dirname(path.resolve(file))});
    }
  }
  const results = {};
  for (const id of ids) {
    const pin = manifest.stories[id];
    assert(pin, `Unknown recorded example: ${id}`);
    const entry = supplied.get(id);
    assert(!exports.length || entry, `Fresh exports are missing ${id}; no historical fallback`);
    const bytes = entry ? null : await fs.readFile(path.join(historical, 'recordings', pin.file));
    if (bytes) assert.equal(hash(bytes), pin.sha256, `Pinned recording changed: ${id}`);
    const record = entry?.record ?? JSON.parse(bytes);
    const p = record.provenance;
    assert.equal(p.mode, 'live-canton');
    assert.deepEqual(record.actual, record.expected, `Golden mismatch: ${id}`);
    assert.equal(p.matched, true);
    const directory = entry?.directory ?? path.join(historical,'recordings');
    const artifacts = {};
    for (const [file, field] of Object.entries({
      'input.md':'input_sha256', 'expected.md':'expected_sha256',
      'actual.md':'actual_sha256', 'observation.json':'observation_sha256'
    })) {
      const contents = await fs.readFile(path.join(directory, 'evidence', id, file), 'utf8');
      assert.equal(hash(contents), p[field], `${id}/${file}: provenance mismatch`);
      artifacts[file] = contents;
    }
    if (!entry) {
      for (const [file, field] of [['input.md','input_sha256'],['expected.md','expected_sha256']]) {
        const current = await fs.readFile(path.join(root, 'examples', pin.collection, id, file));
        assert.equal(hash(current), p[field], `Current baseline differs: ${id}/${file}`);
      }
    }
    const actions = record.input.actions ?? [];
    const events = record.presentation.units.map((unit, sequence) => {
      const request = actions.find(a => a.id === unit.id);
      assert(request, `No recorded request for ${id}/${unit.id}`);
      const actual = record.actual.actions.find(a => a.id === unit.id);
      const expected = record.expected.actions.find(a => a.id === unit.id);
      assert.deepEqual(unit.actual, actual);
      assert.deepEqual(unit.expected, expected);
      return {sequence, id:unit.id, input:request, observed:actual, expected};
    });
    assert.equal(new Set(events.map(e => e.id)).size, events.length);
    assert.equal(events.length,actions.length,'Every requested action needs a persisted observation');
    results[id] = {id, provenance:p, input:record.input, actual:record.actual,
      expected:record.expected, events, artifacts};
  }
  return results;
}

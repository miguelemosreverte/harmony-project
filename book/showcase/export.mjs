// node book/showcase/export.mjs <local-CDP-endpoint> <laboratory-URL> [output] [--check]
import fs from 'node:fs/promises';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
import {createHash} from 'node:crypto';
import assert from 'node:assert/strict';
import {isDeepStrictEqual} from 'node:util';
import {capture} from './capture.mjs';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..');
const [endpoint, entry, ...options] = process.argv.slice(2);
assert(endpoint && entry, 'Supply the local browser debugging endpoint and laboratory URL');
const output = path.resolve(options.find(value => value !== '--check') || path.join(root, 'book/showcase/documents'));
const checking = options.includes('--check');
const repositoryURL = new URL('../../', entry);
const read = file => fs.readFile(path.join(root, file));
const json = async file => JSON.parse(await read(file));
const sha256 = bytes => createHash('sha256').update(bytes).digest('hex');
const sorted = value => Array.isArray(value) ? value.map(sorted)
  : value && typeof value === 'object' ? Object.fromEntries(Object.entries(value)
    .sort(([a], [b]) => a.localeCompare(b)).map(([key, child]) => [key, sorted(child)])) : value;
const href = (story, step) => `laboratory.html?story=${encodeURIComponent(story)}&step=${step}`;
const relative = file => path.relative(root, file).split(path.sep).join('/');
const outputs = new Map();
const save = (name, value) => outputs.set(name, JSON.stringify(value, null, 2) + '\n');
const file = async name => {
  const bytes = await read(name);
  return {file: name, sha256: sha256(bytes)};
};
const repositoryPath = address => {
  const url = new URL(address);
  assert.equal(url.origin, repositoryURL.origin, 'A resource left the selected local book');
  assert(url.pathname.startsWith(repositoryURL.pathname), `Resource outside repository: ${address}`);
  const name = decodeURIComponent(url.pathname.slice(repositoryURL.pathname.length));
  assert(!name.split('/').includes('..'), 'A resource left the repository');
  return name;
};

const captured = await capture(endpoint, entry);
console.log(`Read ${captured.stories.length} workflows from the existing renderer.`);
const images = {};
const documents = [];
const recordingManifest = await json('book/edition-0.2/recordings/manifest.json');
const compareExamples = [
  {story: 'financing-approved', step: 'bank-approval'},
  {story: 'purchase-approved', step: 'bank-assessment'}
];

for (const [storyIndex, {story, moments}] of captured.stories.entries()) {
  const source = `book/edition-0.2/recordings/${story.id}.json`;
  let recording = await file(source);
  if (!isDeepStrictEqual(await json(source), story)) {
    const name = `recordings/${story.id}.json`;
    save(name, story);
    recording = {file: relative(path.join(output, name)), sha256: sha256(outputs.get(name))};
  }
  const frames = moments.map(({frame}) => {
    const {conversation, observation, ...diagram} = frame;
    return diagram;
  });
  const shared = Object.fromEntries(Object.entries(frames[0]).filter(([key, value]) =>
    frames.every(frame => isDeepStrictEqual(frame[key], value))));
  const steps = story.presentation.units.map((unit, index) => {
    const {frame, image} = moments[index];
    const state = Object.fromEntries(Object.entries(frames[index]).filter(([key]) => !(key in shared)));
    assert.deepEqual({...shared, ...state}, frames[index], `${story.id}: diagram reconstruction differs`);
    const previousStory = captured.stories[storyIndex - 1]?.story;
    const nextStory = captured.stories[storyIndex + 1]?.story;
    const step = {
      id: unit.id,
      actor: unit.actor,
      action: unit.action,
      open: href(story.id, index),
      illustration: image.id,
      dialogue: ['first', 'second'].map(key => {
        const {name, text, portrait} = frame.conversation[key]; return {speaker: name, portrait, text};
      }),
      diagram: state,
      observation: frame.observation,
      evidence: {
        observed: sorted(unit.actual),
        expected_pointer: `/presentation/units/${index}/expected`,
        matches_expectation: isDeepStrictEqual(unit.actual, unit.expected)
      },
      navigation: {
        progress: moments[index].nav.map(stop => stop.state),
        previous: index ? href(story.id, index - 1) : previousStory
          ? href(previousStory.id, previousStory.presentation.units.length - 1) : 'workflows.html',
        next: index + 1 < moments.length ? href(story.id, index + 1) : nextStory
          ? href(nextStory.id, 0) : 'coverage.html'
      }
    };
    assert.equal(step.navigation.progress.length, moments.length, 'Missing carousel stops');
    const restored = {...shared, ...state, observation: step.observation,
      conversation: {first: {name: step.dialogue[0].speaker, portrait: step.dialogue[0].portrait, text: step.dialogue[0].text},
        second: {name: step.dialogue[1].speaker, portrait: step.dialogue[1].portrait, text: step.dialogue[1].text},
        illustration: step.illustration}};
    assert.deepEqual(restored, frame, `${story.id}: complete presentation reconstruction differs`);
    return step;
  });
  for (const [index, {image}] of moments.entries()) {
    if (!images[image.id]) {
      const asset = await file(repositoryPath(image.url));
      const served = await fetch(image.url);
      assert(served.ok, `Missing served image: ${image.id}`);
      assert.equal(sha256(Buffer.from(await served.arrayBuffer())), asset.sha256, `Stale image: ${image.id}`);
      images[image.id] = {...asset, description: image.description, width: image.width, height: image.height, used_by: []};
    }
    assert.equal(images[image.id].description, image.description, 'Conflicting image descriptions');
    images[image.id].used_by.push({story: story.id, step: steps[index].id});
  }
  // Link preserved evidence only after checking its hashes against this selected recording.
  const evidence = {};
  const pin = recordingManifest.stories[story.id];
  for (const [artifact, key] of [['input.md', 'input_sha256'], ['expected.md', 'expected_sha256'],
    ['actual.md', 'actual_sha256'], ['observation.json', 'observation_sha256']]) {
    const name = `book/edition-0.2/recordings/evidence/${story.id}/${artifact}`;
    const reference = await file(name);
    if (pin?.artifacts[artifact] === reference.sha256 && story.provenance[key] === reference.sha256) {
      evidence[artifact] = reference;
    }
  }
  const document = {
    format: 'harmonia-showcase/1',
    id: story.id,
    title: story.title,
    purpose: story.description,
    kind: story.presentation.kind,
    diagram: {renderer: moments[0].renderer, shared},
    steps,
    recording: {...recording, provenance: sorted(story.provenance), evidence},
    operation: await file(story.presentation.operation)
  };
  save(`${story.id}.json`, document);
  documents.push(document);
}

const dependencies = [];
for (const address of [...new Set(captured.resources)].sort()) {
  const url = new URL(address);
  if (!['http:', 'https:'].includes(url.protocol)) continue;
  const name = repositoryPath(address);
  // Timings and local ports are not part of the exported document.
  if (!dependencies.some(dependency => dependency.file === name)) {
    const reference = await file(name);
    const response = await fetch(address);
    assert(response.ok, `Missing served dependency: ${name}`);
    const servedHash = sha256(Buffer.from(await response.arrayBuffer()));
    if (name === 'design/0.2/context.js' || name === 'design/0.2/run-recordings.js') {
      reference.served_sha256 = servedHash;
      reference.role = 'Session binding; the selected recording is preserved in each workflow document.';
    } else assert.equal(servedHash, reference.sha256, `Stale served renderer dependency: ${name}`);
    dependencies.push(reference);
  }
}
const sources = await json('book/edition-0.2/sources.json');
for (const source of Object.values(sources)) {
  assert.equal((await file(source.path)).sha256, source.sha256, 'An original document changed');
}
const sortedImages = Object.fromEntries(Object.entries(images).sort(([a], [b]) => a.localeCompare(b)));
const catalog = {
  format: 'harmonia-showcase/1',
  scope: 'Recorded workflow laboratory. Authored chapter introductions and live workspaces are separate surfaces.',
  paths: 'File paths start at the repository root. Open/previous/next URLs start at the selected book directory.',
  reader: 'design/0.2/laboratory.html',
  totals: {workflows: documents.length, steps: documents.reduce((sum, doc) => sum + doc.steps.length, 0), illustrations: Object.keys(images).length},
  comparison: {illustration: 'approval-signed', occurrences: compareExamples},
  workflows: documents.map(doc => ({id: doc.id, title: doc.title, document: `${doc.id}.json`, steps: doc.steps.length})),
  illustrations: sortedImages,
  original_documents: sources,
  passages: captured.passages.map(({id, source, start, end, title, chapter}) => ({id, source, lines: [start, end], title, chapter,
    open: `author.html?source=${source}&passage=${id}`})),
  quotation_scope: 'Existing chapter-level source associations; no new claim that a quotation proves a particular step.',
  rendering: {
    reconstruction: 'Combine diagram.shared with the selected step.diagram; each step overrides the shared fields independently.',
    dependencies: dependencies.sort((a, b) => a.file.localeCompare(b.file)),
    projectors: await Promise.all(['book/browser/src/main/scala/harmonia/book/diagram/RecordedScene.scala',
      'book/browser/src/main/scala/harmonia/book/diagram/StoryDiagram.scala',
      'book/browser/src/main/scala/harmonia/book/narrative/RecordedConversation.scala',
      'book/browser/src/main/scala/harmonia/book/narrative/RecordedIllustrations.scala'].map(file))
  }
};
for (const occurrence of compareExamples) {
  const doc = documents.find(doc => doc.id === occurrence.story);
  assert.equal(doc.steps.find(step => step.id === occurrence.step).illustration, catalog.comparison.illustration);
}
for (const doc of documents) for (const step of doc.steps) {
  for (const address of [step.open, step.navigation.previous, step.navigation.next]) {
    const url = new URL(address, entry);
    if (url.searchParams.has('story')) {
      const target = documents.find(doc => doc.id === url.searchParams.get('story'));
      assert(target?.steps[Number(url.searchParams.get('step'))], `Unknown destination: ${address}`);
    } else assert((await fetch(url)).ok, `Missing destination: ${address}`);
  }
}
save('catalog.json', catalog);
save('comparison.json', {
  question: 'Why do these two workflows show the same approval illustration?',
  illustration: {id: 'approval-signed', ...Object.fromEntries(Object.entries(images['approval-signed']).filter(([key]) => key !== 'used_by'))},
  explanation: 'Both recorded actions approve financing. The standalone example demonstrates who may approve; the purchase uses the result to continue into a property proposal. They are separate executions.',
  examples: compareExamples.map(occurrence => {
    const doc = documents.find(doc => doc.id === occurrence.story);
    const index = doc.steps.findIndex(step => step.id === occurrence.step);
    const step = doc.steps[index];
    return {workflow: doc.id, document: `${doc.id}.json`, purpose: doc.purpose,
      selected_step: step.id, open: step.open, action: step.action, dialogue: step.dialogue,
      observed: step.evidence.observed, next: step.navigation.next,
      remaining_steps: doc.steps.slice(index + 1).map(next => ({id: next.id, action: next.action,
        illustration: next.illustration, outcome: next.evidence.observed.outcome ?? null}))};
  })
});
for (const [name, contents] of outputs) {
  const destination = path.join(output, name);
  if (checking) assert.equal(await fs.readFile(destination, 'utf8'), contents, `Stale document: ${name}`);
  else { await fs.mkdir(path.dirname(destination), {recursive: true}); await fs.writeFile(destination, contents); }
}
console.log(JSON.stringify({...catalog.totals, documents: outputs.size, verified: checking, output}));

import { lstat, mkdir, readFile, readdir, realpath, rm, stat, writeFile } from 'node:fs/promises';
import { createHash } from 'node:crypto';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const source = path.join(root, 'website', 'src');
const output = path.join(root, 'website', 'dist');
const demo = path.join(root, 'composeApp', 'build', 'dist', 'wasmJs', 'productionExecutable');

async function exists(file) {
  try { await stat(file); return true; } catch (error) {
    if (error.code === 'ENOENT') return false;
    throw error;
  }
}

async function readTree(directory, prefix = '') {
  const files = [];
  for (const entry of await readdir(directory, { withFileTypes: true })) {
    const name = `${prefix}${entry.name}`;
    const file = path.join(directory, entry.name);
    if (entry.isSymbolicLink()) throw new Error(`Refusing symbolic link in build input: ${file}`);
    if (entry.isDirectory()) files.push(...await readTree(file, `${name}/`));
    else if (entry.isFile()) files.push([name, await readFile(file)]);
  }
  return files.sort(([a], [b]) => a < b ? -1 : a > b ? 1 : 0);
}

const sha256 = (bytes) => createHash('sha256').update(bytes).digest('hex');
for (const required of ['index.html', 'app.js', 'content.js', 'styles.css', 'favicon.svg']) {
  if (!await exists(path.join(source, required))) throw new Error(`Missing website/src/${required}.`);
}
if (!await exists(path.join(demo, 'index.html'))) {
  throw new Error('Build the shared Compose demo first: ./gradlew :composeApp:wasmJsBrowserDistribution');
}
const sourceFiles = await readTree(source);
const demoFiles = await readTree(demo);
if (!demoFiles.some(([file]) => file.endsWith('.wasm'))) throw new Error('The demo distribution contains no WebAssembly binary.');

// One namespace keeps the HTML, ES modules and all Webpack/Wasm dependencies together.
// Include this builder so changed output transformations cannot reuse an old asset URL.
const inputs = [
  ['builder', await readFile(fileURLToPath(import.meta.url))],
  ...sourceFiles.map(([name, bytes]) => [`source/${name}`, bytes]),
  ...demoFiles.map(([name, bytes]) => [`demo/${name}`, bytes]),
];
const fingerprint = sha256(inputs.map(([name, bytes]) => `${name}\0${sha256(bytes)}\n`).join(''));
const assetRoot = `assets/${fingerprint}`;
const entrypoints = Object.fromEntries(['app.js', 'content.js', 'styles.css', 'favicon.svg', 'demo/index.html']
  .map(file => [file, `${assetRoot}/${file}`]));
const sourceMap = new Map(sourceFiles);
const html = sourceMap.get('index.html').toString('utf8').replace(/((?:src|href)=")(\.\/[^"?#]+)(")/g, (_, before, reference, after) => {
  const file = reference.slice(2);
  if (!sourceMap.has(file) || file === 'index.html') throw new Error(`Unsupported HTML asset: ${reference}`);
  return `${before}./${assetRoot}/${file}${after}`;
});
const demoReference = "new URL('./demo/index.html',";
const sourceApp = sourceMap.get('app.js').toString('utf8');
if (sourceApp.split(demoReference).length !== 2) throw new Error('Expected one shared demo URL factory in app.js.');
const app = sourceApp.replace(demoReference, `new URL('./${entrypoints['demo/index.html']}',`);
const outputFiles = [
  ['index.html', Buffer.from(html)],
  ['.nojekyll', Buffer.from('')],
  ...sourceFiles.filter(([name]) => name !== 'index.html').map(([name, bytes]) =>
    [`${assetRoot}/${name}`, name === 'app.js' ? Buffer.from(app) : bytes]),
  ...demoFiles.map(([name, bytes]) => [`${assetRoot}/demo/${name}`, bytes]),
];
const manifest = {
  version: 1, fingerprint, assetRoot, entrypoints,
  files: Object.fromEntries(outputFiles.map(([name, bytes]) => [name, sha256(bytes)])),
};

// Refuse symlinks and verify the exact build output before replacing it.
const websiteRoot = await realpath(path.dirname(output));
if (websiteRoot !== path.join(await realpath(root), 'website')) throw new Error('Unexpected website directory.');
if (await exists(output)) {
  if ((await lstat(output)).isSymbolicLink() || await realpath(output) !== path.join(websiteRoot, 'dist')) {
    throw new Error('Refusing to replace an output directory outside this checkout.');
  }
  await rm(output, { recursive: true });
}
await mkdir(output, { recursive: true });
for (const [name, bytes] of outputFiles) {
  const file = path.join(output, name);
  await mkdir(path.dirname(file), { recursive: true });
  await writeFile(file, bytes);
}
await writeFile(path.join(output, 'build-manifest.json'), `${JSON.stringify(manifest, null, 2)}\n`);
console.log(`Documentation built at ${output}`);
console.log(`Versioned assets: ${assetRoot}`);
console.log('Preview: node scripts/serve-docs.mjs');

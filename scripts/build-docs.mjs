import { cp, lstat, mkdir, readdir, realpath, rm, stat, writeFile } from 'node:fs/promises';
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

for (const required of ['index.html', 'app.js', 'styles.css']) {
  if (!await exists(path.join(source, required))) throw new Error(`Missing website/src/${required}.`);
}
if (!await exists(path.join(demo, 'index.html'))) {
  throw new Error('Build the shared Compose demo first: ./gradlew :composeApp:wasmJsBrowserDistribution');
}
const demoFiles = await readdir(demo, { recursive: true });
if (!demoFiles.some(file => file.endsWith('.wasm'))) throw new Error('The demo distribution contains no WebAssembly binary.');

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
await cp(source, output, { recursive: true });
await cp(demo, path.join(output, 'demo'), { recursive: true });
await writeFile(path.join(output, '.nojekyll'), '');
console.log(`Documentation built at ${output}`);
console.log('Preview: node scripts/serve-docs.mjs');

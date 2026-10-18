import assert from 'node:assert/strict';
import { readFile, readdir, stat } from 'node:fs/promises';
import { fileURLToPath, pathToFileURL } from 'node:url';
import path from 'node:path';
import { spawnSync } from 'node:child_process';
import { Script } from 'node:vm';
import { createHash } from 'node:crypto';

const website = path.dirname(fileURLToPath(import.meta.url));
const root = path.dirname(website);
const isDistribution = process.argv.includes('--dist');
const directory = path.join(website, isDistribution ? 'dist' : 'src');
const html = await readFile(path.join(directory, 'index.html'), 'utf8');
const sha256 = (bytes) => createHash('sha256').update(bytes).digest('hex');
const resolveAsset = (reference, base = directory) => {
  assert.ok(reference.startsWith('./'), `${reference}: asset uses a relative URL`);
  const file = path.resolve(base, reference);
  const relative = path.relative(directory, file);
  assert.ok(!relative.startsWith('..') && !path.isAbsolute(relative), `${reference}: asset stays inside the site`);
  return file;
};
async function listFiles(base, prefix = '') {
  const files = [];
  for (const entry of await readdir(base, { withFileTypes: true })) {
    assert.ok(!entry.isSymbolicLink(), `${entry.name}: packaged assets cannot be symbolic links`);
    const relative = `${prefix}${entry.name}`;
    if (entry.isDirectory()) files.push(...await listFiles(path.join(base, entry.name), `${relative}/`));
    else if (entry.isFile()) files.push(relative);
  }
  return files.sort();
}
let manifest;
if (isDistribution) {
  manifest = JSON.parse(await readFile(path.join(directory, 'build-manifest.json'), 'utf8'));
  assert.equal(manifest.version, 1);
  assert.match(manifest.fingerprint, /^[a-f0-9]{64}$/);
  assert.equal(manifest.assetRoot, `assets/${manifest.fingerprint}`);
  for (const name of ['app.js', 'content.js', 'styles.css', 'favicon.svg', 'demo/index.html']) {
    assert.equal(manifest.entrypoints[name], `${manifest.assetRoot}/${name}`, `${name}: one release namespace`);
  }
  const files = await listFiles(directory);
  assert.deepEqual(files, [...Object.keys(manifest.files), 'build-manifest.json'].sort(), 'manifest accounts for every output file');
  for (const [name, expectedHash] of Object.entries(manifest.files)) {
    assert.ok(['index.html', '.nojekyll'].includes(name) || name.startsWith(`${manifest.assetRoot}/`), `${name}: no unversioned assets`);
    assert.match(expectedHash, /^[a-f0-9]{64}$/);
    assert.equal(sha256(await readFile(resolveAsset(`./${name}`))), expectedHash, `${name}: packaged content matches the manifest`);
  }
}
const htmlAsset = (pattern, name) => {
  const reference = html.match(pattern)?.[1];
  assert.ok(reference, `${name}: HTML entrypoint exists`);
  assert.equal(reference, `./${isDistribution ? manifest.entrypoints[name] : name}`, `${name}: HTML loads the current release`);
  return resolveAsset(reference);
};
const appFile = htmlAsset(/<script\b(?=[^>]*\btype="module")[^>]*\bsrc="([^"]+)"/, 'app.js');
const cssFile = htmlAsset(/<link\b(?=[^>]*\brel="stylesheet")[^>]*\bhref="([^"]+)"/, 'styles.css');
await stat(htmlAsset(/<link\b(?=[^>]*\brel="icon")[^>]*\bhref="([^"]+)"/, 'favicon.svg'));
const css = await readFile(cssFile, 'utf8');
const app = await readFile(appFile, 'utf8');
const contentReference = app.match(/import\s*\{[^}]+\}\s*from\s*['"]([^'"]+)['"]/)?.[1];
assert.equal(contentReference, './content.js', 'app imports content from its own release namespace');
const contentFile = resolveAsset(contentReference, path.dirname(appFile));
const { components, pages, tokens, searchPages } = await import(pathToFileURL(contentFile));

for (const file of [appFile, contentFile]) {
  const checked = spawnSync(process.execPath, ['--check', file], { encoding: 'utf8' });
  assert.equal(checked.status, 0, `${file}: ${checked.stderr}`);
}
for (const script of html.matchAll(/<script(?![^>]*\bsrc=)[^>]*>([\s\S]*?)<\/script>/g)) new Script(script[1]);

assert.deepEqual(Object.keys(components), ['button', 'input', 'checkbox', 'combobox', 'dialog', 'sheet', 'switch', 'textarea', 'radio-group', 'alert', 'tabs', 'accordion', 'slider', 'progress']);
assert.equal(pages.length, 17);
assert.equal(new Set(pages.map((page) => page.route)).size, pages.length);
assert.equal(searchPages('按钮')[0].route, 'components/button');
assert.equal(searchPages('SInput')[0].route, 'components/input');
for (const [query, component] of [
  ['开关', 'switch'], ['SSwitch', 'switch'],
  ['多行 输入', 'textarea'], ['STextarea', 'textarea'],
  ['单选组', 'radio-group'], ['radio group', 'radio-group'], ['RadioOption', 'radio-group'],
  ['警告', 'alert'], ['SAlert', 'alert'], ['AlertVariant', 'alert'],
  ['标签页', 'tabs'], ['STabs', 'tabs'], ['TabOption', 'tabs'],
  ['折叠面板', 'accordion'], ['SAccordion', 'accordion'], ['AccordionItem', 'accordion'],
  ['滑块', 'slider'], ['SSlider', 'slider'],
  ['进度条', 'progress'], ['SProgress', 'progress'],
]) assert.equal(searchPages(query)[0]?.route, `components/${component}`, `${query}: component is searchable`);
assert.equal(searchPages('iOS')[0].route, 'installation');
assert.equal(searchPages('THEME')[0].route, 'theming');
assert.equal(searchPages('not-a-real-component').length, 0);
assert.equal(searchPages('   ').length, pages.length);

const normalize = (value) => value.replace(/\s+/g, '').replace(/,$/, '');
for (const [key, component] of Object.entries(components)) {
  const sourcePath = path.join(root, 'composeApp/src/commonMain/kotlin/com/shadcn/mobile', component.source);
  const source = await readFile(sourcePath, 'utf8');
  const pattern = new RegExp(`fun ${component.name}\\(([\\s\\S]*?)\\)\\s*\\{`);
  const actual = source.match(pattern)?.[1];
  assert.ok(actual, `${key}: source function exists`);
  const documented = component.signature.match(new RegExp(`fun ${component.name}\\(([\\s\\S]*)\\)$`))?.[1];
  assert.equal(normalize(documented), normalize(actual), `${key}: documented API differs from source`);
  assert.ok(component.sample.includes(`${component.name}(`), `${key}: example calls actual component`);
  for (const type of component.types || []) {
    const pattern = type.signature.startsWith('data class ')
      ? new RegExp(`data class ${type.name}\\([^)]*\\)`)
      : new RegExp(`enum class ${type.name}\\s*\\{[^}]*\\}`);
    const actual = source.match(pattern)?.[0];
    assert.ok(actual, `${type.name}: public type exists`);
    assert.equal(normalize(type.signature), normalize(actual), `${type.name}: documented type differs from source`);
  }
}

const gallery = await readFile(path.join(root, 'composeApp/src/commonMain/kotlin/com/shadcn/mobile/ComponentGallery.kt'), 'utf8');
const registered = gallery.match(/componentNames\s*=\s*setOf\(([^)]*)\)/)?.[1];
assert.ok(registered, 'shared gallery declares its preview keys');
const previewKeys = [...registered.matchAll(/"([^"]+)"/g)].map((match) => match[1]);
assert.deepEqual(previewKeys.toSorted(), Object.keys(components).toSorted(), 'every documented component has a shared preview');
for (const key of previewKeys) assert.ok(gallery.includes(`"${key}" ->`), `${key}: shared preview renders an example`);

const theme = await readFile(path.join(root, 'composeApp/src/commonMain/kotlin/com/shadcn/mobile/ui/Theme.kt'), 'utf8');
for (const [name, light, dark] of tokens) {
  for (const hex of [light, dark]) assert.ok(theme.includes(`0xFF${hex.substring(1)}`) || (hex === '#FFFFFF' && theme.includes('Color.White')), `${name}: theme token exists in Kotlin`);
}

for (const match of html.matchAll(/(?:src|href)="(\.\/[^"?#]+)"/g)) await stat(resolveAsset(match[1]));
assert.ok(!/(?:src|href)="\/(?!\/)/.test(html), 'HTML assets must work under a repository subpath');
const demoReference = `./${isDistribution ? manifest.entrypoints['demo/index.html'] : 'demo/index.html'}`;
assert.ok(app.includes(`new URL('${demoReference}', location.href.split('#')[0])`), 'shared preview URL uses the release entrypoint relative to the deployed page');
assert.ok(css.includes('prefers-reduced-motion'), 'reduced motion is supported');
assert.ok(html.includes('name="viewport"'), 'mobile viewport is present');
assert.ok(html.includes('lang="zh-CN"'), 'document language is Chinese');
assert.ok(!/https?:\/\/(?:localhost|127\.0\.0\.1)/.test(html + app), 'no local development URLs shipped');

if (isDistribution) {
  const demoEntry = resolveAsset(demoReference);
  const demo = path.dirname(demoEntry);
  const demoHtml = await readFile(demoEntry, 'utf8');
  const files = await listFiles(demo);
  assert.ok(files.some((file) => file.endsWith('.wasm')), 'distribution includes real Wasm runtime');
  assert.ok(files.some((file) => file.endsWith('.js')), 'distribution includes Wasm loader');
  const originalDemo = path.join(root, 'composeApp/build/dist/wasmJs/productionExecutable');
  assert.deepEqual(files, await listFiles(originalDemo), 'the entire Compose distribution is packaged together');
  for (const file of files) {
    const bytes = await readFile(path.join(demo, file));
    assert.equal(sha256(bytes), sha256(await readFile(path.join(originalDemo, file))), `${file}: runtime is copied without changes`);
    if (file.endsWith('.wasm')) assert.deepEqual(bytes.subarray(0, 8), Buffer.from([0, 97, 115, 109, 1, 0, 0, 0]), `${file}: valid WebAssembly header`);
  }
  for (const match of demoHtml.matchAll(/(?:src|href)="([^"#]+)"/g)) {
    if (/^(?:https?:|data:|\/\/)/.test(match[1])) continue;
    const file = resolveAsset(match[1].split('?')[0], demo);
    assert.ok(path.relative(demo, file).split(path.sep)[0] !== '..', 'demo dependencies stay in their release directory');
    await stat(file);
  }
}
const relatedTypeCount = Object.values(components).reduce((count, component) => count + (component.types?.length || 0), 0);
console.log(`Verified ${path.relative(root, directory)}: ${pages.length} routes, ${Object.keys(components).length} exact Kotlin APIs, ${relatedTypeCount} related types, shared preview keys, bilingual search, theme tokens, relative assets${isDistribution ? ', fingerprinted assets and packaged Wasm runtime' : ''}.`);

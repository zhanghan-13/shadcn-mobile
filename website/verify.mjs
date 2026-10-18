import assert from 'node:assert/strict';
import { readFile, readdir, stat } from 'node:fs/promises';
import { fileURLToPath, pathToFileURL } from 'node:url';
import path from 'node:path';
import { spawnSync } from 'node:child_process';
import { Script } from 'node:vm';

const website = path.dirname(fileURLToPath(import.meta.url));
const root = path.dirname(website);
const directory = path.join(website, process.argv.includes('--dist') ? 'dist' : 'src');
const { components, pages, tokens, searchPages } = await import(pathToFileURL(path.join(directory, 'content.js')));
const html = await readFile(path.join(directory, 'index.html'), 'utf8');
const css = await readFile(path.join(directory, 'styles.css'), 'utf8');
const app = await readFile(path.join(directory, 'app.js'), 'utf8');

for (const file of ['app.js', 'content.js']) {
  const checked = spawnSync(process.execPath, ['--check', path.join(directory, file)], { encoding: 'utf8' });
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

for (const match of html.matchAll(/(?:src|href)="(\.\/[^"?#]+)"/g)) await stat(path.resolve(directory, match[1]));
assert.ok(!/(?:src|href)="\/(?!\/)/.test(html), 'HTML assets must work under a repository subpath');
assert.ok(app.includes("new URL('./demo/index.html'"), 'iframe resolves relative to deployed page');
assert.ok(css.includes('prefers-reduced-motion'), 'reduced motion is supported');
assert.ok(html.includes('name="viewport"'), 'mobile viewport is present');
assert.ok(html.includes('lang="zh-CN"'), 'document language is Chinese');
assert.ok(!/https?:\/\/(?:localhost|127\.0\.0\.1)/.test(html + app), 'no local development URLs shipped');

if (process.argv.includes('--dist')) {
  const demo = path.join(directory, 'demo');
  const demoHtml = await readFile(path.join(demo, 'index.html'), 'utf8');
  const files = await readdir(demo, { recursive: true });
  assert.ok(files.some((file) => file.endsWith('.wasm')), 'distribution includes real Wasm runtime');
  assert.ok(files.some((file) => file.endsWith('.js')), 'distribution includes Wasm loader');
  for (const match of demoHtml.matchAll(/(?:src|href)="([^"#]+)"/g)) {
    if (/^(?:https?:|data:|\/\/)/.test(match[1])) continue;
    await stat(path.resolve(demo, match[1].split('?')[0]));
  }
}
const relatedTypeCount = Object.values(components).reduce((count, component) => count + (component.types?.length || 0), 0);
console.log(`Verified ${path.relative(root, directory)}: ${pages.length} routes, ${Object.keys(components).length} exact Kotlin APIs, ${relatedTypeCount} related types, shared preview keys, bilingual search, theme tokens, relative assets${process.argv.includes('--dist') ? ', packaged Wasm runtime' : ''}.`);

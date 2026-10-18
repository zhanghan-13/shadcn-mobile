# shadcn-mobile documentation

Buildless Chinese documentation shell in `src/`. It uses hash routes, so links work at the repository's GitHub Pages subpath and on a local static server without rewriting requests. No framework or package install is required.

The preview iframe loads `assets/<fingerprint>/demo/index.html?component=form|button|input|checkbox|combobox|dialog|sheet|switch|textarea|radio-group|alert|tabs|accordion|slider|progress&theme=light|dark`. The directory contains the real Compose Wasm production build, supplied by `scripts/build-docs.mjs`; the site does not recreate the native form with HTML. All fourteen component examples live in the shared `commonMain/ComponentGallery.kt` used by Android, iOS and the browser.

From the repository root:

```shell
node website/verify.mjs
node scripts/build-docs.mjs
node website/verify.mjs --dist
```

Serve `website/dist/` over HTTP. The iframe, assets and fonts resolve relative to the deployed page. Opening the page using `file://` will not run the ES modules or Wasm preview.

The builder places the documentation modules, CSS, icon and complete Wasm demo under one content fingerprint, including the builder itself in that fingerprint. A new entry page therefore cannot mix its modules with cached files from another release. `build-manifest.json` records the emitted paths and SHA-256 hashes; the distribution verifier checks those files and their references. GitHub Pages still controls caching of the root HTML, so deployment does not force an already open tab to update immediately.

## Structure

- `src/index.html`: accessible navigation, command search and initial theme selection.
- `src/content.js`: component descriptions, exact current Kotlin signatures, examples and searchable page metadata.
- `src/app.js`: document rendering, relative preview URLs, route navigation, copy feedback, search, tabs and themes.
- `src/styles.css`: responsive zinc visual system, sidebar, preview area, code and data tables.
- `verify.mjs`: checks 17 routes, fourteen documented APIs and four related types against Kotlin sources, shared preview keys, bilingual search, theme tokens, subpath-safe assets and packaged Wasm output.

## Browser verification

Check the default form and all fourteen components. Toggle Switch and check its disabled state; enter multiline text and check Textarea validation/read-only behavior; select one RadioGroup option and confirm disabled options cannot be chosen; trigger the Alert example's local state change. For the latest four examples, enter a Tabs draft and retain it across tab changes; expand, switch and collapse Accordion items; drag Slider and confirm its step size and disabled state; advance Progress to 100% and reset it. Check that disabled tabs and accordion titles cannot activate.

Exercise search, no results, keyboard result selection, code copying, preview/code tabs, preview reset, width controls, theme switching and the narrow-screen navigation drawer. Confirm that the full form can be completed in the shared Compose preview. Check desktop and mobile widths for page overflow. These are checks to run, not a record of completed verification; see `docs/web-verification.md` for the dated results.

Browser preview demonstrates the same shared UI and state flow; native IME, system back gestures, accessibility and platform window behavior still require native-device verification. The iOS runtime verification boundary is explicitly documented.

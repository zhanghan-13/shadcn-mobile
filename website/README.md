# shadcn-mobile documentation

Buildless Chinese documentation shell in `src/`. It uses hash routes, so links work at the repository's GitHub Pages subpath and on a local static server without rewriting requests. No framework or package install is required.

The preview iframe loads `demo/index.html?component=form|button|input|checkbox|combobox|dialog|sheet&theme=light|dark`. The directory contains the real Compose Wasm production build, supplied by `scripts/build-docs.mjs`; the site does not recreate the native form with HTML.

From the repository root:

```shell
node website/verify.mjs
node scripts/build-docs.mjs
node website/verify.mjs --dist
```

Serve `website/dist/` over HTTP. The iframe, assets and fonts resolve relative to the deployed page. Opening the page using `file://` will not run the ES modules or Wasm preview.

## Structure

- `src/index.html`: accessible navigation, command search and initial theme selection.
- `src/content.js`: component descriptions, exact current Kotlin signatures, examples and searchable page metadata.
- `src/app.js`: document rendering, relative preview URLs, route navigation, copy feedback, search, tabs and themes.
- `src/styles.css`: responsive zinc visual system, sidebar, preview area, code and data tables.
- `verify.mjs`: checks the documented APIs against Kotlin sources, search behavior, theme tokens, subpath-safe assets and packaged Wasm output.

## Browser verification

Check the default form and all six components. Exercise search, no results, keyboard result selection, code copying, preview/code tabs, preview reset, width controls, theme switching and the narrow-screen navigation drawer. Confirm that the full form can be completed in the shared Compose preview. Check desktop and mobile widths for page overflow.

Browser preview demonstrates the same shared UI and state flow; native IME, system back gestures, accessibility and platform window behavior still require native-device verification. The iOS runtime verification boundary is explicitly documented.

# Documentation and browser verification

Verified on **2026-10-17**, using Chrome on Windows. The detailed build and interaction checks below were performed locally at `http://127.0.0.1:4173/shadcn-mobile/`. After publication, the complete form flow was checked separately on [GitHub Pages](https://zhanghan-13.github.io/shadcn-mobile/).

## Built artifact

- `:composeApp:wasmJsBrowserDistribution` completed successfully.
- `node scripts/build-docs.mjs` packaged the documentation and shared Compose/Wasm runtime under `website/dist/`.
- `node website/verify.mjs --dist` passed: 9 routes, 6 exact Kotlin API signatures, bilingual search, theme tokens, JavaScript syntax, relative assets and packaged Wasm runtime.
- Browser examples call the same Kotlin components as the native app. The complete form uses the shared `App()` entry point.
- Chinese text uses the bundled Noto Sans SC font; its SIL license and source are included with the runtime.

## Interactions checked in the browser

These were manual browser interactions through the rendered UI, not a persisted automated end-to-end suite.

| Area | Observed result |
| --- | --- |
| Complete form | Entered a Chinese workspace name and `hello@example.com`, searched and selected the developer role, saved notification preferences, accepted the demo terms, reviewed the entered values, confirmed and reached the success summary. No backend submission occurs. |
| Button example | Clicking the enabled primary button incremented the displayed count. |
| Input example | Typing `a@@example.com` produced the shared validator's invalid-email message. |
| Checkbox example | Consent enabled the continuation button; clicking it showed the confirmation state and explanatory feedback. |
| Combobox, Dialog and Sheet | Search/selection, saved preferences and confirmation worked as part of the complete form flow. Native Back and keyboard behavior are covered separately by the Android checks. |
| Documentation search | A query with no matches showed recovery guidance; an English component query found Button, and Enter navigated to it. |
| Code | The code tab displayed Kotlin and the copy action put the displayed example on the clipboard with visible success feedback. |
| Theme | The documentation and embedded preview switched together between light and dark. The loading overlay now uses the requested theme while Compose initializes. |
| Preview controls | Phone/full-width modes changed the preview width. Reset reloaded the form and displayed reset feedback. |
| Navigation | Component routes, mobile drawer navigation and the in-page API link worked. API and contents links retain the component route and a `section` parameter; clicking and refreshing the API URL both displayed the API heading. Browser scroll restoration is disabled so it cannot override the requested section. |
| Responsive layout | Inspected the desktop layout and a 390 × 844 viewport. The narrow layout had no page-level horizontal overflow, and the drawer closed after selecting a route. |

## Published site

[Docs and Pages run 39045335653](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39045335653) passed and deployed the documentation to [GitHub Pages](https://zhanghan-13.github.io/shadcn-mobile/).

On the deployed site in Chrome, the complete form flow passed: entered the form values, selected a role, saved notification preferences in the Sheet, checked the consent box, reviewed and confirmed the Dialog, and reached the success summary. This was a manual check of the rendered UI; no backend submission occurs. The success-screen capture is retained locally at `.local/published-form-success.png`.

This deployed check covers the complete form flow. The individual documentation controls and component examples in the table above retain their original local verification scope.

## Screenshots

- [Desktop documentation and shared form](screenshots/docs-desktop.png)
- [Mobile-width dark Checkbox documentation](screenshots/docs-mobile-dark.png)

The captures are visual inspection evidence, not pixel comparison assertions. A floating translation badge visible at the right edge belongs to the workstation's browser extension, not the documentation.

## Limits and follow-up

- This check used Chrome on Windows. Safari, Firefox, real mobile browsers and assistive technologies were not tested.
- The browser preview demonstrates the shared appearance and component flow. It does not prove native IME, system gestures, safe areas or VoiceOver behavior. See [native verification](verification.md).
- The app Wasm, Skiko runtime, JavaScript and bundled CJK font total roughly 20 MiB before transfer compression. Webpack reports large-asset warnings; first-load cost remains a prototype limitation.
- The dependency runtime logs a nonfatal deprecation warning about accessing Wasm memory via `wasmExports`. The tested interactions completed; this is not a clean-console claim.
- Docs deployment and the published form flow passed. Hosted Android tests and the iOS build and launch smoke check also passed. The iOS check covers both frameworks, the Swift host, simulator installation and launch, a five-second process check, and a screenshot; it does not cover the complete form interaction flow. See [delivery status](delivery.md) and [CI validation](ci-validation.md).

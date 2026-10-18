# Documentation and browser verification

The original form and documentation checks were recorded on **2026-10-17**, with Switch, Textarea, RadioGroup and Alert added on **2026-10-18**, using Chrome on Windows. Local checks used `http://127.0.0.1:4173/shadcn-mobile/`. The original complete form flow was also checked separately on [GitHub Pages](https://zhanghan-13.github.io/shadcn-mobile/); the new component checks below are local results.

## Built artifact

- `:composeApp:wasmJsBrowserDistribution` completed successfully.
- `node scripts/build-docs.mjs` packaged the documentation and shared Compose/Wasm runtime under `website/dist/`.
- `node website/verify.mjs --dist` passed: 13 routes, 10 exact Kotlin API signatures, 2 related types, bilingual search, theme tokens, JavaScript syntax, relative assets and packaged Wasm runtime.
- Browser examples call the same Kotlin components as the native app. The complete form uses the shared `App()` entry point; all ten component examples use the shared `ComponentGallery` in `commonMain`.
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

The four new previews were checked on 2026-10-18 inside the desktop documentation's phone-width frame. Its measured content area was **388 × 463 CSS pixels**, inside a 390 × 465 frame. This was not a repeat of the entire documentation site's mobile-viewport check.

| Added area | Observed result |
| --- | --- |
| Switch | Label and track clicks changed the enabled setting. Disabled pointer clicks did nothing. An accessibility activation initially bypassed the disabled callback; after the guard fix and Wasm rebuild, the disabled activation preserved both state and change count. |
| Textarea | Empty validation showed an error. Two lines of Chinese text, including an Enter keypress, were preserved and cleared the error. Pointer activation followed by typing did not change read-only or disabled content. The frame scrolled to the validation button, which responded to a real pointer click. |
| RadioGroup | Selecting free/pro changed the single selected option. Disabled pointer clicks did nothing. After the accessibility callback guard fix, activating the disabled option also preserved the professional selection and count. |
| Alert | Default and destructive messages rendered. The local retry changed the error to “已重新连接” and the action to “连接已恢复”; no network request was made. |
| Dark theme and narrow frame | All four previews were inspected in dark mode; no horizontal clipping was observed. Textarea's lower controls remained reachable through internal scrolling. |
| Related API types | Searching `RadioOption` found RadioGroup, Enter opened it, and the API anchor and full signature expanded correctly. `RadioOption` and `AlertVariant` are also compared with Kotlin source by the static verifier. |

The existing disabled Button/Checkbox examples have empty callbacks, so their unchanged browser UI alone cannot prove callback suppression. The Android semantic-action regression verifies those guards and the loading Button guard separately.

## Published site

[Docs and Pages run 39045335653](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39045335653) passed and deployed the documentation to [GitHub Pages](https://zhanghan-13.github.io/shadcn-mobile/).

On the deployed site in Chrome, the complete form flow passed: entered the form values, selected a role, saved notification preferences in the Sheet, checked the consent box, reviewed and confirmed the Dialog, and reached the success summary. This was a manual check of the rendered UI; no backend submission occurs. The success-screen capture is retained locally at `.local/published-form-success.png`.

This deployed check covers the complete form flow. The individual documentation controls and component examples in the table above retain their original local verification scope.

The feature-branch [Docs run 39115959199](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39115959199) passed at `f072a20`: Wasm built, all 13-route/10-API/2-type checks passed, and the Pages artifact was uploaded. Deployment was skipped; this document does not claim that the four added pages have been verified on the public site.

## Screenshots

- [Desktop documentation and shared form](screenshots/docs-desktop.png)
- [Mobile-width dark Checkbox documentation](screenshots/docs-mobile-dark.png)
- [Textarea with Chinese multiline input](screenshots/web-textarea-multiline.png)
- [Alert in dark documentation and preview](screenshots/web-alert-dark.png)

The captures are visual inspection evidence, not pixel comparison assertions. A floating translation badge visible at the right edge belongs to the workstation's browser extension, not the documentation.

## Limits and follow-up

- This check used Chrome on Windows. Safari, Firefox, real mobile browsers and assistive technologies were not tested.
- The browser preview demonstrates the shared appearance and component flow. It does not prove native IME, system gestures, safe areas or VoiceOver behavior. See [native verification](verification.md).
- The app Wasm, Skiko runtime, JavaScript and bundled CJK font total roughly 20 MiB before transfer compression. Webpack reports large-asset warnings; first-load cost remains a prototype limitation.
- The dependency runtime logs a nonfatal deprecation warning about accessing Wasm memory via `wasmExports`. The tested interactions completed; this is not a clean-console claim.
- The recorded public-site check covers the original form. The expanded iOS suite separately passed all 13 native UI tests; browser results do not replace those native results. See [delivery status](delivery.md) and [CI validation](ci-validation.md).

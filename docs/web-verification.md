# Documentation and browser verification

The original form and documentation checks were recorded on **2026-10-17**, with Switch, Textarea, RadioGroup and Alert added on **2026-10-18**, using Chrome on Windows. Local checks used `http://127.0.0.1:4173/shadcn-mobile/`. The local results are listed first; the separate [GitHub Pages](https://zhanghan-13.github.io/shadcn-mobile/) checks at main `4677984` are recorded under Published site.

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

On **2026-10-18**, [Docs and Pages run 39119042228](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39119042228) passed at main commit `46779842fc43820abc9b2a177f638283a1408e7c`. The workflow built Wasm, verified 13 routes / 10 exact Kotlin APIs / 2 related types, uploaded the artifact and deployed it to **https://zhanghan-13.github.io/shadcn-mobile/**. The deployment job completed at 10:27:14 UTC. The following checks used that public URL in Chrome on Windows, through the rendered UI.

| Public check | Observed result |
| --- | --- |
| Switch | The enabled control changed state and count. Pointer and accessibility activation of the disabled control preserved its state and did not increase the count. |
| Textarea | Entered `公开站验证：第一行`, pressed Enter and entered `第二行：中文多行`; both lines remained visible. Clicking the read-only field and typing did not alter its content. Empty validation also displayed the expected error during the later navigation check. |
| RadioGroup | Selected professional, verified that only that option was selected, then returned to free with a count of 2. Pointer and accessibility activation of the disabled enterprise option did not change the selection or count. |
| Alert | Retry changed the destructive message to “已重新连接” and the action to “连接已恢复”. This is local demo state, not a network recovery test. |
| Directory and search | The sidebar exposed all ten components. Searching `RadioOption` returned RadioGroup and Enter opened it. Six subsequent real directory clicks followed Switch → Textarea → RadioGroup → Alert → Switch → Textarea; each preview loaded and responded to an action without Reset. |
| Homepage status | The page displayed 15 Android interaction tests plus 10 shared tests and 13 iOS native tests. The links pointed to runs `39116229205` and `39116804249` respectively. The iOS physical-device, manual VoiceOver and larger-type limitations remained visible. |
| Complete form | Entered `公开站回归工作室` and `hello@example.com`, selected developer, saved both notification options, accepted the demo checkbox, reviewed the same values and confirmed. The success summary showed the name, email, role and both notification settings. No backend submission occurs. |

Two early navigations showed a white preview without the loading overlay while the accessibility tree reported `about:blank`: Textarea → RadioGroup used a hash URL navigation; RadioGroup → Alert used a real sidebar click. Reset recovered both. The frame was visible and its `src` was correct; no `loading` attribute was present. Later Alert reload, search navigation and the six real directory clicks above loaded normally without Reset. The supported Chrome inspection API did not expose the child document's URL/readiness, so the cause was not established and no loading fix is claimed. A later normal observation reported the top-level document as visible. Brief `about:blank` observations also preceded successful loads, so that accessibility value alone was not treated as a failure.

A separate check in the Codex in-app browser opened Switch directly, then used directory links to visit RadioGroup → Alert → Textarea. All four previews loaded without Reset. The Switch count changed to 1, RadioGroup selected professional with a count of 1, Alert changed to its recovered state, and Textarea retained Chinese text across a real Enter keypress. The final multiline screenshot is retained at `.local/published-4677984/iab-textarea.png`. This cross-check did not reproduce the earlier Chrome blanks or establish their cause.

Public screenshots are retained locally under `.local/published-4677984/`: `switch.png`, `textarea.png`, `radio.png`, `alert.png`, `status.png` and `form-success.png`. The deployment JSON and log are retained under `.local/pages-run-39119042228.*`. This was a desktop-site check with the phone-width preview frame, not another full mobile-viewport, dark-theme or assistive-technology audit. The other documentation controls in the earlier tables retain their recorded local scope.

For history, [Docs run 39045335653](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39045335653) deployed the original site and its complete form passed a public Chrome check (`.local/published-form-success.png`). Feature-branch [Docs run 39115959199](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39115959199) later passed at `f072a20`, including the expanded artifact checks; deployment was skipped on that branch.

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
- The public-site check covers the four added previews, representative navigation/search, status links and a complete form flow, with the loading observations recorded above. The expanded iOS suite separately passed all 13 native UI tests; browser results do not replace those native results. See [delivery status](delivery.md) and [CI validation](ci-validation.md).

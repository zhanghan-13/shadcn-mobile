# GitHub and documentation delivery

## Repository

- Public repository created: [zhanghan-13/shadcn-mobile](https://github.com/zhanghan-13/shadcn-mobile).
- Local `origin` points to `https://github.com/zhanghan-13/shadcn-mobile.git`.
- Source and workflows have been pushed to the public repository.
- Published documentation: [shadcn-mobile on GitHub Pages](https://zhanghan-13.github.io/shadcn-mobile/).

## Pipeline

| Workflow | Runs | Output |
| --- | --- | --- |
| Android | Relevant PRs, pushes to main, manual dispatch | Debug APK, 10 shared tests, 8 emulator interaction tests and screenshots |
| iOS | Relevant PRs, pushes to main, manual dispatch | Linked device/simulator frameworks, built Swift host, 8 native simulator UI tests, reports and screenshots |
| Docs and Pages | Relevant PRs, pushes to main, manual dispatch | Static Chinese docs with real shared Compose/Wasm demos; main-only Pages deployment |

As recorded on **2026-10-17**, [Docs and Pages run 39045335653](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39045335653) passed and deployed the site. The complete form was also exercised on the published site in Chrome, through confirmation and the success summary. [Android run 39045766118](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39045766118) passed with 10 shared host tests, 8 instrumentation tests and six screenshots.

On **2026-10-18**, [iOS run 39112862050](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39112862050) passed at commit `ac4ca80` on the validation branch. Both Apple frameworks linked, the Swift host built and all 8 native UI tests passed, with 0 failures, 0 skips and no retries. The run used the standard Apple silicon `macos-26` runner with macOS 26.6.2, Xcode 26.4.1 and Swift 6.3.1, targeting an iPhone 17 Pro simulator on iOS 26.5. The uploaded evidence includes `.xcresult`, JSON reports and screenshots.

The suite covers the complete Chinese form flow, validation, keyboard Next/Done, searchable selection, Sheet cancellation/swipe dismissal, review and terms dialogs, themes and reset. Physical-device/signing tests, manual VoiceOver checks and larger Dynamic Type remain unverified. No local Mac run was performed in this session. The workflow pins action revisions; runner images and toolchain availability should be reviewed when upgrading dependencies.

## Local use

```powershell
.\gradlew.bat :composeApp:wasmJsBrowserDistribution
node scripts/build-docs.mjs
node scripts/serve-docs.mjs
```

The local URL is `http://127.0.0.1:4173/shadcn-mobile/`. `website/dist/` is generated and ignored by Git. Documentation routes use hashes and relative asset paths so a GitHub Pages project subdirectory works without server-side rewrites.

The production Wasm build and static-site checks passed. The complete form, component examples, search, code copying, themes and responsive navigation were exercised in Chrome. See [browser verification and screenshots](web-verification.md) and [native verification](verification.md).

## Checks for later updates

1. Inspect existing local and remote commit history before committing and follow the established subject style. Every commit must end its body with the actual tool and model, for example `Assisted-by: Codex (gpt-6-astra)` for the verified tool and model used here. Confirm the model before committing; if it is unknown, ask the owner and wait for confirmation.
2. Review the exact staged source and exclude local SDK, cache, emulator and generated build directories.
3. Recheck commit descriptions and remote history before pushing the `main` branch.
4. Verify the actual Android, iOS and Docs workflow results before reporting them as successful.
5. Open the deployed Pages URL and repeat the browser form and component interactions affected by the update.

No deployment result is inferred from workflow syntax validation or a successful local build.

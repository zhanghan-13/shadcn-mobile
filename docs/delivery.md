# GitHub and documentation delivery

## Repository

- Public repository created: [hanzhang-13/shadcn-mobile](https://github.com/hanzhang-13/shadcn-mobile).
- Local `origin` points to `https://github.com/hanzhang-13/shadcn-mobile.git`.
- Source and workflows have been pushed to the public repository.
- Published documentation: [shadcn-mobile on GitHub Pages](https://hanzhang-13.github.io/shadcn-mobile/).

## Pipeline

| Workflow | Runs | Output |
| --- | --- | --- |
| Android | Relevant PRs, pushes to main, manual dispatch | Debug APK, 10 shared tests, 8 emulator interaction tests and screenshots |
| iOS | Relevant PRs, pushes to main, manual dispatch | Linked device/simulator frameworks, built Swift host, simulator startup smoke test and screenshot |
| Docs and Pages | Relevant PRs, pushes to main, manual dispatch | Static Chinese docs with real shared Compose/Wasm demos; main-only Pages deployment |

As recorded on **2026-10-17**, [Docs and Pages run 39045335653](https://github.com/hanzhang-13/shadcn-mobile/actions/runs/39045335653) passed and deployed the site. The complete form was also exercised on the published site in Chrome, through confirmation and the success summary. [Android run 39045766118](https://github.com/hanzhang-13/shadcn-mobile/actions/runs/39045766118) passed with 10 shared host tests, 8 instrumentation tests and six screenshots.

The iOS job uses the standard Apple silicon `macos-26` image and Xcode 26.4.1. [iOS run 39045335671](https://github.com/hanzhang-13/shadcn-mobile/actions/runs/39045335671) passed: both device and simulator frameworks linked, the Swift host built, the app installed and launched in the simulator, and its process remained alive after five seconds. The screenshot and other evidence were uploaded. This verifies the build and launch smoke check, not the complete iOS form interaction flow. No local Mac run was performed in this session. The workflow pins action revisions; runner images and toolchain availability should be reviewed when upgrading dependencies.

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

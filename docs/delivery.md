# GitHub and documentation delivery

## Repository

- Public repository created: [zhanghan-13/shadcn-mobile](https://github.com/zhanghan-13/shadcn-mobile).
- Local `origin` points to `https://github.com/zhanghan-13/shadcn-mobile.git`.
- Source and workflows have been pushed to the public repository.
- Published documentation: [shadcn-mobile on GitHub Pages](https://zhanghan-13.github.io/shadcn-mobile/).

## Pipeline

| Workflow | Runs | Output |
| --- | --- | --- |
| Android | Relevant PRs, pushes to main, manual dispatch | Debug APK, 10 shared tests, 15 emulator interaction tests and screenshots |
| iOS | Relevant PRs, pushes to main, manual dispatch | Linked device/simulator frameworks, built Swift host, 13 native simulator UI tests, reports and screenshots |
| Docs and Pages | Relevant PRs, pushes to main, manual dispatch | 13 Chinese documentation routes and ten shared Compose/Wasm component examples; main-only Pages deployment |

On **2026-10-18**, [main Docs and Pages run 39119042228](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39119042228) passed at `46779842fc43820abc9b2a177f638283a1408e7c` and deployed to [the public site](https://zhanghan-13.github.io/shadcn-mobile/). The build verified 13 routes, 10 exact Kotlin APIs and 2 related types. Deployment completed at 10:27 UTC. Public Chrome checks then covered all four added previews, disabled Switch/Radio activation, Chinese multiline and read-only Textarea behavior, Alert recovery, representative navigation/search, homepage test links and a complete form submission to the local success summary. Two early blank previews recovered after Reset; six subsequent real directory navigations loaded and responded without Reset. The cause of those initial blanks was not established. See [the detailed browser record](web-verification.md#published-site).

[Main Android run 39119042251](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39119042251) also passed at `4677984`: 10 shared host tests and 15 distinct instrumentation tests, with no failures or skips. Instrumentation took 113.239 seconds; all ten required screenshots were collected and the artifact was uploaded.

As recorded on **2026-10-17**, [Docs and Pages run 39045335653](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39045335653) passed and deployed the site. The complete form was also exercised on the published site in Chrome, through confirmation and the success summary. [Android run 39045766118](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39045766118) passed with 10 shared host tests, 8 instrumentation tests and six screenshots.

The expanded component set passed validation on the feature branch. [Docs run 39115959199](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39115959199) passed at `f072a20`: the Wasm build and 13-route/10-API/2-type checks succeeded, the Pages artifact was uploaded, and deployment was skipped. [Android run 39116229205](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39116229205) passed at `743c97c`: 10 shared tests and all 15 distinct instrumentation tests passed with 0 failures/skips, and ten screenshots were retained. Instrumentation took 93.053 seconds. The follow-ups adjust only the Android screenshot helper and an iOS keyboard-disappearance wait; shared component sources are unchanged. The full local checks passed 10 shared tests and 15 Android instrumentation tests, and all four new component tests also passed in dark mode at 150% font scale.

On **2026-10-18**, the feature-branch [iOS run 39116804249](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39116804249) passed at commit `5558058`. Both Apple frameworks linked, the Swift host built and all 13 distinct native UI tests passed, with 0 failures, 0 skips, 0 expected failures and no retries. Test execution took 817.756 seconds. The run used the standard Apple silicon `macos-26` runner with macOS 26.6.2, Xcode 26.4.1 and Swift 6.3.1, targeting an iPhone 17 Pro simulator on iOS 26.5. The uploaded evidence includes `.xcresult`, JSON reports and screenshots.

The later [main iOS run 39119042343](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39119042343) at `4677984` failed: 12 of 13 distinct tests passed, one exceeded its total execution-time allowance, and none were skipped or expected failures. The complete Chinese flow crossed the 180-second budget and continued through its success and reset assertions by 225.57 seconds; Xcode still recorded it as failed. After a runner restart, the remaining 11 methods passed without repeating a method. The earlier `5558058` result remains evidence for that successful run, not a passing result for this main rerun.

[iOS run 39121630985](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39121630985) then passed at `ef77931760c3825ca8befdccc75fa16fd48f76e6` on `fix/ios-main-regression`. The commit gives only the complete Chinese flow a 300-second allowance, preserving the other 180-second defaults, all actions, assertions and per-step waits. All 13 distinct methods started and passed once, with 0 failures, 0 skips, 0 expected failures and no retries or runner restarts. The complete flow took 197.856 seconds and the suite took 842.862 seconds; the full job ran 11:02:36–11:28:08 UTC (25 minutes 32 seconds). The run linked both frameworks, built the Swift host and uploaded its reports and attachments. The current checked-in iOS JSON reports come from this run; the reviewed component screenshots still come from `5558058`.

The 13-test result covers the Chinese form flow, validation, keyboard Next/Done, searchable selection, Sheet cancellation/swipe dismissal, review and terms dialogs, themes, reset, Switch, Textarea, RadioGroup, Alert and gallery navigation. Physical-device/signing tests, manual VoiceOver checks and larger Dynamic Type remain unverified. No local Mac run was performed in this session. The workflow pins action revisions; runner images and toolchain availability should be reviewed when upgrading dependencies.

## Local use

```powershell
.\gradlew.bat :composeApp:wasmJsBrowserDistribution
node scripts/build-docs.mjs
node scripts/serve-docs.mjs
```

The local URL is `http://127.0.0.1:4173/shadcn-mobile/`. `website/dist/` is generated and ignored by Git. Documentation routes use hashes and relative asset paths so a GitHub Pages project subdirectory works without server-side rewrites.

The production Wasm build and static-site checks passed. The original form, documentation controls and responsive navigation were exercised in Chrome; the four new component previews were then tested locally in light/dark themes at a 388px content width. Disabled Switch/Radio accessibility activation was fixed and retested, and Textarea Chinese multiline entry and Alert recovery passed. See [browser verification and screenshots](web-verification.md) and [native verification](verification.md).

## Checks for later updates

1. Inspect existing local and remote commit history before committing and follow the established subject style. Every commit must end its body with the actual tool and model, for example `Assisted-by: Codex (gpt-6-astra)` for the verified tool and model used here. Confirm the model before committing; if it is unknown, ask the owner and wait for confirmation.
2. Review the exact staged source and exclude local SDK, cache, emulator and generated build directories.
3. Recheck commit descriptions and remote history before pushing the `main` branch.
4. Verify the actual Android, iOS and Docs workflow results before reporting them as successful.
5. Open the deployed Pages URL and repeat the browser form and component interactions affected by the update.

No deployment result is inferred from workflow syntax validation or a successful local build.

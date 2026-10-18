# CI configuration and validation

Updated on **2026-10-18**. Local checks and hosted results are recorded separately below. The latest verified Docs deployment and Android run target main `4677984`; the verified 13-test iOS result targets `5558058` after its keyboard-wait adjustment. Check [GitHub Actions](https://github.com/zhanghan-13/shadcn-mobile/actions) for subsequent runs.

## Hosted results

| Workflow | Run | Verified result |
| --- | --- | --- |
| Docs and Pages, main | [39119042228](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39119042228) | Passed at `46779842fc43820abc9b2a177f638283a1408e7c`: Wasm built, 13-route/10-API/2-related-type checks passed, artifact uploaded and deployed to [GitHub Pages](https://zhanghan-13.github.io/shadcn-mobile/) at 10:27 UTC. The four added previews and a complete form flow were then exercised on the public site; see [browser results and loading observations](web-verification.md#published-site). |
| Android, main | [39119042251](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39119042251) | Passed at `4677984`: 10 host tests and 15 distinct instrumentation tests, no failures/skips, ten required screenshots collected and artifact uploaded. Instrumentation took 113.239 seconds; the job ran 10:23:15–10:29:41 UTC. |
| Android, expanded suite | [39116229205](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39116229205) | Passed at `743c97c`: 10 host tests and 15 distinct instrumentation tests, 0 failures/skips, ten nonempty screenshots. Instrumentation took 93.053 seconds; APKs, reports and screenshots were uploaded. |
| Docs, ten components | [39115959199](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39115959199) | Passed at `f072a20`: Wasm built, 13-route/10-API/2-related-type checks passed, Pages artifact uploaded. Deployment was skipped on this feature branch. |
| iOS, expanded suite | [39116804249](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39116804249) | Passed at `5558058`: both frameworks linked, Swift host built, 13 distinct native UI tests passed with 0 failures/skips/expected failures and no retries. Test execution took 817.756 seconds; reports and attachments were uploaded. |
| Android | [39045766118](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39045766118) | Passed: 10 shared host tests, 8 instrumentation tests and six screenshots. |
| Docs and Pages | [39045335653](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39045335653) | Passed and deployed the [published documentation](https://zhanghan-13.github.io/shadcn-mobile/). The complete form also passed a manual Chrome check on the deployed site; see [browser verification](web-verification.md). |

## Workflows

| Workflow | Execution | Evidence retained |
| --- | --- | --- |
| [Android](../.github/workflows/android.yml) | One `ubuntu-latest` runner builds the app/test APKs, runs 10 shared host tests, then runs 15 instrumentation tests on a single API 35 x86_64 Google APIs emulator. | APKs, host JUnit XML/HTML, instrumentation output, logcat, and ten required screenshots. |
| [iOS](../.github/workflows/ios.yml) | One standard Apple silicon `macos-26` runner with Xcode 26.4.1 links both Apple frameworks and executes 13 `iosAppUITests` serially on one dedicated iPhone simulator. | Framework/build/test logs, `.xcresult`, exported test reports and attachments, simulator inventory, failure accessibility trees, screenshots and a simulator app ZIP. |
| [Docs and Pages](../.github/workflows/docs.yml) | Builds the shared Compose Wasm distribution and the static documentation, validates the output, and uploads a Pages artifact. A separate job deploys only on `main` push/manual runs. | A Pages artifact containing `website/dist`, including the shared demo under `demo/`. |

The completed iOS run `39116804249` used macOS 26.6.2, Xcode 26.4.1 (17E202) and Swift 6.3.1, with one iPhone 17 Pro simulator running iOS 26.5 (23F77). Its `.xcresult` summary reports 13 passed, 0 failed, 0 skipped and 0 expected failures. The report checker verified 13 distinct passing methods, and the log records one execution of each method with no retries or runner restarts. The native suite took 817.756 seconds. Earlier iOS runs and the keyboard-wait diagnosis are retained in [native verification history](verification.md#earlier-ios-runs).

The UI suite covers required fields, invalid email, a complete Chinese workspace flow with review cancellation and reset, role-search recovery, discarded Sheet edits after cancel/swipe, keyboard Next/Done, theme changes, the terms dialog, Switch, Textarea, RadioGroup, Alert and gallery navigation. It uses system keyboard input, a native Paste action for Chinese text, taps and swipes. Tests do not inject form state. Failed runs retain a screenshot and accessibility tree; successful steps retain screenshots for visual inspection.

This simulator result does not establish physical-device/signing coverage, manual VoiceOver validation or larger Dynamic Type coverage.

Changes relevant to each platform trigger that platform's workflow. Pull requests execute build/test jobs with `contents: read`; they never deploy. The Pages deployment job alone receives `pages: write` and `id-token: write`. No workflow uses `pull_request_target`, repository write permissions, signing secrets, or a runner matrix. Checkout credentials are not persisted. Gradle uses its open-source `basic` cache provider, and pull-request caches are read-only.

## Local checks completed

- **actionlint 1.7.12:** all three workflow files passed with no diagnostics. The official Windows release ZIP was checked against its published SHA-256 manifest before use.
- **Bash parsing:** `bash -n scripts/ci-android.sh` and `bash -n scripts/ci-ios.sh` passed.
- **Host-test report check:** `ci-check-tests.py` read the existing local JUnit report and verified **10 passing tests**. This did not rerun those tests.
- **Instrumentation report check:** the local run at `f072a20` recorded **15 passing tests** in 41.61 seconds; the CI checker now requires at least 15. The separate dark-mode/150%-font run passed the four new component tests in 8.576 seconds. After the Android screenshot adjustment at `743c97c`, the complete-form test passed locally in 9.82 seconds; the hosted full-suite result is recorded separately above.
- **Failure-path checks:** the checker rejected an empty JUnit suite, failed JUnit results, failed instrumentation output, and incomplete instrumentation output.

The Android report checker requires at least 10 host tests and 15 instrumentation tests, with no failed/skipped host tests. All ten expected screenshots must exist before the CI script succeeds. Instrumentation runs through `adb shell am instrument` so the app's screenshot files can be collected before the temporary emulator is removed. Runner output is checked explicitly because `adb` exiting successfully alone does not prove that the tests passed.

The iOS checker requires at least 13 distinct methods from `iosAppUITests`, with zero failures, skips or expected failures, and rejects retries or duplicate executions. Xcode runs the suite once, serially, with a 180-second default and 300-second maximum per-test allowance. The script selects a shutdown iPhone and shuts it down afterward only if this invocation successfully booted it; already-running simulators are left alone. Exported `xcresulttool` JSON and command help are retained with the artifact.

The static website has its own validation command, `node website/verify.mjs --dist`, which is wired into the Docs workflow after the build. Website and browser verification results are recorded separately from these CI configuration checks.

## Reproduce the configuration checks

```sh
actionlint .github/workflows/android.yml .github/workflows/ios.yml .github/workflows/docs.yml
bash -n scripts/ci-android.sh
bash -n scripts/ci-ios.sh
python3 scripts/ci-check-tests.py junit composeApp/build/test-results/testAndroidHostTest 10
python3 scripts/ci-check-tests.py instrumentation docs/instrumentation-run.txt 15
```

The report-check commands require reports from a prior successful local test run. To execute the actual jobs locally on supported hosts:

```sh
# Android SDK/JDK and one running emulator are required.
bash ./gradlew :androidApp:assembleDebug :androidApp:assembleDebugAndroidTest :composeApp:testAndroidHostTest
bash scripts/ci-android.sh

# Requires Apple silicon macOS and Xcode.
DEVELOPER_DIR=/Applications/Xcode_26.4.1.app/Contents/Developer bash scripts/ci-ios.sh

# Build the documentation and shared browser demo.
bash ./gradlew :composeApp:wasmJsBrowserDistribution
node scripts/build-docs.mjs
node website/verify.mjs --dist
```

## Pinned dependencies

External actions are pinned to release commit SHA values, verified against their official GitHub releases, tags or APIs on the recorded date. The readable release number remains next to each SHA in the workflow.

| Action | Release |
| --- | --- |
| [actions/checkout](https://github.com/actions/checkout/releases/tag/v7.0.1) | 7.0.1 |
| [actions/setup-java](https://github.com/actions/setup-java/releases/tag/v6.1.0) | 6.1.0 |
| [gradle/actions](https://github.com/gradle/actions/releases/tag/v6.4.0) | 6.4.0 |
| [android-actions/setup-android](https://github.com/android-actions/setup-android/releases/tag/v4.0.4) | 4.0.4 |
| [ReactiveCircus/android-emulator-runner](https://github.com/ReactiveCircus/android-emulator-runner/releases/tag/v2.38.0) | 2.38.0 |
| [actions/upload-artifact](https://github.com/actions/upload-artifact/releases/tag/v7.0.2) | 7.0.2 |
| [actions/setup-node](https://github.com/actions/setup-node/releases/tag/v7.1.0) | 7.1.0 |
| [actions/configure-pages](https://github.com/actions/configure-pages/releases/tag/v6.0.0) | 6.0.0 |
| [actions/upload-pages-artifact](https://github.com/actions/upload-pages-artifact/releases/tag/v5.0.0) | 5.0.0 |
| [actions/deploy-pages](https://github.com/actions/deploy-pages/releases/tag/v5.0.1) | 5.0.1 |

## Official references

- shadcn/ui separates [code checks](https://github.com/shadcn-ui/ui/blob/main/.github/workflows/code-check.yml), [unit tests](https://github.com/shadcn-ui/ui/blob/main/.github/workflows/test.yml), and [browser tests](https://github.com/shadcn-ui/ui/blob/main/.github/workflows/browser-tests.yml). This project follows that separation of responsibilities with native-platform and documentation workflows; it does not copy the React/pnpm commands.
- [GitHub runner labels](https://github.com/actions/runner-images#available-images) identify `macos-26` as a standard ARM64 runner. The [macOS 26 ARM64 image inventory](https://github.com/actions/runner-images/blob/main/images/macos/macos-26-arm64-Readme.md) lists Xcode 26.4.1. No `large` or `xlarge` runner is requested.
- [Android Emulator Runner documentation](https://github.com/ReactiveCircus/android-emulator-runner#usage--examples) supplies the Ubuntu KVM setup and supported emulator inputs.
- [Gradle setup documentation](https://github.com/gradle/actions/blob/main/docs/setup-gradle.md) documents wrapper validation and cache configuration.
- [GitHub Pages custom workflows](https://docs.github.com/en/pages/getting-started-with-github-pages/using-custom-workflows-with-github-pages) documents separate build/deploy jobs, artifact handling, environment settings, and deployment permissions.
- [actionlint 1.7.12](https://github.com/rhysd/actionlint/releases/tag/v1.7.12) was used for local workflow validation.

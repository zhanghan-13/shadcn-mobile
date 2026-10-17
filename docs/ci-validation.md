# CI configuration and local validation

Recorded on **2026-10-17**, before the initial publication. These are local configuration checks; no hosted workflow or Pages deployment had run at the time of these checks. Passing them does not establish that the hosted runner jobs pass. Check [GitHub Actions](https://github.com/hanzhang-13/shadcn-mobile/actions) for hosted results.

## Workflows

| Workflow | Execution | Evidence retained |
| --- | --- | --- |
| [Android](../.github/workflows/android.yml) | One `ubuntu-latest` runner builds the app/test APKs, runs the shared host tests, then runs the Android instrumentation suite on a single API 35 x86_64 Google APIs emulator. | APKs, host JUnit XML/HTML, instrumentation output, logcat, and six screenshots. |
| [iOS](../.github/workflows/ios.yml) | One standard Apple silicon `macos-26` runner with Xcode 26.4.1 links both Apple frameworks, builds the Swift host, then installs and launches it on one available iPhone simulator. | Framework/Swift build logs, Xcode result bundle, simulator inventory, launch/process logs, screenshot, and a simulator app ZIP. |
| [Docs and Pages](../.github/workflows/docs.yml) | Builds the shared Compose Wasm distribution and the static documentation, validates the output, and uploads a Pages artifact. A separate job deploys only on `main` push/manual runs. | A Pages artifact containing `website/dist`, including the shared demo under `demo/`. |

The iOS job is a **build and launch smoke check**, not an iOS form interaction test suite. It checks that the app process remains alive after launch and captures the screen for review. Its actual execution still requires the future hosted macOS run.

Changes relevant to each platform trigger that platform's workflow. Pull requests execute build/test jobs with `contents: read`; they never deploy. The Pages deployment job alone receives `pages: write` and `id-token: write`. No workflow uses `pull_request_target`, repository write permissions, signing secrets, or a runner matrix. Checkout credentials are not persisted. Gradle uses its open-source `basic` cache provider, and pull-request caches are read-only.

## Local checks completed

- **actionlint 1.7.12:** all three workflow files passed with no diagnostics. The official Windows release ZIP was checked against its published SHA-256 manifest before use.
- **Bash parsing:** `bash -n scripts/ci-android.sh` and `bash -n scripts/ci-ios.sh` passed.
- **Host-test report check:** `ci-check-tests.py` read the existing local JUnit report and verified **10 passing tests**. This did not rerun those tests.
- **Instrumentation report check:** the checker read the existing local emulator run and verified **8 passing tests**. This did not rerun the emulator suite.
- **Failure-path checks:** the checker rejected an empty JUnit suite, failed JUnit results, failed instrumentation output, and incomplete instrumentation output.

The report checker requires at least 10 host tests and 8 instrumentation tests, with no failed/skipped host tests. Android screenshots must also exist before the CI script succeeds. Instrumentation runs through `adb shell am instrument` so the app's screenshot files can be collected before the temporary emulator is removed. Runner output is checked explicitly because `adb` exiting successfully alone does not prove that the tests passed.

The static website has its own validation command, `node website/verify.mjs --dist`, which is wired into the Docs workflow after the build. Website and browser verification results are recorded separately from these CI configuration checks.

## Reproduce the configuration checks

```sh
actionlint .github/workflows/android.yml .github/workflows/ios.yml .github/workflows/docs.yml
bash -n scripts/ci-android.sh
bash -n scripts/ci-ios.sh
python3 scripts/ci-check-tests.py junit composeApp/build/test-results/testAndroidHostTest 10
python3 scripts/ci-check-tests.py instrumentation docs/instrumentation-run.txt 8
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

External actions are pinned to release commit SHA values, resolved from their official GitHub release/tag APIs on the recorded date. The readable release number remains next to each SHA in the workflow.

| Action | Release |
| --- | --- |
| [actions/checkout](https://github.com/actions/checkout/releases/tag/v7.0.1) | 7.0.1 |
| [actions/setup-java](https://github.com/actions/setup-java/releases/tag/v6.1.0) | 6.1.0 |
| [gradle/actions](https://github.com/gradle/actions/releases/tag/v6.4.0) | 6.4.0 |
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

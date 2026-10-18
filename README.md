# shadcn-mobile

A native Compose Multiplatform prototype of ten shadcn-inspired components for **Android and iOS**, with a documentation playground that runs the same Kotlin UI in the browser. The shared form connects Button, Input, Checkbox, a searchable Combobox, Dialog and Sheet. The component gallery also includes Switch, Textarea, RadioGroup and Alert.

Repository: [zhanghan-13/shadcn-mobile](https://github.com/zhanghan-13/shadcn-mobile). The project includes Android/iOS hosts, shared components and an interactive documentation site.

## What the prototype does

Create a local demonstration workspace: enter its name and an email, search for a role in Chinese or English, adjust notification preferences, accept the demo terms, review the details and confirm. Confirmation simulates a short local operation; **nothing is sent to a server and no online account is created**.

- Semantic zinc tokens with light/dark themes and a theme toggle.
- Foundation-based buttons, text fields and checkbox rows; native Compose window containers for menus, dialogs and sheets.
- Switch settings, multiline Textarea, mutually exclusive RadioGroup options and inline Alert messages, with shared interactive examples.
- Minimum 48dp interactive targets, scalable text, focus/pressed/error/disabled/loading states.
- Native IME Next/Done, scrollable form, safe-area and keyboard insets.
- Search with no-results recovery; staged sheet edits that cancel on dismissal.
- Saveable form, theme, selected role and notification preferences across Android recreation. This is saved-instance state, not a durable database.

## Project structure

```text
composeApp/                 Shared Kotlin Multiplatform library
  src/commonMain/           Theme, components, gallery, complete form, validation
  src/commonTest/           Form validation and search tests
  src/iosMain/              Compose UIViewController entry
  src/wasmJsMain/           Browser entry and bundled CJK font
androidApp/                 Android Activity, manifest and executable app
  src/androidTest/          Native emulator interaction and restoration tests
iosApp/                     SwiftUI/UIKit host, XCUITest suite and shared Xcode scheme
docs/                       Verification notes and captured emulator screenshots
website/src/                Chinese documentation, navigation and preview frames
scripts/                    Local builds, preview server and CI checks
.github/workflows/          Android, iOS and documentation/Pages workflows
```

## Android

Open this directory in Android Studio and run the `androidApp` configuration. Install the Android SDK platform required by `compileSdk` in the Gradle configuration. Use JDK 17 or newer compatible with the Gradle wrapper (this workstation uses Android Studio's bundled JDK).

```shell
./gradlew :androidApp:assembleDebug
./gradlew :composeApp:testAndroidHostTest
./gradlew :androidApp:connectedDebugAndroidTest
```

On Windows use `gradlew.bat`. Run connected tests with only the intended emulator connected, or use the helper below to select it explicitly.

The Windows helper configures the bundled JDK and short temporary paths, and explicitly targets only an emulator:

```powershell
.\scripts\android.ps1 -Action Build
.\scripts\android.ps1 -Action Run -Serial emulator-5582
.\scripts\android.ps1 -Action Test -Serial emulator-5582
```

Use the serial reported by `adb devices` on your own machine. The `Test` action checks the runner's final success result and downloads the screenshots.

For a non-default ADB server, pass `-AdbPort`; the current workstation's isolated test instance uses `-Serial emulator-5596 -AdbPort 5043`.

APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.

On **2026-10-18**, [Android run 39116229205](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39116229205) at `743c97c` passed **10 shared host tests and all 15 instrumentation tests**, with no failures or skips. Instrumentation took 93.053 seconds and ten screenshots were retained. Coverage includes the four added components, gallery navigation, Activity recreation, and disabled/loading callback guards under accessibility activation. The earlier local 15-test run at `f072a20` passed in 41.61 seconds. The four new component tests also passed locally in dark mode at 150% system font scale; reviewed screenshots showed no clipping.

## iOS

Requires **macOS, Xcode and a compatible JDK**. Open `iosApp/iosApp.xcodeproj`, choose the shared `iosApp` scheme, then an iPhone simulator. The Xcode build phase invokes the Gradle wrapper to build/embed the `ShadcnMobile` framework. If necessary run `chmod +x gradlew` once after cloning on macOS. Select your own signing team only when running on a physical device.

Targets: `iosArm64` and `iosSimulatorArm64` (Apple silicon simulator), with iOS 16+ in the Xcode host. The same `App()` composable and form logic are shared.

Shared Kotlin sources compile successfully to KLIB for both targets on this Windows workstation. On **2026-10-18**, [iOS run 39116804249](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39116804249) at `5558058` linked both Apple frameworks, built the Swift host and passed all **13 native UI tests**, with no failures, skips, expected failures or retries. The run used an iPhone 17 Pro simulator on iOS 26.5, hosted macOS 26.6.2, Xcode 26.4.1 and Swift 6.3.1. Test execution took 817.756 seconds.

The suite covers validation, Chinese input through native Paste, role search, saved and discarded Sheet edits, keyboard Next/Done, review cancellation, submission/reset, themes and terms-dialog dismissal. It also covers Switch, Chinese multiline Textarea, RadioGroup, Alert and gallery navigation, including disabled/read-only behavior and draft preservation. Run it with Xcode's Test action or `bash scripts/ci-ios.sh` on an Apple silicon Mac. **Physical-device testing, manual VoiceOver checks and larger Dynamic Type remain unverified.**

## Tests and evidence

See [native verification](docs/verification.md), [browser verification](docs/web-verification.md) and [CI configuration checks](docs/ci-validation.md) for the tested environments, results and remaining checks. Android instrumentation screenshots are written to the app's external-files `screenshots` directory, and selected captures are copied to `docs/screenshots/`.

## Interactive documentation

The documentation uses static HTML/CSS/JavaScript around a real Compose/Wasm preview. Its 13 routes include the complete workspace form, ten component examples, Kotlin usage snippets, searchable navigation, light/dark themes and quick-start guides. Browser previews share native component code; they do not emulate Android or iOS system UI.

With Node.js 24 and the Java/Android SDK setup above:

```shell
./gradlew :composeApp:wasmJsBrowserDistribution
node scripts/build-docs.mjs
node scripts/serve-docs.mjs
```

Open [the local documentation](http://127.0.0.1:4173/shadcn-mobile/). The build outputs `website/dist/`, including the Wasm distribution in `demo/`. A self-hosted, SIL-licensed Noto Sans SC font provides Chinese text without a runtime font-service dependency.

The preview requires a browser with WebAssembly GC support. Native keyboard, gesture and assistive-technology behavior must still be tested on the target mobile platforms.

## GitHub Actions and Pages

Three workflows are prepared:

- **Android:** build app/test APKs, verify 10 shared tests and 15 API 35 emulator tests actually ran, and upload APKs/reports/screenshots.
- **iOS:** build device and simulator frameworks on macOS, compile the Swift host, execute 13 native UI tests serially in an iPhone simulator, and collect reports, screenshots and failure evidence.
- **Docs and Pages:** build the shared Wasm preview and static documentation; pull requests build only, while `main` can deploy the validated artifact to Pages.

Actions use pinned revisions and deployment permissions are restricted to the Pages deployment job. This repository uses **Settings → Pages → Source → GitHub Actions**. The workflow publishes the documentation to [zhanghan-13.github.io/shadcn-mobile](https://zhanghan-13.github.io/shadcn-mobile/). Check [GitHub Actions](https://github.com/zhanghan-13/shadcn-mobile/actions) for the latest build and deployment results, and [delivery status](docs/delivery.md) for the verification scope.

## Scope

This is a prototype, not an official shadcn/ui release or a published library. The source is organized for extraction into a reusable component module. No backend, persistence layer, analytics, permission request or network access is included. Production submission should use an application state owner instead of the demo's cancellable local delay.

Visual reference: [shadcn/ui](https://github.com/shadcn-ui/ui) and its [theme conventions](https://ui.shadcn.com/docs/theming). The Kotlin implementations are native reimplementations; React/Radix source is not used at runtime.

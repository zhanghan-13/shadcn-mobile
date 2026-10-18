# Prototype verification

Android verification was recorded on 2026-10-17: builds and all 18 executed tests passed locally and in GitHub Actions. On 2026-10-18, the hosted iOS run linked both Apple frameworks, built the Swift host and passed all 8 native interaction tests with no failures, skips or retries.

## Environment

| Item | Value |
| --- | --- |
| Host | Windows, Android Studio bundled JBR 25 |
| Build | Gradle 9.7.0, Kotlin 2.4.21, AGP 9.3.1 |
| Shared UI | Compose Multiplatform 1.12.2, Material3 1.9.0 |
| Android | compileSdk 37, targetSdk 36, minSdk 24 |
| Tested device | Dedicated `shadcn-mobile` AVD, final regression on `emulator-5596`, ADB server 5043 |
| Emulator OS | Android 15 / API 35, x86_64 |
| Display | 1080 × 2400 pixels, 440 dpi |
| Visual checks | Light, dark, 100% and 150% system font scale |
| iOS targets | `iosArm64`, `iosSimulatorArm64`; iOS 16+ host |
| iOS CI host | Standard Apple silicon `macos-26` runner, macOS 26.6.2 |
| iOS toolchain | Xcode 26.4.1 (17E202), Swift 6.3.1 |
| iOS tested device | iPhone 17 Pro simulator, iOS 26.5 (23F77), arm64 |

All device commands explicitly targeted the dedicated emulator.

## Repeat the Android checks

Start an Android emulator and use its serial from `adb devices`:

```powershell
.\scripts\android.ps1 -Action Test -Serial emulator-5596 -AdbPort 5043
```

The script builds the application and test APKs, runs the shared form tests on the host, installs both APKs on the selected emulator, checks the instrumentation runner's final `OK (8 tests)` result, and retrieves the screenshots. `AdbPort` defaults to 5037; the isolated server used for this workstation's final regression is 5043. The final run after adding Wasm support passed in 21.59 seconds of instrumentation time.

| Check | Result | Evidence |
| --- | --- | --- |
| Android debug APK + test APK | Passed | [Build output](build-run.txt) |
| Shared form logic | 10 passed, 0 failures, 0 skipped | `composeApp/build/reports/tests/testAndroidHostTest/index.html` |
| Android interaction tests | 8 passed, 0 failures | [Runner output](instrumentation-run.txt) |
| iOS device shared Kotlin compilation | Passed | [Compiler output](ios-kotlin-compile.txt) |
| iOS simulator shared Kotlin compilation | Passed | [Compiler output](ios-kotlin-compile.txt) |

Android APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.

### Android interaction coverage

1. Main controls have at least 48dp touch-target height.
2. An empty submission displays required-field errors and does not open confirmation.
3. System Back dismisses Combobox, Sheet and Dialog without losing the form; dismissing the Sheet discards unsaved preferences.
4. The complete form supports Chinese text, English role search, saved preferences, checkbox consent, confirmation cancellation, successful local submission and starting again.
5. IME Next focuses the email field; Done clears focus while preserving entered text.
6. An empty search result can recover to a valid selection.
7. Switching themes preserves the draft and selected role.
8. Compose saved-instance-state restoration restores text, selection, consent and notification preferences.

The 10 shared tests cover required fields, name boundaries, whitespace normalization, email validation, invalid role identifiers, and Chinese/English role searching. The restoration test uses `StateRestorationTester`; this is not a claim of durable storage or a device process-kill test.

### Captured UI

The screenshots below are captured from the running Android application. Files 01–06 use Compose PixelCopy capture, with overlays captured at their component bounds; other images use full-device screenshots. They are visual inspection evidence, not pixel-golden assertions.

| Capture | What was inspected |
| --- | --- |
| [Initial screen](screenshots/00-initial.png) | Form layout, light system bars and native navigation area |
| [Form](screenshots/01-form.png) | Shared typography, spacing, neutral colors and controls |
| [Combobox](screenshots/02-combobox.png) | Search field and bilingual choices |
| [Sheet](screenshots/03-sheet.png) | Preference rows, selected states and actions |
| [Dialog](screenshots/04-review.png) | Entered values and confirmation actions |
| [Success](screenshots/05-success.png) | Local confirmation and reset action |
| [Dark theme](screenshots/06-dark.png) | Token colors, borders and foreground contrast |
| [150% font, dark](screenshots/07-large-dark.png) | Larger text and header layout |
| [150% font, scrolled](screenshots/08-large-dark-bottom.png) | Bottom action remains reachable by scrolling |
| [Native keyboard](screenshots/09-keyboard.png) | Email keyboard and visible focused input |

The emulator was returned to 100% system font scale and light mode after the checks.

## iOS verification boundary

These tasks completed successfully and produced target KLIBs:

```shell
./gradlew :composeApp:compileKotlinIosArm64 :composeApp:compileKotlinIosSimulatorArm64
```

The [hosted iOS run 39112862050](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39112862050) tested commit `ac4ca80` on 2026-10-18. It linked both Apple frameworks, built the SwiftUI/UIKit host and ran `iosAppUITests` serially on the iPhone simulator listed above. The exported `.xcresult` summary reports **8 passed, 0 failed, 0 skipped and 0 expected failures**. The test tree contains eight distinct passing methods; the log records each method starting and passing once, without retries or runner restarts. Test execution took 486.230 seconds.

The eight tests cover:

1. A complete Chinese workspace flow: native Paste, role selection, saved preferences, consent, review cancellation, submission and reset.
2. Required-field errors after an empty submission, without opening confirmation.
3. Invalid email blocking review even when the other required fields are complete.
4. Native keyboard Next/Done preserving the entered name and email.
5. Sheet cancellation and swipe dismissal discarding unsaved preferences.
6. Role search recovering from no results to a valid selection.
7. Terms-dialog dismissal preserving the draft without accepting consent.
8. Theme switching preserving the draft, role and consent through review.

Tests use native keyboard input, Paste, taps and swipes; they do not inject form state. The CI artifact retains `.xcresult`, JSON reports, logs and step screenshots. Screenshots are inspection evidence, not pixel-golden assertions. Reviewed captures: [initial form](screenshots/ios-form.png), [selected notification preferences](screenshots/ios-preferences.png), and [Chinese success summary](screenshots/ios-success.png).

To repeat the suite on an Apple silicon Mac, select the shared `iosApp` scheme in `iosApp/iosApp.xcodeproj` and use Xcode's Test action, or run `bash scripts/ci-ios.sh`. These runtime results come from the hosted Mac runner, not this Windows workstation. Physical-device installation/signing, manual VoiceOver use, larger Dynamic Type and broader device/orientation coverage remain unverified.

## Prototype limits

- Submission is a cancellable 700ms local demonstration; no server or online account exists. Recreation during that delay returns to the editable confirmation state.
- Drafts use saved-instance state, not persistent storage.
- TalkBack/VoiceOver, predictive-back gestures, RTL, landscape, tablets and a broader Android API/device matrix were not manually verified.
- The Combobox is wired to the demonstration role model. A general public component API and library publishing are future work.

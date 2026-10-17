# Prototype verification

Verified on 2026-10-17. Android builds and all 18 executed tests passed locally and in GitHub Actions. iOS shared Kotlin compilation passed locally for both configured targets; the hosted macOS check subsequently linked both frameworks, built the Swift host and ran the app in an iPhone simulator.

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

The [hosted macOS check](https://github.com/hanzhang-13/shadcn-mobile/actions/runs/39045335671) subsequently linked both Apple frameworks, built the SwiftUI/UIKit host, installed and launched the app in an iPhone simulator, checked that its process remained alive after five seconds, and captured a startup screenshot. This check does **not** validate signing, full form interactions, iOS keyboard handling, safe areas or VoiceOver.

On an Apple silicon Mac, open `iosApp/iosApp.xcodeproj`, select the shared `iosApp` scheme and an iPhone simulator, then run. Verify the full form, keyboard Next/Done, searchable selection, Sheet dragging/dismissal, Dialog dismissal, dark appearance and larger Dynamic Type. The iOS runtime result above comes from the hosted Mac runner, not this Windows workstation.

## Prototype limits

- Submission is a cancellable 700ms local demonstration; no server or online account exists. Recreation during that delay returns to the editable confirmation state.
- Drafts use saved-instance state, not persistent storage.
- TalkBack/VoiceOver, predictive-back gestures, RTL, landscape, tablets and a broader Android API/device matrix were not manually verified.
- The Combobox is wired to the demonstration role model. A general public component API and library publishing are future work.

# Prototype verification

On **2026-10-18**, [Android run 39116229205](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39116229205) at `743c97c` passed 10 shared host tests and all 15 instrumentation tests, with no failures or skips. [iOS run 39116804249](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39116804249) at `5558058` passed all 13 native UI tests with no failures, skips, expected failures or retries. Local Android verification at `f072a20` also passed 10 shared and 15 instrumentation tests, and the four new component tests passed in dark mode at 150% font scale.

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

The script builds the application and test APKs, runs the shared form tests on the host, installs both APKs on the selected emulator, checks the instrumentation runner's final success result, and retrieves the screenshots. `AdbPort` defaults to 5037; the isolated server used for this workstation's regression is 5043. The full local run at `f072a20` returned `OK (15 tests)` in 41.61 seconds. The four new component tests were also rerun in dark mode at 150% font scale and passed in 8.576 seconds. After the screenshot-helper change at `743c97c`, the complete-form test passed locally in 9.82 seconds and the hosted full suite passed in 93.053 seconds. The local 15-test log remains evidence for `f072a20`.

| Check | Result | Evidence |
| --- | --- | --- |
| Android debug APK + test APK at `f072a20` | Passed | [122-task build output](build-run.txt) |
| Shared form logic | 10 passed, 0 failures, 0 skipped | `composeApp/build/reports/tests/testAndroidHostTest/index.html` |
| Android interaction tests at `f072a20` | 15 passed, 0 failures | [Runner output](instrumentation-run.txt) |
| Hosted Android at `743c97c` | 10 shared + 15 distinct instrumentation tests passed, 0 failures/skips; ten screenshots | [Run 39116229205](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39116229205) |
| Four new components, dark + 150% font | 4 passed, 0 failures | [Runner output](instrumentation-dark-large-run.txt) |
| iOS device shared Kotlin compilation at `f072a20` | Passed | [Build output](build-run.txt) |
| iOS simulator shared Kotlin compilation at `f072a20` | Passed | [Build output](build-run.txt) |

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
9. Switch label and track each toggle once; disabled touch and accessibility activation preserve state and callback count.
10. Textarea preserves Chinese text, newlines and spaces, clears validation errors after input, and protects read-only/disabled content.
11. RadioGroup keeps exactly one selection and ignores disabled options under touch and accessibility activation.
12. Alert exposes the intended live-region semantics without a click action and updates its message after a local retry. This checks semantics, not actual screen-reader speech.
13. Gallery navigation preserves the form draft, theme and component state.
14. Activity recreation restores the gallery route and text together with the hidden form draft.
15. Disabled Button/Checkbox and loading Button callbacks remain blocked even when their semantic click action is invoked directly.

The 10 shared tests cover required fields, name boundaries, whitespace normalization, email validation, invalid role identifiers, and Chinese/English role searching. Form restoration uses `StateRestorationTester`; the additional gallery test calls `ActivityScenario.recreate()`. Neither establishes durable storage or process-kill restoration.

### Captured UI

The screenshots below are captured from the running Android application. Files 01–06 and the added component captures use Compose PixelCopy capture, with overlays captured at their component bounds; the original system-level captures use full-device screenshots. They are visual inspection evidence, not pixel-golden assertions.

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
| [Switch](screenshots/android-switch.png) | Toggle positions, disabled appearance and callback count |
| [Textarea](screenshots/android-textarea.png) | Chinese multiline text and read-only/disabled fields |
| [RadioGroup](screenshots/android-radio.png) | Mutually exclusive choice and disabled option |
| [Alert](screenshots/android-alert.png) | Default and destructive messages |
| [Textarea, dark + 150% font](screenshots/android-textarea-dark-large.png) | Larger multiline content, readable labels and scrollable layout |
| [Alert, dark + 150% font](screenshots/android-alert-dark-large.png) | Wrapped text, message borders and reachable retry action |

The emulator was returned to 100% system font scale and light mode after the checks.

## iOS verification boundary

The [hosted iOS run 39116804249](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39116804249) tested commit `5558058` on 2026-10-18. It linked both Apple frameworks, built the SwiftUI/UIKit host and ran `iosAppUITests` serially on the iPhone simulator listed above. The exported [.xcresult summary](ios-test-summary.json) reports **13 passed, 0 failed, 0 skipped and 0 expected failures**. The [test tree](ios-test-results.json) contains 13 distinct passing methods; the log records each method starting and passing once, without retries or runner restarts. Test execution took 817.756 seconds. The hosted job, including build and evidence collection, took 29 minutes 10 seconds.

These tasks completed successfully and produced target KLIBs:

```shell
./gradlew :composeApp:compileKotlinIosArm64 :composeApp:compileKotlinIosSimulatorArm64
```

The 13 tests cover:

1. A complete Chinese workspace flow: native Paste, role selection, saved preferences, consent, review cancellation, submission and reset.
2. Required-field errors after an empty submission, without opening confirmation.
3. Invalid email blocking review even when the other required fields are complete.
4. Native keyboard Next/Done preserving the entered name and email.
5. Sheet cancellation and swipe dismissal discarding unsaved preferences.
6. Role search recovering from no results to a valid selection.
7. Terms-dialog dismissal preserving the draft without accepting consent.
8. Theme switching preserving the draft, role and consent through review.
9. Switch label and track toggling once per tap while a disabled switch stays unchanged.
10. Textarea accepting Chinese multiline input and preserving read-only/disabled content.
11. RadioGroup keeping one selected option and ignoring disabled taps.
12. Alert replacing an error message with local recovery feedback.
13. Component-gallery navigation preserving the form draft, saved notification preferences and theme.

Tests use native keyboard input, Paste, taps and swipes; they do not inject form state. The CI artifact retains `.xcresult`, JSON reports, logs and step screenshots. Screenshots are inspection evidence, not pixel-golden assertions. Earlier reviewed form captures remain available: [initial form](screenshots/ios-form.png), [selected notification preferences](screenshots/ios-preferences.png), and [Chinese success summary](screenshots/ios-success.png).

Reviewed captures from the final 13-test run:

| Capture | What was inspected |
| --- | --- |
| [Switch](screenshots/ios-switch.png) | Enabled and disabled positions, two-change counter |
| [Textarea and keyboard](screenshots/ios-textarea.png) | Three lines of Chinese text with the native keyboard |
| [Textarea, read-only and disabled](screenshots/ios-textarea-readonly.png) | Preserved values and no keyboard after the noneditable interactions |
| [RadioGroup](screenshots/ios-radio.png) | Single selection, disabled option and change count |
| [Alert](screenshots/ios-alert.png) | Default and destructive messages |
| [Alert after retry](screenshots/ios-alert-recovered.png) | Recovery message and disabled completion action |

To repeat the suite on an Apple silicon Mac, select the shared `iosApp` scheme in `iosApp/iosApp.xcodeproj` and use Xcode's Test action, or run `bash scripts/ci-ios.sh`. These runtime results come from the hosted Mac runner, not this Windows workstation. Physical-device installation/signing, manual VoiceOver use, larger Dynamic Type and broader device/orientation coverage remain unverified.

### Earlier iOS runs

The original form suite passed all eight tests in [run 39112862050](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39112862050) at `ac4ca80`, with no failures, skips or retries, in 486.230 seconds.

A later run of that eight-test suite, [39114842776](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39114842776), passed seven tests and failed the complete Chinese flow while waiting for the keyboard to disappear after Done. The eight-second wait expired, but the subsequent screenshot and accessibility tree showed the keyboard had disappeared. The targeted fix increases only that disappearance wait to 15 seconds; the `exists == false` assertion, single Done action, general eight-second waits and no-retry policy remain unchanged. The final 13-test run above verified the fix, including the complete Chinese flow.

## Prototype limits

- Submission is a cancellable 700ms local demonstration; no server or online account exists. Recreation during that delay returns to the editable confirmation state.
- Drafts use saved-instance state, not persistent storage.
- TalkBack/VoiceOver, predictive-back gestures, RTL, landscape, tablets and a broader Android API/device matrix were not manually verified.
- The Combobox is wired to the demonstration role model. A general public component API and library publishing are future work.

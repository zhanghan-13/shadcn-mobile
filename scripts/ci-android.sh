#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

export ANDROID_SERIAL="${ANDROID_SERIAL:-$(adb get-serialno)}"
if [[ ! "$ANDROID_SERIAL" =~ ^emulator-[0-9]+$ ]]; then
  printf 'Expected one emulator, got: %s\n' "$ANDROID_SERIAL" >&2
  exit 1
fi

output="build/ci/android"
mkdir -p "$output/screenshots"
adb wait-for-device
adb shell cmd uimode night no
adb shell settings put system font_scale 1.0
adb shell settings put secure show_ime_with_hard_keyboard 1
adb shell wm size 1080x2400
adb shell wm density 420
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
adb install -r androidApp/build/outputs/apk/androidTest/debug/androidApp-debug-androidTest.apk
adb logcat -c

# Running instrumentation directly keeps app-owned screenshot files available
# until they have been collected; the runner summary is explicitly validated.
set +e
adb shell am instrument -w -r com.shadcn.mobile.test/androidx.test.runner.AndroidJUnitRunner | tee "$output/instrumentation.txt"
runner_status=${PIPESTATUS[0]}
set -e
adb logcat -d > "$output/logcat.txt"
if adb shell test -d /sdcard/Android/data/com.shadcn.mobile/files/screenshots; then
  adb pull /sdcard/Android/data/com.shadcn.mobile/files/screenshots/. "$output/screenshots/"
fi
if [[ "$runner_status" -ne 0 ]]; then
  exit "$runner_status"
fi
python3 scripts/ci-check-tests.py instrumentation "$output/instrumentation.txt" 15
for capture in 01-form 02-combobox 03-sheet 04-review 05-success 06-dark 07-switch 08-textarea 09-radio 10-alert; do
  test -s "$output/screenshots/$capture.png"
done

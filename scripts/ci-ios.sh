#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

if [[ "$(uname -s)" != Darwin || "$(uname -m)" != arm64 ]]; then
  printf 'iOS CI requires macOS on Apple silicon.\n' >&2
  exit 1
fi
output="$PWD/build/ci/ios"
mkdir -p "$output"
xcodebuild -version | tee "$output/toolchain.log"
xcrun swift --version | tee -a "$output/toolchain.log"

# Check both Apple framework targets; the Xcode build phase then embeds the
# simulator framework using the same integration as local Xcode development.
bash ./gradlew :composeApp:linkDebugFrameworkIosArm64 :composeApp:linkDebugFrameworkIosSimulatorArm64 \
  --stacktrace --console=plain --max-workers=2 | tee "$output/framework-build.log"
xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -configuration Debug \
  -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' \
  -derivedDataPath "$output/DerivedData" \
  -resultBundlePath "$output/build.xcresult" \
  ARCHS=arm64 ONLY_ACTIVE_ARCH=YES CODE_SIGNING_ALLOWED=NO \
  build | tee "$output/host-build.log"
ditto -c -k --keepParent \
  "$output/DerivedData/Build/Products/Debug-iphonesimulator/shadcn-mobile.app" \
  "$output/shadcn-mobile-simulator.zip"

# Choose an installed iPhone rather than hard-coding a device name that changes
# as GitHub refreshes its image. Prefer the most recent available iOS runtime.
xcrun simctl list devices available --json > "$output/simulators.json"
simulator_id=$(python3 - "$output/simulators.json" <<'PY'
import json
import sys

with open(sys.argv[1], encoding="utf-8") as source:
    runtimes = json.load(source)["devices"]
for runtime in sorted(runtimes, reverse=True):
    if ".iOS-" not in runtime:
        continue
    for device in runtimes[runtime]:
        if device.get("isAvailable") and device["name"].startswith("iPhone"):
            print(device["udid"])
            raise SystemExit(0)
raise SystemExit("No available iPhone simulator runtime")
PY
)
xcrun simctl boot "$simulator_id"
xcrun simctl bootstatus "$simulator_id" -b
xcrun simctl ui "$simulator_id" appearance light
xcrun simctl install "$simulator_id" "$output/DerivedData/Build/Products/Debug-iphonesimulator/shadcn-mobile.app"
launch_result=$(xcrun simctl launch "$simulator_id" com.shadcn.mobile)
printf '%s\n' "$launch_result" | tee "$output/launch.log"
app_pid=$(printf '%s\n' "$launch_result" | awk '{print $NF}')
sleep 5
xcrun simctl spawn "$simulator_id" launchctl list > "$output/processes.log"
awk -v expected="$app_pid" '$1 == expected { found = 1 } END { exit !found }' "$output/processes.log"
xcrun simctl io "$simulator_id" screenshot "$output/ios-form.png"
xcrun simctl shutdown "$simulator_id"

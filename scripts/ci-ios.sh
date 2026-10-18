#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

if [[ "$(uname -s)" != Darwin || "$(uname -m)" != arm64 ]]; then
  printf 'iOS CI requires macOS on Apple silicon.\n' >&2
  exit 1
fi
mkdir -p "$PWD/build/ci/ios"
# A new directory prevents a previous local run from supplying stale evidence.
output=$(mktemp -d "$PWD/build/ci/ios/run-XXXXXXXX")
result_bundle="$output/ui-tests.xcresult"
derived_data="$PWD/build/ci/ios-derived-data"
simulator_id=''
simulator_started=0
cleanup() {
  status=$?
  trap - EXIT
  if (( simulator_started == 1 )); then
    xcrun simctl io "$simulator_id" screenshot "$output/ios-final-screen.png" \
      >> "$output/simulator-cleanup.log" 2>&1 || true
    xcrun simctl shutdown "$simulator_id" >> "$output/simulator-cleanup.log" 2>&1 || true
  fi
  exit "$status"
}
trap cleanup EXIT
xcodebuild -version | tee "$output/toolchain.log"
xcrun swift --version | tee -a "$output/toolchain.log"
xcrun xcresulttool version | tee -a "$output/toolchain.log"
# Apple documents these help commands as the source of the JSON output schema:
# https://developer.apple.com/documentation/xcode-release-notes/xcode-16_3-release-notes
xcrun xcresulttool help get test-results tests > "$output/xcresulttool-tests-help.log"
xcrun xcresulttool help get test-results summary > "$output/xcresulttool-summary-help.log"
python3 scripts/test-ci-ios-results.py 2>&1 | tee "$output/report-checker-tests.log"

# Check both Apple framework targets; the Xcode build phase then embeds the
# simulator framework using the same integration as local Xcode development.
bash ./gradlew :composeApp:linkDebugFrameworkIosArm64 :composeApp:linkDebugFrameworkIosSimulatorArm64 \
  --stacktrace --console=plain --max-workers=2 | tee "$output/framework-build.log"

# Choose a shutdown iPhone, leaving a local developer's already-running device
# alone. Only a simulator successfully booted below is eligible for cleanup.
# Prefer the most recent available iOS runtime without hard-coding its name.
xcrun simctl list devices available --json > "$output/simulators.json"
simulator_id=$(python3 - "$output/simulators.json" <<'PY'
import json
import re
import sys

with open(sys.argv[1], encoding="utf-8") as source:
    runtimes = json.load(source)["devices"]
def version(runtime):
    return tuple(int(part) for part in re.findall(r"\d+", runtime))

for runtime in sorted(runtimes, key=version, reverse=True):
    if ".iOS-" not in runtime:
        continue
    for device in runtimes[runtime]:
        if (device.get("isAvailable") and device.get("state") == "Shutdown"
                and device["name"].startswith("iPhone")):
            print(device["udid"])
            raise SystemExit(0)
raise SystemExit("No available shutdown iPhone simulator; existing booted devices were left untouched")
PY
)
xcrun simctl boot "$simulator_id"
simulator_started=1
xcrun simctl bootstatus "$simulator_id" -b
xcrun simctl ui "$simulator_id" appearance light

# One selected device and one UI target. Xcode runs tests once by default;
# do not add repetition or -retry-tests-on-failure settings.
# Timeout flags are documented in Apple's Xcode 11.4 Testing release notes:
# https://developer.apple.com/documentation/xcode-release-notes/xcode-11_4-release-notes
set +e
xcodebuild test \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -configuration Debug \
  -sdk iphonesimulator \
  -destination "platform=iOS Simulator,id=$simulator_id" \
  -destination-timeout 120 \
  -derivedDataPath "$derived_data" \
  -resultBundlePath "$result_bundle" \
  -only-testing:iosAppUITests \
  -parallel-testing-enabled NO \
  -maximum-concurrent-test-simulator-destinations 1 \
  -test-timeouts-enabled YES \
  -default-test-execution-time-allowance 180 \
  -maximum-test-execution-time-allowance 300 \
  ARCHS=arm64 ONLY_ACTIVE_ARCH=YES CODE_SIGNING_ALLOWED=NO \
  2>&1 | tee "$output/ui-tests.log"
pipeline_status=("${PIPESTATUS[@]}")
set -e

# Export evidence even when xcodebuild fails. The original failure is retained;
# missing/unreadable reports and attachment export failures also fail the job.
evidence_status=0
if [[ -d "$result_bundle" ]]; then
  xcrun xcresulttool get test-results summary --path "$result_bundle" \
    > "$output/test-summary.json" 2> "$output/test-summary-errors.log" || evidence_status=1
  xcrun xcresulttool get test-results tests --path "$result_bundle" \
    > "$output/test-results.json" 2> "$output/test-results-errors.log" || evidence_status=1
  xcrun xcresulttool export attachments --path "$result_bundle" \
    --output-path "$output/attachments" > "$output/attachment-export.log" 2>&1 || evidence_status=1
  python3 scripts/ci-check-ios-results.py "$output/test-results.json" iosAppUITests 13 \
    2>&1 | tee "$output/test-check.log" || evidence_status=1
else
  printf 'Missing XCUITest result bundle: %s\n' "$result_bundle" >&2
  evidence_status=1
fi

app="$derived_data/Build/Products/Debug-iphonesimulator/shadcn-mobile.app"
if [[ -d "$app" ]]; then
  ditto -c -k --keepParent "$app" "$output/shadcn-mobile-simulator.zip" || evidence_status=1
fi
if (( pipeline_status[0] != 0 || pipeline_status[1] != 0 || evidence_status != 0 )); then
  printf 'iOS CI failed: xcodebuild=%s, log=%s, evidence=%s\n' \
    "${pipeline_status[0]}" "${pipeline_status[1]}" "$evidence_status" >&2
  exit 1
fi

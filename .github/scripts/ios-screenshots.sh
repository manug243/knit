#!/usr/bin/env bash
# Verifies that the example app was built with the iOS 26 SDK, runs it on a simulator and saves a screenshot.
# Usage: ios-screenshots.sh <path to .app> <output directory>
set -euo pipefail

app="$1"
out="$2"
mkdir -p "$out"

plist() { /usr/libexec/PlistBuddy -c "Print :$1" "$app/Info.plist"; }

sdk="$(plist DTSDKName)"
echo "Built with SDK: $sdk"
case "$sdk" in
  iphonesimulator2[6-9]* | iphonesimulator[3-9][0-9]*) ;;
  *)
    echo "Liquid Glass needs the iOS 26 SDK or newer, got $sdk"
    exit 1
    ;;
esac

if plist UIDesignRequiresCompatibility >/dev/null 2>&1; then
  echo "UIDesignRequiresCompatibility is set, which opts the app out of Liquid Glass"
  exit 1
fi

# Newest available iOS runtime, first iPhone on it.
device="$(xcrun simctl list devices available -j | python3 -c '
import json, re, sys

devices = json.load(sys.stdin)["devices"]
def version(runtime):
    return [int(part) for part in re.findall(r"\d+", runtime.rsplit(".", 1)[-1])]
for runtime in sorted((r for r in devices if ".iOS-" in r), key=version, reverse=True):
    phones = [d for d in devices[runtime] if d["name"].startswith("iPhone")]
    if phones:
        print(phones[0]["udid"])
        break
')"
if [ -z "$device" ]; then
  echo "No available iPhone simulator found"
  exit 1
fi

xcrun simctl boot "$device"
xcrun simctl bootstatus "$device" -b
xcrun simctl install "$device" "$app"
xcrun simctl launch "$device" "$(plist CFBundleIdentifier)"

# Give the first Compose frame time to render on a cold simulator.
sleep 20
xcrun simctl io "$device" screenshot "$out/launch.png"

# Second start straight on a detail screen, which shows the native navigation bar with the back button.
xcrun simctl terminate "$device" "$(plist CFBundleIdentifier)"
xcrun simctl launch "$device" "$(plist CFBundleIdentifier)" -knit-detail
sleep 20
xcrun simctl io "$device" screenshot "$out/detail.png"
xcrun simctl shutdown "$device"

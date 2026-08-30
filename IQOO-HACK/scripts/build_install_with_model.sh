#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PACKAGE_NAME="com.geniex.assistant"
APK_PATH="$ROOT_DIR/app/build/outputs/apk/debug/app-debug.apk"
LOCAL_MODEL_PATH="${1:-$ROOT_DIR/models/Qwen3.5-4B-Q4_0.gguf}"
SHARED_MODEL_DIR="/sdcard/IQOO-HACK"
SHARED_MODEL_PATH="$SHARED_MODEL_DIR/qwen.gguf"
TMP_MODEL_PATH="/data/local/tmp/qwen.gguf"
APP_MODEL_DIR="files/models/qwen"
APP_MODEL_PATH="$APP_MODEL_DIR/qwen.gguf"
UNINSTALL_FIRST="${UNINSTALL_FIRST:-1}"

cd "$ROOT_DIR"

echo "Checking ADB device..."
adb start-server >/dev/null
if ! adb get-state >/dev/null 2>&1; then
  echo "No Android device found. Connect the phone, enable USB debugging, then retry."
  adb devices -l
  exit 1
fi

echo "Building debug APK..."
./gradlew assembleDebug

echo "Ensuring persistent shared GGUF model exists..."
if adb shell "test -f $SHARED_MODEL_PATH" >/dev/null 2>&1; then
  echo "Shared model already exists: $SHARED_MODEL_PATH"
elif [[ -f "$LOCAL_MODEL_PATH" ]]; then
  echo "Copying local model once to shared storage: $LOCAL_MODEL_PATH"
  adb shell "mkdir -p $SHARED_MODEL_DIR"
  adb push "$LOCAL_MODEL_PATH" "$SHARED_MODEL_PATH"
elif adb shell 'test -f /sdcard/Download/Qwen3.5-4B-Q4_0.gguf' >/dev/null 2>&1; then
  echo "Copying phone download model once to shared storage."
  adb shell "mkdir -p $SHARED_MODEL_DIR && cp /sdcard/Download/Qwen3.5-4B-Q4_0.gguf $SHARED_MODEL_PATH"
else
  echo "Could not find a GGUF model."
  echo "Expected one of:"
  echo "  $SHARED_MODEL_PATH"
  echo "  $LOCAL_MODEL_PATH"
  echo "  /sdcard/Download/Qwen3.5-4B-Q4_0.gguf"
  exit 1
fi

if [[ "$UNINSTALL_FIRST" == "1" ]]; then
  echo "Uninstalling old app if present..."
  adb uninstall "$PACKAGE_NAME" >/dev/null 2>&1 || true
else
  echo "Skipping uninstall; app-private model/data can be reused."
fi

echo "Installing APK..."
adb install -r "$APK_PATH"

echo "Granting runtime permissions..."
adb shell "pm grant $PACKAGE_NAME android.permission.RECORD_AUDIO" >/dev/null 2>&1 || true
adb shell "pm grant $PACKAGE_NAME android.permission.POST_NOTIFICATIONS" >/dev/null 2>&1 || true
adb shell "pm grant $PACKAGE_NAME android.permission.READ_EXTERNAL_STORAGE" >/dev/null 2>&1 || true

echo "Granting all-files access for shared model reading..."
adb shell "appops set $PACKAGE_NAME MANAGE_EXTERNAL_STORAGE allow" >/dev/null 2>&1 || true
adb shell "cmd appops set $PACKAGE_NAME MANAGE_EXTERNAL_STORAGE allow" >/dev/null 2>&1 || true

echo "Verifying persistent shared model..."
adb shell "ls -lh $SHARED_MODEL_PATH"

echo "Ensuring app-private GGUF model exists..."
if adb shell "run-as $PACKAGE_NAME test -f $APP_MODEL_PATH" >/dev/null 2>&1; then
  echo "App-private model already exists: $APP_MODEL_PATH"
else
  adb shell "cp $SHARED_MODEL_PATH $TMP_MODEL_PATH"
  adb shell "chmod 644 $TMP_MODEL_PATH"
  adb shell "run-as $PACKAGE_NAME mkdir -p $APP_MODEL_DIR"
  adb shell "run-as $PACKAGE_NAME cp $TMP_MODEL_PATH $APP_MODEL_PATH"
  adb shell "rm -f $TMP_MODEL_PATH"
fi

echo "Verifying app-private model..."
adb shell "run-as $PACKAGE_NAME ls -lh $APP_MODEL_PATH"

echo "Launching app..."
adb shell am force-stop "$PACKAGE_NAME"
adb shell am start -n "$PACKAGE_NAME/.MainActivity"

echo "Done."

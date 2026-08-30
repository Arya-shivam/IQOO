# Quick Run Guide

## One Command

Build, uninstall the old app, install fresh, ensure the model exists in persistent phone storage, copy it into app-private storage if needed, verify it, and launch:

```bash
cd /home/nandish/Desktop/IQOO/IQOO-HACK
./scripts/build_install_with_model.sh
```

After the first clean install, use this faster mode to keep the app-private model in place:

```bash
UNINSTALL_FIRST=0 ./scripts/build_install_with_model.sh
```

You can also pass a custom local GGUF path:

```bash
./scripts/build_install_with_model.sh /path/to/model.gguf
```

## Build and install

```bash
cd /home/nandish/Desktop/IQOO/IQOO-HACK
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Install the local model

The persistent phone-storage copy is kept here so we do not lose the model source:

```text
/sdcard/IQOO-HACK/qwen.gguf
```

The app itself reads the app-private copy because this device does not allow all-files access from ADB reliably:

```text
/data/user/0/com.geniex.assistant/files/models/qwen/qwen.gguf
```

The script restores the app-private copy from `/sdcard/IQOO-HACK/qwen.gguf` only when needed. If the shared copy is missing, copy it once:

```bash
adb shell 'mkdir -p /sdcard/IQOO-HACK'
adb push models/Qwen3.5-4B-Q4_0.gguf /sdcard/IQOO-HACK/qwen.gguf
adb shell 'ls -lh /sdcard/IQOO-HACK/qwen.gguf'
```

## Launch

```bash
adb shell am force-stop com.geniex.assistant
adb shell am start -n com.geniex.assistant/.MainActivity
```

Use `am start`, not `monkey`; `monkey` can inject a random tap into the capture screen.

## Try the end-to-end flow

1. Tap **Voice input**.
2. Say: “Raj will send the API credentials by Tuesday. Once they arrive, finish integration before the client demo on Friday.”
3. Tap **Finish voice input**.
4. Confirm that **Priority timetable**, **My read**, and **What I heard** update.
5. Force-stop and relaunch the app; the timetable and assistant reply should return from local state.

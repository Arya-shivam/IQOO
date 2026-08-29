# Open Granola Android

An Android-first, local-first meeting notetaker. This is a separate project from
the existing `geniex_chat_android` GenieX demo in this repository.

## Current MVP

- Native Kotlin/Jetpack Compose meeting workspace
- Meeting list and editor
- Microphone permission and foreground recording service with local `.m4a` capture
- Room database schema and DAO for durable meeting records
- Local GenieX LLM provider with chat-template formatting and streamed summaries
- Transcript and note fields ready for local STT/LLM pipelines
- No cloud account or network API in the application flow

`GenieXLocalLlmProvider` initializes the SDK once, loads the privately imported
model with the same `LlmWrapper` flow used by the companion GenieX app, applies
the model chat template, and streams structured meeting notes locally. Inference
is serialized because the native wrapper is not re-entrant.

## Build

Requirements:

- JDK 17
- Gradle 8.13 or newer compatible with Android Gradle Plugin 8.13
- Android SDK Platform 34
- Android Build Tools 35.0.0
- A device with Android 12/API 31 or newer

From this directory, generate a wrapper if one is not already present:

```bash
gradle wrapper --gradle-version 8.13
./gradlew assembleDebug
```

On Windows:

```powershell
gradle wrapper --gradle-version 8.13
.\gradlew.bat assembleDebug
adb install -r -t app\build\outputs\apk\debug\app-debug.apk
adb shell monkey -p com.opengranola.android 1
```

GenieX local inference requires a compatible Qualcomm Snapdragon device and a
model installed through the GenieX model workflow. Other devices need a
different local provider implementation.

## Roadmap

1. Add on-device Whisper transcription and connect transcript persistence to the Room DAO.
2. Add GenieX model-manager downloads and structured JSON note generation.
3. Connect the Compose screens to Room-backed `MeetingViewModel` state.
4. Add Markdown export, search, retention controls, and encrypted storage.
5. Add device capability detection and non-Qualcomm local-runtime fallback.
6. Add explicit sharing, encrypted sync, and release-quality privacy controls.

Recording laws differ by location. The production app must show recording status
and consent guidance before capturing a meeting.

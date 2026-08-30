# GenieX Agent Context

## Product Direction

Build GenieX as a very simple personal executive assistant, not a database-style productivity app.

The main user flow should feel like:

1. Press `Start meeting capture` or `Voice input`.
2. Speak naturally about meetings, standups, commitments, deadlines, and tasks.
3. GenieX stores the information internally.
4. GenieX extracts valid tasks and commitments.
5. GenieX evaluates pending work by importance, priority, deadline urgency, blockers, and time-of-day fit.
6. GenieX shows only two outputs:
   - A priority timetable: first do this, then this, then this.
   - A PA-style summary: warm, direct, human, and reasoned.

The app should not feel like it is showing raw DB data. Avoid exposing IDs, epoch dates, status dumps, table-like screens, or technical runtime details in the main UX.

## Current Implementation State

The current app has been simplified to one main screen:

- `app/src/main/java/com/geniex/assistant/ui/screens/AssistantHomeScreen.kt`
- `app/src/main/java/com/geniex/assistant/ui/GenieXApp.kt`
- `app/src/main/java/com/geniex/assistant/MainActivity.kt`

Old tab screens were deleted from the main codebase:

- Dashboard
- Goals
- Tasks
- Meetings
- Settings

The visible UI now focuses on:

- `Start meeting capture`
- `Stop and analyze`
- `Voice input`
- `Clear all data` temporary developer button
- `Priority timetable`
- `My read`
- `What I heard`

## Recording And Voice Notes

Android speech recognition is used as the hidden note-capture layer for now.

- GenieX requests offline recognition, but Android ultimately chooses the installed recognition service. A downloaded offline language pack is required for a reliably offline demo.
- Partial speech is retained when capture stops or the recognizer fails.

Important caveat:

- Do not run `MediaRecorder` and Android `SpeechRecognizer` at the same time. Google Speech Recognition reports that it cannot record while GenieX is using the microphone.
- Current meeting capture uses speech-note capture only, so the app can analyze what was said.
- `MeetingRecorder.kt` still exists, but the current MVP does not start it during meeting capture because of the microphone conflict.

Relevant files:

- `app/src/main/java/com/geniex/assistant/audio/VoiceNoteCapture.kt`
- `app/src/main/java/com/geniex/assistant/audio/MeetingRecorder.kt`

## Analysis Loop

Captured notes flow through:

- `AssistantViewModel.analyzeCapturedMeeting`
- `AssistantCoordinator.processCapturedMeeting`
- `LocalModelBridge.extractMeeting`
- `ScheduleEngine.prioritize`
- `ScheduleEngine.buildTimetable`
- `LocalModelBridge.generateRecommendation`

Relevant files:

- `app/src/main/java/com/geniex/assistant/ui/AssistantViewModel.kt`
- `app/src/main/java/com/geniex/assistant/domain/AssistantCoordinator.kt`
- `app/src/main/java/com/geniex/assistant/domain/ScheduleEngine.kt`
- `app/src/main/java/com/geniex/assistant/llm/GenieXQwenLocalBridge.kt`

## Model Setup

The model should be inside app-private storage:

```text
/data/user/0/com.geniex.assistant/files/models/qwen/qwen.gguf
```

Verified working GGUF source on the phone:

```text
/sdcard/Download/Qwen3.5-4B-Q4_0.gguf
/sdcard/IQOO-HACK/qwen.gguf
```

If the app-private copy is missing, restore it:

```bash
adb shell 'cp /sdcard/Download/Qwen3.5-4B-Q4_0.gguf /data/local/tmp/qwen.gguf'
adb shell 'chmod 644 /data/local/tmp/qwen.gguf'
adb shell 'run-as com.geniex.assistant mkdir -p files/models/qwen'
adb shell 'run-as com.geniex.assistant cp /data/local/tmp/qwen.gguf files/models/qwen/qwen.gguf'
adb shell 'rm -f /data/local/tmp/qwen.gguf'
adb shell 'run-as com.geniex.assistant ls -lh files/models/qwen/qwen.gguf'
```

## Database

Room DB:

```text
databases/geniex_assistant.db
```

Current user-data tables:

- `goals`
- `tasks`
- `meetings`
- `memories`
- `assistant_settings`

`Clear all data` clears Room tables but should not delete the GGUF model file.

The app asks for confirmation before clearing. The latest transcript, timetable, and PA reply are restored from Room after relaunch.

Sample data auto-seeding has been disabled. After clearing, user data should remain empty until recording or voice input creates new data.

## Build And Run

```bash
cd /home/nandish/Desktop/IQOO/IQOO-HACK
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am force-stop com.geniex.assistant
adb shell am start -n com.geniex.assistant/.MainActivity
```

Use `am start`, not `monkey`, for launching during tests. `monkey` may inject a random UI event and accidentally press a button.

## Current UX Requirements

The assistant should sound like a CEO's personal assistant:

- Direct
- Warm
- Human
- Reasoned
- Emotionally aware, but not pretending to be conscious

Example tone:

```text
I'd do the meeting follow-up first. It matters because the client demo depends on Raj sending the credentials, and waiting another day puts the integration at risk. After that, use your morning deep-work block for the local model pipeline while your mind is fresh.
```

Avoid:

```text
Priority: task
Why now: reason
Next action: step
Role: assistant
Formal: Exactly three lines
```

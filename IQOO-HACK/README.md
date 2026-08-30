# GenieX Assistant

GenieX is a private, on-device personal-assistant MVP for Android. Its single-screen flow captures a meeting or voice update, extracts actions and commitments, persists them in Room, prioritizes open work, and returns a timetable plus a short PA-style recommendation.

## Current flow

1. Tap **Start meeting capture** or **Voice input**.
2. Speak naturally about owners, deadlines, dependencies, and commitments.
3. Stop capture. GenieX extracts structured work with the local Qwen GGUF model when available, with a deterministic fallback if inference fails.
4. The app stores the transcript, actions, commitments, and assistant reply locally.
5. The home screen shows **Priority timetable**, **My read**, and **What I heard**.

The latest timetable and reply are restored after relaunch. Repeated analysis does not create duplicate open tasks or identical memories.

## Privacy and model location

Room data, captured speech text, and inference stay in the app sandbox. Android speech recognition is requested in offline mode, but actual offline availability depends on the recognition service and installed language pack.

The GGUF model must be at:

```text
/data/user/0/com.geniex.assistant/files/models/qwen/qwen.gguf
```

The app intentionally does not request broad shared-storage access. See [RUN_APP.md](RUN_APP.md) for model copy and launch commands.

## Architecture

- `audio` — Android speech-note capture
- `data/db` — Room world model
- `data/repo` — persistence boundary
- `domain` — extraction coordination, importance, scheduling, briefings, nudges
- `llm` — local Qwen bridge and rule-based fallback
- `ui` — one-screen Compose experience and lifecycle state

## Verification

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest lintDebug
```

The implemented scope is the vertical slice in `plans/AGENT_CONTEXT.md`. The root product brief contains long-term directions—not claims about current functionality.

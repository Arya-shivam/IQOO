# GenieX Assistant (Core MVP)

Android Kotlin app implementing the core personal-executive-assistant slice from the markdown spec:

- Goal creation with deadline and intent
- Auto task planning from goal
- SQLite/Room persistent world model
- Meeting transcript ingestion and structured extraction
- Memory timeline (commitments/episodic/long-term)
- Dynamic morning briefing and proactive nudges
- Runtime settings with placeholder hooks for **GenieX + Qwen** local models

## What is implemented now

### Core features

1. **Goals + planning**
   - Create goal with deadline.
   - Planning engine generates milestone tasks.

2. **Task execution loop**
   - View tasks and mark complete.
   - Morning briefing recalculates from current state.
   - Proactive nudges detect overdue/blocked work.

3. **Meeting intelligence (MVP)**
   - Meeting screen accepts transcript text.
   - Extracts commitments and likely tasks.
   - Writes extracted info to tasks + memory.

4. **Persistent memory/state**
   - Room tables: goals, tasks, meetings, memories, settings.

5. **Local model integration hooks**
   - `LocalModelBridge` abstraction ready for runtime binding.
   - `GenieXQwenLocalBridge` currently contains fallback logic + path-aware behavior.

## Where to plug GenieX/Qwen later

- Configure model path in app `Settings` tab.
- Default internal storage path is `/data/user/0/com.geniex.assistant/files/models`.
- Drop or mount your model files under that path (or any custom path you set).
- Replace fallback methods in:
  - `app/src/main/java/com/geniex/assistant/llm/GenieXQwenLocalBridge.kt`

## Project structure

- `app/src/main/java/com/geniex/assistant/data/db` → Room schema/DAO/database
- `app/src/main/java/com/geniex/assistant/data/repo` → repository
- `app/src/main/java/com/geniex/assistant/domain` → planning/briefing/proactive engines
- `app/src/main/java/com/geniex/assistant/llm` → local model bridge + config
- `app/src/main/java/com/geniex/assistant/ui` → viewmodel + compose app shell
- `app/src/main/java/com/geniex/assistant/ui/screens` → dashboard/goals/tasks/meeting/settings

## Build notes

- Open in Android Studio (AGP 8.5+, Kotlin 1.9.24, JDK 17).
- Sync Gradle and run on device/emulator.

## Next recommended step

- Replace transcript-only meeting input with microphone recording + local STT.
- Then replace fallback extraction with actual GenieX/Qwen inference.

# Personal Android LLM Assistant Architecture Plan

## Goal

Build a local-first personal assistant where the Android phone hosts the GenieX/NPU LLM and native phone context, while the PC CLI handles developer actions such as Git, docs, and project scanning.

```text
Android phone
  - GenieX LLM on NPU
  - OpenAI-compatible API server
  - Native Android context collectors
  - Local memory and planning state
  - Dashboard for context, memory, plans, and approvals

PC CLI
  - Git status / pull / commit / push
  - Project scanning
  - Docs generation
  - Gmail / GitHub / Discord integrations
  - Approval-gated actions
```

## Three Structural Challenges

### 1. Context Ingestion

Bring useful signals into the assistant from phone, PC, and cloud APIs.

#### Android sources

- Device state: battery, charging, Android version, model.
- Notifications through `NotificationListenerService`.
- Calendar through Android Calendar provider or Google Calendar API.
- Files shared into the app through Android intents.
- Wake/start signals through first unlock, app open, charging changes, and notification events.
- TTS is a top-up UX layer, not base infrastructure.

#### PC sources

- Git repositories.
- Current branch and uncommitted diffs.
- Project README/docs/TODO files.
- Terminal command outputs.
- Local notes and selected files.

#### Cloud/API sources

- Gmail through Gmail OAuth API.
- GitHub through GitHub API token.
- Discord through bot/OAuth where permitted.
- Google Calendar through OAuth.
- Notion/Jira/Linear later through official APIs.

#### Normalized event shape

```json
{
  "source": "gmail",
  "type": "email",
  "title": "Build failed",
  "body": "...",
  "timestamp": "2026-08-29T09:00:00Z",
  "importance": 0.82,
  "project": "IQOO",
  "action_required": true
}
```

### 2. Context Management / Memory

Turn raw context into reliable short-term and long-term memory.

#### Memory layers

```text
Raw events
  -> recent working context
  -> daily summaries
  -> long-term facts
  -> project state
  -> tasks and plans
```

#### Core storage

Use SQLite first. Keep it local and auditable.

Suggested tables:

```text
facts(id, text, source, created_at, importance, tags)
events(id, type, title, body, source, timestamp, metadata_json)
projects(id, name, path, repo_url, summary, current_branch, last_seen_at)
tasks(id, title, status, project, due_at, source)
```

#### Required memory controls

- List memory.
- Add memory.
- Delete/forget memory.
- Summarize today.
- Export memory.
- Redact secrets.

Example CLI:

```powershell
python geniex_cli.py memory add "I am building IQOO Android assistant"
python geniex_cli.py memory list
python geniex_cli.py memory forget FACT_ID
python geniex_cli.py memory summarize today
```

### 3. Action Execution / Control

Let the assistant act safely after proposing actions and receiving approval.

#### Supported actions

- Git: status, pull, diff summary, commit, push.
- Docs: create plans, changelogs, README sections.
- Gmail: summarize, draft replies, label/archive after approval.
- Calendar: create reminders and focus blocks after approval.
- Android: show notifications, open app intents, run foreground service controls.
- Top-up UX: speak briefings through TTS after the core context/memory/plan loop is stable.

#### Required action loop

```text
Observe -> Propose -> Confirm -> Execute -> Verify -> Log
```

#### Safety rule

The LLM must never silently:

- Push code.
- Delete files.
- Send emails.
- Send Discord/WhatsApp messages.
- Read sensitive sources without explicit permission.

Every destructive or external action requires confirmation.

## Cross-Cutting Security Model

Security is not a separate feature; it applies to every layer.

### Context ingestion security

- Explicit permissions.
- Allowlist apps/accounts.
- Redact OTPs, passwords, tokens, and API keys.
- Store minimal raw content.

### Memory security

- Local-first SQLite.
- Optional encryption later.
- Inspect/delete/export controls.
- No hidden memory.

### Action security

- Dry-run by default.
- Confirmation gates.
- Command logs.
- No background auto-send.
- No public exposure of port `8080` without auth.

## Current State

Implemented:

- Android GenieX app builds and runs on device.
- OpenAI-compatible `/v1/models` endpoint.
- OpenAI-compatible `/v1/chat/completions` endpoint.
- `/health` endpoint.
- `/tools/device` endpoint exposing native Android device state.
- USB forwarding through `adb forward tcp:8080 tcp:8080`.
- PC CLI wrapper: `geniex_cli.py`.
- CLI commands: `models`, `health`, `device`, `ask`, `status`, `pull`, `push`, `commit`, `doc`.

Verified native endpoint example:

```json
{
  "manufacturer": "vivo",
  "brand": "iQOO",
  "model": "I2501",
  "android_version": "16",
  "battery_percent": 83,
  "charging": true
}
```

## Immediate Testing Loop

```powershell
adb forward tcp:8080 tcp:8080
cd C:\Users\aryas\ai-hub-apps\IQOO
python geniex_cli.py health
python geniex_cli.py device
python geniex_cli.py models
python geniex_cli.py ask "Reply with one word: OK"
```

`ask` requires a model to be loaded inside the Android app first.

## Roadmap

### Milestone 1: Assistant Core

This is the base. Build context, memory, and planning before voice/TTS polish.

- Add Android SQLite memory store.
- Add normalized context event store.
- Add dashboard screen for status, context, memory, current plan, and approvals.
- Add `/memory/add`.
- Add `/memory/list`.
- Add `/memory/search`.
- Add `/context/events`.
- Add `/context/recent`.
- Add `/context/today`.
- Add `/plan/current`.
- Add `/plan/today`.
- Add memory inspect/delete controls.

### Milestone 2: Context Ingestion

Bring useful local signals into the assistant.

- Keep `/health` and `/tools/device` as baseline phone context.
- Add `/tools/storage`.
- Add `/tools/network`.
- Add manual context entry from app and CLI.
- Add `context add`, `context list`, and `context today` commands in PC CLI.
- Add `projects scan` in PC CLI.
- Store repo paths, branches, diff summaries, current goals, and project notes.

### Milestone 3: Project Awareness

Make the assistant know active development state.

- Add `git summary` command.
- Add `git plan` command.
- Add `docs update` command with overwrite protection.
- Store daily project summaries.
- Link Git changes to project memory and current plan.
- Detect blockers and next actions from repo state.

### Milestone 4: Notification Ingestion

Notifications are the first high-value Android context source.

- Add `NotificationListenerService`.
- User enables notification access manually.
- Store recent notifications from allowlisted apps.
- Add:

```text
GET /tools/notifications
POST /tools/notifications/summarize
```

- Start read-only.
- Redact OTPs, password-reset links, and token-like content.
- No auto-replies.

### Milestone 5: Gmail

Use Gmail API OAuth. Do not scrape Gmail app private data.

- Implement Gmail OAuth in PC CLI.
- Add read-only commands first:

```powershell
python geniex_cli.py gmail unread
python geniex_cli.py gmail summarize
```

- Add draft-only replies:

```powershell
python geniex_cli.py gmail draft-reply THREAD_ID
```

- Sending requires explicit approval.

### Milestone 6: Action Executor

Actions are base, but only after context and memory exist.

- Standardize tool calls.
- Add confirmation UI.
- Add action logs.
- Add rollback hints where possible.
- Add policy: no silent external actions.
- PC companion handles Git/files/builds for PC-local projects.
- Android handles native phone actions it has permission for.

### Milestone 7: Top-Up UX

TTS and voice are useful polish, not the foundation.

- Add foreground service notification: `GenieX assistant running`.
- Add basic auth token for LAN mode.
- Add streaming responses for `/v1/chat/completions`.
- Add TTS endpoint:

```text
POST /tools/tts/speak
```

- Add voice input later.
- Add widgets/quick settings tile later.

## Product Principle

Build the assistant as a trusted local operator:

```text
Sense -> Remember -> Decide -> Act -> Verify
```

Default behavior:

```text
Read first. Draft second. Act only after approval.
```

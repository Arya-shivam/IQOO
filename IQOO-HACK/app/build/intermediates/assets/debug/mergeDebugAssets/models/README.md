# Local Model Integration Notes

This folder is only a codebase placeholder for documentation.

Current runtime expects models in app internal storage, by default:

- `/data/user/0/com.geniex.assistant/files/models`

Suggested runtime structure at that internal path:

- `models/qwen/` for GGUF/model metadata
- `models/geniex/` for GenieX runtime files

Current app behavior:

- Uses the app-private model directory; the simplified MVP has no Settings screen.
- Attempts local GGUF inference through `llama-android` when a readable `.gguf` file is found.
- Uses rule-based fallback extraction/recommendation if model load/inference fails.
- Keeps inference behind `LocalModelBridge` so the runtime can be replaced later.

When you integrate runtime APIs, update:

- `app/src/main/java/com/geniex/assistant/llm/GenieXQwenLocalBridge.kt`

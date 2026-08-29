# Local Model Integration Notes

This folder is only a codebase placeholder for documentation.

Current runtime expects models in app internal storage, by default:

- `/data/user/0/com.geniex.assistant/files/models`

Suggested runtime structure at that internal path:

- `models/qwen/` for GGUF/model metadata
- `models/geniex/` for GenieX runtime files

Current app behavior:

- Reads configured model directory path from Settings.
- Uses rule-based fallback extraction/recommendation if model is not mounted.
- Keeps interfaces (`LocalModelBridge`) ready for direct GenieX/Qwen binding.

When you integrate runtime APIs, update:

- `app/src/main/java/com/geniex/assistant/llm/GenieXQwenLocalBridge.kt`

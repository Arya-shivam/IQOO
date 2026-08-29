# GenieX Mobile Development Studio

## Product and technical plan

### 1. Product vision

GenieX Mobile Development Studio is a native Android application that lets a person describe, create, test, and ship software from an Android phone or tablet. The primary interaction is a conversational coding agent, but the product must also provide the controls that make generated software trustworthy: a project tree, editable source files, diffs, build logs, previews, secrets management, release signing, and explicit publish approval.

The experience must not depend on opening a browser. A user should be able to:

1. Create a project from a prompt or a template.
2. Ask the local model to inspect and change the project.
3. Review the proposed file changes.
4. Run tests and builds from the app.
5. Preview a website or install an Android debug APK.
6. Connect a Git or hosting account.
7. Approve a release and publish it.

The product should be offline-first for coding assistance, while clearly indicating when a build, dependency download, Git operation, or deployment needs network access.

### 2. Why GenieX is a suitable foundation

[Qualcomm GenieX](https://github.com/qualcomm/GenieX) is an on-device generative-AI runtime for Qualcomm platforms. Its Android SDK exposes Kotlin/Java integration and can run supported LLM/VLM models through Qualcomm hardware acceleration paths, including CPU, GPU, and NPU. Qualcomm also describes GenieX as providing an OpenAI-compatible local server interface and support for GGUF or Qualcomm AI Hub model packages in its [developer announcement](https://www.qualcomm.com/developer/blog/2026/06/geniex-developer-preview).

This makes GenieX a good inference layer, not the entire product. The model should propose plans, patches, commands, and explanations. The host application must decide which tools are available, validate model output, display changes, and request user approval for side effects.

GenieX is Qualcomm-focused. Device capability detection is therefore required. On unsupported hardware, the app should use a second local runtime, a user-configured local endpoint, or an optional cloud provider. The model provider must be abstracted behind one application interface so that the rest of the app does not depend directly on GenieX.

### 3. Proposed architecture

```text
┌─────────────────────────────────────────────────────────┐
│ Native Android UI                                       │
│ Compose chat · editor · project tree · preview · logs   │
└──────────────────────────┬──────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────┐
│ Agent orchestration layer                                │
│ context selection · planning · tool calls · approvals   │
└───────────────┬──────────────────────┬───────────────────┘
                │                      │
┌───────────────▼──────────────┐  ┌────▼──────────────────┐
│ Model provider               │  │ Workspace/tool runner │
│ GenieX · fallback runtimes  │  │ files · search · test │
└──────────────────────────────┘  └────┬──────────────────┘
                                       │
                    ┌──────────────────▼─────────────────┐
                    │ Build and release adapters         │
                    │ Web build · APK/AAB · Git · deploy │
                    └────────────────────────────────────┘
```

#### Android application

Use Kotlin and Jetpack Compose for the first-party UI. Separate UI state from the workspace and agent services so the same project can be restored after process death. Store projects in app-private storage by default, with an optional Android Storage Access Framework export/import flow.

Suggested modules:

- `app`: navigation, screens, permissions, lifecycle.
- `model-runtime`: GenieX adapter and fallback providers.
- `agent`: prompt assembly, tool schema, loop limits, cancellation.
- `workspace`: project files, snapshots, diffs, search, indexing.
- `execution`: sandboxed commands, process output, resource limits.
- `build`: web and Android build adapters.
- `release`: Git, hosting, signing, and publish workflows.

The GenieX Android API reference is available in the [GenieX Android documentation](https://github.com/qualcomm/GenieX/blob/main/docs/en/run/android/api-reference.mdx). Keep model loading, inference, cancellation, and streaming behind an interface such as `LocalModelProvider`.

### 4. Agent behavior and safety

The coding agent should use structured tools rather than emitting shell commands as plain text. Initial tools can be:

- `list_files`
- `read_file`
- `search_files`
- `propose_patch`
- `apply_patch`
- `run_test`
- `run_build`
- `preview_project`
- `git_diff`

The default loop is: understand request, inspect only relevant files, propose a patch, show a diff, apply after approval, run validation, and summarize results. Limit file size, recursion depth, process duration, output size, and concurrent jobs. Never give an agent unrestricted access to Android settings, private files, signing keys, or network credentials.

Every mutation should create a recoverable snapshot. The UI should show the exact files changed and provide undo. Commands that can publish, delete, overwrite, or expose data require a separate confirmation step.

### 5. Building websites on-device

Website projects are the best first target because they can be previewed quickly. Support a small set of templates first: static HTML/CSS/JavaScript, a lightweight TypeScript site, and eventually a selected React/Vite template.

The app can serve generated static files from an in-process local HTTP server and display them in an embedded WebView. This is an app-internal preview, not a browser-based development workflow. For projects requiring Node or large dependency trees, use a controlled build adapter or a remote build worker rather than attempting to install arbitrary executables inside the app.

Deployment adapters should initially target one provider at a time and use OAuth where available. Store access tokens in Android Keystore-backed encrypted storage and never include them in the model context or project files.

### 6. Building Android applications

Android projects should begin from vetted templates, such as a minimal Kotlin/Compose app. The agent can modify application code, resources, and Gradle configuration within an allowlisted project structure.

The official Android documentation explains that Android builds can be executed using the Gradle wrapper and command-line tooling, and that `bundletool` can produce deployable artifacts from compiled code and resources: [Build your app from the command line](https://developer.android.com/build/building-cmdline). The Gradle wrapper should be pinned to a known version and builds should run with a controlled SDK, JDK, and dependency cache.

There are two implementation phases:

**Phase A — dependable MVP:** the Android app edits projects and sends a build job to a controlled build service. The service returns logs, an APK/AAB, and a build manifest. The user still controls the complete workflow through the Android app.

**Phase B — local builds on selected devices:** bundle or download the required JDK, Android SDK components, Gradle distribution, and dependencies on supported devices. Enforce storage and thermal limits, and make offline dependency availability visible. This phase is technically possible but substantially increases application size, maintenance, and device-compatibility risk.

A debug APK can be installed for local testing. Release artifacts must be signed using a user-controlled keystore. Android’s [app signing documentation](https://developer.android.com/studio/publish/app-signing) should guide key handling and release configuration.

### 7. Publishing workflow

Publishing is a deliberate release pipeline, not an automatic consequence of code generation:

```text
Prompt → patch → diff approval → tests → build → artifact scan
      → signing → release summary → user approval → publish
```

For websites, the release adapter can commit to a selected repository or upload to a selected hosting provider. For Android, generate an AAB, retain the version code/name and signing metadata, and guide the user through Google Play requirements. The app should support export of an artifact even when Play publishing is unavailable.

Use separate credentials for development and release. Provide a dry-run mode that validates the release without uploading it. Record a local release manifest containing project hash, toolchain versions, model identifier, changed files, test results, artifact hashes, and destination.

### 8. Model strategy

Do not assume that the largest local model is the best mobile coding model. Use a small, instruction-tuned model for routine edits and a stronger optional model for architectural tasks. Context should be assembled selectively from relevant files, symbols, build errors, and the current diff rather than sending the entire project on every turn.

The model prompt should include:

- project type and supported commands;
- exact tool schemas and their permission levels;
- files selected by the workspace indexer;
- build/test failures with bounded logs;
- formatting and security rules;
- an instruction to explain uncertainty and avoid invented APIs.

Measure task completion, build success, patch size, latency, memory usage, battery impact, and rollback rate on representative Snapdragon devices. Store model downloads separately from user projects and show their storage footprint.

### 9. Security and privacy requirements

- Keep source projects in app-private storage by default.
- Use Android Keystore for encryption keys and signing-key protection.
- Require explicit consent before network access or deployment.
- Treat generated code and model output as untrusted input.
- Use an allowlist for commands and project paths.
- Prevent path traversal, access to other apps’ data, and accidental secret inclusion.
- Redact tokens and private keys from logs, prompts, crash reports, and diffs.
- Make local-only mode a real setting that blocks network-capable tools.
- Add dependency and artifact scanning before release.

The application should follow Android’s security guidance for app-private data and cryptographic key storage; the [Android security overview](https://developer.android.com/privacy-and-security/security-overview) is the baseline reference.

### 10. Delivery roadmap

**Milestone 1: native shell**

Create the Kotlin/Compose application, project creation screen, file browser, editor, local snapshots, and a mock model provider.

**Milestone 2: GenieX integration**

Add device detection, model management, streaming inference, cancellation, and a provider interface. Validate on supported Snapdragon hardware and provide a clear unsupported-device message.

**Milestone 3: safe coding agent**

Implement file inspection, patch proposals, diff approval, undo, bounded commands, and test/build log capture.

**Milestone 4: website workflow**

Add static-site templates, in-app preview, export, Git integration, and one deployment provider.

**Milestone 5: Android workflow**

Add a Kotlin/Compose template, controlled debug builds, APK installation, and artifact export.

**Milestone 6: release workflow**

Add encrypted credentials, signing, AAB generation, release manifests, dry runs, and user-approved publishing.

### 11. Success criteria

The MVP is successful when a user can start with a natural-language request, generate a small website or Android app, inspect every change, build it, preview or install it, and export or publish the result without leaving the native Android application. Offline local inference should continue to work for supported devices even when deployment services are unavailable.

### References and resources

- [Qualcomm GenieX GitHub repository](https://github.com/qualcomm/GenieX)
- [GenieX Android API reference](https://github.com/qualcomm/GenieX/blob/main/docs/en/run/android/api-reference.mdx)
- [Qualcomm GenieX developer announcement](https://www.qualcomm.com/developer/blog/2026/06/geniex-developer-preview)
- [GenieX Android sample application](https://github.com/qualcomm/ai-hub-apps/tree/release/apps/geniex_chat_android)
- [Android command-line build documentation](https://developer.android.com/build/building-cmdline)
- [Android app signing documentation](https://developer.android.com/studio/publish/app-signing)
- [Android security overview](https://developer.android.com/privacy-and-security/security-overview)
- [Gradle command-line interface](https://docs.gradle.org/current/userguide/command_line_interface.html)


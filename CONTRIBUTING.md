# Contributing to KeybindPlus

Thank you for your interest in contributing to KeybindPlus.

## Code of Conduct

Please maintain a constructive, respectful, and collaborative environment across all issues and pull requests.

## How to Contribute

### Reporting Bugs
- Check the issue tracker before creating a new issue to avoid duplicates.
- Use the Bug Report template and provide reproduction steps, logs, and version details.

### Suggesting Enhancements
- Open a Feature Request describing your use case and proposed workflow.

### Submitting Pull Requests
1. Fork the repository and create a descriptive branch from `main`:
   ```bash
   git checkout -b feat/my-new-feature
   ```
2. Follow standard Java coding conventions and keep code comments minimal, concise, and focused on essential logic.
3. Avoid adding emoji characters to code, comments, log messages, or UI labels.
4. **UI & Notification Policy**:
   - Do NOT send diagnostic messages or feedback via player in-game chat (`sendSystemMessage`).
   - In-game actions (profile switches, saves, key edits) should provide immediate visual feedback in the GUI.
   - `SystemToast` notifications are reserved strictly for asynchronous external file I/O (profile JSON import and export).
   - Use the SLF4J logger (`KeybindPlus.LOGGER`) for backend error handling and diagnostic tracking.
5. Ensure the project builds cleanly before opening a pull request:
   ```bash
   ./gradlew build
   ```
6. Submit a pull request targeting the `main` branch with a clear summary of changes.

## Branching Guidelines

- **`main`**: Production-ready, clean open-source code targeting the current active Minecraft release.
- **`debug-tools`**: Dedicated development branch containing extended debug instrumentation and experimental testing utilities.
- **Version branches** (e.g. `1.21.x`, `1.20.x`, `1.8.9`): Platform port branches maintaining compatibility with specific Minecraft releases. See [VERSIONS.md](VERSIONS.md).

## Architecture Overview

- `common`: Cross-platform business logic, GUI screens, widgets, profile storage, conflict detection.
- `fabric`: Fabric-specific entry points and client initialization.
- `neoforge`: NeoForge-specific entry points and client initialization.

# KeybindPlus (VaultBind) Workspace Rules & Skill Auto-Routing

Whenever working in this repository, the agent must adhere to the following specialized rules and automatically leverage the workspace skills in `.agents/skills/`:

---

## 1. Skill Auto-Invocation Matrix

| Scenario / Intent | Trigger Condition | Specialized Workspace Skill |
| :--- | :--- | :--- |
| **Release & Deployment** | Commit, tag, push, release, version bump | `vaultbind-release` ([`.agents/skills/vaultbind-release/SKILL.md`](file:///d:/Private/PersonalProject/MinecraftMods/VaultBind/.agents/skills/vaultbind-release/SKILL.md)) |
| **Multi-Version Porting** | Porting to 1.21.x, 1.20.x, 1.19.x, 1.8.9 | `vaultbind-port` ([`.agents/skills/vaultbind-port/SKILL.md`](file:///d:/Private/PersonalProject/MinecraftMods/VaultBind/.agents/skills/vaultbind-port/SKILL.md)) |
| **GUI & Widget Engineering** | Creating or editing Screens, Popups, Widgets, I18n | `vaultbind-gui` ([`.agents/skills/vaultbind-gui/SKILL.md`](file:///d:/Private/PersonalProject/MinecraftMods/VaultBind/.agents/skills/vaultbind-gui/SKILL.md)) |
| **In-Game Client Testing** | Running tests, client verification, validating profiles | `vaultbind-test` ([`.agents/skills/vaultbind-test/SKILL.md`](file:///d:/Private/PersonalProject/MinecraftMods/VaultBind/.agents/skills/vaultbind-test/SKILL.md)) |

---

## 2. Core Operational Constraints

1. **Architecture Separation**:
   - `common`: All UI screens, business logic, serialization, and conflict algorithms.
   - `fabric` & `neoforge`: Only loader entrypoints and platform-specific event hooks.
2. **UI Layout Standards**:
   - Lists with interactive buttons must set `getRowWidth()` to `Math.min(330, this.width - 20)`.
   - Single-column lists must set `getRowWidth()` to `Math.min(308, this.width - 20)` for symmetry with bottom action buttons.
   - Two-line entries must have `itemHeight >= 28px`.
3. **No Chat Spam & Silent UX**:
   - Never send diagnostic or success messages via player chat (`sendSystemMessage`).
   - Use `SystemToast` strictly for external file I/O (import/export).
4. **Commit & Versioning Standards**:
   - Always format commits using the `/commit-format` standard.
   - Keep `gradle.properties`, `VERSIONS.md`, and git tags synchronized.

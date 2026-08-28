---
name: vaultbind-port
description: Runbook for backporting and maintaining KeybindPlus across multiple Minecraft versions (26.2+, 1.21.x, 1.20.x, 1.19.x, 1.18.x, 1.17.x, and legacy 1.8.9 Forge).
---

# KeybindPlus Version Porting & Branching Workflow

This skill guides the agent when backporting, branching, or adapting KeybindPlus across different Minecraft versions and mod loaders.

---

## 1. Minecraft Version Compatibility Matrix

| Target Version | Target Branch | Mod Loader(s) | Java Runtime | GUI Rendering API | Key Mapping Registry |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **26.2+ (Active)** | `main` | Fabric, NeoForge | Java 25 | `GuiGraphicsExtractor` | `dev.architectury.registry.client.keymappings` |
| **1.21.x** | `1.21.x` | Fabric, NeoForge | Java 21 | `GuiGraphics` (`render`) | Architectury / NeoForge Event |
| **1.20.x** | `1.20.x` | Fabric, Forge/NeoForge | Java 17 | `GuiGraphics` | Architectury |
| **1.19.x** | `1.19.x` | Fabric, Forge | Java 17 | `PoseStack` (`render`) | Architectury |
| **1.18.x - 1.17.x** | `1.18.x`, `1.17.x` | Fabric, Forge | Java 17 / 16 | `PoseStack` | Architectury |
| **1.8.9 (Legacy)** | `1.8.9` | Forge 1.8.9 (RetroLoom) | Java 8 | `GuiScreen.drawScreen()` | `ClientRegistry.registerKeyBinding` |

---

## 2. Porting Procedure

### Phase 1: Branch Creation & Isolation
1. Always create the version-specific branch from a clean state:
   ```powershell
   git checkout -b <target-version>
   ```
2. Verify branch naming adheres to [`VERSIONS.md`](file:///d:/Private/PersonalProject/MinecraftMods/VaultBind/VERSIONS.md) (`1.21.x`, `1.20.x`, `1.8.9`).

### Phase 2: Toolchain & Dependency Adaptation
1. Update `gradle.properties`:
   - `minecraft_version`: Set to target version (e.g. `1.21.4`, `1.20.4`, `1.8.9`).
   - `fabric_loader_version` & `fabric_api_version`: Update for target version.
   - `neoforge_version` / `forge_version`: Adapt to platform requirements.
   - `architectury_api_version`: Select compatible snapshot/release for target MC.
2. Update Java Toolchain in `build.gradle` to the matching Java release (Java 25 for 26.2+, Java 21 for 1.21, Java 17 for 1.20/1.19, Java 8 for 1.8.9).

### Phase 3: GUI & Rendering Refactoring
1. **Modern 26.2+ to 1.21/1.20 (`GuiGraphicsExtractor` -> `GuiGraphics`)**:
   - Replace `extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta)` with `render(GuiGraphics graphics, int mouseX, int mouseY, float delta)`.
   - Update text rendering from `graphics.textRenderer().accept(...)` to `graphics.drawString(this.font, Component, x, y, color)`.
2. **1.20 to 1.19/1.18 (`GuiGraphics` -> `PoseStack`)**:
   - Replace `GuiGraphics` parameter with `PoseStack poseStack`.
   - Use `this.font.draw(poseStack, text, x, y, color)` and `fill(poseStack, x1, y1, x2, y2, color)`.
3. **Modern to Legacy 1.8.9 (`ContainerObjectSelectionList` -> `GuiSlot` / `GuiListExtended`)**:
   - Refactor screens to inherit from `GuiScreen`.
   - Replace `Component.translatable` / `Component.literal` with `I18n.format(...)` and raw strings.
   - Replace `TinyFileDialogs` with standard Swing/AWT file chooser or RetroLoom-bundled native dialogs.

### Phase 4: Verification & Build
1. Run test compile:
   ```powershell
   .\gradlew build --no-daemon
   ```
2. Launch client test:
   ```powershell
   .\gradlew :fabric:runClient
   .\gradlew :neoforge:runClient
   ```

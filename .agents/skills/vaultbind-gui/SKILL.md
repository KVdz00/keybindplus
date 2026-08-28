---
name: vaultbind-gui
description: Guidelines and architectural patterns for designing, creating, and modifying Minecraft Screens, Selection Lists, Popups, and Widgets in KeybindPlus.
---

# KeybindPlus GUI & Widget Engineering Standards

This skill enforces strict UI/UX layout rules, accessibility guidelines, and rendering geometry for all user interface screens in KeybindPlus.

---

## 1. Core Layout & Geometry Rules

### A. Selection List Sizing (`getRowWidth` & `itemHeight`)
- **Never rely on default vanilla width (220px)** when rendering items with multiple columns or side buttons.
- For list widgets with interactive buttons on the right:
  - Override `getRowWidth()`:
    ```java
    @Override
    public int getRowWidth() {
        return Math.min(330, this.width - 20);
    }
    ```
  - Use `itemHeight >= 28px` whenever displaying 2 lines of text (e.g. Action Name at `rowY + 4` and Category at `rowY + 15`).
  - Align right-side buttons relative to `getRowWidth()`:
    ```java
    int unbindX = this.getX() + rowWidth - 46;
    int keyX = unbindX - 88;
    ```
- For single-column lists (e.g. `ProfileListWidget`, `ConflictWarningPopup`):
  - Set `getRowWidth()` to `Math.min(308, this.width - 20)` to maintain perfect symmetry with the 308px button clusters below.

### B. Screen Bottom Button Grid Conventions
Bottom action rows should use standardized button bounds centered around `centerX = this.width / 2`:
- **Single 3-Button Row (308px total span)**:
  - Left: `centerX - 154`, width `100`, height `20`
  - Center: `centerX - 50`, width `96` or `100`, height `20`
  - Right: `centerX + 50` or `centerX + 54`, width `104` or `100`, height `20`
- **Bottom Y Spacing**:
  - Row 1 (Primary): `this.height - 76`
  - Row 2 (Secondary): `this.height - 52`
  - Row 3 (Navigation / File I/O): `this.height - 28`

---

## 2. Input Handling & Key Listeners

- **Rebinding State**:
  - Highlight the listening row in bold yellow (`ChatFormatting.YELLOW, ChatFormatting.BOLD`).
  - Set rebind label to `> Press Key <` (`keybindplus.editor.press_key`).
  - Intercept keyboard events in `keyPressed(KeyEvent event)`.
  - Intercept mouse clicks in `mouseClicked(MouseButtonEvent event, boolean doubleClick)` via `InputConstants.Type.MOUSE.getOrCreate(event.button())`.
  - Pressing `ESCAPE` unbinds the action to `InputConstants.UNKNOWN` (`"key.keyboard.unknown"`).
- **Unbound State**:
  - Always format unknown keys as `NONE` in dark gray (`ChatFormatting.DARK_GRAY`).
  - Disable the "Unbind" button (`active = false`) if the action is already `NONE` or in listening state.

---

## 3. UI Notification & Feedback Policy

- **No Chat Spam**: Never use `player.sendSystemMessage()` or dispatch chat text for UI interactions.
- **Immediate In-GUI Visual Feedback**: Profile loading, editing, deleting, and renaming must reflect instantly in widget entries.
- **Strict Toast Policy**: `SystemToast` notifications are exclusively permitted for asynchronous file dialog I/O (`keybindplus.toast.import_*` and `keybindplus.toast.export_*`).

---

## 4. Internationalization & Localization (`en_us.json`)

Every UI string must be referenced via translation keys:
1. Always define keys in [`common/src/main/resources/assets/keybindplus/lang/en_us.json`](file:///d:/Private/PersonalProject/MinecraftMods/VaultBind/common/src/main/resources/assets/keybindplus/lang/en_us.json).
2. Naming convention:
   - `key.keybindplus.<name>` for keybindings
   - `keybindplus.screen.<action>` for main screen
   - `keybindplus.editor.<action>` for editor screen
   - `keybindplus.popup.<action>` for modal dialogs
   - `keybindplus.toast.<action>` for system toast notifications
3. Never hardcode raw user-facing English strings inside Java GUI classes.

---
name: vaultbind-test
description: Client testing and verification runbook for KeybindPlus: running debug game instances on Fabric and NeoForge, creating test profile fixtures, validating conflict detection, and testing backup rolling.
---

# KeybindPlus Client Testing & Verification Runbook

This skill outlines the testing protocols for validating KeybindPlus features in real Minecraft runtime environments.

---

## 1. Running Test Clients

### Launch Fabric Client
```powershell
.\gradlew :fabric:runClient
```

### Launch NeoForge Client
```powershell
.\gradlew :neoforge:runClient
```

---

## 2. Test Scenarios & Verification Matrix

### Scenario 1: Profile CRUD & Persistence
1. Open KeybindPlus via keybind (`V`).
2. Click **Save Current** -> enter a profile name (e.g. `PvP_Config`) -> verify profile appears in the list.
3. Select profile -> Click **Copy** -> enter `PvP_Config_Copy` -> verify duplication.
4. Select profile -> Click **Rename** -> rename to `PvP_Config_Renamed`.
5. Select profile -> Click **Delete** -> confirm deletion.
6. Verify file system: check that `.minecraft/config/keybindplus/profiles/` reflects all changes.

### Scenario 2: Key Editing & Unbinding
1. Select a profile -> Click **Edit**.
2. Click the key binding button for an action (e.g. `Attack/Destroy`).
3. Press a keyboard key (e.g. `F`) or mouse button -> verify the label updates immediately.
4. Click **Unbind** on any bound key -> verify label becomes `NONE` (dark gray) and button disables.
5. Click **Save & Apply** -> close GUI -> test in game that the unbind/rebind took effect immediately.
6. Re-open **Edit** screen -> verify key states are retained accurately.

### Scenario 3: Conflict Detection & Warnings
1. In the Editor, bind two different gameplay actions to the same key (e.g. `Jump` and `Sneak` both to `SPACE`).
2. Verify both rows highlight the key in bold red in the editor list.
3. Click the filter button **Conflicts Only** -> verify only the 2 conflicting rows are displayed.
4. Save and return to the main profile list.
5. Click **Load** on the conflicted profile -> verify that `ConflictWarningPopup` modal appears displaying the conflict details.
6. Test clicking **Resolve Conflicts** -> verify it re-opens the editor directly filtered to conflicts.

### Scenario 4: Undo & Auto-Backup Rolling
1. Apply any profile -> verify that the **Undo** button becomes active (`active = true`).
2. Click **Undo** -> verify previous keybinds are restored and the button becomes disabled.
3. Check `.minecraft/config/keybindplus/backups/` -> verify auto-backup timestamped files exist (`backup_yyyyMMdd_HHmmss.json`).
4. Apply profiles repeatedly (> 5 times) -> verify that only the 5 most recent backups are kept (oldest pruned automatically).

### Scenario 5: File Import & Export (Native File Dialog)
1. Select a profile -> Click **Export** -> verify `SystemToast` appears confirming export to `.minecraft/config/keybindplus/exports/`.
2. Click **Import** -> verify native OS file chooser opens in a background thread without freezing game rendering.
3. Select an exported `.json` file -> verify `SystemToast` confirms successful import and profile list refreshes.

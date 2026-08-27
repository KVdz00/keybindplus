package com.github.kvdz00.keybindplus.gui;

import com.github.kvdz00.keybindplus.config.KeybindPlusConfig;
import com.github.kvdz00.keybindplus.keybind.*;
import com.github.kvdz00.keybindplus.profile.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class KeybindPlusScreen extends Screen {
    private ProfileListWidget profileList;
    private EditBox searchField;
    private Button loadButton;
    private Button editButton;
    private Button deleteButton;
    private Button compareButton;
    private Button exportButton;
    private Button setDefaultButton;
    private Button undoButton;
    private Button renameButton;
    private Button duplicateButton;

    public KeybindPlusScreen() {
        super(Component.translatable("keybindplus.screen.title"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        // Search field
        this.searchField = new EditBox(this.font, centerX - 100, 26, 200, 18,
            Component.translatable("keybindplus.screen.search"));
        this.searchField.setHint(Component.translatable("keybindplus.screen.search"));
        this.searchField.setResponder(query -> refreshList());
        this.addRenderableWidget(this.searchField);

        // Profile list
        this.profileList = new ProfileListWidget(this.minecraft, this,
            this.width, this.height - 114, 48, 28);
        this.addRenderableWidget(this.profileList);

        // Row 1: Primary Action buttons
        int btnY1 = this.height - 56;
        int rx1 = centerX - 154;

        this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.save"),
            btn -> onSave()
        ).bounds(rx1, btnY1, 68, 20).build());
        rx1 += 72;

        this.loadButton = this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.load"),
            btn -> onLoad()
        ).bounds(rx1, btnY1, 46, 20).build());
        rx1 += 50;

        this.editButton = this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.edit"),
            btn -> onEdit()
        ).bounds(rx1, btnY1, 44, 20).build());
        rx1 += 48;

        this.undoButton = this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.undo"),
            btn -> onUndo()
        ).bounds(rx1, btnY1, 44, 20).build());
        rx1 += 48;

        this.compareButton = this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.compare"),
            btn -> onCompare()
        ).bounds(rx1, btnY1, 52, 20).build());
        rx1 += 56;

        this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.done"),
            btn -> this.onClose()
        ).bounds(rx1, btnY1, 38, 20).build());

        // Row 2: Management buttons
        int btnY2 = this.height - 32;
        int bx = centerX - 154;

        this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.open_folder"),
            btn -> onOpenFolder()
        ).bounds(bx, btnY2, 42, 20).build());
        bx += 45;

        this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.import"),
            btn -> onImport()
        ).bounds(bx, btnY2, 44, 20).build());
        bx += 47;

        this.exportButton = this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.export"),
            btn -> onExport()
        ).bounds(bx, btnY2, 44, 20).build());
        bx += 47;

        this.renameButton = this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.rename"),
            btn -> onRename()
        ).bounds(bx, btnY2, 50, 20).build());
        bx += 53;

        this.duplicateButton = this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.duplicate"),
            btn -> onDuplicate()
        ).bounds(bx, btnY2, 38, 20).build());
        bx += 41;

        this.deleteButton = this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.delete"),
            btn -> onDelete()
        ).bounds(bx, btnY2, 38, 20).build());
        bx += 41;

        this.setDefaultButton = this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.set_default"),
            btn -> onSetDefault()
        ).bounds(bx, btnY2, 44, 20).build());

        refreshList();
    }

    private void refreshList() {
        ProfileManager pm = ProfileManager.get();
        String query = searchField != null ? searchField.getValue() : "";
        List<KeybindProfile> profiles = pm.searchProfiles(query);
        profileList.updateEntries(profiles);
        onSelectionUpdated();
    }

    public void onSelectionUpdated() {
        boolean hasSelection = profileList != null && profileList.getSelectedProfile() != null;
        if (loadButton != null) loadButton.active = hasSelection;
        if (editButton != null) editButton.active = hasSelection;
        if (compareButton != null) compareButton.active = hasSelection;
        if (exportButton != null) exportButton.active = hasSelection;
        if (renameButton != null) renameButton.active = hasSelection;
        if (duplicateButton != null) duplicateButton.active = hasSelection;
        if (deleteButton != null) deleteButton.active = hasSelection;
        if (setDefaultButton != null) setDefaultButton.active = hasSelection;
        if (undoButton != null) undoButton.active = KeybindApplier.hasUndoSnapshot();
    }

    private void onOpenFolder() {
        Path configDir = KeybindPlusConfig.getConfigDir();
        Util.getPlatform().openPath(configDir);
    }

    private void onSave() {
        this.minecraft.setScreenAndShow(new SaveProfilePopup(this, name -> {
            ProfileManager pm = ProfileManager.get();
            if (pm.profileExists(name)) {
                this.minecraft.setScreenAndShow(new ConfirmPopup(this,
                    Component.translatable("keybindplus.popup.confirm_overwrite", name),
                    () -> {
                        pm.saveProfile(name);
                        refreshList();
                    }
                ));
            } else {
                pm.saveProfile(name);
                refreshList();
            }
        }));
    }

    public void onLoad() {
        KeybindProfile profile = profileList.getSelectedProfile();
        if (profile == null) return;

        List<KeyConflict> conflicts = ConflictDetector.detect(profile);
        if (!conflicts.isEmpty()) {
            this.minecraft.setScreenAndShow(new ConflictWarningPopup(this, profile, conflicts, () -> {
                applyProfile(profile);
            }));
        } else {
            applyProfile(profile);
        }
    }

    private void onEdit() {
        KeybindProfile profile = profileList.getSelectedProfile();
        if (profile == null) return;
        this.minecraft.setScreenAndShow(new KeybindEditorScreen(this, profile, false));
    }

    private void applyProfile(KeybindProfile profile) {
        ProfileManager.get().createAutoBackup();
        KeybindApplier.apply(profile);
        onSelectionUpdated();
    }

    private void onUndo() {
        if (!KeybindApplier.hasUndoSnapshot()) return;
        KeybindApplier.undoLastApply();
        onSelectionUpdated();
    }

    private void onDelete() {
        KeybindProfile profile = profileList.getSelectedProfile();
        if (profile == null) return;

        this.minecraft.setScreenAndShow(new ConfirmPopup(this,
            Component.translatable("keybindplus.popup.confirm_delete", profile.getName()),
            () -> {
                ProfileManager.get().deleteProfile(profile.getName());
                refreshList();
            }
        ));
    }

    private void onExport() {
        KeybindProfile profile = profileList.getSelectedProfile();
        if (profile == null) return;

        Path path = ProfileManager.get().exportProfile(profile.getName());
        if (path != null) {
            ToastNotification.toast("keybindplus.toast.export_title",
                "keybindplus.toast.export_desc", path.getFileName().toString());
        } else {
            ToastNotification.toast("keybindplus.toast.error_title",
                "keybindplus.toast.export_desc", "Export failed");
        }
    }

    private void onImport() {
        new Thread(() -> {
            String defaultPath = KeybindPlusConfig.getImportsDir().toAbsolutePath().toString();
            String result;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer filters = stack.mallocPointer(1);
                filters.put(stack.UTF8("*.json"));
                filters.flip();
                result = TinyFileDialogs.tinyfd_openFileDialog(
                    "Import KeybindPlus Profile",
                    defaultPath + "\\",
                    filters,
                    "KeybindPlus Profiles (*.json)",
                    false
                );
            }

            if (result == null) return;

            Path filePath = Paths.get(result);
            this.minecraft.execute(() -> {
                if (!ProfileManager.isValidProfileFile(filePath)) {
                    ToastNotification.toast("keybindplus.toast.error_title",
                        "keybindplus.toast.import_invalid");
                    return;
                }

                KeybindProfile imported = ProfileManager.get().importProfile(filePath);
                if (imported != null) {
                    ToastNotification.toast("keybindplus.toast.import_title",
                        "keybindplus.toast.import_desc", imported.getName());
                    refreshList();
                } else {
                    ToastNotification.toast("keybindplus.toast.error_title",
                        "keybindplus.toast.import_invalid");
                }
            });
        }, "KeybindPlus-FileDialog").start();
    }

    private void onRename() {
        KeybindProfile profile = profileList.getSelectedProfile();
        if (profile == null) return;

        String oldName = profile.getName();
        this.minecraft.setScreenAndShow(new SaveProfilePopup(this, newName -> {
            if (newName.equals(oldName)) return;
            boolean success = ProfileManager.get().renameProfile(oldName, newName);
            if (success) {
                refreshList();
            }
        }));
    }

    private void onDuplicate() {
        KeybindProfile profile = profileList.getSelectedProfile();
        if (profile == null) return;

        this.minecraft.setScreenAndShow(new SaveProfilePopup(this, newName -> {
            KeybindProfile copy = ProfileManager.get().duplicateProfile(profile.getName(), newName);
            if (copy != null) {
                refreshList();
            }
        }));
    }

    private void onCompare() {
        KeybindProfile selected = profileList.getSelectedProfile();
        if (selected == null) return;
        KeybindProfile defaultProfile = ProfileManager.get().getDefaultProfile();
        if (defaultProfile == null) return;
        if (selected.getName().equals(defaultProfile.getName())) return;

        this.minecraft.setScreenAndShow(new CompareScreen(this, selected, defaultProfile));
    }

    private void onSetDefault() {
        KeybindProfile profile = profileList.getSelectedProfile();
        if (profile == null) return;
        ProfileManager.get().setDefaultProfile(profile.getName());
        refreshList();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.textRenderer().accept(TextAlignment.CENTER, this.width / 2, 10, this.title);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

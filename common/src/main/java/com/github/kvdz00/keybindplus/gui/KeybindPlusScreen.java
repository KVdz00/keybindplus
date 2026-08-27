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
        this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.save"),
            btn -> onSave()
        ).bounds(centerX - 154, btnY1, 72, 20).build());

        this.loadButton = this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.load"),
            btn -> onLoad()
        ).bounds(centerX - 78, btnY1, 50, 20).build());

        this.undoButton = this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.undo"),
            btn -> onUndo()
        ).bounds(centerX - 24, btnY1, 50, 20).build());

        this.compareButton = this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.compare"),
            btn -> onCompare()
        ).bounds(centerX + 30, btnY1, 60, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.done"),
            btn -> this.onClose()
        ).bounds(centerX + 94, btnY1, 60, 20).build());

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
        ToastNotification.toast("keybindplus.toast.open_folder", "keybindplus.toast.open_folder");
    }

    private void onSave() {
        this.minecraft.setScreenAndShow(new SaveProfilePopup(this, name -> {
            ProfileManager pm = ProfileManager.get();
            if (pm.profileExists(name)) {
                this.minecraft.setScreenAndShow(new ConfirmPopup(this,
                    Component.translatable("keybindplus.popup.confirm_overwrite", name),
                    () -> {
                        pm.saveProfile(name);
                        ToastNotification.toast("keybindplus.toast.saved_title",
                            "keybindplus.toast.saved_desc", name);
                        refreshList();
                    }
                ));
            } else {
                pm.saveProfile(name);
                ToastNotification.toast("keybindplus.toast.saved_title",
                    "keybindplus.toast.saved_desc", name);
                refreshList();
            }
        }));
    }

    public void onLoad() {
        KeybindProfile profile = profileList.getSelectedProfile();
        if (profile == null) {
            ToastNotification.toast("keybindplus.toast.error_title",
                "keybindplus.toast.no_selection");
            return;
        }

        List<KeyConflict> conflicts = ConflictDetector.detect(profile);
        if (!conflicts.isEmpty()) {
            this.minecraft.setScreenAndShow(new ConflictWarningPopup(this, conflicts, () -> {
                applyProfile(profile);
            }));
        } else {
            applyProfile(profile);
        }
    }

    private void applyProfile(KeybindProfile profile) {
        ProfileManager.get().createAutoBackup();
        ApplyResult result = KeybindApplier.apply(profile);
        ToastNotification.toast("keybindplus.toast.loaded_title",
            "keybindplus.toast.loaded_desc", profile.getName());
        onSelectionUpdated();
    }

    private void onUndo() {
        if (!KeybindApplier.hasUndoSnapshot()) {
            ToastNotification.toast("keybindplus.toast.error_title",
                "keybindplus.toast.no_undo");
            return;
        }
        KeybindApplier.undoLastApply();
        ToastNotification.toast("keybindplus.toast.undo_title",
            "keybindplus.toast.undo_desc");
        onSelectionUpdated();
    }

    private void onDelete() {
        KeybindProfile profile = profileList.getSelectedProfile();
        if (profile == null) {
            ToastNotification.toast("keybindplus.toast.error_title",
                "keybindplus.toast.no_selection");
            return;
        }

        this.minecraft.setScreenAndShow(new ConfirmPopup(this,
            Component.translatable("keybindplus.popup.confirm_delete", profile.getName()),
            () -> {
                ProfileManager.get().deleteProfile(profile.getName());
                ToastNotification.toast("keybindplus.toast.deleted_title",
                    "keybindplus.toast.deleted_desc", profile.getName());
                refreshList();
            }
        ));
    }

    private void onExport() {
        KeybindProfile profile = profileList.getSelectedProfile();
        if (profile == null) {
            ToastNotification.toast("keybindplus.toast.error_title",
                "keybindplus.toast.no_selection");
            return;
        }

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
        if (profile == null) {
            ToastNotification.toast("keybindplus.toast.error_title",
                "keybindplus.toast.no_selection");
            return;
        }

        String oldName = profile.getName();
        this.minecraft.setScreenAndShow(new SaveProfilePopup(this, newName -> {
            if (newName.equals(oldName)) return;
            boolean success = ProfileManager.get().renameProfile(oldName, newName);
            if (success) {
                ToastNotification.toast("keybindplus.toast.renamed_title",
                    "keybindplus.toast.renamed_desc", oldName, newName);
                refreshList();
            } else {
                ToastNotification.toast("keybindplus.toast.error_title",
                    "keybindplus.toast.renamed_desc", "Name already exists");
            }
        }));
    }

    private void onDuplicate() {
        KeybindProfile profile = profileList.getSelectedProfile();
        if (profile == null) {
            ToastNotification.toast("keybindplus.toast.error_title",
                "keybindplus.toast.no_selection");
            return;
        }

        this.minecraft.setScreenAndShow(new SaveProfilePopup(this, newName -> {
            KeybindProfile copy = ProfileManager.get().duplicateProfile(profile.getName(), newName);
            if (copy != null) {
                ToastNotification.toast("keybindplus.toast.duplicated_title",
                    "keybindplus.toast.duplicated_desc", newName);
                refreshList();
            } else {
                ToastNotification.toast("keybindplus.toast.error_title",
                    "keybindplus.toast.duplicated_desc", "Name already exists");
            }
        }));
    }

    private void onCompare() {
        KeybindProfile selected = profileList.getSelectedProfile();
        if (selected == null) {
            ToastNotification.toast("keybindplus.toast.error_title",
                "keybindplus.toast.no_selection");
            return;
        }
        KeybindProfile defaultProfile = ProfileManager.get().getDefaultProfile();
        if (defaultProfile == null) {
            ToastNotification.toast("keybindplus.toast.error_title",
                "keybindplus.toast.no_default");
            return;
        }
        if (selected.getName().equals(defaultProfile.getName())) {
            ToastNotification.toast("keybindplus.toast.error_title",
                "keybindplus.toast.no_default");
            return;
        }

        this.minecraft.setScreenAndShow(new CompareScreen(this, selected, defaultProfile));
    }

    private void onSetDefault() {
        KeybindProfile profile = profileList.getSelectedProfile();
        if (profile == null) {
            ToastNotification.toast("keybindplus.toast.error_title",
                "keybindplus.toast.no_selection");
            return;
        }
        ProfileManager.get().setDefaultProfile(profile.getName());
        ToastNotification.toast("keybindplus.toast.default_set_title",
            "keybindplus.toast.default_set_desc", profile.getName());
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

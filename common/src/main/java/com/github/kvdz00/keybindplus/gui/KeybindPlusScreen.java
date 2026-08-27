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

import java.nio.file.Path;
import java.util.List;

public class KeybindPlusScreen extends Screen {
    private ProfileListWidget profileList;
    private EditBox searchField;
    private Button loadButton;
    private Button deleteButton;
    private Button compareButton;
    private Button exportButton;
    private Button setDefaultButton;

    public KeybindPlusScreen() {
        super(Component.translatable("keybindplus.screen.title"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        // Search field
        this.searchField = new EditBox(this.font, centerX - 100, 28, 200, 18,
            Component.translatable("keybindplus.screen.search"));
        this.searchField.setHint(Component.translatable("keybindplus.screen.search"));
        this.searchField.setResponder(query -> refreshList());
        this.addRenderableWidget(this.searchField);

        // Profile list
        this.profileList = new ProfileListWidget(this.minecraft, this,
            this.width, this.height - 112, 52, 20);
        this.addRenderableWidget(this.profileList);

        // Bottom buttons - Row 1
        int btnY1 = this.height - 56;
        this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.save"),
            btn -> onSave()
        ).bounds(centerX - 154, btnY1, 100, 20).build());

        this.loadButton = this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.load"),
            btn -> onLoad()
        ).bounds(centerX - 50, btnY1, 100, 20).build());

        this.compareButton = this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.compare"),
            btn -> onCompare()
        ).bounds(centerX + 54, btnY1, 100, 20).build());

        // Bottom buttons - Row 2
        int btnY2 = this.height - 32;
        this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.open_folder"),
            btn -> onOpenFolder()
        ).bounds(centerX - 154, btnY2, 70, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.import"),
            btn -> onImport()
        ).bounds(centerX - 80, btnY2, 46, 20).build());

        this.exportButton = this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.export"),
            btn -> onExport()
        ).bounds(centerX - 30, btnY2, 46, 20).build());

        this.deleteButton = this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.delete"),
            btn -> onDelete()
        ).bounds(centerX + 20, btnY2, 44, 20).build());

        this.setDefaultButton = this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.set_default"),
            btn -> onSetDefault()
        ).bounds(centerX + 68, btnY2, 50, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.screen.done"),
            btn -> this.onClose()
        ).bounds(centerX + 122, btnY2, 32, 20).build());

        refreshList();
    }

    private void refreshList() {
        ProfileManager pm = ProfileManager.get();
        String query = searchField != null ? searchField.getValue() : "";
        List<KeybindProfile> profiles = pm.searchProfiles(query);
        profileList.updateEntries(profiles);
    }

    private void onOpenFolder() {
        Path configDir = KeybindPlusConfig.getConfigDir();
        Util.getPlatform().openPath(configDir);
        sendChat("keybindplus.chat.open_folder");
    }

    private void onSave() {
        this.minecraft.setScreenAndShow(new SaveProfilePopup(this, name -> {
            ProfileManager pm = ProfileManager.get();
            if (pm.profileExists(name)) {
                this.minecraft.setScreenAndShow(new ConfirmPopup(this,
                    Component.translatable("keybindplus.popup.confirm_overwrite", name),
                    () -> {
                        pm.saveProfile(name);
                        sendChat("keybindplus.chat.saved", name);
                        refreshList();
                    }
                ));
            } else {
                pm.saveProfile(name);
                sendChat("keybindplus.chat.saved", name);
                refreshList();
            }
        }));
    }

    private void onLoad() {
        KeybindProfile profile = profileList.getSelectedProfile();
        if (profile == null) {
            sendChat("keybindplus.chat.no_selection");
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
        ApplyResult result = KeybindApplier.apply(profile);
        sendChat("keybindplus.chat.loaded", profile.getName());
        if (result.hasSkipped()) {
            sendChat("keybindplus.chat.skipped_keybinds",
                String.valueOf(result.skipped().size()),
                String.join(", ", result.skipped()));
        }
    }

    private void onDelete() {
        KeybindProfile profile = profileList.getSelectedProfile();
        if (profile == null) {
            sendChat("keybindplus.chat.no_selection");
            return;
        }

        this.minecraft.setScreenAndShow(new ConfirmPopup(this,
            Component.translatable("keybindplus.popup.confirm_delete", profile.getName()),
            () -> {
                ProfileManager.get().deleteProfile(profile.getName());
                sendChat("keybindplus.chat.deleted", profile.getName());
                refreshList();
            }
        ));
    }

    private void onExport() {
        KeybindProfile profile = profileList.getSelectedProfile();
        if (profile == null) {
            sendChat("keybindplus.chat.no_selection");
            return;
        }

        Path path = ProfileManager.get().exportProfile(profile.getName());
        if (path != null) {
            Util.getPlatform().openPath(KeybindPlusConfig.getExportsDir());
            sendChat("keybindplus.chat.exported", path.getFileName().toString());
        } else {
            sendChat("keybindplus.chat.error", "Failed to export profile");
        }
    }

    private void onImport() {
        List<Path> importFiles = ProfileManager.get().listImportFiles();
        if (importFiles.isEmpty()) {
            Util.getPlatform().openPath(KeybindPlusConfig.getImportsDir());
            sendChat("keybindplus.chat.imports_opened");
            return;
        }
        int count = 0;
        for (Path file : importFiles) {
            KeybindProfile imported = ProfileManager.get().importProfile(file);
            if (imported != null) {
                sendChat("keybindplus.chat.imported", imported.getName());
                count++;
            }
        }
        if (count == 0) {
            sendChat("keybindplus.chat.error", "Could not parse files from imports/ folder");
        }
        refreshList();
    }

    private void onCompare() {
        KeybindProfile selected = profileList.getSelectedProfile();
        if (selected == null) {
            sendChat("keybindplus.chat.no_selection");
            return;
        }
        KeybindProfile defaultProfile = ProfileManager.get().getDefaultProfile();
        if (defaultProfile == null) {
            sendChat("keybindplus.chat.error", "No default profile set to compare against. Use 'Set Default' first!");
            return;
        }
        if (selected.getName().equals(defaultProfile.getName())) {
            sendChat("keybindplus.chat.error", "Selected profile is already the default profile.");
            return;
        }

        this.minecraft.setScreenAndShow(new CompareScreen(this, selected, defaultProfile));
    }

    private void onSetDefault() {
        KeybindProfile profile = profileList.getSelectedProfile();
        if (profile == null) {
            sendChat("keybindplus.chat.no_selection");
            return;
        }
        ProfileManager.get().setDefaultProfile(profile.getName());
        sendChat("keybindplus.chat.default_set", profile.getName());
        refreshList();
    }

    private void sendChat(String translationKey, Object... args) {
        if (this.minecraft.player != null) {
            this.minecraft.player.sendSystemMessage(
                Component.translatable(translationKey, args));
        }
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

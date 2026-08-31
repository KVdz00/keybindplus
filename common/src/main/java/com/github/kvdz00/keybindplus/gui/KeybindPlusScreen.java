package com.github.kvdz00.keybindplus.gui;

import com.github.kvdz00.keybindplus.KeybindPlusClient;
import com.github.kvdz00.keybindplus.config.KeybindPlusConfig;
import com.github.kvdz00.keybindplus.keybind.*;
import com.github.kvdz00.keybindplus.profile.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class KeybindPlusScreen extends Screen {
    public enum SortMode {
        AZ("keybindplus.sort.az", "A-Z"),
        ZA("keybindplus.sort.za", "Z-A"),
        NEWEST("keybindplus.sort.newest", "Newest"),
        OLDEST("keybindplus.sort.oldest", "Oldest"),
        IMPORTED("keybindplus.sort.imported", "Imported"),
        LOCAL("keybindplus.sort.local", "Local");

        private final String langKey;
        private final String label;

        SortMode(String langKey, String label) {
            this.langKey = langKey;
            this.label = label;
        }

        public Component getDisplayName() {
            return Component.translatable(langKey);
        }

        public SortMode next() {
            SortMode[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }
    }

    private SortMode currentSort = SortMode.AZ;
    private ProfileListWidget profileList;
    private EditBox searchField;
    private Button sortButton;
    private Button loadButton;
    private Button editButton;
    private Button undoButton;
    private Button saveButton;
    private Button compareButton;
    private Button setDefaultButton;
    private Button renameButton;
    private Button duplicateButton;
    private Button deleteButton;
    private Button openFolderButton;
    private Button importButton;
    private Button exportButton;
    private Button doneButton;

    public KeybindPlusScreen() {
        super(Component.translatable("keybindplus.screen.title"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        this.searchField = new EditBox(this.font, centerX - 154, 26, 168, 18,
            Component.translatable("keybindplus.screen.search"));
        this.searchField.setResponder(query -> refreshList());
        this.addRenderableWidget(this.searchField);

        this.sortButton = this.addRenderableWidget(new Button(
            centerX + 18, 25, 68, 20,
            currentSort.getDisplayName(),
            btn -> {
                currentSort = currentSort.next();
                btn.setMessage(currentSort.getDisplayName());
                refreshList();
            },
            (btn, poseStack, mx, my) -> renderTooltip(poseStack, Component.translatable("keybindplus.tooltip.sort", currentSort.getDisplayName()), mx, my)
        ));

        this.openFolderButton = this.addRenderableWidget(new Button(
            centerX + 90, 25, 64, 20,
            Component.translatable("keybindplus.screen.open_folder"),
            btn -> onOpenFolder(),
            (btn, poseStack, mx, my) -> renderTooltip(poseStack, Component.translatable("keybindplus.tooltip.open_folder"), mx, my)
        ));

        this.profileList = new ProfileListWidget(this.minecraft, this,
            this.width, this.height, 48, this.height - 86, 28);
        this.addRenderableWidget(this.profileList);

        int btnY1 = this.height - 76;
        this.loadButton = this.addRenderableWidget(new Button(
            centerX - 154, btnY1, 100, 20,
            Component.translatable("keybindplus.screen.load"),
            btn -> onLoad(),
            (btn, poseStack, mx, my) -> renderTooltip(poseStack, Component.translatable("keybindplus.tooltip.load"), mx, my)
        ));

        this.editButton = this.addRenderableWidget(new Button(
            centerX - 50, btnY1, 100, 20,
            Component.translatable("keybindplus.screen.edit"),
            btn -> onEdit(),
            (btn, poseStack, mx, my) -> renderTooltip(poseStack, Component.translatable("keybindplus.tooltip.edit"), mx, my)
        ));

        this.undoButton = this.addRenderableWidget(new Button(
            centerX + 54, btnY1, 100, 20,
            Component.translatable("keybindplus.screen.undo"),
            btn -> onUndo(),
            (btn, poseStack, mx, my) -> renderTooltip(poseStack, Component.translatable("keybindplus.tooltip.undo"), mx, my)
        ));

        int btnY2 = this.height - 52;
        this.saveButton = this.addRenderableWidget(new Button(
            centerX - 154, btnY2, 74, 20,
            Component.translatable("keybindplus.screen.save"),
            btn -> onSave(),
            (btn, poseStack, mx, my) -> renderTooltip(poseStack, Component.translatable("keybindplus.tooltip.save"), mx, my)
        ));

        this.duplicateButton = this.addRenderableWidget(new Button(
            centerX - 76, btnY2, 74, 20,
            Component.translatable("keybindplus.screen.duplicate"),
            btn -> onDuplicate(),
            (btn, poseStack, mx, my) -> renderTooltip(poseStack, Component.translatable("keybindplus.tooltip.duplicate"), mx, my)
        ));

        this.renameButton = this.addRenderableWidget(new Button(
            centerX + 2, btnY2, 74, 20,
            Component.translatable("keybindplus.screen.rename"),
            btn -> onRename(),
            (btn, poseStack, mx, my) -> renderTooltip(poseStack, Component.translatable("keybindplus.tooltip.rename"), mx, my)
        ));

        this.deleteButton = this.addRenderableWidget(new Button(
            centerX + 80, btnY2, 74, 20,
            Component.translatable("keybindplus.screen.delete"),
            btn -> onDelete(),
            (btn, poseStack, mx, my) -> renderTooltip(poseStack, Component.translatable("keybindplus.tooltip.delete"), mx, my)
        ));

        int btnY3 = this.height - 28;
        this.compareButton = this.addRenderableWidget(new Button(
            centerX - 153, btnY3, 58, 20,
            Component.translatable("keybindplus.screen.compare"),
            btn -> onCompare(),
            (btn, poseStack, mx, my) -> renderTooltip(poseStack, Component.translatable("keybindplus.tooltip.compare"), mx, my)
        ));

        this.setDefaultButton = this.addRenderableWidget(new Button(
            centerX - 91, btnY3, 58, 20,
            Component.translatable("keybindplus.screen.set_default"),
            btn -> onSetDefault(),
            (btn, poseStack, mx, my) -> {
                KeybindProfile sel = profileList != null ? profileList.getSelectedProfile() : null;
                boolean isDef = sel != null && sel.isDefault();
                renderTooltip(poseStack, Component.translatable(isDef ? "keybindplus.tooltip.unset_default" : "keybindplus.tooltip.set_default"), mx, my);
            }
        ));

        this.importButton = this.addRenderableWidget(new Button(
            centerX - 29, btnY3, 58, 20,
            Component.translatable("keybindplus.screen.import"),
            btn -> onImport(),
            (btn, poseStack, mx, my) -> renderTooltip(poseStack, Component.translatable("keybindplus.tooltip.import"), mx, my)
        ));

        this.exportButton = this.addRenderableWidget(new Button(
            centerX + 33, btnY3, 58, 20,
            Component.translatable("keybindplus.screen.export"),
            btn -> onExport(),
            (btn, poseStack, mx, my) -> renderTooltip(poseStack, Component.translatable("keybindplus.tooltip.export"), mx, my)
        ));

        this.doneButton = this.addRenderableWidget(new Button(
            centerX + 95, btnY3, 58, 20,
            Component.translatable("keybindplus.screen.done"),
            btn -> this.onClose()
        ));

        refreshList();
    }

    public String getSearchQuery() {
        return searchField != null ? searchField.getValue() : "";
    }

    private void refreshList() {
        ProfileManager pm = ProfileManager.get();
        String query = searchField != null ? searchField.getValue() : "";
        List<KeybindProfile> profiles = new ArrayList<>(pm.searchProfiles(query));

        if (currentSort == SortMode.IMPORTED) {
            profiles = profiles.stream().filter(KeybindProfile::isImported).collect(Collectors.toList());
        } else if (currentSort == SortMode.LOCAL) {
            profiles = profiles.stream().filter(p -> !p.isImported()).collect(Collectors.toList());
        }

        switch (currentSort) {
            case AZ, IMPORTED, LOCAL -> profiles.sort(Comparator.comparing(KeybindProfile::getName, String.CASE_INSENSITIVE_ORDER));
            case ZA -> profiles.sort(Comparator.comparing(KeybindProfile::getName, String.CASE_INSENSITIVE_ORDER).reversed());
            case NEWEST -> profiles.sort((a, b) -> {
                Instant tA = a.getUpdatedAt() != null ? a.getUpdatedAt() : a.getCreatedAt();
                Instant tB = b.getUpdatedAt() != null ? b.getUpdatedAt() : b.getCreatedAt();
                if (tA == null && tB == null) return 0;
                if (tA == null) return 1;
                if (tB == null) return -1;
                return tB.compareTo(tA);
            });
            case OLDEST -> profiles.sort((a, b) -> {
                Instant tA = a.getUpdatedAt() != null ? a.getUpdatedAt() : a.getCreatedAt();
                Instant tB = b.getUpdatedAt() != null ? b.getUpdatedAt() : b.getCreatedAt();
                if (tA == null && tB == null) return 0;
                if (tA == null) return 1;
                if (tB == null) return -1;
                return tA.compareTo(tB);
            });
        }

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
        if (setDefaultButton != null) {
            setDefaultButton.active = hasSelection;
            if (hasSelection) {
                KeybindProfile selected = profileList.getSelectedProfile();
                boolean isDef = selected != null && selected.isDefault();
                if (isDef) {
                    setDefaultButton.setMessage(Component.translatable("keybindplus.screen.unset_default"));
                } else {
                    setDefaultButton.setMessage(Component.translatable("keybindplus.screen.set_default"));
                }
            }
        }
        if (undoButton != null) undoButton.active = KeybindApplier.hasUndoSnapshot();
    }

    private void onOpenFolder() {
        Path configDir = KeybindPlusConfig.getConfigDir();
        Util.getPlatform().openFile(configDir.toFile());
    }

    private void onSave() {
        this.minecraft.execute(() -> this.minecraft.setScreen(new SaveProfilePopup(
            this,
            Component.translatable("keybindplus.popup.save_title"),
            "",
            name -> {
                ProfileManager pm = ProfileManager.get();
                if (pm.profileExists(name)) {
                    this.minecraft.setScreen(new ConfirmPopup(this,
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
            }
        )));
    }

    public void onLoad() {
        KeybindProfile profile = profileList.getSelectedProfile();
        if (profile == null) return;

        List<KeyConflict> conflicts = ConflictDetector.detect(profile);
        if (!conflicts.isEmpty()) {
            this.minecraft.setScreen(new ConflictWarningPopup(this, profile, conflicts, () -> {
                applyProfile(profile);
            }));
        } else {
            applyProfile(profile);
        }
    }

    private void onEdit() {
        KeybindProfile profile = profileList.getSelectedProfile();
        if (profile == null) return;
        this.minecraft.setScreen(new KeybindEditorScreen(this, profile, false));
    }

    private void applyProfile(KeybindProfile profile) {
        ProfileManager.get().createAutoBackup();
        KeybindApplier.apply(profile);
        refreshList();
    }

    private void onUndo() {
        if (!KeybindApplier.hasUndoSnapshot()) return;
        KeybindApplier.undoLastApply();
        refreshList();
    }

    private void onDelete() {
        KeybindProfile profile = profileList.getSelectedProfile();
        if (profile == null) return;

        this.minecraft.setScreen(new ConfirmPopup(this,
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
        this.minecraft.execute(() -> this.minecraft.setScreen(new SaveProfilePopup(
            this,
            Component.translatable("keybindplus.popup.rename_title"),
            oldName,
            newName -> {
                if (newName.equals(oldName)) return;
                boolean success = ProfileManager.get().renameProfile(oldName, newName);
                if (success) {
                    refreshList();
                }
            }
        )));
    }

    private void onDuplicate() {
        KeybindProfile profile = profileList.getSelectedProfile();
        if (profile == null) return;

        String defaultCopyName = profile.getName() + " Copy";
        this.minecraft.execute(() -> this.minecraft.setScreen(new SaveProfilePopup(
            this,
            Component.translatable("keybindplus.popup.duplicate_title"),
            defaultCopyName,
            newName -> {
                KeybindProfile copy = ProfileManager.get().duplicateProfile(profile.getName(), newName);
                if (copy != null) {
                    refreshList();
                }
            }
        )));
    }

    private void onCompare() {
        KeybindProfile selected = profileList.getSelectedProfile();
        if (selected == null) return;

        List<KeybindProfile> otherProfiles = ProfileManager.get().listProfiles().stream()
            .filter(p -> !p.getName().equals(selected.getName()))
            .collect(Collectors.toList());

        if (otherProfiles.isEmpty()) {
            ToastNotification.toast("keybindplus.toast.error_title", "keybindplus.toast.need_two_profiles");
            return;
        }

        this.minecraft.setScreen(new CompareSelectPopup(
            this, selected, otherProfiles,
            target -> this.minecraft.setScreen(new CompareScreen(this, selected, target))
        ));
    }

    private void onSetDefault() {
        KeybindProfile profile = profileList.getSelectedProfile();
        if (profile == null) return;
        if (profile.isDefault()) {
            ProfileManager.get().setDefaultProfile("");
            ToastNotification.toast("keybindplus.toast.default_cleared_title", "keybindplus.toast.default_cleared_desc");
        } else {
            ProfileManager.get().setDefaultProfile(profile.getName());
            ToastNotification.toast("keybindplus.toast.default_set_title", "keybindplus.toast.default_set_desc", profile.getName());
        }
        refreshList();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        if (profileList != null && profileList.isEmpty()) {
            String query = getSearchQuery();
            Component emptyText = (query != null && !query.isBlank())
                ? Component.translatable("keybindplus.screen.no_search_results")
                : Component.translatable("keybindplus.screen.empty");
            drawCenteredString(
                poseStack,
                this.font,
                emptyText.copy().withStyle(net.minecraft.ChatFormatting.GRAY),
                this.width / 2,
                this.height / 2 - 16,
                0xAAAAAA
            );
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.searchField != null && !this.searchField.isFocused()) {
            if (KeybindPlusClient.OPEN_GUI_KEY.matches(keyCode, scanCode)) {
                this.onClose();
                return true;
            }

            KeybindProfile selected = profileList != null ? profileList.getSelectedProfile() : null;

            if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (selected != null) {
                    onDelete();
                    return true;
                }
            }

            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                if (selected != null) {
                    onLoad();
                    return true;
                }
            }

            boolean ctrl = hasControlDown();

            if (ctrl) {
                if (keyCode == GLFW.GLFW_KEY_S) {
                    onSave();
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_Z) {
                    if (KeybindApplier.hasUndoSnapshot()) {
                        onUndo();
                        return true;
                    }
                }
                if (keyCode == GLFW.GLFW_KEY_D) {
                    if (selected != null) {
                        onDuplicate();
                        return true;
                    }
                }
            } else {
                if (keyCode == GLFW.GLFW_KEY_E) {
                    if (selected != null) {
                        onEdit();
                        return true;
                    }
                }
                if (keyCode == GLFW.GLFW_KEY_C) {
                    if (selected != null) {
                        onCompare();
                        return true;
                    }
                }
                if (keyCode == GLFW.GLFW_KEY_R) {
                    if (selected != null) {
                        onRename();
                        return true;
                    }
                }
                if (keyCode == GLFW.GLFW_KEY_D) {
                    if (selected != null) {
                        onSetDefault();
                        return true;
                    }
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

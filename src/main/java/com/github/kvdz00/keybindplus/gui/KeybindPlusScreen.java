package com.github.kvdz00.keybindplus.gui;

import com.github.kvdz00.keybindplus.KeybindPlus;
import com.github.kvdz00.keybindplus.config.KeybindPlusConfig;
import com.github.kvdz00.keybindplus.keybind.ApplyResult;
import com.github.kvdz00.keybindplus.keybind.ConflictDetector;
import com.github.kvdz00.keybindplus.keybind.KeyConflict;
import com.github.kvdz00.keybindplus.keybind.KeybindApplier;
import com.github.kvdz00.keybindplus.profile.KeybindProfile;
import com.github.kvdz00.keybindplus.profile.ProfileManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class KeybindPlusScreen extends GuiScreen {
    private final GuiScreen parent;
    private ProfileListWidget listWidget;
    private GuiTextField searchField;
    private SortMode currentSort = SortMode.AZ;

    // Buttons
    private GuiButton loadBtn;
    private GuiButton saveBtn;
    private GuiButton editBtn;
    private GuiButton compareBtn;
    private GuiButton deleteBtn;
    private GuiButton renameBtn;
    private GuiButton copyBtn;
    private GuiButton defaultBtn;
    private GuiButton exportBtn;
    private GuiButton importBtn;
    private GuiButton folderBtn;
    private GuiButton undoBtn;
    private GuiButton sortBtn;

    public KeybindPlusScreen(GuiScreen parent) {
        this.parent = parent;
    }

    public enum SortMode {
        AZ("keybindplus.sort.az"),
        ZA("keybindplus.sort.za"),
        NEWEST("keybindplus.sort.newest"),
        OLDEST("keybindplus.sort.oldest"),
        IMPORTED("keybindplus.sort.imported"),
        LOCAL("keybindplus.sort.local");

        private final String key;
        SortMode(String key) { this.key = key; }
        public String getDisplayName() { return I18n.format(key); }
        public SortMode next() {
            SortMode[] vals = values();
            return vals[(ordinal() + 1) % vals.length];
        }
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        int topY = 32;
        int bottomY = this.height - 40;
        int listWidth = this.width - 150;

        listWidget = new ProfileListWidget(this, this.mc, listWidth, this.height, topY, bottomY, 28);
        listWidget.setSlotXBoundsFromLeft(10);

        searchField = new GuiTextField(0, this.fontRendererObj, 10, 10, 150, 16);
        searchField.setFocused(false);

        sortBtn = new GuiButton(100, 165, 8, 80, 20, I18n.format("keybindplus.tooltip.sort", currentSort.getDisplayName()));
        this.buttonList.add(sortBtn);

        int rightX = this.width - 130;
        int btnW = 120;
        int btnH = 18;
        int y = 32;

        loadBtn = new GuiButton(1, rightX, y, btnW, btnH, I18n.format("keybindplus.screen.load"));
        saveBtn = new GuiButton(2, rightX, y += 20, btnW, btnH, I18n.format("keybindplus.screen.save"));
        editBtn = new GuiButton(3, rightX, y += 20, btnW, btnH, I18n.format("keybindplus.screen.edit"));
        compareBtn = new GuiButton(4, rightX, y += 20, btnW, btnH, I18n.format("keybindplus.screen.compare"));
        deleteBtn = new GuiButton(5, rightX, y += 20, btnW, btnH, I18n.format("keybindplus.screen.delete"));
        renameBtn = new GuiButton(6, rightX, y += 20, btnW, btnH, I18n.format("keybindplus.screen.rename"));
        copyBtn = new GuiButton(7, rightX, y += 20, btnW, btnH, I18n.format("keybindplus.screen.duplicate"));
        defaultBtn = new GuiButton(8, rightX, y += 20, btnW, btnH, I18n.format("keybindplus.screen.set_default"));
        exportBtn = new GuiButton(9, rightX, y += 20, btnW, btnH, I18n.format("keybindplus.screen.export"));
        importBtn = new GuiButton(10, rightX, y += 20, btnW, btnH, I18n.format("keybindplus.screen.import"));
        folderBtn = new GuiButton(11, rightX, y += 20, btnW, btnH, I18n.format("keybindplus.screen.open_folder"));
        undoBtn = new GuiButton(12, rightX, y += 20, btnW, btnH, I18n.format("keybindplus.screen.undo"));

        this.buttonList.add(loadBtn);
        this.buttonList.add(saveBtn);
        this.buttonList.add(editBtn);
        this.buttonList.add(compareBtn);
        this.buttonList.add(deleteBtn);
        this.buttonList.add(renameBtn);
        this.buttonList.add(copyBtn);
        this.buttonList.add(defaultBtn);
        this.buttonList.add(exportBtn);
        this.buttonList.add(importBtn);
        this.buttonList.add(folderBtn);
        this.buttonList.add(undoBtn);

        this.buttonList.add(new GuiButton(13, this.width / 2 - 75, this.height - 28, 150, 20, I18n.format("keybindplus.screen.done")));

        refreshList();
        updateButtonStates();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    public void onProfileSelectionChanged() {
        updateButtonStates();
    }

    public void onProfileDoubleClicked() {
        KeybindProfile selected = listWidget.getSelectedProfile();
        if (selected != null) {
            applyProfile(selected);
        }
    }

    private void updateButtonStates() {
        KeybindProfile selected = listWidget.getSelectedProfile();
        boolean hasSel = (selected != null);

        loadBtn.enabled = hasSel;
        editBtn.enabled = hasSel;
        compareBtn.enabled = hasSel && ProfileManager.get().listProfiles().size() >= 2;
        deleteBtn.enabled = hasSel;
        renameBtn.enabled = hasSel;
        copyBtn.enabled = hasSel;
        exportBtn.enabled = hasSel;
        undoBtn.enabled = KeybindApplier.hasUndoSnapshot();

        if (hasSel) {
            defaultBtn.enabled = true;
            defaultBtn.displayString = selected.isDefault() ?
                I18n.format("keybindplus.screen.unset_default") :
                I18n.format("keybindplus.screen.set_default");
        } else {
            defaultBtn.enabled = false;
            defaultBtn.displayString = I18n.format("keybindplus.screen.set_default");
        }
    }

    public void refreshList() {
        String query = searchField.getText();
        List<KeybindProfile> profiles = ProfileManager.get().searchProfiles(query);
        List<KeybindProfile> sorted = new ArrayList<>(profiles);

        switch (currentSort) {
            case AZ:
                sorted.sort(Comparator.comparing(KeybindProfile::getName, String.CASE_INSENSITIVE_ORDER));
                break;
            case ZA:
                sorted.sort((a, b) -> b.getName().compareToIgnoreCase(a.getName()));
                break;
            case NEWEST:
                sorted.sort((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()));
                break;
            case OLDEST:
                sorted.sort(Comparator.comparing(KeybindProfile::getUpdatedAt));
                break;
            case IMPORTED:
                sorted = sorted.stream().filter(KeybindProfile::isImported).collect(Collectors.toList());
                break;
            case LOCAL:
                sorted = sorted.stream().filter(p -> !p.isImported()).collect(Collectors.toList());
                break;
        }

        listWidget.setProfiles(sorted);
        updateButtonStates();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        KeybindProfile selected = listWidget.getSelectedProfile();

        switch (button.id) {
            case 100: // Sort
                currentSort = currentSort.next();
                sortBtn.displayString = I18n.format("keybindplus.tooltip.sort", currentSort.getDisplayName());
                refreshList();
                break;
            case 1: // Load
                if (selected != null) applyProfile(selected);
                break;
            case 2: // Save
                this.mc.displayGuiScreen(new SaveProfilePopup(this, I18n.format("keybindplus.popup.save_title"), "", name -> {
                    ProfileManager.get().createProfileFromCurrent(name);
                    ToastNotification.toast("keybindplus.toast.saved_title", "keybindplus.toast.saved_desc", name);
                    refreshList();
                }, null));
                break;
            case 3: // Edit
                if (selected != null) {
                    this.mc.displayGuiScreen(new KeybindEditorScreen(this, selected));
                }
                break;
            case 4: // Compare
                if (selected != null) {
                    this.mc.displayGuiScreen(new CompareSelectPopup(this, selected, target -> {
                        this.mc.displayGuiScreen(new CompareScreen(this, selected, target));
                    }));
                }
                break;
            case 5: // Delete
                if (selected != null) {
                    String msg = I18n.format("keybindplus.popup.confirm_delete", selected.getName());
                    this.mc.displayGuiScreen(new ConfirmPopup(this, msg, () -> {
                        ProfileManager.get().deleteProfile(selected.getName());
                        ToastNotification.toast("keybindplus.toast.deleted_title", "keybindplus.toast.deleted_desc", selected.getName());
                        refreshList();
                    }, null));
                }
                break;
            case 6: // Rename
                if (selected != null) {
                    this.mc.displayGuiScreen(new SaveProfilePopup(this, I18n.format("keybindplus.popup.rename_title"), selected.getName(), newName -> {
                        ProfileManager.get().renameProfile(selected.getName(), newName);
                        ToastNotification.toast("keybindplus.toast.renamed_title", "keybindplus.toast.renamed_desc", selected.getName(), newName);
                        refreshList();
                    }, null));
                }
                break;
            case 7: // Duplicate
                if (selected != null) {
                    this.mc.displayGuiScreen(new SaveProfilePopup(this, I18n.format("keybindplus.popup.duplicate_title"), selected.getName() + "_copy", newName -> {
                        ProfileManager.get().duplicateProfile(selected.getName(), newName);
                        ToastNotification.toast("keybindplus.toast.duplicated_title", "keybindplus.toast.duplicated_desc", newName);
                        refreshList();
                    }, null));
                }
                break;
            case 8: // Set/Unset Default
                if (selected != null) {
                    if (selected.isDefault()) {
                        ProfileManager.get().clearDefaultProfile();
                        ToastNotification.toast("keybindplus.toast.default_cleared_title", "keybindplus.toast.default_cleared_desc");
                    } else {
                        ProfileManager.get().setDefaultProfile(selected.getName());
                        ToastNotification.toast("keybindplus.toast.default_set_title", "keybindplus.toast.default_set_desc", selected.getName());
                    }
                    refreshList();
                }
                break;
            case 9: // Export
                if (selected != null) {
                    Path p = ProfileManager.get().exportProfile(selected.getName());
                    if (p != null) {
                        ToastNotification.toast("keybindplus.toast.export_title", "keybindplus.toast.export_desc", p.getFileName().toString());
                    }
                }
                break;
            case 10: // Import
                openImportChooser();
                break;
            case 11: // Folder
                openConfigFolder();
                break;
            case 12: // Undo
                ApplyResult res = KeybindApplier.undoLastApply();
                if (res.getAppliedCount() > 0) {
                    ToastNotification.toast("keybindplus.toast.undo_title", "keybindplus.toast.undo_desc");
                } else {
                    ToastNotification.toast("keybindplus.toast.error_title", "keybindplus.toast.no_undo");
                }
                refreshList();
                break;
            case 13: // Done
                this.mc.displayGuiScreen(parent);
                break;
        }
    }

    private void applyProfile(KeybindProfile profile) {
        List<KeyConflict> conflicts = ConflictDetector.detect(profile);
        if (!conflicts.isEmpty()) {
            this.mc.displayGuiScreen(new ConflictWarningPopup(this, conflicts, () -> {
                doApply(profile);
            }, () -> {
                this.mc.displayGuiScreen(new KeybindEditorScreen(this, profile));
            }));
        } else {
            doApply(profile);
        }
    }

    private void doApply(KeybindProfile profile) {
        ProfileManager.get().createBackup();
        ApplyResult res = KeybindApplier.apply(profile);
        ToastNotification.toast("keybindplus.toast.loaded_title", "keybindplus.toast.loaded_desc", profile.getName());
        refreshList();
    }

    private void openImportChooser() {
        new Thread(() -> {
            try {
                JFileChooser chooser = new JFileChooser(KeybindPlusConfig.getImportsDir().toFile());
                chooser.setFileFilter(new FileNameExtensionFilter("KeybindPlus Profile (*.json)", "json"));
                int ret = chooser.showOpenDialog(null);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    if (file != null && file.exists()) {
                        mc.addScheduledTask(() -> {
                            KeybindProfile p = ProfileManager.get().importProfile(file.toPath());
                            if (p != null) {
                                ToastNotification.toast("keybindplus.toast.import_title", "keybindplus.toast.import_desc", p.getName());
                                refreshList();
                            } else {
                                ToastNotification.toast("keybindplus.toast.error_title", "keybindplus.toast.import_invalid");
                            }
                        });
                    }
                }
            } catch (Exception e) {
                // Fallback: list imports directory files
                mc.addScheduledTask(() -> {
                    List<Path> imports = ProfileManager.get().listImportFiles();
                    if (!imports.isEmpty()) {
                        KeybindProfile p = ProfileManager.get().importProfile(imports.get(0));
                        if (p != null) {
                            ToastNotification.toast("keybindplus.toast.import_title", "keybindplus.toast.import_desc", p.getName());
                            refreshList();
                        }
                    } else {
                        ToastNotification.toast("keybindplus.toast.error_title", "keybindplus.toast.import_invalid");
                    }
                });
            }
        }).start();
    }

    private void openConfigFolder() {
        try {
            File dir = KeybindPlusConfig.getConfigDir().toFile();
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(dir);
            } else {
                Sys.openURL("file://" + dir.getAbsolutePath());
            }
            ToastNotification.toast("keybindplus.toast.open_folder", null);
        } catch (Exception e) {
            KeybindPlus.LOGGER.error("Failed to open directory: {}", e.getMessage());
        }
    }

    @Override
    public void updateScreen() {
        searchField.updateCursorCounter();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (listWidget != null) {
            listWidget.handleMouseInput();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { // ESC
            this.mc.displayGuiScreen(parent);
            return;
        }

        if (searchField.isFocused()) {
            searchField.textboxKeyTyped(typedChar, keyCode);
            refreshList();
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        searchField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        if (listWidget != null) {
            listWidget.drawScreen(mouseX, mouseY, partialTicks);
        }

        drawCenteredString(this.fontRendererObj, I18n.format("keybindplus.screen.title"), this.width / 2, 12, 0xFFFFFF);
        searchField.drawTextBox();

        if (searchField.getText().isEmpty() && !searchField.isFocused()) {
            drawString(this.fontRendererObj, I18n.format("keybindplus.screen.search"), 14, 14, 0x888888);
        }

        if (listWidget.getSize() == 0) {
            String emptyMsg = searchField.getText().isEmpty() ?
                I18n.format("keybindplus.screen.empty") :
                I18n.format("keybindplus.screen.no_search_results");
            drawCenteredString(this.fontRendererObj, emptyMsg, (this.width - 130) / 2, this.height / 2, 0xAAAAAA);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}

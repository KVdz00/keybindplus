package com.github.kvdz00.keybindplus.gui;

import com.github.kvdz00.keybindplus.keybind.ApplyResult;
import com.github.kvdz00.keybindplus.keybind.ConflictDetector;
import com.github.kvdz00.keybindplus.keybind.KeyConflict;
import com.github.kvdz00.keybindplus.keybind.KeybindApplier;
import com.github.kvdz00.keybindplus.keybind.KeybindCapture;
import com.github.kvdz00.keybindplus.profile.KeybindProfile;
import com.github.kvdz00.keybindplus.profile.ProfileManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class KeybindEditorScreen extends GuiScreen {
    private final GuiScreen parent;
    private final KeybindProfile profile;
    private final Map<String, String> workingBinds;
    private KeybindEditListWidget listWidget;
    private GuiTextField searchField;
    private boolean conflictsOnly = false;
    private GuiButton filterBtn;
    private KeybindEditListWidget.KeybindEntry listeningEntry = null;

    public KeybindEditorScreen(GuiScreen parent, KeybindProfile profile) {
        this.parent = parent;
        this.profile = profile;
        this.workingBinds = new LinkedHashMap<>(profile.getKeybinds());
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        int topY = 32;
        int bottomY = this.height - 40;

        listWidget = new KeybindEditListWidget(this, this.mc, this.width, this.height, topY, bottomY, 22);

        searchField = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 180, 10, 150, 16);
        searchField.setFocused(false);

        filterBtn = new GuiButton(100, this.width / 2 - 20, 8, 110, 20, getFilterButtonText());
        this.buttonList.add(filterBtn);

        this.buttonList.add(new GuiButton(1, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("keybindplus.editor.save_apply")));
        this.buttonList.add(new GuiButton(2, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("keybindplus.popup.cancel")));

        refreshList();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    private String getFilterButtonText() {
        int count = conflictsOnly ? getConflictingActions().size() : workingBinds.size();
        return conflictsOnly ?
            I18n.format("keybindplus.editor.filter_conflicts", count) :
            I18n.format("keybindplus.editor.filter_all", count);
    }

    private Set<String> getConflictingActions() {
        KeybindProfile temp = new KeybindProfile(profile.getName(), workingBinds);
        List<KeyConflict> conflicts = ConflictDetector.detect(temp);
        Set<String> set = new HashSet<>();
        for (KeyConflict c : conflicts) {
            set.addAll(c.getConflictingActions());
        }
        return set;
    }

    public void refreshList() {
        Set<String> conflicts = getConflictingActions();
        String query = searchField.getText().toLowerCase().trim();

        List<KeybindEditListWidget.KeybindEntry> entries = new ArrayList<>();
        for (Map.Entry<String, String> entry : workingBinds.entrySet()) {
            String action = entry.getKey();
            String key = entry.getValue();

            if (conflictsOnly && !conflicts.contains(action)) {
                continue;
            }

            if (!query.isEmpty()) {
                String translated = I18n.format(action).toLowerCase();
                if (!action.toLowerCase().contains(query) && !translated.contains(query)) {
                    continue;
                }
            }

            entries.add(new KeybindEditListWidget.KeybindEntry(action, key));
        }

        listWidget.setEntries(entries, conflicts);
        if (filterBtn != null) {
            filterBtn.displayString = getFilterButtonText();
        }
    }

    public void onStartListeningKey(KeybindEditListWidget.KeybindEntry entry) {
        this.listeningEntry = entry;
    }

    public void onKeyChanged(KeybindEditListWidget.KeybindEntry entry) {
        workingBinds.put(entry.getActionId(), entry.getKey());
        this.listeningEntry = null;
        refreshList();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 100) {
            conflictsOnly = !conflictsOnly;
            refreshList();
        } else if (button.id == 1) {
            profile.setKeybinds(workingBinds);
            ProfileManager.get().saveProfile(profile);
            ProfileManager.get().createBackup();
            ApplyResult res = KeybindApplier.apply(profile);
            ToastNotification.toast("keybindplus.toast.saved_title", "keybindplus.toast.saved_desc", profile.getName());
            this.mc.displayGuiScreen(parent);
        } else if (button.id == 2) {
            this.mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (listeningEntry != null) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                listeningEntry.setKey("NONE");
            } else {
                String keyStr = KeybindCapture.getKeySaveString(keyCode);
                listeningEntry.setKey(keyStr);
            }
            onKeyChanged(listeningEntry);
            return;
        }

        if (keyCode == Keyboard.KEY_ESCAPE) {
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
        if (listeningEntry != null) {
            int code = mouseButton - 100;
            String keyStr = KeybindCapture.getKeySaveString(code);
            listeningEntry.setKey(keyStr);
            onKeyChanged(listeningEntry);
            return;
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
        searchField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (listWidget != null) {
            listWidget.handleMouseInput();
        }
    }

    @Override
    public void updateScreen() {
        searchField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        if (listWidget != null) {
            listWidget.drawScreen(mouseX, mouseY, partialTicks);
        }

        drawCenteredString(this.fontRendererObj, I18n.format("keybindplus.editor.title", profile.getName()), this.width / 2, 12, 0xFFFFFF);
        searchField.drawTextBox();

        if (searchField.getText().isEmpty() && !searchField.isFocused()) {
            drawString(this.fontRendererObj, I18n.format("keybindplus.editor.search"), this.width / 2 - 176, 14, 0x888888);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}

package com.github.kvdz00.keybindplus.gui;

import com.github.kvdz00.keybindplus.keybind.KeybindCapture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.resources.I18n;

import java.util.*;

public class KeybindEditListWidget extends GuiSlot {
    private final KeybindEditorScreen parent;
    private final List<KeybindEntry> entries = new ArrayList<>();
    private final Set<String> conflictingActions = new HashSet<>();
    private KeybindEntry activeListeningEntry = null;

    public KeybindEditListWidget(KeybindEditorScreen parent, Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
        super(mc, width, height, top, bottom, slotHeight);
        this.parent = parent;
    }

    public void setEntries(List<KeybindEntry> newEntries, Set<String> conflicts) {
        this.entries.clear();
        if (newEntries != null) {
            this.entries.addAll(newEntries);
        }
        this.conflictingActions.clear();
        if (conflicts != null) {
            this.conflictingActions.addAll(conflicts);
        }
    }

    public KeybindEntry getActiveListeningEntry() {
        return activeListeningEntry;
    }

    public void setActiveListeningEntry(KeybindEntry entry) {
        this.activeListeningEntry = entry;
    }

    @Override
    protected int getSize() {
        return entries.size();
    }

    @Override
    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
        if (slotIndex < 0 || slotIndex >= entries.size()) return;
        KeybindEntry entry = entries.get(slotIndex);

        int listLeft = (this.width - getListWidth()) / 2;
        int btnKeyX = listLeft + getListWidth() - 120;
        int btnUnbindX = listLeft + getListWidth() - 35;

        if (mouseX >= btnKeyX && mouseX <= btnKeyX + 80) {
            this.activeListeningEntry = entry;
            parent.onStartListeningKey(entry);
        } else if (mouseX >= btnUnbindX && mouseX <= btnUnbindX + 30) {
            entry.setKey("NONE");
            parent.onKeyChanged(entry);
        }
    }

    @Override
    protected boolean isSelected(int slotIndex) {
        return false;
    }

    @Override
    protected void drawBackground() {}

    @Override
    protected int getContentHeight() {
        return getSize() * slotHeight;
    }

    @Override
    protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn) {
        if (entryID < 0 || entryID >= entries.size()) return;

        KeybindEntry entry = entries.get(entryID);
        FontRenderer font = mc.fontRendererObj;

        boolean isConflict = conflictingActions.contains(entry.getActionId());
        boolean isListening = (activeListeningEntry == entry);

        int nameColor = isConflict ? 0xFF5555 : 0xFFFFFF;
        String actionDisplay = I18n.format(entry.getActionId());
        font.drawString(actionDisplay, insideLeft + 4, yPos + 6, nameColor);

        int btnKeyX = insideLeft + getListWidth() - 120;
        int btnKeyY = yPos + 2;
        int btnKeyW = 80;
        int btnKeyH = 16;

        int keyBgColor = isListening ? 0xFF888800 : (isConflict ? 0xFF882222 : 0xFF333333);
        Gui.drawRect(btnKeyX, btnKeyY, btnKeyX + btnKeyW, btnKeyY + btnKeyH, keyBgColor);

        String keyText = isListening ? "> Press Key <" : entry.getDisplayKey();
        int keyTextColor = isListening ? 0xFFFFAA : (isConflict ? 0xFFAAAA : 0xFFFFFF);
        int textX = btnKeyX + (btnKeyW - font.getStringWidth(keyText)) / 2;
        font.drawString(keyText, textX, btnKeyY + 4, keyTextColor);

        int btnUnbindX = insideLeft + getListWidth() - 35;
        int btnUnbindY = yPos + 2;
        int btnUnbindW = 30;
        int btnUnbindH = 16;

        Gui.drawRect(btnUnbindX, btnUnbindY, btnUnbindX + btnUnbindW, btnUnbindY + btnUnbindH, 0xFF444444);
        String unbindText = "X";
        int unbindTextX = btnUnbindX + (btnUnbindW - font.getStringWidth(unbindText)) / 2;
        font.drawString(unbindText, unbindTextX, btnUnbindY + 4, 0xCCCCCC);
    }

    public static class KeybindEntry {
        private final String actionId;
        private String key;

        public KeybindEntry(String actionId, String key) {
            this.actionId = actionId;
            this.key = (key != null && !key.isEmpty()) ? key : "NONE";
        }

        public String getActionId() { return actionId; }
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }

        public String getDisplayKey() {
            if ("NONE".equalsIgnoreCase(key) || "0".equals(key)) return "NONE";
            int code = KeybindCapture.parseKeyCode(key);
            return KeybindCapture.getKeyDisplay(code);
        }
    }
}

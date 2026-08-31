package com.github.kvdz00.keybindplus.gui;

import com.github.kvdz00.keybindplus.keybind.KeybindCapture;
import com.github.kvdz00.keybindplus.profile.KeybindProfile;
import com.github.kvdz00.keybindplus.profile.ProfileManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.*;

public class CompareScreen extends GuiScreen {
    private final GuiScreen parent;
    private final KeybindProfile profileA;
    private final KeybindProfile profileB;
    private final Map<String, String> bindsA;
    private final Map<String, String> bindsB;
    private CompareSlotList listWidget;
    private GuiTextField searchField;
    private boolean diffsOnly = false;
    private GuiButton filterBtn;

    public CompareScreen(GuiScreen parent, KeybindProfile profileA, KeybindProfile profileB) {
        this.parent = parent;
        this.profileA = profileA;
        this.profileB = profileB;
        this.bindsA = new LinkedHashMap<>(profileA.getKeybinds());
        this.bindsB = new LinkedHashMap<>(profileB.getKeybinds());
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        int topY = 32;
        int bottomY = this.height - 40;

        listWidget = new CompareSlotList(this.mc, this.width, this.height, topY, bottomY, 22);

        searchField = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 180, 10, 150, 16);
        searchField.setFocused(false);

        filterBtn = new GuiButton(100, this.width / 2 - 20, 8, 120, 20, getFilterButtonText());
        this.buttonList.add(filterBtn);

        this.buttonList.add(new GuiButton(1, this.width / 2 - 75, this.height - 28, 150, 20, I18n.format("keybindplus.screen.done")));

        refreshList();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    private String getFilterButtonText() {
        int count = diffsOnly ? getDiffCount() : getAllActions().size();
        return diffsOnly ?
            I18n.format("keybindplus.compare.filter_diffs", count) :
            I18n.format("keybindplus.compare.filter_all", count);
    }

    private Set<String> getAllActions() {
        Set<String> all = new LinkedHashSet<>(bindsA.keySet());
        all.addAll(bindsB.keySet());
        return all;
    }

    private int getDiffCount() {
        int diffs = 0;
        for (String action : getAllActions()) {
            String a = bindsA.getOrDefault(action, "NONE");
            String b = bindsB.getOrDefault(action, "NONE");
            if (!a.equalsIgnoreCase(b)) {
                diffs++;
            }
        }
        return diffs;
    }

    public void refreshList() {
        String query = searchField.getText().toLowerCase().trim();
        List<CompareEntry> entries = new ArrayList<>();

        for (String action : getAllActions()) {
            String a = bindsA.getOrDefault(action, "NONE");
            String b = bindsB.getOrDefault(action, "NONE");
            boolean isDiff = !a.equalsIgnoreCase(b);

            if (diffsOnly && !isDiff) {
                continue;
            }

            if (!query.isEmpty()) {
                String translated = I18n.format(action).toLowerCase();
                if (!action.toLowerCase().contains(query) && !translated.contains(query)) {
                    continue;
                }
            }

            entries.add(new CompareEntry(action, a, b, isDiff));
        }

        listWidget.setEntries(entries);
        if (filterBtn != null) {
            filterBtn.displayString = getFilterButtonText();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 100) {
            diffsOnly = !diffsOnly;
            refreshList();
        } else if (button.id == 1) {
            this.mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
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

        String title = I18n.format("keybindplus.compare.title", profileA.getName(), profileB.getName());
        drawCenteredString(this.fontRendererObj, title, this.width / 2, 12, 0xFFFFFF);
        searchField.drawTextBox();

        if (searchField.getText().isEmpty() && !searchField.isFocused()) {
            drawString(this.fontRendererObj, I18n.format("keybindplus.compare.search"), this.width / 2 - 176, 14, 0x888888);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public static class CompareEntry {
        final String actionId;
        String keyA;
        String keyB;
        boolean isDiff;

        public CompareEntry(String actionId, String keyA, String keyB, boolean isDiff) {
            this.actionId = actionId;
            this.keyA = keyA;
            this.keyB = keyB;
            this.isDiff = isDiff;
        }

        public String getDisplayKeyA() {
            int code = KeybindCapture.parseKeyCode(keyA);
            return KeybindCapture.getKeyDisplay(code);
        }

        public String getDisplayKeyB() {
            int code = KeybindCapture.parseKeyCode(keyB);
            return KeybindCapture.getKeyDisplay(code);
        }
    }

    private class CompareSlotList extends GuiSlot {
        private final List<CompareEntry> entries = new ArrayList<>();

        public CompareSlotList(Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
            super(mc, width, height, top, bottom, slotHeight);
        }

        public void setEntries(List<CompareEntry> newEntries) {
            this.entries.clear();
            if (newEntries != null) {
                this.entries.addAll(newEntries);
            }
        }

        @Override
        protected int getSize() {
            return entries.size();
        }

        @Override
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
            if (slotIndex < 0 || slotIndex >= entries.size()) return;
            CompareEntry entry = entries.get(slotIndex);

            int listLeft = (this.width - getListWidth()) / 2;
            int copyToAX = listLeft + getListWidth() - 175;
            int copyToBX = listLeft + getListWidth() - 95;

            if (mouseX >= copyToAX && mouseX <= copyToAX + 35) {
                // Copy B's key to A
                bindsA.put(entry.actionId, entry.keyB);
                entry.keyA = entry.keyB;
                entry.isDiff = false;
                profileA.setKeybinds(bindsA);
                ProfileManager.get().saveProfile(profileA);
                ToastNotification.toast("keybindplus.toast.synced_title", "keybindplus.toast.synced_desc", I18n.format(entry.actionId), entry.getDisplayKeyA(), profileA.getName());
                refreshList();
            } else if (mouseX >= copyToBX && mouseX <= copyToBX + 35) {
                // Copy A's key to B
                bindsB.put(entry.actionId, entry.keyA);
                entry.keyB = entry.keyA;
                entry.isDiff = false;
                profileB.setKeybinds(bindsB);
                ProfileManager.get().saveProfile(profileB);
                ToastNotification.toast("keybindplus.toast.synced_title", "keybindplus.toast.synced_desc", I18n.format(entry.actionId), entry.getDisplayKeyB(), profileB.getName());
                refreshList();
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

            CompareEntry entry = entries.get(entryID);
            FontRenderer font = mc.fontRendererObj;

            int nameColor = entry.isDiff ? 0xFFFFAA : 0xFFFFFF;
            font.drawString(I18n.format(entry.actionId), insideLeft + 4, yPos + 6, nameColor);

            int rightBase = insideLeft + getListWidth();

            int keyAX = rightBase - 260;
            Gui.drawRect(keyAX, yPos + 2, keyAX + 70, yPos + 18, entry.isDiff ? 0xFF443322 : 0xFF333333);
            String dispA = entry.getDisplayKeyA();
            font.drawString(dispA, keyAX + (70 - font.getStringWidth(dispA)) / 2, yPos + 5, 0xFFFFFF);

            int copyAX = rightBase - 180;
            Gui.drawRect(copyAX, yPos + 2, copyAX + 35, yPos + 18, 0xFF444444);
            font.drawString("<- A", copyAX + (35 - font.getStringWidth("<- A")) / 2, yPos + 5, 0xCCCCCC);

            int copyBX = rightBase - 140;
            Gui.drawRect(copyBX, yPos + 2, copyBX + 35, yPos + 18, 0xFF444444);
            font.drawString("B ->", copyBX + (35 - font.getStringWidth("B ->")) / 2, yPos + 5, 0xCCCCCC);

            int keyBX = rightBase - 95;
            Gui.drawRect(keyBX, yPos + 2, keyBX + 70, yPos + 18, entry.isDiff ? 0xFF223344 : 0xFF333333);
            String dispB = entry.getDisplayKeyB();
            font.drawString(dispB, keyBX + (70 - font.getStringWidth(dispB)) / 2, yPos + 5, 0xFFFFFF);
        }
    }
}

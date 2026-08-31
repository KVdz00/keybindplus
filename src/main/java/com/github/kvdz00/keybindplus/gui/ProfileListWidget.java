package com.github.kvdz00.keybindplus.gui;

import com.github.kvdz00.keybindplus.profile.KeybindProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.resources.I18n;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ProfileListWidget extends GuiSlot {
    private final KeybindPlusScreen parent;
    private final List<KeybindProfile> profiles = new ArrayList<>();
    private int selectedIndex = -1;

    public ProfileListWidget(KeybindPlusScreen parent, Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
        super(mc, width, height, top, bottom, slotHeight);
        this.parent = parent;
    }

    public void setProfiles(List<KeybindProfile> newProfiles) {
        this.profiles.clear();
        if (newProfiles != null) {
            this.profiles.addAll(newProfiles);
        }
        if (selectedIndex >= profiles.size()) {
            selectedIndex = profiles.isEmpty() ? -1 : 0;
        }
    }

    public KeybindProfile getSelectedProfile() {
        if (selectedIndex >= 0 && selectedIndex < profiles.size()) {
            return profiles.get(selectedIndex);
        }
        return null;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int index) {
        this.selectedIndex = index;
    }

    @Override
    protected int getSize() {
        return profiles.size();
    }

    @Override
    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
        this.selectedIndex = slotIndex;
        parent.onProfileSelectionChanged();
        if (isDoubleClick) {
            parent.onProfileDoubleClicked();
        }
    }

    @Override
    protected boolean isSelected(int slotIndex) {
        return slotIndex == selectedIndex;
    }

    @Override
    protected void drawBackground() {}

    @Override
    protected int getContentHeight() {
        return getSize() * slotHeight;
    }

    @Override
    protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn) {
        if (entryID < 0 || entryID >= profiles.size()) return;

        KeybindProfile p = profiles.get(entryID);
        FontRenderer font = mc.fontRendererObj;

        int textColor = p.isLoaded() ? 0x55FF55 : 0xFFFFFF;
        font.drawString(p.getName(), insideLeft + 4, yPos + 3, textColor);

        // Meta info line: key count and updated time
        String keyCount = I18n.format("keybindplus.profile.keys", p.getKeybinds().size());
        String timeAgo = I18n.format("keybindplus.profile.updated", formatTimeAgo(p.getUpdatedAt()));
        String meta = keyCount + "  •  " + timeAgo;
        font.drawString(meta, insideLeft + 4, yPos + 15, 0x888888);

        // Badges on the right side of the slot
        int badgeX = insideLeft + getListWidth() - 55;
        if (p.isDefault()) {
            Gui.drawRect(badgeX - 2, yPos + 4, badgeX + 48, yPos + 14, 0x90FF8800);
            font.drawString("DEFAULT", badgeX + 2, yPos + 5, 0xFFFFFF);
            badgeX -= 52;
        }
        if (p.isLoaded()) {
            Gui.drawRect(badgeX - 2, yPos + 4, badgeX + 44, yPos + 14, 0x9000AA00);
            font.drawString("LOADED", badgeX + 2, yPos + 5, 0xFFFFFF);
            badgeX -= 48;
        }
        if (p.isImported()) {
            Gui.drawRect(badgeX - 2, yPos + 4, badgeX + 50, yPos + 14, 0x905555FF);
            font.drawString("IMPORTED", badgeX + 2, yPos + 5, 0xFFFFFF);
        }
    }

    private static String formatTimeAgo(Instant instant) {
        if (instant == null) return "unknown";
        long seconds = Instant.now().getEpochSecond() - instant.getEpochSecond();
        if (seconds < 60) return seconds + "s";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "m";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h";
        long days = hours / 24;
        return days + "d";
    }
}

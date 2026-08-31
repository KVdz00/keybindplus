package com.github.kvdz00.keybindplus.gui;

import com.github.kvdz00.keybindplus.profile.KeybindProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class ProfileListWidget extends ObjectSelectionList<ProfileListWidget.Entry> {
    private final KeybindPlusScreen parentScreen;

    public ProfileListWidget(Minecraft minecraft, KeybindPlusScreen parentScreen,
                             int width, int height, int top, int itemHeight) {
        super(minecraft, width, height, top, itemHeight);
        this.parentScreen = parentScreen;
    }

    @Override
    public int getRowWidth() {
        return Math.min(308, this.width - 20);
    }

    public boolean isEmpty() {
        return this.getItemCount() == 0;
    }

    public void updateEntries(List<KeybindProfile> profiles) {
        KeybindProfile prevSelected = getSelectedProfile();
        this.clearEntries();
        Entry toSelect = null;
        for (KeybindProfile profile : profiles) {
            Entry entry = new Entry(profile);
            this.addEntry(entry);
            if (prevSelected != null && profile.getName().equals(prevSelected.getName())) {
                toSelect = entry;
            }
        }
        if (toSelect != null) {
            this.setSelected(toSelect);
        }
    }

    public KeybindProfile getSelectedProfile() {
        Entry entry = this.getSelected();
        return entry != null ? entry.profile : null;
    }

    @Override
    public void setSelected(Entry entry) {
        super.setSelected(entry);
        if (this.parentScreen != null) {
            this.parentScreen.onSelectionUpdated();
        }
    }

    private static String formatRelativeTime(Instant instant) {
        if (instant == null) return "";
        Duration d = Duration.between(instant, Instant.now());
        long secs = Math.max(0, d.getSeconds());
        if (secs < 60) return secs + "s";
        if (secs < 3600) return (secs / 60) + "m";
        if (secs < 86400) return (secs / 3600) + "h";
        return (secs / 86400) + "d";
    }

    public class Entry extends ObjectSelectionList.Entry<Entry> {
        final KeybindProfile profile;
        private long lastClickTime = 0L;

        Entry(KeybindProfile profile) {
            this.profile = profile;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovering, float partialTick) {
            ChatFormatting nameColor;
            if (profile.isLoaded()) {
                nameColor = ChatFormatting.GREEN;
            } else if (profile.isImported()) {
                nameColor = ChatFormatting.AQUA;
            } else {
                nameColor = ChatFormatting.WHITE;
            }

            MutableComponent title = Component.literal(profile.getName()).withStyle(nameColor);
            if (profile.isDefault()) {
                title.append(Component.literal(" \u2605").withStyle(ChatFormatting.GOLD));
            }

            int keyCount = profile.getKeybinds().size();
            String meta = keyCount + " keys";
            String timeAgo = formatRelativeTime(profile.getUpdatedAt());
            if (!timeAgo.isEmpty()) {
                meta += " | " + timeAgo + " ago";
            }

            graphics.drawString(ProfileListWidget.this.minecraft.font, title, left + 6, top + 3, 0xFFFFFF, false);
            graphics.drawString(ProfileListWidget.this.minecraft.font, Component.literal(meta).withStyle(ChatFormatting.DARK_GRAY),
                left + 6, top + 14, 0x888888, false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            ProfileListWidget.this.setSelected(this);
            if (button == 0) {
                long now = Util.getMillis();
                if (now - this.lastClickTime < 250L) {
                    ProfileListWidget.this.parentScreen.onLoad();
                }
                this.lastClickTime = now;
            }
            return true;
        }

        @Override
        public Component getNarration() {
            return Component.literal(profile.getName());
        }
    }
}

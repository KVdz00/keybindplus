package com.github.kvdz00.keybindplus.gui;

import com.github.kvdz00.keybindplus.profile.KeybindProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

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

    public void updateEntries(List<KeybindProfile> profiles) {
        this.clearEntries();
        for (KeybindProfile profile : profiles) {
            this.addEntry(new Entry(profile));
        }
    }

    public KeybindProfile getSelectedProfile() {
        Entry entry = this.getSelected();
        return entry != null ? entry.profile : null;
    }

    private static String formatRelativeTime(Instant instant) {
        if (instant == null) return "";
        Duration d = Duration.between(instant, Instant.now());
        long secs = d.getSeconds();
        if (secs < 60) return secs + "s";
        if (secs < 3600) return (secs / 60) + "m";
        if (secs < 86400) return (secs / 3600) + "h";
        return (secs / 86400) + "d";
    }

    public class Entry extends ObjectSelectionList.Entry<Entry> {
        final KeybindProfile profile;

        Entry(KeybindProfile profile) {
            this.profile = profile;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   boolean hovered, float delta) {
            String name = profile.getName();
            if (profile.isDefault()) {
                name += " [DEFAULT]";
            }

            int keyCount = profile.getKeybinds().size();
            String meta = keyCount + " keys";
            String timeAgo = formatRelativeTime(profile.getUpdatedAt());
            if (!timeAgo.isEmpty()) {
                meta += " | " + timeAgo + " ago";
            }

            graphics.textRenderer().accept(this.getX() + 5, this.getY() + 2,
                Component.literal(name));
            graphics.textRenderer().accept(this.getX() + 5, this.getY() + 12,
                Component.literal(meta).withStyle(ChatFormatting.GRAY));
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            ProfileListWidget.this.setSelected(this);
            return true;
        }

        @Override
        public Component getNarration() {
            return Component.literal(profile.getName());
        }
    }
}

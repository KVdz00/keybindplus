package com.github.kvdz00.keybindplus.gui;

import com.github.kvdz00.keybindplus.profile.KeybindProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;
import java.util.function.Consumer;

public class CompareSelectPopup extends Screen {
    private final Screen parent;
    private final KeybindProfile sourceProfile;
    private final List<KeybindProfile> candidateProfiles;
    private final Consumer<KeybindProfile> onTargetSelected;

    private TargetListWidget listWidget;
    private Button compareButton;

    public CompareSelectPopup(Screen parent, KeybindProfile sourceProfile,
                              List<KeybindProfile> candidateProfiles,
                              Consumer<KeybindProfile> onTargetSelected) {
        super(new TranslatableComponent("keybindplus.popup.compare_title"));
        this.parent = parent;
        this.sourceProfile = sourceProfile;
        this.candidateProfiles = candidateProfiles;
        this.onTargetSelected = onTargetSelected;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        this.listWidget = new TargetListWidget(this.minecraft, this.width, this.height, 52, this.height - 44, 24);
        for (KeybindProfile p : candidateProfiles) {
            this.listWidget.addProfile(p);
        }
        this.addRenderableWidget(this.listWidget);

        int btnY = this.height - 32;
        this.compareButton = this.addRenderableWidget(new Button(
            centerX - 105, btnY, 100, 20,
            new TranslatableComponent("keybindplus.screen.compare"),
            btn -> {
                KeybindProfile target = listWidget.getSelectedProfile();
                if (target != null) {
                    onTargetSelected.accept(target);
                }
            }
        ));
        this.compareButton.active = false;

        this.addRenderableWidget(new Button(
            centerX + 5, btnY, 100, 20,
            new TranslatableComponent("keybindplus.popup.cancel"),
            btn -> this.minecraft.setScreen(parent)
        ));

        if (!candidateProfiles.isEmpty()) {
            this.listWidget.setSelected(this.listWidget.children().get(0));
            this.compareButton.active = true;
        }
    }

    void onSelectionChanged() {
        if (compareButton != null) {
            compareButton.active = listWidget.getSelectedProfile() != null;
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 12, 0xFFFFFF);

        ChatFormatting sourceColor = sourceProfile.isLoaded() ? ChatFormatting.GREEN : (sourceProfile.isImported() ? ChatFormatting.AQUA : ChatFormatting.WHITE);
        MutableComponent sub = new TranslatableComponent(
            "keybindplus.popup.compare_subtitle",
            new TextComponent(sourceProfile.getName()).withStyle(sourceColor, ChatFormatting.BOLD)
        );
        drawCenteredString(poseStack, this.font, sub, this.width / 2, 28, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private class TargetListWidget extends ObjectSelectionList<TargetListWidget.TargetEntry> {
        TargetListWidget(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
            super(minecraft, width, height, top, bottom, itemHeight);
        }

        @Override
        public int getRowWidth() {
            return Math.min(260, this.width - 40);
        }

        void addProfile(KeybindProfile profile) {
            this.addEntry(new TargetEntry(profile));
        }

        KeybindProfile getSelectedProfile() {
            TargetEntry entry = this.getSelected();
            return entry != null ? entry.profile : null;
        }

        @Override
        public void setSelected(TargetEntry entry) {
            super.setSelected(entry);
            CompareSelectPopup.this.onSelectionChanged();
        }

        private class TargetEntry extends ObjectSelectionList.Entry<TargetEntry> {
            final KeybindProfile profile;
            private long lastClickTime = 0L;

            TargetEntry(KeybindProfile profile) {
                this.profile = profile;
            }

            @Override
            public void render(PoseStack poseStack, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovering, float partialTick) {
                ChatFormatting nameColor;
                if (profile.isLoaded()) {
                    nameColor = ChatFormatting.GREEN;
                } else if (profile.isImported()) {
                    nameColor = ChatFormatting.AQUA;
                } else {
                    nameColor = ChatFormatting.WHITE;
                }

                MutableComponent text = new TextComponent(profile.getName()).withStyle(nameColor);
                if (profile.isDefault()) {
                    text.append(new TextComponent(" \u2605").withStyle(ChatFormatting.GOLD));
                }

                CompareSelectPopup.this.minecraft.font.draw(poseStack, text, left + 6, top + 6, 0xFFFFFF);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                TargetListWidget.this.setSelected(this);
                if (button == 0) {
                    long now = Util.getMillis();
                    if (now - this.lastClickTime < 250L) {
                        onTargetSelected.accept(this.profile);
                    }
                    this.lastClickTime = now;
                }
                return true;
            }

            @Override
            public Component getNarration() {
                return new TextComponent(profile.getName());
            }
        }
    }
}

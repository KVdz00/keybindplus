package com.github.kvdz00.keybindplus.gui;

import com.github.kvdz00.keybindplus.keybind.KeyConflict;
import com.github.kvdz00.keybindplus.profile.KeybindProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

public class ConflictWarningPopup extends Screen {
    private final Screen parent;
    private final KeybindProfile profile;
    private final List<KeyConflict> conflicts;
    private final Runnable onApply;
    private ConflictListWidget conflictList;

    public ConflictWarningPopup(Screen parent, KeybindProfile profile, List<KeyConflict> conflicts, Runnable onApply) {
        super(new TranslatableComponent("keybindplus.popup.conflict_title"));
        this.parent = parent;
        this.profile = profile;
        this.conflicts = conflicts;
        this.onApply = onApply;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        this.conflictList = new ConflictListWidget(this.minecraft, this.width, this.height, 48, this.height - 36, 20);
        for (KeyConflict conflict : conflicts) {
            this.conflictList.addConflict(conflict);
        }
        this.addRenderableWidget(this.conflictList);

        int btnY = this.height - 30;
        this.addRenderableWidget(new Button(
            centerX - 154, btnY, 96, 20,
            new TranslatableComponent("keybindplus.popup.conflict_apply"),
            btn -> { onApply.run(); this.minecraft.setScreen(parent); }
        ));

        this.addRenderableWidget(new Button(
            centerX - 54, btnY, 114, 20,
            new TranslatableComponent("keybindplus.popup.conflict_resolve"),
            btn -> this.minecraft.setScreen(new KeybindEditorScreen(parent, profile, true))
        ));

        this.addRenderableWidget(new Button(
            centerX + 64, btnY, 90, 20,
            new TranslatableComponent("keybindplus.popup.cancel"),
            btn -> this.minecraft.setScreen(parent)
        ));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
        int centerX = this.width / 2;

        drawCenteredString(poseStack, this.font, this.title, centerX, 12, 0xFFFFFF);
        drawCenteredString(poseStack, this.font, new TranslatableComponent("keybindplus.popup.conflict_subtitle"), centerX, 28, 0xAAAAAA);
    }

    public static class ConflictListWidget extends ObjectSelectionList<ConflictListWidget.ConflictEntry> {
        public ConflictListWidget(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
            super(minecraft, width, height, top, bottom, itemHeight);
        }

        @Override
        public int getRowWidth() {
            return Math.min(308, this.width - 20);
        }

        public void addConflict(KeyConflict conflict) {
            this.addEntry(new ConflictEntry(conflict));
        }

        public static class ConflictEntry extends ObjectSelectionList.Entry<ConflictEntry> {
            private final KeyConflict conflict;

            public ConflictEntry(KeyConflict conflict) {
                this.conflict = conflict;
            }

            @Override
            public void render(PoseStack poseStack, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovering, float partialTick) {
                String keyDisplay = "[" + formatKey(conflict.key()) + "]";
                String actionsDisplay = String.join(", ", conflict.actions().stream()
                    .map(this::formatAction).toList());

                Minecraft.getInstance().font.draw(poseStack,
                    new TextComponent(keyDisplay + "  ->  " + actionsDisplay), left + 6, top + 4, 0xFFFFFF);
            }

            @Override
            public Component getNarration() {
                return new TextComponent(conflict.key());
            }

            private String formatKey(String key) {
                return key
                    .replace("key.keyboard.", "")
                    .replace("key.mouse.", "Mouse ")
                    .replace('.', ' ')
                    .replace('_', ' ')
                    .toUpperCase();
            }

            private String formatAction(String action) {
                return action
                    .replace("key.", "")
                    .replace('.', ' ')
                    .replace('_', ' ');
            }
        }
    }
}

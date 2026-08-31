package com.github.kvdz00.keybindplus.gui;

import com.github.kvdz00.keybindplus.keybind.KeyConflict;
import com.github.kvdz00.keybindplus.profile.KeybindProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ConflictWarningPopup extends Screen {
    private final Screen parent;
    private final KeybindProfile profile;
    private final List<KeyConflict> conflicts;
    private final Runnable onApply;
    private ConflictListWidget conflictList;

    public ConflictWarningPopup(Screen parent, KeybindProfile profile, List<KeyConflict> conflicts, Runnable onApply) {
        super(Component.translatable("keybindplus.popup.conflict_title"));
        this.parent = parent;
        this.profile = profile;
        this.conflicts = conflicts;
        this.onApply = onApply;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        // Scrollable conflict list
        this.conflictList = new ConflictListWidget(this.minecraft, this.width, this.height, 48, this.height - 36, 20);
        for (KeyConflict conflict : conflicts) {
            this.conflictList.addConflict(conflict);
        }
        this.addRenderableWidget(this.conflictList);

        // Action buttons fixed at the bottom: Apply Anyway, Resolve Conflicts, Cancel
        int btnY = this.height - 30;
        this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.popup.conflict_apply"),
            btn -> { onApply.run(); this.minecraft.setScreen(parent); }
        ).bounds(centerX - 154, btnY, 96, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.popup.conflict_resolve"),
            btn -> this.minecraft.setScreen(new KeybindEditorScreen(parent, profile, true))
        ).bounds(centerX - 54, btnY, 114, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.popup.cancel"),
            btn -> this.minecraft.setScreen(parent)
        ).bounds(centerX + 64, btnY, 90, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        int centerX = this.width / 2;

        graphics.drawCenteredString(this.font, this.title, centerX, 12, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("keybindplus.popup.conflict_subtitle"), centerX, 28, 0xAAAAAA);
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
            public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovering, float partialTick) {
                String keyDisplay = "[" + formatKey(conflict.key()) + "]";
                String actionsDisplay = String.join(", ", conflict.actions().stream()
                    .map(this::formatAction).toList());

                graphics.drawString(Minecraft.getInstance().font,
                    Component.literal(keyDisplay + "  ->  " + actionsDisplay), left + 6, top + 4, 0xFFFFFF, false);
            }

            @Override
            public Component getNarration() {
                return Component.literal(conflict.key());
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
                return action.startsWith("key.") ? action.substring(4) : action;
            }
        }
    }
}

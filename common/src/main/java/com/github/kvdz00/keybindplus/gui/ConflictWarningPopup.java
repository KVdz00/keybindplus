package com.github.kvdz00.keybindplus.gui;

import com.github.kvdz00.keybindplus.keybind.KeyConflict;
import com.github.kvdz00.keybindplus.profile.KeybindProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
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
        this.conflictList = new ConflictListWidget(this.minecraft, this.width, this.height - 84, 48, 20);
        for (KeyConflict conflict : conflicts) {
            this.conflictList.addConflict(conflict);
        }
        this.addRenderableWidget(this.conflictList);

        // Action buttons fixed at the bottom: Apply Anyway, Resolve Conflicts, Cancel
        int btnY = this.height - 30;
        this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.popup.conflict_apply"),
            btn -> { onApply.run(); this.minecraft.setScreenAndShow(parent); }
        ).bounds(centerX - 154, btnY, 96, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.popup.conflict_resolve"),
            btn -> this.minecraft.setScreenAndShow(new KeybindEditorScreen(parent, profile, true))
        ).bounds(centerX - 54, btnY, 114, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.popup.cancel"),
            btn -> this.minecraft.setScreenAndShow(parent)
        ).bounds(centerX + 64, btnY, 90, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        int centerX = this.width / 2;

        graphics.textRenderer().accept(TextAlignment.CENTER, centerX, 12, this.title);
        graphics.textRenderer().accept(TextAlignment.CENTER, centerX, 28,
            Component.translatable("keybindplus.popup.conflict_subtitle"));
    }

    public static class ConflictListWidget extends ObjectSelectionList<ConflictListWidget.ConflictEntry> {
        public ConflictListWidget(Minecraft minecraft, int width, int height, int top, int itemHeight) {
            super(minecraft, width, height, top, itemHeight);
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
            public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                       boolean hovered, float delta) {
                String keyDisplay = "[" + formatKey(conflict.key()) + "]";
                String actionsDisplay = String.join(", ", conflict.actions().stream()
                    .map(this::formatAction).toList());

                graphics.textRenderer().accept(this.getX() + 10, this.getY() + 4,
                    Component.literal(keyDisplay + "  ->  " + actionsDisplay));
            }

            @Override
            public Component getNarration() {
                return Component.literal(conflict.key());
            }

            private String formatKey(String key) {
                return key
                    .replace("key.keyboard.", "")
                    .replace("key.mouse.", "Mouse ")
                    .toUpperCase();
            }

            private String formatAction(String action) {
                return action.startsWith("key.") ? action.substring(4) : action;
            }
        }
    }
}

package com.github.kvdz00.keybindplus.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.*;
import java.util.stream.Collectors;

public class KeybindEditListWidget extends ContainerObjectSelectionList<KeybindEditListWidget.KeybindEntry> {
    private final KeybindEditorScreen screen;

    public KeybindEditListWidget(Minecraft minecraft, KeybindEditorScreen screen,
                                 int width, int height, int top, int itemHeight) {
        super(minecraft, width, height, top, itemHeight);
        this.screen = screen;
    }

    @Override
    public int getRowWidth() {
        return Math.min(330, this.width - 20);
    }

    public void setEntries(List<KeybindRowData> rows, Map<String, List<String>> conflictMap, String activeRebindAction) {
        this.clearEntries();
        for (KeybindRowData row : rows) {
            List<String> conflictingActions = conflictMap.get(row.actionId());
            boolean isConflicted = conflictingActions != null && !conflictingActions.isEmpty();
            boolean isListening = row.actionId().equals(activeRebindAction);
            this.addEntry(new KeybindEntry(row, isConflicted, conflictingActions, isListening));
        }
    }

    public record KeybindRowData(String actionId, String keyName, String category) {}

    public class KeybindEntry extends ContainerObjectSelectionList.Entry<KeybindEntry> {
        private final KeybindRowData data;
        private final Button keyButton;
        private final Button unbindButton;
        private final List<Button> children;

        public KeybindEntry(KeybindRowData data, boolean isConflicted, List<String> conflictingActions, boolean isListening) {
            this.data = data;

            Component keyLabel;
            if (isListening) {
                keyLabel = Component.translatable("keybindplus.editor.press_key")
                    .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);
            } else if (isUnknownKey(data.keyName())) {
                keyLabel = Component.translatable("keybindplus.editor.none")
                    .withStyle(ChatFormatting.DARK_GRAY);
            } else {
                String formatted = formatKeyName(data.keyName());
                keyLabel = isConflicted
                    ? Component.literal(formatted).withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                    : Component.literal(formatted).withStyle(ChatFormatting.WHITE);
            }

            this.keyButton = Button.builder(keyLabel, btn -> {
                screen.setActiveRebindAction(data.actionId());
            }).bounds(0, 0, 84, 20).build();

            if (isConflicted && conflictingActions != null && !conflictingActions.isEmpty()) {
                String conflictNames = conflictingActions.stream()
                    .map(a -> Component.translatable(a).getString())
                    .collect(Collectors.joining(", "));
                this.keyButton.setTooltip(Tooltip.create(
                    Component.translatable("keybindplus.editor.conflict_tooltip", conflictNames)
                        .withStyle(ChatFormatting.RED)
                ));
            }

            boolean isAlreadyNone = isUnknownKey(data.keyName());
            this.unbindButton = Button.builder(
                Component.translatable("keybindplus.editor.unbind"),
                btn -> screen.unbindAction(data.actionId())
            ).bounds(0, 0, 44, 20)
            .tooltip(Tooltip.create(Component.translatable("keybindplus.tooltip.editor_unbind")))
            .build();
            this.unbindButton.active = !isAlreadyNone && !isListening;

            this.children = List.of(this.keyButton, this.unbindButton);
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   boolean hovered, float delta) {
            int rowX = this.getX() + 4;
            int rowY = this.getY();
            int rowWidth = KeybindEditListWidget.this.getRowWidth();

            int unbindX = this.getX() + rowWidth - 46;
            int keyX = unbindX - 88;

            this.keyButton.setX(keyX);
            this.keyButton.setY(rowY + 4);
            this.keyButton.extractRenderState(graphics, mouseX, mouseY, delta);

            this.unbindButton.setX(unbindX);
            this.unbindButton.setY(rowY + 4);
            this.unbindButton.extractRenderState(graphics, mouseX, mouseY, delta);

            MutableComponent actionText = Component.translatable(data.actionId());
            if (isListening()) {
                actionText = actionText.withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);
            }

            graphics.textRenderer().accept(rowX, rowY + 4, actionText);

            String cat = data.category();
            if (cat != null && !cat.isBlank()) {
                graphics.textRenderer().accept(rowX, rowY + 15,
                    Component.literal(cat).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        private boolean isListening() {
            return data.actionId().equals(screen.getActiveRebindAction());
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.children;
        }
    }

    private static boolean isUnknownKey(String keyName) {
        return keyName == null || keyName.isBlank()
            || keyName.equals("key.keyboard.unknown")
            || keyName.equalsIgnoreCase("none")
            || keyName.equals(InputConstants.UNKNOWN.getName());
    }

    private static String formatKeyName(String key) {
        if (isUnknownKey(key)) return "NONE";
        return key
            .replace("key.keyboard.", "")
            .replace("key.mouse.", "Mouse ")
            .replace('.', ' ')
            .replace('_', ' ')
            .toUpperCase();
    }
}

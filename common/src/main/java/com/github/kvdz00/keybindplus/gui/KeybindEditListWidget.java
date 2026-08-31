package com.github.kvdz00.keybindplus.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.*;
import java.util.stream.Collectors;

public class KeybindEditListWidget extends ContainerObjectSelectionList<KeybindEditListWidget.KeybindEntry> {
    private final KeybindEditorScreen screen;

    public KeybindEditListWidget(Minecraft minecraft, KeybindEditorScreen screen,
                                 int width, int height, int top, int bottom, int itemHeight) {
        super(minecraft, width, height, top, bottom, itemHeight);
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
                keyLabel = new TranslatableComponent("keybindplus.editor.press_key")
                    .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);
            } else if (isUnknownKey(data.keyName())) {
                keyLabel = new TranslatableComponent("keybindplus.editor.none")
                    .withStyle(ChatFormatting.DARK_GRAY);
            } else {
                String formatted = formatKeyName(data.keyName());
                keyLabel = isConflicted
                    ? new TextComponent(formatted).withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                    : new TextComponent(formatted).withStyle(ChatFormatting.WHITE);
            }

            if (isConflicted && conflictingActions != null && !conflictingActions.isEmpty()) {
                String conflictNames = conflictingActions.stream()
                    .map(a -> new TranslatableComponent(a).getString())
                    .collect(Collectors.joining(", "));
                this.keyButton = new Button(0, 0, 84, 20, keyLabel,
                    btn -> screen.setActiveRebindAction(data.actionId()),
                    (btn, poseStack, mx, my) -> screen.renderTooltip(poseStack,
                        new TranslatableComponent("keybindplus.editor.conflict_tooltip", conflictNames)
                            .withStyle(ChatFormatting.RED), mx, my));
            } else {
                this.keyButton = new Button(0, 0, 84, 20, keyLabel,
                    btn -> screen.setActiveRebindAction(data.actionId()));
            }

            boolean isAlreadyNone = isUnknownKey(data.keyName());
            this.unbindButton = new Button(0, 0, 44, 20,
                new TranslatableComponent("keybindplus.editor.unbind"),
                btn -> screen.unbindAction(data.actionId()),
                (btn, poseStack, mx, my) -> screen.renderTooltip(poseStack,
                    new TranslatableComponent("keybindplus.tooltip.editor_unbind"), mx, my));
            this.unbindButton.active = !isAlreadyNone && !isListening;

            this.children = List.of(this.keyButton, this.unbindButton);
        }

        @Override
        public void render(PoseStack poseStack, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovering, float partialTick) {
            int rowX = left + 4;
            int rowY = top;
            int rowWidth = KeybindEditListWidget.this.getRowWidth();

            int unbindX = left + rowWidth - 46;
            int keyX = unbindX - 88;

            this.keyButton.x = keyX;
            this.keyButton.y = rowY + 4;
            this.keyButton.render(poseStack, mouseX, mouseY, partialTick);

            this.unbindButton.x = unbindX;
            this.unbindButton.y = rowY + 4;
            this.unbindButton.render(poseStack, mouseX, mouseY, partialTick);

            MutableComponent actionText = new TranslatableComponent(data.actionId());
            if (isListening()) {
                actionText = actionText.withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);
            }

            Minecraft.getInstance().font.draw(poseStack, actionText, rowX, rowY + 4, 0xFFFFFF);

            String cat = data.category();
            if (cat != null && !cat.isBlank()) {
                Minecraft.getInstance().font.draw(poseStack,
                    new TextComponent(cat).withStyle(ChatFormatting.DARK_GRAY), rowX, rowY + 15, 0x888888);
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

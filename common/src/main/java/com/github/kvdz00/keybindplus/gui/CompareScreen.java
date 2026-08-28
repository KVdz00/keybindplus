package com.github.kvdz00.keybindplus.gui;

import com.github.kvdz00.keybindplus.profile.KeybindProfile;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.network.chat.Component;

import java.util.*;

public class CompareScreen extends Screen {
    private final Screen parent;
    private final KeybindProfile profileA;
    private final KeybindProfile profileB;
    private final List<CompareRow> rows = new ArrayList<>();
    private int scrollOffset = 0;
    private static final int ROW_HEIGHT = 14;
    private static final int HEADER_HEIGHT = 50;

    public CompareScreen(Screen parent, KeybindProfile profileA, KeybindProfile profileB) {
        super(Component.translatable("keybindplus.compare.title", profileA.getName(), profileB.getName()));
        this.parent = parent;
        this.profileA = profileA;
        this.profileB = profileB;
        buildRows();
    }

    private void buildRows() {
        Set<String> allActions = new LinkedHashSet<>();
        allActions.addAll(profileA.getKeybinds().keySet());
        allActions.addAll(profileB.getKeybinds().keySet());

        for (String action : allActions) {
            String valueA = profileA.getKeybinds().getOrDefault(action, "-");
            String valueB = profileB.getKeybinds().getOrDefault(action, "-");
            boolean different = !valueA.equals(valueB);
            rows.add(new CompareRow(action, valueA, valueB, different));
        }
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(
            Component.literal("Close"),
            btn -> this.minecraft.setScreenAndShow(parent)
        ).bounds(this.width / 2 - 50, this.height - 28, 100, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.textRenderer().accept(TextAlignment.CENTER, this.width / 2, 10, this.title);

        // Column headers
        int colAction = 10;
        int colA = this.width / 2 - 60;
        int colB = this.width / 2 + 60;
        int headerY = 30;

        graphics.textRenderer().accept(colAction, headerY, Component.translatable("keybindplus.compare.action"));
        graphics.textRenderer().accept(colA, headerY, Component.literal(profileA.getName()));
        graphics.textRenderer().accept(colB, headerY, Component.literal(profileB.getName()));

        // Divider line
        graphics.fill(10, headerY + 12, this.width - 10, headerY + 13, 0xFF555555);

        // Rows
        int visibleRows = (this.height - HEADER_HEIGHT - 40) / ROW_HEIGHT;
        for (int i = 0; i < visibleRows && (i + scrollOffset) < rows.size(); i++) {
            CompareRow row = rows.get(i + scrollOffset);
            int y = HEADER_HEIGHT + (i * ROW_HEIGHT);
            String suffix = row.different ? " [!]" : "";

            String displayAction = row.action.startsWith("key.") ?
                row.action.substring(4) : row.action;

            graphics.textRenderer().accept(colAction, y, Component.literal(displayAction));
            graphics.textRenderer().accept(colA, y, Component.literal(shortenKey(row.valueA)));
            graphics.textRenderer().accept(colB, y, Component.literal(shortenKey(row.valueB) + suffix));
        }
    }

    private String shortenKey(String keyName) {
        if (keyName == null || keyName.isBlank() || keyName.equals("key.keyboard.unknown") || keyName.equalsIgnoreCase("none")) {
            return "NONE";
        }
        return keyName
            .replace("key.keyboard.", "")
            .replace("key.mouse.", "Mouse ")
            .replace("left.", "L-")
            .replace("right.", "R-")
            .replace('.', ' ')
            .replace('_', ' ')
            .toUpperCase();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int visibleRows = (this.height - HEADER_HEIGHT - 40) / ROW_HEIGHT;
        scrollOffset = Math.max(0, Math.min(scrollOffset - (int) verticalAmount,
            Math.max(0, rows.size() - visibleRows)));
        return true;
    }

    record CompareRow(String action, String valueA, String valueB, boolean different) {}
}

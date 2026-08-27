package com.github.kvdz00.keybindplus.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.network.chat.Component;

public class ConfirmPopup extends Screen {
    private final Screen parent;
    private final Component message;
    private final Runnable onConfirm;

    public ConfirmPopup(Screen parent, Component message, Runnable onConfirm) {
        super(Component.translatable("keybindplus.popup.confirm"));
        this.parent = parent;
        this.message = message;
        this.onConfirm = onConfirm;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.popup.confirm"),
            btn -> { onConfirm.run(); this.minecraft.setScreenAndShow(parent); }
        ).bounds(centerX - 105, centerY + 10, 100, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.popup.cancel"),
            btn -> this.minecraft.setScreenAndShow(parent)
        ).bounds(centerX + 5, centerY + 10, 100, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.textRenderer().accept(TextAlignment.CENTER, this.width / 2, this.height / 2 - 20, this.message);
    }
}

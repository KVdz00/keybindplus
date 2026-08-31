package com.github.kvdz00.keybindplus.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class ConfirmPopup extends Screen {
    private final Screen parent;
    private final Component message;
    private final Runnable onConfirm;

    public ConfirmPopup(Screen parent, Component message, Runnable onConfirm) {
        super(new TranslatableComponent("keybindplus.popup.confirm"));
        this.parent = parent;
        this.message = message;
        this.onConfirm = onConfirm;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.addRenderableWidget(new Button(
            centerX - 105, centerY + 10, 100, 20,
            new TranslatableComponent("keybindplus.popup.confirm"),
            btn -> { onConfirm.run(); this.minecraft.setScreen(parent); }
        ));

        this.addRenderableWidget(new Button(
            centerX + 5, centerY + 10, 100, 20,
            new TranslatableComponent("keybindplus.popup.cancel"),
            btn -> this.minecraft.setScreen(parent)
        ));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
        drawCenteredString(poseStack, this.font, this.message, this.width / 2, this.height / 2 - 20, 0xFFFFFF);
    }
}

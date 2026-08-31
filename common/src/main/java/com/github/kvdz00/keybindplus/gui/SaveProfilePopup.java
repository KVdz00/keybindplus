package com.github.kvdz00.keybindplus.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class SaveProfilePopup extends Screen {
    private final Screen parent;
    private final String initialValue;
    private final Consumer<String> onSave;
    private EditBox nameField;

    public SaveProfilePopup(Screen parent, Component title, String initialValue, Consumer<String> onSave) {
        super(title);
        this.parent = parent;
        this.initialValue = initialValue != null ? initialValue : "";
        this.onSave = onSave;
    }

    public SaveProfilePopup(Screen parent, Consumer<String> onSave) {
        this(parent, Component.translatable("keybindplus.popup.save_title"), "", onSave);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.nameField = new EditBox(this.font, centerX - 100, centerY - 10, 200, 20,
            Component.translatable("keybindplus.popup.save_name"));
        this.nameField.setMaxLength(64);
        if (!this.initialValue.isEmpty()) {
            this.nameField.setValue(this.initialValue);
            this.nameField.setHighlightPos(0);
        }
        this.addRenderableWidget(this.nameField);
        this.setInitialFocus(this.nameField);

        this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.popup.save"),
            btn -> doSave()
        ).bounds(centerX - 105, centerY + 20, 100, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.popup.cancel"),
            btn -> this.minecraft.setScreen(parent)
        ).bounds(centerX + 5, centerY + 20, 100, 20).build());
    }

    private void doSave() {
        String name = this.nameField.getValue().trim();
        if (!name.isEmpty()) {
            onSave.accept(name);
            this.minecraft.setScreen(parent);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 35, 0xFFFFFF);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            doSave();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}

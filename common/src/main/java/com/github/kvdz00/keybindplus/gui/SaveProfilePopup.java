package com.github.kvdz00.keybindplus.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class SaveProfilePopup extends Screen {
    private final Screen parent;
    private final Consumer<String> onSave;
    private EditBox nameField;

    public SaveProfilePopup(Screen parent, Consumer<String> onSave) {
        super(Component.translatable("keybindplus.popup.save_title"));
        this.parent = parent;
        this.onSave = onSave;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.nameField = new EditBox(this.font, centerX - 100, centerY - 10, 200, 20,
            Component.translatable("keybindplus.popup.save_name"));
        this.nameField.setMaxLength(64);
        this.addRenderableWidget(this.nameField);
        this.setInitialFocus(this.nameField);

        this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.popup.save"),
            btn -> doSave()
        ).bounds(centerX - 105, centerY + 20, 100, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.popup.cancel"),
            btn -> this.minecraft.setScreenAndShow(parent)
        ).bounds(centerX + 5, centerY + 20, 100, 20).build());
    }

    private void doSave() {
        String name = this.nameField.getValue().trim();
        if (!name.isEmpty()) {
            onSave.accept(name);
            this.minecraft.setScreenAndShow(parent);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.textRenderer().accept(TextAlignment.CENTER, this.width / 2, this.height / 2 - 35, this.title);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 257) { // Enter key
            doSave();
            return true;
        }
        return super.keyPressed(event);
    }
}

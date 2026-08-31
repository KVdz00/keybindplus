package com.github.kvdz00.keybindplus.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.function.Consumer;

public class SaveProfilePopup extends GuiScreen {
    private final GuiScreen parent;
    private final String title;
    private final String initialName;
    private final Consumer<String> onSave;
    private final Runnable onCancel;
    private GuiTextField nameField;
    private GuiButton saveButton;

    public SaveProfilePopup(GuiScreen parent, String title, String initialName, Consumer<String> onSave, Runnable onCancel) {
        this.parent = parent;
        this.title = title;
        this.initialName = initialName != null ? initialName : "";
        this.onSave = onSave;
        this.onCancel = onCancel;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        int cx = this.width / 2;
        int cy = this.height / 2;

        nameField = new GuiTextField(0, this.fontRendererObj, cx - 100, cy - 10, 200, 20);
        nameField.setText(initialName);
        nameField.setFocused(true);

        saveButton = new GuiButton(1, cx - 105, cy + 25, 100, 20, I18n.format("keybindplus.popup.save"));
        this.buttonList.add(saveButton);
        this.buttonList.add(new GuiButton(2, cx + 5, cy + 25, 100, 20, I18n.format("keybindplus.popup.cancel")));
        updateSaveButtonState();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void updateScreen() {
        nameField.updateCursorCounter();
        updateSaveButtonState();
    }

    private void updateSaveButtonState() {
        if (saveButton != null) {
            String text = nameField.getText();
            saveButton.enabled = text != null && !text.trim().isEmpty();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) {
            String text = nameField.getText().trim();
            if (!text.isEmpty()) {
                if (onSave != null) onSave.accept(text);
                this.mc.displayGuiScreen(parent);
            }
        } else if (button.id == 2) {
            if (onCancel != null) onCancel.run();
            this.mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { // ESC
            if (onCancel != null) onCancel.run();
            this.mc.displayGuiScreen(parent);
            return;
        }

        if (keyCode == 28) { // ENTER
            String text = nameField.getText().trim();
            if (!text.isEmpty()) {
                if (onSave != null) onSave.accept(text);
                this.mc.displayGuiScreen(parent);
                return;
            }
        }

        nameField.textboxKeyTyped(typedChar, keyCode);
        updateSaveButtonState();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        nameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (parent != null) {
            parent.drawScreen(-1, -1, partialTicks);
        }
        drawRect(0, 0, this.width, this.height, 0x88000000);

        int cx = this.width / 2;
        int cy = this.height / 2;
        int boxW = 230;
        int boxH = 110;

        drawRect(cx - boxW / 2, cy - boxH / 2, cx + boxW / 2, cy + boxH / 2, 0xF0181818);
        drawHorizontalLine(cx - boxW / 2, cx + boxW / 2, cy - boxH / 2, 0xFF444444);
        drawHorizontalLine(cx - boxW / 2, cx + boxW / 2, cy + boxH / 2, 0xFF444444);
        drawVerticalLine(cx - boxW / 2, cy - boxH / 2, cy + boxH / 2, 0xFF444444);
        drawVerticalLine(cx + boxW / 2, cy - boxH / 2, cy + boxH / 2, 0xFF444444);

        drawCenteredString(this.fontRendererObj, title, cx, cy - 40, 0xFFFFFF);
        drawString(this.fontRendererObj, I18n.format("keybindplus.popup.save_name"), cx - 100, cy - 25, 0xAAAAAA);

        nameField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}

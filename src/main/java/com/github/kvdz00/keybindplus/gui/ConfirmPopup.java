package com.github.kvdz00.keybindplus.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class ConfirmPopup extends GuiScreen {
    private final GuiScreen parent;
    private final String message;
    private final Runnable onConfirm;
    private final Runnable onCancel;

    public ConfirmPopup(GuiScreen parent, String message, Runnable onConfirm, Runnable onCancel) {
        this.parent = parent;
        this.message = message;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        int cx = this.width / 2;
        int cy = this.height / 2;

        this.buttonList.add(new GuiButton(1, cx - 105, cy + 20, 100, 20, I18n.format("keybindplus.popup.confirm")));
        this.buttonList.add(new GuiButton(2, cx + 5, cy + 20, 100, 20, I18n.format("keybindplus.popup.cancel")));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) {
            if (onConfirm != null) onConfirm.run();
            this.mc.displayGuiScreen(parent);
        } else if (button.id == 2) {
            if (onCancel != null) onCancel.run();
            this.mc.displayGuiScreen(parent);
        }
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
        int boxH = 90;

        drawRect(cx - boxW / 2, cy - boxH / 2, cx + boxW / 2, cy + boxH / 2, 0xF0181818);
        drawHorizontalLine(cx - boxW / 2, cx + boxW / 2, cy - boxH / 2, 0xFF444444);
        drawHorizontalLine(cx - boxW / 2, cx + boxW / 2, cy + boxH / 2, 0xFF444444);
        drawVerticalLine(cx - boxW / 2, cy - boxH / 2, cy + boxH / 2, 0xFF444444);
        drawVerticalLine(cx + boxW / 2, cy - boxH / 2, cy + boxH / 2, 0xFF444444);

        drawCenteredString(this.fontRendererObj, message, cx, cy - 20, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (onCancel != null) onCancel.run();
            this.mc.displayGuiScreen(parent);
        }
    }
}

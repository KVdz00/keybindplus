package com.github.kvdz00.keybindplus.gui;

import com.github.kvdz00.keybindplus.keybind.KeyConflict;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.List;

public class ConflictWarningPopup extends GuiScreen {
    private final GuiScreen parent;
    private final List<KeyConflict> conflicts;
    private final Runnable onApplyAnyway;
    private final Runnable onResolve;

    public ConflictWarningPopup(GuiScreen parent, List<KeyConflict> conflicts, Runnable onApplyAnyway, Runnable onResolve) {
        this.parent = parent;
        this.conflicts = conflicts;
        this.onApplyAnyway = onApplyAnyway;
        this.onResolve = onResolve;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        int cx = this.width / 2;
        int cy = this.height / 2;

        this.buttonList.add(new GuiButton(1, cx - 145, cy + 65, 90, 20, I18n.format("keybindplus.popup.conflict_apply")));
        this.buttonList.add(new GuiButton(2, cx - 45, cy + 65, 100, 20, I18n.format("keybindplus.popup.conflict_resolve")));
        this.buttonList.add(new GuiButton(3, cx + 65, cy + 65, 80, 20, I18n.format("keybindplus.popup.cancel")));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) {
            if (onApplyAnyway != null) onApplyAnyway.run();
            this.mc.displayGuiScreen(parent);
        } else if (button.id == 2) {
            if (onResolve != null) onResolve.run();
        } else if (button.id == 3) {
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
        int boxW = 310;
        int boxH = 180;

        drawRect(cx - boxW / 2, cy - boxH / 2, cx + boxW / 2, cy + boxH / 2, 0xF0181818);
        drawHorizontalLine(cx - boxW / 2, cx + boxW / 2, cy - boxH / 2, 0xFF444444);
        drawHorizontalLine(cx - boxW / 2, cx + boxW / 2, cy + boxH / 2, 0xFF444444);
        drawVerticalLine(cx - boxW / 2, cy - boxH / 2, cy + boxH / 2, 0xFF444444);
        drawVerticalLine(cx + boxW / 2, cy - boxH / 2, cy + boxH / 2, 0xFF444444);

        drawCenteredString(this.fontRendererObj, I18n.format("keybindplus.popup.conflict_title"), cx, cy - 75, 0xFF5555);
        drawCenteredString(this.fontRendererObj, I18n.format("keybindplus.popup.conflict_subtitle"), cx, cy - 60, 0xAAAAAA);

        int y = cy - 40;
        int shown = Math.min(conflicts.size(), 4);
        for (int i = 0; i < shown; i++) {
            KeyConflict c = conflicts.get(i);
            String line = "• " + c.getBoundKey() + " (" + c.getConflictCount() + " actions)";
            drawString(this.fontRendererObj, line, cx - 130, y, 0xFFFFAA);
            y += 12;
            if (y > cy + 45) break;
        }

        if (conflicts.size() > 4) {
            drawString(this.fontRendererObj, "... +" + (conflicts.size() - 4) + " more", cx - 130, y, 0x888888);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(parent);
        }
    }
}

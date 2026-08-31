package com.github.kvdz00.keybindplus.gui;

import com.github.kvdz00.keybindplus.profile.KeybindProfile;
import com.github.kvdz00.keybindplus.profile.ProfileManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CompareSelectPopup extends GuiScreen {
    private final GuiScreen parent;
    private final KeybindProfile baseProfile;
    private final Consumer<KeybindProfile> onSelected;
    private final List<KeybindProfile> candidates = new ArrayList<>();
    private int selectedIndex = -1;
    private ProfileSlotList slotList;
    private GuiButton selectButton;

    public CompareSelectPopup(GuiScreen parent, KeybindProfile baseProfile, Consumer<KeybindProfile> onSelected) {
        this.parent = parent;
        this.baseProfile = baseProfile;
        this.onSelected = onSelected;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        candidates.clear();
        for (KeybindProfile p : ProfileManager.get().listProfiles()) {
            if (!p.getName().equals(baseProfile.getName())) {
                candidates.add(p);
            }
        }

        int cx = this.width / 2;
        int cy = this.height / 2;
        int listTop = cy - 60;
        int listBottom = cy + 50;

        slotList = new ProfileSlotList(this.mc, 220, listBottom - listTop, listTop, listBottom, 20);
        slotList.setSlotXBoundsFromLeft(cx - 110);

        selectButton = new GuiButton(1, cx - 105, cy + 60, 100, 20, I18n.format("keybindplus.popup.confirm"));
        this.buttonList.add(selectButton);
        this.buttonList.add(new GuiButton(2, cx + 5, cy + 60, 100, 20, I18n.format("keybindplus.popup.cancel")));
        updateSelectButtonState();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (slotList != null) {
            slotList.handleMouseInput();
        }
    }

    private void updateSelectButtonState() {
        if (selectButton != null) {
            selectButton.enabled = selectedIndex >= 0 && selectedIndex < candidates.size();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) {
            if (selectedIndex >= 0 && selectedIndex < candidates.size()) {
                if (onSelected != null) onSelected.accept(candidates.get(selectedIndex));
            }
        } else if (button.id == 2) {
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
        int boxW = 250;
        int boxH = 170;

        drawRect(cx - boxW / 2, cy - boxH / 2, cx + boxW / 2, cy + boxH / 2, 0xF0181818);
        drawHorizontalLine(cx - boxW / 2, cx + boxW / 2, cy - boxH / 2, 0xFF444444);
        drawHorizontalLine(cx - boxW / 2, cx + boxW / 2, cy + boxH / 2, 0xFF444444);
        drawVerticalLine(cx - boxW / 2, cy - boxH / 2, cy + boxH / 2, 0xFF444444);
        drawVerticalLine(cx + boxW / 2, cy - boxH / 2, cy + boxH / 2, 0xFF444444);

        drawCenteredString(this.fontRendererObj, I18n.format("keybindplus.popup.compare_title"), cx, cy - 75, 0xFFFFFF);
        drawCenteredString(this.fontRendererObj, I18n.format("keybindplus.popup.compare_subtitle", baseProfile.getName()), cx, cy - 62, 0xAAAAAA);

        if (slotList != null) {
            slotList.drawScreen(mouseX, mouseY, partialTicks);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(parent);
        }
    }

    private class ProfileSlotList extends GuiSlot {
        public ProfileSlotList(net.minecraft.client.Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
            super(mc, width, height, top, bottom, slotHeight);
        }

        @Override
        protected int getSize() {
            return candidates.size();
        }

        @Override
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
            selectedIndex = slotIndex;
            updateSelectButtonState();
            if (isDoubleClick && slotIndex >= 0 && slotIndex < candidates.size()) {
                if (onSelected != null) onSelected.accept(candidates.get(slotIndex));
            }
        }

        @Override
        protected boolean isSelected(int slotIndex) {
            return slotIndex == selectedIndex;
        }

        @Override
        protected void drawBackground() {}

        @Override
        protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn) {
            if (entryID >= 0 && entryID < candidates.size()) {
                KeybindProfile p = candidates.get(entryID);
                String name = p.getName();
                fontRendererObj.drawString(name, insideLeft + 4, yPos + 4, 0xFFFFFF);
            }
        }
    }
}

package com.github.kvdz00.keybindplus.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public final class ToastNotification {
    private ToastNotification() {}

    public static void show(String title, String description) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null) return;

        String msg = EnumChatFormatting.GOLD + "[KeybindPlus] " + EnumChatFormatting.YELLOW + title;
        if (description != null && !description.trim().isEmpty()) {
            msg += EnumChatFormatting.WHITE + ": " + description;
        }
        mc.thePlayer.addChatMessage(new ChatComponentText(msg));
    }

    public static void toast(String titleKey, String descKey, Object... args) {
        String title = I18n.format(titleKey);
        String desc = (descKey != null && !descKey.isEmpty()) ? I18n.format(descKey, args) : "";
        show(title, desc);
    }
}

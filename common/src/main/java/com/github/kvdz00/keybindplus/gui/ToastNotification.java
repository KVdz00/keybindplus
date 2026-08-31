package com.github.kvdz00.keybindplus.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.network.chat.Component;

public final class ToastNotification {
    private ToastNotification() {}

    public static void show(Component title, Component description) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        ToastManager tm = mc.getToastManager();
        if (tm != null) {
            SystemToast.addOrUpdate(tm, SystemToast.SystemToastId.PERIODIC_NOTIFICATION, title, description);
        }
    }

    public static void toast(String titleKey, String descKey, Object... args) {
        show(Component.translatable(titleKey), Component.translatable(descKey, args));
    }
}

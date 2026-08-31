package com.github.kvdz00.keybindplus.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public final class ToastNotification {
    private ToastNotification() {}

    public static void show(Component title, Component description) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        ToastComponent tc = mc.getToasts();
        if (tc != null) {
            SystemToast.addOrUpdate(tc, SystemToast.SystemToastIds.PERIODIC_NOTIFICATION, title, description);
        }
    }

    public static void toast(String titleKey, String descKey, Object... args) {
        show(new TranslatableComponent(titleKey), new TranslatableComponent(descKey, args));
    }
}

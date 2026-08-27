package com.github.kvdz00.keybindplus.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Method;

public final class ToastNotification {
    private ToastNotification() {}

    public static void show(Component title, Component description) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        try {
            Object toastManager = null;
            for (Method m : mc.getClass().getMethods()) {
                if (m.getParameterCount() == 0 && m.getReturnType().getName().toLowerCase().contains("toast")) {
                    toastManager = m.invoke(mc);
                    break;
                }
            }

            if (toastManager != null) {
                Class<?> systemToastClass = Class.forName("net.minecraft.client.gui.components.toasts.SystemToast");
                Class<?> typeClass = Class.forName("net.minecraft.client.gui.components.toasts.SystemToast$SystemToastId");
                Object typePeriodic = null;
                for (Object constant : typeClass.getEnumConstants()) {
                    if (constant.toString().equals("PERIODIC_NOTIFICATION") || typePeriodic == null) {
                        typePeriodic = constant;
                    }
                }

                for (Method m : systemToastClass.getMethods()) {
                    if (m.getName().equals("addOrUpdate") || m.getName().equals("add")) {
                        if (m.getParameterCount() == 4) {
                            m.invoke(null, toastManager, typePeriodic, title, description);
                            return;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        // Fallback message
        if (mc.player != null) {
            mc.player.sendSystemMessage(
                Component.literal("[").append(title).append(Component.literal("] ")).append(description)
            );
        }
    }

    public static void toast(String titleKey, String descKey, Object... args) {
        show(Component.translatable(titleKey), Component.translatable(descKey, args));
    }
}

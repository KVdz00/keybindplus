package com.github.kvdz00.keybindplus;

import com.github.kvdz00.keybindplus.config.KeybindPlusConfig;
import com.github.kvdz00.keybindplus.gui.KeybindPlusScreen;
import com.github.kvdz00.keybindplus.keybind.KeybindApplier;
import com.github.kvdz00.keybindplus.profile.KeybindProfile;
import com.github.kvdz00.keybindplus.profile.ProfileManager;
import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class KeybindPlusClient {
    public static final String KEYBIND_CATEGORY = "key.categories.keybindplus";

    public static final KeyMapping OPEN_GUI_KEY = new KeyMapping(
        "key.keybindplus.open_gui",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_V,
        KEYBIND_CATEGORY
    );

    public static final KeyMapping QUICK_LOAD_KEY = new KeyMapping(
        "key.keybindplus.quick_load",
        InputConstants.Type.KEYSYM,
        InputConstants.UNKNOWN.getValue(),
        KEYBIND_CATEGORY
    );

    private static boolean autoLoaded = false;

    public static void initClient() {
        KeybindPlus.LOGGER.info("KeybindPlus client initializing");
        KeyMappingRegistry.register(OPEN_GUI_KEY);
        KeyMappingRegistry.register(QUICK_LOAD_KEY);

        ClientTickEvent.CLIENT_POST.register(client -> {
            if (OPEN_GUI_KEY.consumeClick()) {
                Minecraft.getInstance().setScreen(new KeybindPlusScreen());
            }

            if (QUICK_LOAD_KEY.consumeClick()) {
                onQuickLoad();
            }

            if (!autoLoaded && client.player != null) {
                autoLoaded = true;
                autoLoadDefault();
            }
        });
    }

    private static void onQuickLoad() {
        KeybindProfile defaultProfile = ProfileManager.get().getDefaultProfile();
        if (defaultProfile == null) return;
        ProfileManager.get().createAutoBackup();
        KeybindApplier.apply(defaultProfile);
    }

    private static void autoLoadDefault() {
        KeybindPlusConfig config = KeybindPlusConfig.get();
        if (!config.isAutoLoadDefaultOnStartup()) return;

        KeybindProfile defaultProfile = ProfileManager.get().getDefaultProfile();
        if (defaultProfile != null) {
            KeybindApplier.apply(defaultProfile);
            KeybindPlus.LOGGER.info("Auto-loaded default profile: {}", defaultProfile.getName());
        }
    }
}

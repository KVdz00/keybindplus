package com.github.kvdz00.keybindplus;

import com.github.kvdz00.keybindplus.config.KeybindPlusConfig;
import com.github.kvdz00.keybindplus.gui.KeybindPlusScreen;
import com.github.kvdz00.keybindplus.gui.ToastNotification;
import com.github.kvdz00.keybindplus.keybind.KeybindApplier;
import com.github.kvdz00.keybindplus.profile.KeybindProfile;
import com.github.kvdz00.keybindplus.profile.ProfileManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public final class KeybindPlusClient {
    public static KeyBinding openGuiKey;
    public static KeyBinding quickLoadKey;

    public static void init() {
        openGuiKey = new KeyBinding("key.keybindplus.open_gui", Keyboard.KEY_K, "key.categories.keybindplus");
        quickLoadKey = new KeyBinding("key.keybindplus.quick_load", Keyboard.KEY_NONE, "key.categories.keybindplus");

        ClientRegistry.registerKeyBinding(openGuiKey);
        ClientRegistry.registerKeyBinding(quickLoadKey);

        MinecraftForge.EVENT_BUS.register(new KeybindPlusClient());
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null) return;

        if (openGuiKey.isPressed()) {
            mc.displayGuiScreen(new KeybindPlusScreen(null));
        } else if (quickLoadKey.isPressed()) {
            String defaultName = KeybindPlusConfig.get().getDefaultProfile();
            if (defaultName != null && !defaultName.trim().isEmpty()) {
                KeybindProfile p = ProfileManager.get().getProfile(defaultName);
                if (p != null) {
                    KeybindApplier.apply(p);
                    ToastNotification.toast("keybindplus.toast.quick_load_title", "keybindplus.toast.quick_load_desc", defaultName);
                } else {
                    ToastNotification.toast("keybindplus.toast.error_title", "keybindplus.toast.no_default");
                }
            } else {
                ToastNotification.toast("keybindplus.toast.error_title", "keybindplus.toast.no_default");
            }
        }
    }
}

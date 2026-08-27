package com.github.kvdz00.keybindplus.neoforge;

import com.github.kvdz00.keybindplus.KeybindPlus;
import com.github.kvdz00.keybindplus.KeybindPlusClient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(KeybindPlus.MOD_ID)
public final class KeybindPlusNeoForge {
    public KeybindPlusNeoForge(IEventBus modBus) {
        KeybindPlus.init();
        modBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        KeybindPlusClient.initClient();
    }
}

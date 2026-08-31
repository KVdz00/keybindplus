package com.github.kvdz00.keybindplus.forge;

import com.github.kvdz00.keybindplus.KeybindPlus;
import com.github.kvdz00.keybindplus.KeybindPlusClient;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(KeybindPlus.MOD_ID)
public final class KeybindPlusForge {
    public KeybindPlusForge() {
        EventBuses.registerModEventBus(KeybindPlus.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        KeybindPlus.init();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            KeybindPlusClient.initClient();
        }
    }
}

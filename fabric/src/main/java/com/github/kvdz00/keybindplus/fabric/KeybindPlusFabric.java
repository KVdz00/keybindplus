package com.github.kvdz00.keybindplus.fabric;

import com.github.kvdz00.keybindplus.KeybindPlus;
import net.fabricmc.api.ModInitializer;

public final class KeybindPlusFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        KeybindPlus.init();
    }
}

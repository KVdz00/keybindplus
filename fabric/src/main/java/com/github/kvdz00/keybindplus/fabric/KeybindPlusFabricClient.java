package com.github.kvdz00.keybindplus.fabric;

import com.github.kvdz00.keybindplus.KeybindPlusClient;
import net.fabricmc.api.ClientModInitializer;

public final class KeybindPlusFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeybindPlusClient.initClient();
    }
}

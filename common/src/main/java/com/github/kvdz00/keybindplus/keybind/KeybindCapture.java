package com.github.kvdz00.keybindplus.keybind;

import com.github.kvdz00.keybindplus.KeybindPlus;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.LinkedHashMap;
import java.util.Map;

public final class KeybindCapture {
    private KeybindCapture() {}

    /**
     * Captures all currently registered keybinds from Minecraft's options.
     * Returns a map of action translation key -> bound key save string.
     */
    public static Map<String, String> captureAll() {
        var minecraft = Minecraft.getInstance();
        var options = minecraft.options;
        var result = new LinkedHashMap<String, String>();

        for (KeyMapping keyMapping : options.keyMappings) {
            String actionId = keyMapping.getName();
            String boundKey = keyMapping.saveString();
            result.put(actionId, boundKey);
        }

        KeybindPlus.LOGGER.info("Captured {} keybinds", result.size());
        return result;
    }
}

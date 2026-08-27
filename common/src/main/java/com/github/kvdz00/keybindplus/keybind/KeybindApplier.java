package com.github.kvdz00.keybindplus.keybind;

import com.github.kvdz00.keybindplus.KeybindPlus;
import com.github.kvdz00.keybindplus.profile.KeybindProfile;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class KeybindApplier {
    private KeybindApplier() {}

    /**
     * Applies a keybind profile to the current Minecraft instance.
     * Skips keybinds for mods that aren't installed.
     * Calls KeyMapping.resetMapping() and options.save() after applying.
     */
    public static ApplyResult apply(KeybindProfile profile) {
        var minecraft = Minecraft.getInstance();
        var options = minecraft.options;
        var profileBinds = profile.getKeybinds();
        int applied = 0;
        List<String> skipped = new ArrayList<>();

        Map<String, KeyMapping> mappingByName = new HashMap<>();
        for (KeyMapping km : options.keyMappings) {
            mappingByName.put(km.getName(), km);
        }

        for (var entry : profileBinds.entrySet()) {
            String actionId = entry.getKey();
            String keyName = entry.getValue();

            KeyMapping mapping = mappingByName.get(actionId);
            if (mapping == null) {
                skipped.add(actionId);
                continue;
            }

            try {
                InputConstants.Key key = InputConstants.getKey(keyName);
                mapping.setKey(key);
                applied++;
            } catch (Exception e) {
                KeybindPlus.LOGGER.warn("Failed to set key '{}' for '{}': {}", keyName, actionId, e.getMessage());
                skipped.add(actionId);
            }
        }

        KeyMapping.resetMapping();
        options.save();

        KeybindPlus.LOGGER.info("Applied {} keybinds, skipped {}", applied, skipped.size());
        return new ApplyResult(applied, skipped);
    }
}

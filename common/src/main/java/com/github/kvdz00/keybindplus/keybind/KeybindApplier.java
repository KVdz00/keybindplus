package com.github.kvdz00.keybindplus.keybind;

import com.github.kvdz00.keybindplus.KeybindPlus;
import com.github.kvdz00.keybindplus.profile.KeybindProfile;
import com.github.kvdz00.keybindplus.profile.ProfileManager;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class KeybindApplier {
    private static Map<String, String> lastSnapshot;
    private static String lastSnapshotLoadedProfile;

    private KeybindApplier() {}

    public static boolean hasUndoSnapshot() {
        return lastSnapshot != null && !lastSnapshot.isEmpty();
    }

    public static ApplyResult apply(KeybindProfile profile) {
        lastSnapshot = KeybindCapture.captureAll();
        lastSnapshotLoadedProfile = ProfileManager.get().getLoadedProfile();

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
                InputConstants.Key key;
                if (keyName == null || keyName.isBlank() || keyName.equals("key.keyboard.unknown") || keyName.equalsIgnoreCase("none")) {
                    key = InputConstants.UNKNOWN;
                } else {
                    key = InputConstants.getKey(keyName);
                }
                mapping.setKey(key);
                applied++;
            } catch (Exception e) {
                KeybindPlus.LOGGER.warn("Failed to set key '{}' for '{}': {}", keyName, actionId, e.getMessage());
                skipped.add(actionId);
            }
        }

        KeyMapping.resetMapping();
        options.save();

        ProfileManager.get().setLoadedProfile(profile.getName());
        KeybindPlus.LOGGER.info("Applied {} keybinds, skipped {}", applied, skipped.size());
        return new ApplyResult(applied, skipped);
    }

    public static ApplyResult undoLastApply() {
        if (lastSnapshot == null || lastSnapshot.isEmpty()) {
            return new ApplyResult(0, List.of());
        }

        var minecraft = Minecraft.getInstance();
        var options = minecraft.options;
        int restored = 0;
        List<String> skipped = new ArrayList<>();

        Map<String, KeyMapping> mappingByName = new HashMap<>();
        for (KeyMapping km : options.keyMappings) {
            mappingByName.put(km.getName(), km);
        }

        for (var entry : lastSnapshot.entrySet()) {
            KeyMapping mapping = mappingByName.get(entry.getKey());
            if (mapping == null) {
                skipped.add(entry.getKey());
                continue;
            }
            try {
                String val = entry.getValue();
                InputConstants.Key key;
                if (val == null || val.isBlank() || val.equals("key.keyboard.unknown") || val.equalsIgnoreCase("none")) {
                    key = InputConstants.UNKNOWN;
                } else {
                    key = InputConstants.getKey(val);
                }
                mapping.setKey(key);
                restored++;
            } catch (Exception e) {
                skipped.add(entry.getKey());
            }
        }

        KeyMapping.resetMapping();
        options.save();
        lastSnapshot = null;
        ProfileManager.get().setLoadedProfile(lastSnapshotLoadedProfile);
        lastSnapshotLoadedProfile = null;

        KeybindPlus.LOGGER.info("Undo: restored {} keybinds", restored);
        return new ApplyResult(restored, skipped);
    }
}

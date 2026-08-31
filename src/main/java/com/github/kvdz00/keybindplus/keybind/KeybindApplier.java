package com.github.kvdz00.keybindplus.keybind;

import com.github.kvdz00.keybindplus.KeybindPlus;
import com.github.kvdz00.keybindplus.profile.KeybindProfile;
import com.github.kvdz00.keybindplus.profile.ProfileManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import java.util.*;

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

        Minecraft mc = Minecraft.getMinecraft();
        KeyBinding[] keyBindings = mc.gameSettings.keyBindings;
        Map<String, String> profileBinds = profile.getKeybinds();
        int applied = 0;
        List<String> skipped = new ArrayList<>();

        Map<String, KeyBinding> mappingByName = new HashMap<>();
        if (keyBindings != null) {
            for (KeyBinding kb : keyBindings) {
                mappingByName.put(kb.getKeyDescription(), kb);
            }
        }

        for (Map.Entry<String, String> entry : profileBinds.entrySet()) {
            String actionId = entry.getKey();
            String keyName = entry.getValue();

            KeyBinding kb = mappingByName.get(actionId);
            if (kb == null) {
                skipped.add(actionId);
                continue;
            }

            try {
                int code = KeybindCapture.parseKeyCode(keyName);
                kb.setKeyCode(code);
                applied++;
            } catch (Exception e) {
                KeybindPlus.LOGGER.warn("Failed to set key '{}' for '{}': {}", keyName, actionId, e.getMessage());
                skipped.add(actionId);
            }
        }

        KeyBinding.resetKeyBindingArrayAndHash();
        mc.gameSettings.saveOptions();

        ProfileManager.get().setLoadedProfile(profile.getName());
        KeybindPlus.LOGGER.info("Applied {} keybinds, skipped {}", applied, skipped.size());
        return new ApplyResult(applied, skipped);
    }

    public static ApplyResult undoLastApply() {
        if (lastSnapshot == null || lastSnapshot.isEmpty()) {
            return new ApplyResult(0, Collections.emptyList());
        }

        Minecraft mc = Minecraft.getMinecraft();
        KeyBinding[] keyBindings = mc.gameSettings.keyBindings;
        int restored = 0;
        List<String> skipped = new ArrayList<>();

        Map<String, KeyBinding> mappingByName = new HashMap<>();
        if (keyBindings != null) {
            for (KeyBinding kb : keyBindings) {
                mappingByName.put(kb.getKeyDescription(), kb);
            }
        }

        for (Map.Entry<String, String> entry : lastSnapshot.entrySet()) {
            KeyBinding kb = mappingByName.get(entry.getKey());
            if (kb == null) {
                skipped.add(entry.getKey());
                continue;
            }
            try {
                int code = KeybindCapture.parseKeyCode(entry.getValue());
                kb.setKeyCode(code);
                restored++;
            } catch (Exception e) {
                skipped.add(entry.getKey());
            }
        }

        KeyBinding.resetKeyBindingArrayAndHash();
        mc.gameSettings.saveOptions();
        lastSnapshot = null;
        ProfileManager.get().setLoadedProfile(lastSnapshotLoadedProfile);
        lastSnapshotLoadedProfile = null;

        KeybindPlus.LOGGER.info("Undo: restored {} keybinds", restored);
        return new ApplyResult(restored, skipped);
    }
}

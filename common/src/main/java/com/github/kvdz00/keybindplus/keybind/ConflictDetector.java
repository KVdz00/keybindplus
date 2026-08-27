package com.github.kvdz00.keybindplus.keybind;

import com.github.kvdz00.keybindplus.profile.KeybindProfile;

import java.util.*;

public final class ConflictDetector {
    private ConflictDetector() {}

    /**
     * Detects gameplay key conflicts: multiple gameplay actions bound to the same key.
     * Filters out debug shortcuts (F3 combinations) and unbound keys.
     * Returns a list of conflicts (only keys with 2+ actions).
     */
    public static List<KeyConflict> detect(KeybindProfile profile) {
        Map<String, List<String>> keyToActions = new LinkedHashMap<>();
        for (var entry : profile.getKeybinds().entrySet()) {
            String action = entry.getKey();
            String key = entry.getValue();

            if (key == null || key.isEmpty() || key.equals("key.keyboard.unknown")) {
                continue;
            }

            // Ignore debug shortcuts (F3 + key combos) as they do not conflict during standard gameplay
            if (action.startsWith("key.debug.") || action.contains(".debug.")) {
                continue;
            }

            keyToActions.computeIfAbsent(key, k -> new ArrayList<>()).add(action);
        }

        List<KeyConflict> conflicts = new ArrayList<>();
        for (var entry : keyToActions.entrySet()) {
            if (entry.getValue().size() > 1) {
                conflicts.add(new KeyConflict(entry.getKey(), List.copyOf(entry.getValue())));
            }
        }
        return conflicts;
    }
}

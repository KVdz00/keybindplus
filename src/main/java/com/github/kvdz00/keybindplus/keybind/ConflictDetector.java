package com.github.kvdz00.keybindplus.keybind;

import com.github.kvdz00.keybindplus.profile.KeybindProfile;

import java.util.*;

public final class ConflictDetector {
    private ConflictDetector() {}

    public static List<KeyConflict> detect(KeybindProfile profile) {
        Map<String, List<String>> keyToActions = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : profile.getKeybinds().entrySet()) {
            String action = entry.getKey();
            String key = entry.getValue();

            if (key == null || key.trim().isEmpty() || key.equalsIgnoreCase("none") || key.equals("0")) {
                continue;
            }

            if (action.startsWith("key.debug.") || action.contains(".debug.")) {
                continue;
            }

            List<String> list = keyToActions.get(key);
            if (list == null) {
                list = new ArrayList<>();
                keyToActions.put(key, list);
            }
            list.add(action);
        }

        List<KeyConflict> conflicts = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : keyToActions.entrySet()) {
            if (entry.getValue().size() > 1) {
                conflicts.add(new KeyConflict(entry.getKey(), entry.getValue()));
            }
        }
        return conflicts;
    }
}

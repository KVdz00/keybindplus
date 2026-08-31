package com.github.kvdz00.keybindplus.keybind;

import java.util.Collections;
import java.util.List;

public final class KeyConflict {
    private final String boundKey;
    private final List<String> conflictingActions;

    public KeyConflict(String boundKey, List<String> conflictingActions) {
        this.boundKey = boundKey;
        this.conflictingActions = Collections.unmodifiableList(conflictingActions);
    }

    public String getBoundKey() { return boundKey; }
    public List<String> getConflictingActions() { return conflictingActions; }
    public int getConflictCount() { return conflictingActions.size(); }
}

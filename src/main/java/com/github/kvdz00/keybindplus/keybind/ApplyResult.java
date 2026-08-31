package com.github.kvdz00.keybindplus.keybind;

import java.util.Collections;
import java.util.List;

public final class ApplyResult {
    private final int appliedCount;
    private final List<String> skippedActions;

    public ApplyResult(int appliedCount, List<String> skippedActions) {
        this.appliedCount = appliedCount;
        this.skippedActions = Collections.unmodifiableList(skippedActions);
    }

    public int getAppliedCount() { return appliedCount; }
    public List<String> getSkippedActions() { return skippedActions; }
    public boolean hasSkipped() { return !skippedActions.isEmpty(); }
}

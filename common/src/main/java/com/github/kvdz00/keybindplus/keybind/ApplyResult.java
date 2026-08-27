package com.github.kvdz00.keybindplus.keybind;

import java.util.List;

public record ApplyResult(int appliedCount, List<String> skipped) {
    public boolean hasSkipped() {
        return skipped != null && !skipped.isEmpty();
    }
}

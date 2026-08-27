package com.github.kvdz00.keybindplus.profile;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class KeybindProfile {
    private int schemaVersion = 1;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean isDefault;
    private Map<String, String> keybinds;

    public KeybindProfile() {
        this.keybinds = new LinkedHashMap<>();
    }

    public KeybindProfile(String name, Map<String, String> keybinds) {
        this.schemaVersion = 1;
        this.name = name;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.isDefault = false;
        this.keybinds = new LinkedHashMap<>(keybinds);
    }

    public int getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(int schemaVersion) { this.schemaVersion = schemaVersion; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    public Map<String, String> getKeybinds() {
        return Collections.unmodifiableMap(keybinds);
    }

    public void setKeybinds(Map<String, String> keybinds) {
        this.keybinds = new LinkedHashMap<>(keybinds);
    }

    public void putKeybind(String action, String key) {
        this.keybinds.put(action, key);
    }

    public String toFileName() {
        return this.name.replaceAll("[^a-zA-Z0-9._-]", "_") + ".json";
    }
}

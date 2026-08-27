package com.github.kvdz00.keybindplus.profile;

import com.github.kvdz00.keybindplus.KeybindPlus;
import com.github.kvdz00.keybindplus.config.KeybindPlusConfig;
import com.github.kvdz00.keybindplus.keybind.KeybindCapture;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public final class ProfileManager {
    private static ProfileManager instance;
    private final List<KeybindProfile> profiles = new ArrayList<>();

    private ProfileManager() {}

    public static ProfileManager get() {
        if (instance == null) {
            instance = new ProfileManager();
            instance.loadAllProfiles();
        }
        return instance;
    }

    public void loadAllProfiles() {
        profiles.clear();
        Path dir = KeybindPlusConfig.getProfilesDir();
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                return;
            }
            try (var stream = Files.list(dir)) {
                stream.filter(p -> p.toString().endsWith(".json"))
                    .forEach(path -> {
                        try {
                            String json = Files.readString(path);
                            KeybindProfile profile = ProfileSerializer.deserialize(json);
                            if (profile != null) {
                                profiles.add(profile);
                            }
                        } catch (IOException e) {
                            KeybindPlus.LOGGER.error("Failed to read profile {}: {}", path, e.getMessage());
                        }
                    });
            }
        } catch (IOException e) {
            KeybindPlus.LOGGER.error("Failed to list profiles: {}", e.getMessage());
        }

        String defaultName = KeybindPlusConfig.get().getDefaultProfile();
        for (var p : profiles) {
            p.setDefault(p.getName().equals(defaultName));
        }

        KeybindPlus.LOGGER.info("Loaded {} profiles", profiles.size());
    }

    public KeybindProfile saveProfile(String name) {
        Map<String, String> keybinds = KeybindCapture.captureAll();
        if (keybinds.isEmpty()) {
            KeybindPlus.LOGGER.error("Cannot save empty keybind set");
            return null;
        }

        KeybindProfile existing = getProfile(name);
        KeybindProfile profile;
        if (existing != null) {
            existing.setKeybinds(keybinds);
            existing.setUpdatedAt(Instant.now());
            profile = existing;
        } else {
            profile = new KeybindProfile(name, keybinds);
            profiles.add(profile);
        }

        writeProfileToFile(profile);
        return profile;
    }

    public KeybindProfile getProfile(String name) {
        return profiles.stream()
            .filter(p -> p.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    public boolean deleteProfile(String name) {
        KeybindProfile profile = getProfile(name);
        if (profile == null) return false;

        Path file = KeybindPlusConfig.getProfilesDir().resolve(profile.toFileName());
        try {
            Files.deleteIfExists(file);
            profiles.remove(profile);
            if (profile.isDefault()) {
                KeybindPlusConfig.get().setDefaultProfile("");
            }
            return true;
        } catch (IOException e) {
            KeybindPlus.LOGGER.error("Failed to delete profile {}: {}", name, e.getMessage());
            return false;
        }
    }

    public List<KeybindProfile> listProfiles() {
        return Collections.unmodifiableList(profiles);
    }

    public List<KeybindProfile> searchProfiles(String query) {
        if (query == null || query.isBlank()) return listProfiles();
        String lower = query.toLowerCase();
        return profiles.stream()
            .filter(p -> p.getName().toLowerCase().contains(lower))
            .collect(Collectors.toList());
    }

    public boolean profileExists(String name) {
        return profiles.stream().anyMatch(p -> p.getName().equals(name));
    }

    public Path exportProfile(String name) {
        KeybindProfile profile = getProfile(name);
        if (profile == null) return null;

        Path exportPath = KeybindPlusConfig.getExportsDir().resolve(profile.toFileName());
        try {
            String json = ProfileSerializer.serialize(profile);
            Files.writeString(exportPath, json);
            return exportPath;
        } catch (IOException e) {
            KeybindPlus.LOGGER.error("Failed to export profile {}: {}", name, e.getMessage());
            return null;
        }
    }

    public KeybindProfile importProfile(Path file) {
        try {
            String json = Files.readString(file);
            KeybindProfile profile = ProfileSerializer.deserialize(json);
            if (profile == null) {
                KeybindPlus.LOGGER.error("Failed to parse import file: {}", file);
                return null;
            }

            KeybindProfile existing = getProfile(profile.getName());
            if (existing != null) {
                profiles.remove(existing);
            }
            profiles.add(profile);
            writeProfileToFile(profile);
            return profile;
        } catch (IOException e) {
            KeybindPlus.LOGGER.error("Failed to import profile from {}: {}", file, e.getMessage());
            return null;
        }
    }

    public List<Path> listImportFiles() {
        Path dir = KeybindPlusConfig.getImportsDir();
        try {
            if (!Files.exists(dir)) return List.of();
            try (var stream = Files.list(dir)) {
                return stream.filter(p -> p.toString().endsWith(".json"))
                    .collect(Collectors.toList());
            }
        } catch (IOException e) {
            return List.of();
        }
    }

    public void setDefaultProfile(String name) {
        for (var p : profiles) {
            p.setDefault(p.getName().equals(name));
        }
        KeybindPlusConfig.get().setDefaultProfile(name);
    }

    public KeybindProfile getDefaultProfile() {
        String defaultName = KeybindPlusConfig.get().getDefaultProfile();
        if (defaultName == null || defaultName.isBlank()) return null;
        return getProfile(defaultName);
    }

    private void writeProfileToFile(KeybindProfile profile) {
        Path file = KeybindPlusConfig.getProfilesDir().resolve(profile.toFileName());
        try {
            String json = ProfileSerializer.serialize(profile);
            Files.writeString(file, json);
        } catch (IOException e) {
            KeybindPlus.LOGGER.error("Failed to write profile {}: {}", profile.getName(), e.getMessage());
        }
    }
}

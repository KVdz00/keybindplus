package com.github.kvdz00.keybindplus.profile;

import com.github.kvdz00.keybindplus.KeybindPlus;
import com.github.kvdz00.keybindplus.config.KeybindPlusConfig;
import com.github.kvdz00.keybindplus.keybind.KeybindCapture;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public final class ProfileManager {
    private static final int MAX_BACKUPS = 5;
    private static final DateTimeFormatter BACKUP_FORMAT =
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").withZone(ZoneId.systemDefault());

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

    public void syncDefaultFlags() {
        String defaultName = KeybindPlusConfig.get().getDefaultProfile();
        for (var p : profiles) {
            p.setDefault(!defaultName.isEmpty() && p.getName().equals(defaultName));
        }
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

        syncDefaultFlags();
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

        syncDefaultFlags();
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
            syncDefaultFlags();
            return true;
        } catch (IOException e) {
            KeybindPlus.LOGGER.error("Failed to delete profile {}: {}", name, e.getMessage());
            return false;
        }
    }

    public boolean renameProfile(String oldName, String newName) {
        KeybindProfile profile = getProfile(oldName);
        if (profile == null) return false;
        if (getProfile(newName) != null) return false;

        Path oldFile = KeybindPlusConfig.getProfilesDir().resolve(profile.toFileName());
        try {
            Files.deleteIfExists(oldFile);
        } catch (IOException e) {
            KeybindPlus.LOGGER.error("Failed to delete old profile file: {}", e.getMessage());
            return false;
        }

        boolean wasDefault = profile.isDefault();
        profile.setName(newName);
        profile.setUpdatedAt(Instant.now());

        if (wasDefault) {
            KeybindPlusConfig.get().setDefaultProfile(newName);
        }
        syncDefaultFlags();
        writeProfileToFile(profile);
        return true;
    }

    public KeybindProfile duplicateProfile(String sourceName, String newName) {
        KeybindProfile source = getProfile(sourceName);
        if (source == null) return null;
        if (getProfile(newName) != null) return null;

        KeybindProfile copy = new KeybindProfile(newName, source.getKeybinds());
        copy.setDefault(false);
        profiles.add(copy);
        syncDefaultFlags();
        writeProfileToFile(copy);
        return copy;
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

            // Always ensure imported profile is not default unless it matches active default name
            profile.setDefault(false);

            KeybindProfile existing = getProfile(profile.getName());
            if (existing != null) {
                profiles.remove(existing);
            }
            profiles.add(profile);
            syncDefaultFlags();
            writeProfileToFile(profile);
            return profile;
        } catch (IOException e) {
            KeybindPlus.LOGGER.error("Failed to import profile from {}: {}", file, e.getMessage());
            return null;
        }
    }

    public static boolean isValidProfileFile(Path file) {
        try {
            String content = Files.readString(file);
            JsonObject obj = JsonParser.parseString(content).getAsJsonObject();
            return obj.has("schemaVersion") && obj.has("keybinds") && obj.has("name");
        } catch (Exception e) {
            return false;
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

    public void createAutoBackup() {
        Map<String, String> currentKeybinds = KeybindCapture.captureAll();
        if (currentKeybinds.isEmpty()) return;

        KeybindProfile backup = new KeybindProfile("backup_" + BACKUP_FORMAT.format(Instant.now()), currentKeybinds);
        backup.setDefault(false);
        Path backupDir = KeybindPlusConfig.getBackupsDir();
        Path backupFile = backupDir.resolve(backup.toFileName());

        try {
            Files.createDirectories(backupDir);
            String json = ProfileSerializer.serialize(backup);
            Files.writeString(backupFile, json);
            pruneOldBackups(backupDir);
            KeybindPlus.LOGGER.info("Auto-backup created: {}", backupFile.getFileName());
        } catch (IOException e) {
            KeybindPlus.LOGGER.error("Failed to create auto-backup: {}", e.getMessage());
        }
    }

    private void pruneOldBackups(Path backupDir) {
        try (var stream = Files.list(backupDir)) {
            List<Path> backups = stream
                .filter(p -> p.getFileName().toString().startsWith("backup_"))
                .filter(p -> p.toString().endsWith(".json"))
                .sorted(Comparator.comparingLong(p -> {
                    try { return Files.getLastModifiedTime(p).toMillis(); }
                    catch (IOException e) { return 0L; }
                }))
                .collect(Collectors.toList());

            while (backups.size() > MAX_BACKUPS) {
                Path oldest = backups.remove(0);
                Files.deleteIfExists(oldest);
                KeybindPlus.LOGGER.info("Pruned old backup: {}", oldest.getFileName());
            }
        } catch (IOException e) {
            KeybindPlus.LOGGER.error("Failed to prune backups: {}", e.getMessage());
        }
    }

    public void setDefaultProfile(String name) {
        KeybindPlusConfig.get().setDefaultProfile(name);
        syncDefaultFlags();
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
